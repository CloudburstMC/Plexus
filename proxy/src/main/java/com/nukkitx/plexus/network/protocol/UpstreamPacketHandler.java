package com.nukkitx.plexus.network.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Preconditions;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.network.NetworkManager;
import com.nukkitx.plexus.network.session.PlexusPlayer;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.plexus.utils.EncryptionUtils;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.session.data.AuthData;
import io.netty.util.AsciiString;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class UpstreamPacketHandler implements BedrockPacketHandler {

    private final BedrockSession<ProxyPlayerSession> session;
    private final NetworkManager networkManager;
    private JSONObject skinData;
    private JSONObject extraData;
    private ArrayNode chainData;

    private static boolean validateChainData(JsonNode data) throws Exception {
        ECPublicKey lastKey = null;
        boolean validChain = false;
        for (JsonNode node : data) {
            JWSObject jwt = JWSObject.parse(node.asText());

            if (!validChain) {
                validChain = verifyJwt(jwt, EncryptionUtils.MOJANG_PUBLIC_KEY);
            }

            if (lastKey != null) {
                verifyJwt(jwt, lastKey);
            }

            JsonNode payloadNode = PlexusProxy.JSON_MAPPER.readTree(jwt.getPayload().toString());
            JsonNode ipkNode = payloadNode.get("identityPublicKey");
            Preconditions.checkState(ipkNode != null && ipkNode.getNodeType() == JsonNodeType.STRING, "identityPublicKey node is missing in chain");
            lastKey = EncryptionUtils.generateKey(ipkNode.asText());
        }
        return validChain;
    }

    private static boolean verifyJwt(JWSObject jwt, ECPublicKey key) throws JOSEException {
        return jwt.verify(new DefaultJWSVerifierFactory().createJWSVerifier(jwt.getHeader(), key));
    }

    @Override
    public boolean handle(LoginPacket packet) {
        int protocolVersion = packet.getProtocolVersion();
        session.setProtocolVersion(protocolVersion);

        if (protocolVersion != NetworkManager.PROTOCOL_VERSION) {
            PlayStatusPacket status = new PlayStatusPacket();
            if (protocolVersion > NetworkManager.PROTOCOL_VERSION) {
                status.setStatus(PlayStatusPacket.Status.FAILED_SERVER);
            } else {
                status.setStatus(PlayStatusPacket.Status.FAILED_CLIENT);
            }
        }
        session.setPacketCodec(NetworkManager.CODEC);

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
        chainData = (ArrayNode) certChainData;

        boolean validChain;
        try {
            validChain = validateChainData(certChainData);

            log.debug("Is player data valid? {}", validChain);
            JWSObject jwt = JWSObject.parse(certChainData.get(certChainData.size() - 1).asText());
            JsonNode payload = PlexusProxy.JSON_MAPPER.readTree(jwt.getPayload().toBytes());

            if (payload.get("extraData").getNodeType() != JsonNodeType.OBJECT) {
                throw new RuntimeException("AuthData was not found!");
            }

            extraData = (JSONObject) jwt.getPayload().toJSONObject().get("extraData");

            session.setAuthData(new AuthData() {
                @Override
                public String getDisplayName() {
                    return extraData.getAsString("displayName");
                }

                @Override
                public UUID getIdentity() {
                    return UUID.fromString(extraData.getAsString("identity"));
                }

                @Override
                public String getXuid() {
                    return extraData.getAsString("XUID");
                }
            });

            if (payload.get("identityPublicKey").getNodeType() != JsonNodeType.STRING) {
                throw new RuntimeException("Identity Public Key was not found!");
            }
            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(payload.get("identityPublicKey").textValue());

            JWSObject clientJwt = JWSObject.parse(packet.getSkinData().toString());
            verifyJwt(clientJwt, identityPublicKey);
            skinData = clientJwt.getPayload().toJSONObject();

            PlexusPlayer plexusPlayer = new PlexusPlayer(this);

            log.debug("Initializing proxy session");
            networkManager.getRakNetClient().connect(new InetSocketAddress("127.0.0.1", 19134)).whenComplete((session, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to connect to downstream server", throwable);
                    session.disconnect("Unable to connect to downstream server");
                    return;
                }
                plexusPlayer.setServerConnection(session);
                ProxyPlayerSession proxySession = new ProxyPlayerSession();
                session.setPlayer(proxySession);

                SignedJWT authData = EncryptionUtils.forgeAuthData(proxySession.getProxyKeyPair(), extraData);
                JWSObject skinData = EncryptionUtils.forgeSkinData(proxySession.getProxyKeyPair(), this.skinData);
                chainData.remove(chainData.size() - 1);
                chainData.add(authData.serialize());
                JsonNode json = PlexusProxy.JSON_MAPPER.createObjectNode().set("chain", chainData);
                AsciiString chainData;
                try {
                    chainData = new AsciiString(PlexusProxy.JSON_MAPPER.writeValueAsBytes(json));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }


                LoginPacket login = new LoginPacket();
                login.setChainData(chainData);
                login.setSkinData(AsciiString.of(skinData.serialize()));
                login.setProtocolVersion(NetworkManager.PROTOCOL_VERSION);

                session.sendPacketImmediately(login);
                this.session.setWrapperTailHandler(proxySession.getUpstreamWrapperTailHandler(this.session));
                session.setWrapperTailHandler(proxySession.getDownstreamWrapperTailHandler(session));
                this.session.setLogging(false);
                //session.setLogging(false);

                log.debug("Downstream connected");
            });
        } catch (Exception e) {
            session.disconnect("disconnectionScreen.internalError.cantConnect");
            throw new RuntimeException("Unable to complete login", e);
        }
        return true;
    }
}