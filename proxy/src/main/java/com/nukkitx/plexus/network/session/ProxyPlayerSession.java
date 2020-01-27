package com.nukkitx.plexus.network.session;

import com.nukkitx.network.raknet.RakNetClientSession;
import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.api.ProxiedPlayer;
import com.nukkitx.plexus.network.downstream.InitialDownstreamHandler;
import com.nukkitx.plexus.network.downstream.SwitchDownstreamHandler;
import com.nukkitx.plexus.network.session.data.AuthData;
import com.nukkitx.plexus.network.utils.ProxyBatchHandler;
import com.nukkitx.protocol.bedrock.*;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.NetworkStackLatencyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
            downstream.setPacketCodec(PlexusProxy.CODEC);
            if (this.downstream == null) {
                this.downstream = downstream;
                this.upstream.setBatchedHandler(new ProxyBatchHandler(downstream, "Server-bound"));
            } else {
                this.connectingDownstream = downstream;
            }
            downstream.setPacketHandler(handler);
            downstream.setBatchedHandler(new ProxyBatchHandler(this.upstream, "Client-bound"));
            downstream.sendPacketImmediately(this.loginPacket);
            downstream.setLogging(true);

            log.debug("Downstream connected");
        });
    }
}
