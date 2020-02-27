package com.nukkitx.plexus.api;

import java.net.InetSocketAddress;

public interface ProxyConfiguration {

    String getMotd();

    void setMotd(String motd);

    String getSubMotd();

    void setSubMotd(String subMotd);

    int getMaxPlayerCount();

    void setMaxPlayerCount(int maxPlayerCount);

    InetSocketAddress getBindAddress();
}
