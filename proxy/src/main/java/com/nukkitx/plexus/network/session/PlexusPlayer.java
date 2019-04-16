package com.nukkitx.plexus.network.session;

import com.nukkitx.plexus.network.protocol.UpstreamPacketHandler;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public class PlexusPlayer {

    @Getter
    private final UUID uuid;
    @Getter
    private final String displayName;
    private final UpstreamPacketHandler playerConnection;
    @Getter
    private BedrockSession<ProxyPlayerSession> serverConnection;

    public void setServerConnection(BedrockSession<ProxyPlayerSession> serverConnection) {
        this.serverConnection = serverConnection;
    }

    public void closeDownstream() {
        if(this.serverConnection != null) {
            this.serverConnection.getConnection().disconnect();
        } else {
            log.debug("Downstream connection for player " + uuid + " can't be closed, it's null!");
        }
    }

    public void switchServer(InetSocketAddress address) {
        this.playerConnection.connect(address);
    }
}
