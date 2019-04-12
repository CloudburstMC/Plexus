package com.nukkitx.plexus.network;

import com.nukkitx.network.raknet.RakNetServer;
import com.nukkitx.plexus.network.session.PlexusSessionManager;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.wrapper.WrappedPacket;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;

@Log4j2
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
