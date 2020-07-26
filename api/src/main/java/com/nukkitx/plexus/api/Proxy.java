package com.nukkitx.plexus.api;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The core proxy class which represents an interface
 * to the Minecraft server proxy. It also provides
 * singleton handling and access to common classes.
 *
 * Access to these functions are available through
 * {@link Proxy#get()}.
 */
public abstract class Proxy {

    private static Proxy INSTANCE;

    public static void setInstance(@Nonnull Proxy proxy) {
        checkNotNull(proxy, "proxy");
        checkState(INSTANCE == null, "Instance has already been set");
        Proxy.INSTANCE = proxy;
    }

    /**
     * Gets the current instance of this proxy.
     *
     * @return the current instance of this proxy
     */
    public static Proxy get() {
        return INSTANCE;
    }

    /**
     * Gets all of the players currently connected to the proxy.
     *
     * @return all of the players currently connected to the proxy
     */
    public abstract Set<? extends ProxiedPlayer> getPlayers();

    /**
     * Gets the name of the proxy implementation.
     *
     * @return the name of the proxy implementation
     */
    public abstract String getName();

    /**
     * Gets the version of the proxy currently being ran.
     *
     * @return the version of the proxy currently being ran
     */
    public abstract String getVersion();

    /**
     * Stops the proxy.
     */
    public abstract void stop();

    /**
     * Stops the proxy with the specified reason.
     *
     * @param reason the reason the proxy was stopped
     */
    public abstract void stop(String reason);

    /**
     * Gets the directory in which plugins are located.
     *
     * @return the directory in which plugins are located
     */
    public abstract Path getPluginDirectory();

    /**
     * Gets the amount of players currently connected
     * to this proxy.
     *
     * @return the amount of players currently connected to this proxy
     */
    public abstract int getOnlineCount();

    /**
     * Gets the proxy configuration.
     *
     * @return the proxy configuration
     */
    public abstract ProxyConfiguration getConfiguration();
}
