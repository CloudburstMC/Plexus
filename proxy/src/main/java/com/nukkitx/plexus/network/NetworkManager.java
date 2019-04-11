package com.nukkitx.plexus.network;

import com.nukkitx.network.raknet.RakNetServer;
import com.nukkitx.plexus.network.session.PlexusSessionManager;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.wrapper.WrappedPacket;

import java.net.InetSocketAddress;

public class NetworkManager {

    private final RakNetServer<BedrockSession<ProxyPlayerSession>> rakNetServer;

    public NetworkManager() {
        RakNetServer.Builder<BedrockSession<ProxyPlayerSession>> builder = RakNetServer.builder();
        this.rakNetServer = builder
                .eventListener(new ProxyEventListener())
                .address(new InetSocketAddress(19132))
                .packet(WrappedPacket::new, 0xfe)
                .sessionManager(new PlexusSessionManager(50))
                .sessionFactory(BedrockSession::new)
                .build();
    }

    public void close() {
        this.rakNetServer.close();
    }
}
