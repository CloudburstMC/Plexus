package com.nukkitx.plexus.network.utils;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.NetworkStackLatencyPacket;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles packets more efficiently be resending the original buffer from one peer to the other if no packets are
 * handled.
 */
@Log4j2
@RequiredArgsConstructor
public class ProxyBatchHandler implements BatchHandler {
    private final BedrockSession session;
    private final String message;

    @Override
    public void handle(BedrockSession session, ByteBuf compressed, Collection<BedrockPacket> packets) {
        boolean wrapperHandled = false;
        List<BedrockPacket> unhandled = new ArrayList<>();
        for (BedrockPacket packet : packets) {
            if (session.isLogging() && log.isTraceEnabled() && !(packet instanceof NetworkStackLatencyPacket)) {
                log.trace("{} {}: {}", message, session.getAddress(), packet);
            }

            BedrockPacketHandler handler = session.getPacketHandler();

            if (handler != null && packet.handle(handler)) {
                wrapperHandled = true;
            } else {
                unhandled.add(packet);
            }
        }

        if (!wrapperHandled) {
            compressed.readerIndex(1); // FE - packet id
            this.session.sendWrapped(compressed, this.session.isEncrypted());
        } else if (!unhandled.isEmpty()) {
            this.session.sendWrapped(unhandled, true);
        }
    }
}
