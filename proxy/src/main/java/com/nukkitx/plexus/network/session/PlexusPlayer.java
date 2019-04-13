package com.nukkitx.plexus.network.session;

import com.nukkitx.plexus.network.protocol.UpstreamPacketHandler;
import com.nukkitx.protocol.bedrock.session.BedrockSession;

public class PlexusPlayer {

    private UpstreamPacketHandler playerConnection;
    private BedrockSession<ProxyPlayerSession> serverConnection;

    public PlexusPlayer(UpstreamPacketHandler playerConnection) {
        this.playerConnection = playerConnection;
    }

    public void setServerConnection(BedrockSession<ProxyPlayerSession> serverConnection) {
        this.serverConnection = serverConnection;
    }
}
