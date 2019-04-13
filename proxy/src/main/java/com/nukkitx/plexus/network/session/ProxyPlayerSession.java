package com.nukkitx.plexus.network.session;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.plexus.utils.EncryptionUtils;
import com.nukkitx.protocol.PlayerSession;
import com.nukkitx.protocol.bedrock.handler.WrapperTailHandler;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import com.nukkitx.protocol.bedrock.wrapper.WrappedPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.security.KeyPair;

public class ProxyPlayerSession implements PlayerSession {

    @Getter
    private final KeyPair proxyKeyPair = EncryptionUtils.createKeyPair();

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void onDisconnect(@Nonnull DisconnectReason disconnectReason) {

    }

    @Override
    public void onDisconnect(@Nonnull String s) {

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
                packet.getBatched().writerIndex(0);
                session.sendWrapped(packet);
            }
        }
    }
}
