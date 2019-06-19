package com.nukkitx.plexus.network.session;

import com.nukkitx.network.raknet.RakNetClientSession;
import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.api.ProxiedPlayer;
import com.nukkitx.plexus.network.downstream.InitialDownstreamHandler;
import com.nukkitx.plexus.network.downstream.SwitchDownstreamHandler;
import com.nukkitx.plexus.network.session.data.AuthData;
import com.nukkitx.protocol.bedrock.*;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkStackLatencyPacket;
import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Log4j2
public class ProxyPlayerSession implements ProxiedPlayer {

    @Setter(AccessLevel.NONE)
    private boolean closed;
    private final PlexusProxy proxy;
    private final BedrockServerSession upstream;
    private final KeyPair proxyKeyPair;
    private final LoginPacket loginPacket;
    private final AuthData authData;
    private BedrockClient downstreamClient;
    private BedrockClientSession downstream;
    private BedrockClientSession connectingDownstream;

    public UUID getUniqueId() {
        return this.authData.getIdentity();
    }

    public String getXuid() {
        return this.authData.getXuid();
    }

    public String getName() {
        return this.authData.getDisplayName();
    }

    private void closeDownstream() {
        if (this.downstream != null && !this.downstream.isClosed()) {
            this.downstream.disconnect();
            this.downstreamClient.close();
        } else {
            log.debug("Downstream connection for player " + this.getName() + " can't be closed, it's null!");
        }
    }

    public void connect(InetSocketAddress address) {
        log.debug("Connecting to downstream server {}", address);
        final BedrockPacketHandler handler;
        if (this.downstream == null) {
            // Initial connection
            handler = new InitialDownstreamHandler(this);
        } else {
            // Switching server
            handler = new SwitchDownstreamHandler(this);
        }
        this.downstreamClient = this.proxy.newClient();
        this.downstreamClient.connect(address).whenComplete((downstream, throwable) -> {
            if (throwable != null) {
                log.error("Unable to connect to downstream server", throwable);
                return;
            }
            if (this.downstream == null) {
                this.downstream = downstream;
                this.upstream.setBatchedHandler(this.getUpstreamBatchHandler(downstream));
            } else {
                this.connectingDownstream = downstream;
            }
            downstream.setPacketHandler(handler);
            downstream.sendPacketImmediately(this.loginPacket);
            downstream.setBatchedHandler(this.getDownstreamBatchHandler(this.upstream));
            downstream.setLogging(true);

            log.debug("Downstream connected");
        });
    }

    private BatchHandler getUpstreamBatchHandler(BedrockClientSession session) {
        return new ProxyBatchHandler(session);
    }

    private BatchHandler getDownstreamBatchHandler(BedrockServerSession session) {
        return new ProxyBatchHandler(session);
    }

    @RequiredArgsConstructor
    private class ProxyBatchHandler implements BatchHandler {
        private final BedrockSession session;

        @Override
        public void handle(BedrockSession session, ByteBuf compressed, Collection<BedrockPacket> packets) {
            boolean wrapperHandled = false;
            List<BedrockPacket> unhandled = new ArrayList<>();
            for (BedrockPacket packet : packets) {
                if (session.isLogging() && log.isTraceEnabled() && !(packet instanceof NetworkStackLatencyPacket)) {
                    log.trace("Inbound {}: {}", session.getAddress(), packet);
                }

                BedrockPacketHandler handler = session.getPacketHandler();

                if (handler != null && packet.handle(handler)) {
                    wrapperHandled = true;
                } else {
                    unhandled.add(packet);
                }
            }

            if (!wrapperHandled) {
                compressed.resetReaderIndex();
                this.session.sendWrapped(compressed, true);
            } else if (!unhandled.isEmpty()) {
                this.session.sendWrapped(unhandled, true);
            }
        }
    }
}
