package com.nukkitx.plexus.network.downstream;

import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SwitchDownstreamHandler implements BedrockPacketHandler {
    private final ProxyPlayerSession player;
}
