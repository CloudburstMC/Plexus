package com.nukkitx.plexus.network.downstream;

import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.ChangeDimensionPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectedDownstreamHandler implements BedrockPacketHandler {
    private final ProxyPlayerSession player;

    public boolean handle(ChangeDimensionPacket packet) {
        return false;
    }
}
