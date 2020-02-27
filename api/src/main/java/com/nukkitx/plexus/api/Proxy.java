package com.nukkitx.plexus.api;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class Proxy {

    private static Proxy INSTANCE;

    public static void setInstance(@Nonnull Proxy proxy) {
        checkNotNull(proxy, "proxy");
        checkState(INSTANCE == null, "Instance has already been set");
        Proxy.INSTANCE = proxy;
    }

    public static Proxy get() {
        return INSTANCE;
    }

    public abstract Set<ProxiedPlayer> getPlayers();

    public abstract String getName();

    public abstract String getVersion();

    public abstract void stop();

    public abstract void stop(String reason);

    public abstract Path getPluginDirectory();

    public abstract int getOnlineCount();

    public abstract ProxyConfiguration getConfiguration();
}
