package com.nukkitx.plexus.network.session;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.PlayerSession;

import javax.annotation.Nonnull;

public class ProxyPlayerSession implements PlayerSession {

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void onDisconnect(@Nonnull DisconnectReason disconnectReason) {

    }

    @Override
    public void onDisconnect(@Nonnull String s) {

    }
}
