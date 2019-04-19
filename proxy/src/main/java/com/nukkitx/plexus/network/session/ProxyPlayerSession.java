package com.nukkitx.plexus.network.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.flowpowered.math.vector.Vector3i;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.network.util.Preconditions;
import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.api.ProxiedPlayer;
import com.nukkitx.plexus.network.NetworkManager;
import com.nukkitx.plexus.network.downstream.InitialDownstreamHandler;
import com.nukkitx.plexus.network.downstream.SwitchDownstreamHandler;
import com.nukkitx.plexus.network.upstream.InitialUpstreamHandler;
import com.nukkitx.plexus.utils.EncryptionUtils;
import com.nukkitx.protocol.PlayerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.handler.WrapperTailHandler;
import com.nukkitx.protocol.bedrock.packet.ChangeDimensionPacket;
import com.nukkitx.protocol.bedrock.packet.FullChunkDataPacket;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.wrapper.WrappedPacket;
import io.netty.util.AsciiString;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.security.KeyPair;

@Data
@Log4j2
public class ProxyPlayerSession implements PlayerSession, ProxiedPlayer {

    @Setter(AccessLevel.NONE)
    private boolean closed;
    private final KeyPair proxyKeyPair;
    private final NetworkManager networkManager;
    private final LoginPacket loginPacket;
    private BedrockSession<ProxyPlayerSession> upstream;
    private BedrockSession<ProxyPlayerSession> downstream;
    private int dimensionId = -1;
    private int chunkRadius;
    private Vector3i playerPosition;

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        Preconditions.checkArgument(!this.closed, "Trying to close an already closed session");
        this.closed = true;
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectReason disconnectReason) {
        if(disconnectReason.equals(DisconnectReason.CLIENT_DISCONNECT)) {
            this.closeDownstream();
        }
        System.out.println(disconnectReason.name());
    }

    public void closeDownstream() {
        if (this.downstream != null) {
            this.downstream.getConnection().disconnect();
        } else {
            log.debug("Downstream connection for player " + upstream.getAuthData().getIdentity() + " can't be closed, it's null!");
        }
    }

    public void connect(InetSocketAddress address) {
        log.debug("Connecting to downstream server {}", address);
        BedrockPacketHandler handler;
        if (this.downstream == null) {
            // Initial connection
            handler = new InitialDownstreamHandler(this);
        } else {
            // Switching server
            handler = new SwitchDownstreamHandler(this);
        }

        if(this.downstream != null && !this.downstream.isClosed()) {
            this.closeDownstream();
        }
        networkManager.getRakNetClient().connect(address).whenComplete((downstream, throwable) -> {
//            if(session.getPlayer().getDownstream() != null) {
//                log.debug("Changing dimension");
//                ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
//                changeDimensionPacket.setDimension(this.plexusPlayer.getNewDimensionId(this.plexusPlayer.getDimensionId()));
//                changeDimensionPacket.setPosition(this.plexusPlayer.getPlayerPosition().toFloat());
//                changeDimensionPacket.setRespawn(false);
//                int playerChunkX = this.playerPosition.getX() >> 4;
//                int playerChunkZ = this.playerPosition.getZ() >> 4;
//                int radius = this.chunkRadius;
//                for (int chunkX = (playerChunkX - radius); chunkX < (playerChunkX + radius); chunkX++) {
//                    for (int chunkZ = (playerChunkZ - radius); chunkZ < (playerChunkZ + radius); chunkZ++) {
//                        FullChunkDataPacket fullChunkDataPacket = new FullChunkDataPacket();
//                        fullChunkDataPacket.setChunkX(chunkX);
//                        fullChunkDataPacket.setChunkZ(chunkZ);
//                        fullChunkDataPacket.setData(NetworkManager.EMPTY_CHUNK);
//                        this.upstream.sendPacketImmediately(fullChunkDataPacket);
//                    }
//                }
//            }

            this.downstream = downstream;
            if (throwable != null) {
                log.error("Unable to connect to downstream server", throwable);
                downstream.disconnect("Unable to connect to downstream server");
                return;
            }
            downstream.setHandler(handler);

            downstream.sendPacketImmediately(loginPacket);
            this.upstream.setWrapperTailHandler(this.getUpstreamWrapperTailHandler(downstream));
            downstream.setWrapperTailHandler(this.getDownstreamWrapperTailHandler(this.upstream));
            //session.setLogging(false);

            log.debug("Downstream connected");
        });
    }

    @Override
    public void onDisconnect(@Nonnull String s) {
        this.onDisconnect(DisconnectReason.CLIENT_DISCONNECT);
        //TODO: Else transfer player to the fallback server
    }

    public WrapperTailHandler<ProxyPlayerSession> getUpstreamWrapperTailHandler(BedrockSession<ProxyPlayerSession> downstream) {
        return new ProxyWrapperTailHandler(downstream);
    }

    public WrapperTailHandler<ProxyPlayerSession> getDownstreamWrapperTailHandler(BedrockSession<ProxyPlayerSession> upstream) {
        return new ProxyWrapperTailHandler(upstream);
    }

    @RequiredArgsConstructor
    private class ProxyWrapperTailHandler implements WrapperTailHandler<ProxyPlayerSession> {
        private final BedrockSession<ProxyPlayerSession> session;

        @Override
        public void handle(WrappedPacket<ProxyPlayerSession> packet, boolean packetsHandled) {
            if (!packetsHandled) {
                packet.getBatched().retain();
                packet.getBatched().readerIndex(0);
                packet.getBatched().writerIndex(packet.getBatched().readableBytes());
                session.sendWrapped(packet);
            }
        }
    }
}
