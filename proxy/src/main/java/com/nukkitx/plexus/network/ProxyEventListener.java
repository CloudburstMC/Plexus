package com.nukkitx.plexus.network;

import com.nukkitx.network.raknet.RakNetServerEventListener;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;

public class ProxyEventListener implements RakNetServerEventListener {

    //TODO Modifyable
    private final Advertisement advertisement = new Advertisement("MCPE", "Plexus",
            0, "0", 0,
            1, "PlexusProxy", "SMP");

    @Nonnull
    @Override
    public Action onConnectionRequest(InetSocketAddress address, int protocolVersion) {
        return Action.CONTINUE;
    }

    @Nonnull
    @Override
    public Advertisement onQuery(InetSocketAddress inetSocketAddress) {
        return advertisement;
    }
}
