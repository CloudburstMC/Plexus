package com.nukkitx.plexus.network;

import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.api.ProxyConfiguration;
import com.nukkitx.plexus.api.event.QueryEvent;
import com.nukkitx.plexus.network.upstream.InitialUpstreamHandler;
import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class BedrockProxyListener implements BedrockServerEventHandler {
    private static final ThreadLocal<BedrockPong> PONG_THREAD_LOCAL = ThreadLocal.withInitial(BedrockPong::new);

    private final PlexusProxy proxy;

    @Override
    public boolean onConnectionRequest(@Nonnull InetSocketAddress inetSocketAddress) {
        return true;
    }

    @Nullable
    @Override
    public BedrockPong onQuery(@Nonnull InetSocketAddress address) {
        ProxyConfiguration configuration = this.proxy.getConfiguration();
        QueryEvent event = new QueryEvent(address);
        event.setMotd(configuration.getMotd());
        event.setSubMotd(configuration.getSubMotd());
        event.setGameType("SMP");
        event.setOnlineCount(this.proxy.getOnlineCount());
        event.setMaxPlayerCount(configuration.getMaxPlayerCount());

        this.proxy.getEventManager().fire(event);

        BedrockPong pong = PONG_THREAD_LOCAL.get();
        pong.setEdition("MCPE");
        pong.setMotd(event.getMotd());
        pong.setSubMotd(event.getSubMotd());
        pong.setGameType(event.getGameType());
        pong.setMaximumPlayerCount(event.getMaxPlayerCount());
        pong.setPlayerCount(event.getOnlineCount());
        pong.setIpv4Port(configuration.getBindAddress().getPort());
        pong.setIpv6Port(configuration.getBindAddress().getPort());
        pong.setProtocolVersion(PlexusProxy.CODEC.getProtocolVersion());
        pong.setVersion("");
        pong.setNintendoLimited(false);

        return pong;
    }

    @Override
    public void onSessionCreation(@Nonnull BedrockServerSession session) {
        session.setPacketHandler(new InitialUpstreamHandler(session, this.proxy));
    }
}
