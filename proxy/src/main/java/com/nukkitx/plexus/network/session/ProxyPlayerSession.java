package com.nukkitx.plexus.network.session;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.network.util.Preconditions;
import com.nukkitx.plexus.network.NetworkManager;
import com.nukkitx.plexus.utils.EncryptionUtils;
import com.nukkitx.protocol.PlayerSession;
import com.nukkitx.protocol.bedrock.handler.WrapperTailHandler;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.wrapper.WrappedPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.security.KeyPair;

@Log4j2
@RequiredArgsConstructor
public class ProxyPlayerSession implements PlayerSession {

    private boolean closed;
    @Getter
    @Setter
    private PlexusPlayer plexusPlayer;
    @Getter
    private final KeyPair proxyKeyPair = EncryptionUtils.createKeyPair();
    private final NetworkManager networkManager;
    @Getter
    private final boolean isPlayerConnection;

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
        if(disconnectReason.equals(DisconnectReason.CLIENT_DISCONNECT) && this.isPlayerConnection) {
            if(this.plexusPlayer != null) {
                this.networkManager.getPlayerSessions().remove(this.plexusPlayer.getUuid());
                this.plexusPlayer.closeDownstream();
            } else {
                log.debug("Tried to cleanup downstream connection but the plexusPlayer is null");
            }
        }
        System.out.println(disconnectReason.name());
    }

    @Override
    public void onDisconnect(@Nonnull String s) {
        if(this.isPlayerConnection) {
            this.onDisconnect(DisconnectReason.CLIENT_DISCONNECT);
        }
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
