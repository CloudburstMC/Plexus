package com.nukkitx.plexus.network.upstream;

import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectedUpstreamHandler implements BedrockPacketHandler {
    private final ProxyPlayerSession player;

    @Override
    public boolean handle(RequestChunkRadiusPacket packet) {
        this.player.setChunkRadius(packet);
        return false;
    }
}
