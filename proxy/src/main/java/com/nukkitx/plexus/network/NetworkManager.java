package com.nukkitx.plexus.network;

import com.nukkitx.network.raknet.RakNetClient;
import com.nukkitx.network.raknet.RakNetServer;
import com.nukkitx.plexus.network.protocol.DownstreamPacketHandler;
import com.nukkitx.plexus.network.protocol.UpstreamPacketHandler;
import com.nukkitx.plexus.network.session.PlexusSessionManager;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.v340.Bedrock_v340;
import com.nukkitx.protocol.bedrock.wrapper.WrappedPacket;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;

@Log4j2
public class NetworkManager {

    public static final BedrockPacketCodec CODEC = Bedrock_v340.V340_CODEC;
    public static final int PROTOCOL_VERSION = CODEC.getProtocolVersion();

    private final RakNetServer<BedrockSession<ProxyPlayerSession>> rakNetServer;
    @Getter
    private final RakNetClient<BedrockSession<ProxyPlayerSession>> rakNetClient;

    public NetworkManager() {
        RakNetServer.Builder<BedrockSession<ProxyPlayerSession>> serverBuilder = RakNetServer.builder();
        this.rakNetServer = serverBuilder
                .eventListener(new ProxyEventListener())
                .address(new InetSocketAddress(19132))
                .packet(WrappedPacket::new, 0xfe)
                .sessionManager(new PlexusSessionManager(50))
                .sessionFactory(rakNetSession -> {
                    BedrockSession<ProxyPlayerSession> session = new BedrockSession<>(rakNetSession);
                    session.setHandler(new UpstreamPacketHandler(session, this));
                    return session;
                })
                .build();

        RakNetClient.Builder<BedrockSession<ProxyPlayerSession>> clientBuilder = RakNetClient.builder();
        this.rakNetClient = clientBuilder
                .packet(WrappedPacket::new, 0xfe)
                .sessionFactory(rakNetSession -> {
                    BedrockSession<ProxyPlayerSession> session = new BedrockSession<>(rakNetSession, CODEC);
                    session.setHandler(new DownstreamPacketHandler(session));
                    return session;
                })
                .sessionManager(new PlexusSessionManager(50))
                .build();

        if(this.rakNetServer.bind()) {
            log.info("Yay we binded!");
        } else {
            log.warn("Failed to bind to port");
        }
    }

    public void close() {
        this.rakNetServer.close();
    }
}
