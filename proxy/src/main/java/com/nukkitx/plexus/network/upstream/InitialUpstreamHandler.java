package com.nukkitx.plexus.network.upstream;

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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

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
                status.setStatus(PlayStatusPacket.Status.FAILED_SERVER);
            } else {
                status.setStatus(PlayStatusPacket.Status.FAILED_CLIENT);
            }
        }
        upstream.setPacketCodec(codec);
        upstream.setLogging(true);

        JSONObject certData = (JSONObject) JSONValue.parse(packet.getChainData().toByteArray());
        Object chainObject = certData.get("chain");
        if (!(chainObject instanceof JSONArray)) {
            throw new RuntimeException("Certificate data is not valid");
        }
        JSONArray certChain = (JSONArray) chainObject;

        boolean validChain;
        try {
            validChain = EncryptionUtils.verifyChain(certChain);

            log.debug("Is player data valid? {}", validChain);
            JWSObject jwt = JWSObject.parse((String) certChain.get(certChain.size() - 1));
            JSONObject payload = jwt.getPayload().toJSONObject();

            Object extraDataObject = payload.get("extraData");
            if (!(extraDataObject instanceof JSONObject)) {
                throw new IllegalStateException("Invalid 'extraData'");
            }

            JSONObject extraData = (JSONObject) extraDataObject;

            AuthData authData = new AuthData(
                    extraData.getAsString("displayName"),
                    UUID.fromString(extraData.getAsString("identity")),
                    extraData.getAsString("XUID")
            );

            String identityPublicKeyString = payload.getAsString("identityPublicKey");
            if (identityPublicKeyString == null) {
                throw new RuntimeException("Identity Public Key was not found!");
            }
            ECPublicKey identityPublicKey = EncryptionUtils.generateKey(identityPublicKeyString);

            JWSObject clientJwt = JWSObject.parse(packet.getSkinData().toString());
            EncryptionUtils.verifyJwt(clientJwt, identityPublicKey);
            JSONObject skinData = clientJwt.getPayload().toJSONObject();


            // Create forged LoginPacket
            KeyPair keyPair = EncryptionUtils.createKeyPair();

            SignedJWT signedExtraData = ForgeryUtils.forgeAuthData(keyPair, extraData);
            JWSObject signedSkinData = ForgeryUtils.forgeSkinData(keyPair, skinData);
            certChain.remove(certChain.size() - 1);
            certChain.add(signedExtraData.serialize());
            JSONObject chainJson = new JSONObject();
            chainJson.put("chain", certChain);
            AsciiString chainData = AsciiString.of(chainJson.toString(JSONStyle.LT_COMPRESS));

            LoginPacket loginPacket = new LoginPacket();
            loginPacket.setChainData(chainData);
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
}