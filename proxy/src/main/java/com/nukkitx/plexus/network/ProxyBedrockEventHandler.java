package com.nukkitx.plexus.network;

import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.network.upstream.InitialUpstreamHandler;
import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class ProxyBedrockEventHandler implements BedrockServerEventHandler {
    private static final BedrockPong ADVERTISEMENT = new BedrockPong();

    private final PlexusProxy proxy;

    static {
        ADVERTISEMENT.setEdition("MCPE");
        ADVERTISEMENT.setGameType("Survival");
        ADVERTISEMENT.setProtocolVersion(354);
        ADVERTISEMENT.setMotd("A Plexus Proxy");
        ADVERTISEMENT.setPlayerCount(0);
        ADVERTISEMENT.setMaximumPlayerCount(20);
        ADVERTISEMENT.setSubMotd("https://github.com/NukkitX/Plexus");
    }

    @Override
    public boolean onConnectionRequest(InetSocketAddress address) {
        return true;
    }

    @Nonnull
    public BedrockPong onQuery(InetSocketAddress address) {
        return ADVERTISEMENT;
    }

    @Override
    public void onSessionCreation(BedrockServerSession session) {
        session.setPacketHandler(new InitialUpstreamHandler(session, this.proxy));
    }
}
