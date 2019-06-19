package com.nukkitx.plexus.network;

import com.flowpowered.math.GenericMath;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class SessionManager {
    private static final int SESSIONS_PER_THREAD = 50;

    private final ConcurrentMap<UUID, ProxyPlayerSession> playerSessions = new ConcurrentHashMap<>();
    @Getter
    private final ThreadPoolExecutor sessionTicker = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("Session Ticker - #%d").setDaemon(true).build());

    public int playerSessionCount() {
        return this.playerSessions.size();
    }

    public void add(ProxyPlayerSession session) {
        ProxyPlayerSession previousSession = this.playerSessions.put(session.getUniqueId(), session);
        if (previousSession != null) {
            previousSession.getUpstream().disconnect("disconnectionScreen.loggedinOtherLocation");
        } else {
            this.adjustPoolSize();
        }
    }

    public void remove(ProxyPlayerSession session) {
        if (this.playerSessions.values().remove(session)) {
            this.adjustPoolSize();
        }
    }

    @Nullable
    public ProxyPlayerSession getPlayer(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        return playerSessions.get(uuid);
    }

    @Nullable
    public ProxyPlayerSession getPlayer(String name) {
        Preconditions.checkNotNull(name, "name");
        ProxyPlayerSession found = null;
        name = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (ProxyPlayerSession player : allPlayers()) {
            if (player.getName().toLowerCase().startsWith(name)) {
                int curDelta = player.getName().length() - name.length();
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) {
                    break;
                }
            }
        }

        return found;
    }

    public List<ProxyPlayerSession> allPlayers() {
        return ImmutableList.copyOf(playerSessions.values());
    }

    private void adjustPoolSize() {
        int threads = GenericMath.clamp(playerSessions.size() / SESSIONS_PER_THREAD, 1, Runtime.getRuntime().availableProcessors());
        if (sessionTicker.getMaximumPoolSize() != threads) {
            sessionTicker.setMaximumPoolSize(threads);
        }
    }
}
