package com.nukkitx.plexus.network.protocol;

import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.plexus.network.session.PlexusPlayer;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.plexus.utils.EncryptionUtils;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Base64;

@RequiredArgsConstructor
public class DownstreamPacketHandler implements BedrockPacketHandler {

    private final BedrockSession<ProxyPlayerSession> session;
    @Setter
    private PlexusPlayer plexusPlayer;

    @Override
    public boolean handle(ServerToClientHandshakePacket packet) {
        try {
            SignedJWT saltJwt = SignedJWT.parse(packet.getJwt());
            URI x5u = saltJwt.getHeader().getX509CertURL();
            ECPublicKey serverKey = EncryptionUtils.generateKey(x5u.toASCIIString());
            byte[] encryptionKey = EncryptionUtils.getServerKey(session.getPlayer().getProxyKeyPair(), serverKey,
                    Base64.getDecoder().decode(saltJwt.getJWTClaimsSet().getStringClaim("salt")));
            session.enableEncryption(new SecretKeySpec(encryptionKey, "AES"));
        } catch (ParseException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        ClientToServerHandshakePacket clientToServerHandshake = new ClientToServerHandshakePacket();
        session.sendPacketImmediately(clientToServerHandshake);
        return true;
    }

    @Override
    public boolean handle(ChangeDimensionPacket packet) {
        this.plexusPlayer.setDimensionId(packet.getDimension());
        return false;
    }

    @Override
    public boolean handle(NetworkChunkPublisherUpdatePacket packet) {
        this.plexusPlayer.setChunkRadius(packet.getRadius());
        this.plexusPlayer.setPlayerPosition(packet.getPosition());
        return false;
    }
}
