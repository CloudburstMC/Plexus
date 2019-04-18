package com.nukkitx.plexus.network.session;

import com.flowpowered.math.vector.Vector3i;
import com.nukkitx.plexus.network.NetworkManager;
import com.nukkitx.plexus.network.protocol.UpstreamPacketHandler;
import com.nukkitx.protocol.bedrock.packet.ChangeDimensionPacket;
import com.nukkitx.protocol.bedrock.packet.FullChunkDataPacket;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
    @Getter
    private final UpstreamPacketHandler playerConnection;
    @Getter
    private BedrockSession<ProxyPlayerSession> serverConnection;
    @Getter
    @Setter
    private int dimensionId = -1;
    @Getter
    @Setter
    private int chunkRadius;
    @Getter
    @Setter
    private Vector3i playerPosition;

    public void setServerConnection(BedrockSession<ProxyPlayerSession> serverConnection) {
        this.serverConnection = serverConnection;
    }

    public void closeDownstream() {
        if (this.serverConnection != null) {
            this.serverConnection.getConnection().disconnect();
        } else {
            log.debug("Downstream connection for player " + uuid + " can't be closed, it's null!");
        }
    }

    public void switchServer(InetSocketAddress address) {
        this.playerConnection.connect(address);
    }

    public int getNewDimensionId(int dimensionId) {
        switch (this.dimensionId) {
            case 0:
                return 1;
            case 1:
                return 2;
            default:
                return 0;
        }
    }
}
