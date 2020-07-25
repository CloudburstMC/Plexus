package com.nukkitx.plexus.network.upstream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Preconditions;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.plexus.network.session.data.AuthData;
import com.nukkitx.plexus.network.utils.ForgeryUtils;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import io.netty.util.AsciiString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class InitialUpstreamHandler implements BedrockPacketHandler {

    @Getter
    private final BedrockServerSession upstream;
    private final PlexusProxy proxy;


    @Override
    public boolean handle(LoginPacket packet) {
        int protocolVersion = packet.getProtocolVersion();

        BedrockPacketCodec codec = PlexusProxy.CODEC;

        if (protocolVersion != codec.getProtocolVersion()) {
            PlayStatusPacket status = new PlayStatusPacket();
            if (protocolVersion > codec.getProtocolVersion()) {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD);
            } else {
                status.setStatus(PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
            }
        }
        upstream.setPacketCodec(codec);
        upstream.setLogging(true);

        JsonNode certData;
        try {
            certData = PlexusProxy.JSON_MAPPER.readTree(packet.getChainData().toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Certificate JSON can not be read.");
        }
        JsonNode certChainData = certData.get("chain");
        if (certChainData.getNodeType() != JsonNodeType.ARRAY) {
            throw new RuntimeException("Certificate data is not valid");
        }

        ArrayNode chainData = (ArrayNode) certChainData;
        boolean validChain;
        try {
            validChain = verifyChain(certChainData);
            log.debug("Is player data valid? {}", validChain);
            if (!validChain && this.proxy.getConfiguration().isXboxAuth()) {
                this.upstream.disconnect("disconnectionScreen.notAuthenticated");
                return true;
            }

            JWSObject jwt = JWSObject.parse(certChainData.get(certChainData.size() - 1).asText());
            JsonNode payload = PlexusProxy.JSON_MAPPER.readTree(jwt.getPayload().toBytes());
            if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                throw new RuntimeException("AuthData was not found!");
            }

            JSONObject extraData = (JSONObject) jwt.getPayload().toJSONObject().get("extraData");;
            AuthData authData = new AuthData(
                    extraData.getAsString("displayName"),
                    UUID.fromString(extraData.getAsString("identity")),
                    extraData.getAsString("XUID")
            );

            if (payload.get("identityPublicKey").getNodeType() != JsonNodeType.STRING) {
                throw new RuntimeException("Identity Public Key was not found!");
            }

            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(payload.get("identityPublicKey").textValue());

            JWSObject clientJwt = JWSObject.parse(packet.getSkinData().toString());
            EncryptionUtils.verifyJwt(clientJwt, identityPublicKey);
            JSONObject skinData = clientJwt.getPayload().toJSONObject();

            // Create forged LoginPacket
            KeyPair keyPair = EncryptionUtils.createKeyPair();
            SignedJWT signedExtraData = ForgeryUtils.forgeAuthData(keyPair, extraData);
            JWSObject signedSkinData = ForgeryUtils.forgeSkinData(keyPair, skinData);
            chainData.remove(certChainData.size() - 1);
            chainData.add(signedExtraData.serialize());
            JsonNode json = PlexusProxy.JSON_MAPPER.createObjectNode().set("chain", chainData);
            AsciiString loginChainData;
            try {
                loginChainData = new AsciiString(PlexusProxy.JSON_MAPPER.writeValueAsBytes(json));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            LoginPacket loginPacket = new LoginPacket();
            loginPacket.setChainData(loginChainData);
            loginPacket.setSkinData(AsciiString.of(signedSkinData.serialize()));
            loginPacket.setProtocolVersion(protocolVersion);

            ProxyPlayerSession player = new ProxyPlayerSession(this.proxy, this.upstream, keyPair, loginPacket, authData);

            this.proxy.getSessionManager().add(player);

            player.connect(this.proxy.getDefaultServer());
        } catch (Exception e) {
            upstream.disconnect("disconnectionScreen.internalError.cantConnect");
            throw new RuntimeException("Unable to complete login", e);
        }
        return true;
    }

    private static boolean verifyChain(JsonNode data) throws Exception {
        ECPublicKey lastKey = null;
        boolean validChain = false;
        for (JsonNode node : data) {
            JWSObject jwt = JWSObject.parse(node.asText());

            if (!validChain) {
                validChain = EncryptionUtils.verifyJwt(jwt, EncryptionUtils.getMojangPublicKey());
            }

            if (lastKey != null) {
                EncryptionUtils.verifyJwt(jwt, lastKey);
            }

            JsonNode payloadNode = PlexusProxy.JSON_MAPPER.readTree(jwt.getPayload().toString());
            JsonNode ipkNode = payloadNode.get("identityPublicKey");
            Preconditions.checkState(ipkNode != null && ipkNode.getNodeType() == JsonNodeType.STRING, "identityPublicKey node is missing in chain");
            lastKey = EncryptionUtils.generateKey(ipkNode.asText());
        }
        return validChain;
    }
}