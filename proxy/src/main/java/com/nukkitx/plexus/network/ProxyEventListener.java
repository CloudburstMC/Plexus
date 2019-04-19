package com.nukkitx.plexus.network;

import com.nukkitx.network.raknet.RakNetServerEventListener;
import io.netty.util.internal.ConcurrentSet;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

public class ProxyEventListener implements RakNetServerEventListener {
    private final Set<InetAddress> banned = new ConcurrentSet<>();

    //TODO Modifyable
    private final Advertisement normalAdvert = new Advertisement("MCPE", "Plexus Proxy",
            NetworkManager.PROTOCOL_VERSION, "0", 0,
            1, "A Proxy for Minecraft", "SMP");


    //TODO Modifyable
    private final Advertisement bannedAdvert = new Advertisement("MCPE", "§4§l%stream.user.mode.banned",
            NetworkManager.PROTOCOL_VERSION, "0", 0,
            1, "", "SMP");

    @Nonnull
    @Override
    public Action onConnectionRequest(InetSocketAddress address, int protocolVersion) {
        return Action.CONTINUE;
    }

    @Nonnull
    @Override
    public Advertisement onQuery(InetSocketAddress socketAddress) {
        if (this.banned.contains(socketAddress.getAddress())) {
            return this.bannedAdvert;
        } else {
            return this.normalAdvert;
        }
    }

    public void banAddress(InetAddress address) {
        banned.add(address);
    }

    public void unbanAddress(InetAddress address) {
        banned.remove(address);
    }
}
