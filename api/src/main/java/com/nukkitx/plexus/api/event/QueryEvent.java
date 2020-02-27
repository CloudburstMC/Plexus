package com.nukkitx.plexus.api.event;

import com.nukkitx.api.event.Event;
import lombok.Data;
import lombok.NonNull;

import java.net.InetSocketAddress;

@Data
@NonNull
public class QueryEvent implements Event {

    private final InetSocketAddress address;
    private String motd;
    private String subMotd;
    private int onlineCount;
    private int maxPlayerCount;
    private String gameType;
}
