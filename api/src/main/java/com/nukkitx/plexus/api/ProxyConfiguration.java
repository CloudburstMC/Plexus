package com.nukkitx.plexus.api;

import java.net.InetSocketAddress;

/**
 * Contains configurable values for the proxy.
 */
public interface ProxyConfiguration {

    /**
     * Gets the motd of the proxy displayed in the
     * server list.
     *
     * @return the motd of the proxy displayed in the server list
     */
    String getMotd();

    /**
     * Sets the motd of the proxy displayed in the
     * server list
     *
     * @param motd the motd of the proxy
     */
    void setMotd(String motd);

    /**
     * Gets the sub motd of the proxy
     *
     * @return the sub motd of the proxy
     */
    String getSubMotd();

    /**
     * Sets the sub motd of the proxy
     *
     * @param subMotd the sub motd of the proxy
     */
    void setSubMotd(String subMotd);

    /**
     * Gets the maximum amount of players allowed
     * on the proxy.
     *
     * @return the maximum amount of players allowed on the proxy
     */
    int getMaxPlayerCount();

    /**
     * Sets the maximum amount of players allowed
     * on the proxy.
     *
     * @param maxPlayerCount the maximum amount of players allowed on
     *                       the proxy
     */
    void setMaxPlayerCount(int maxPlayerCount);

    /**
     * Gets if xbox auth is required.
     *
     * @return if xbox auth is required
     */
    boolean isXboxAuth();

    /**
     * Gets the address the proxy is bound to.
     *
     * @return the address the proxy is bound to
     */
    InetSocketAddress getBindAddress();
}
