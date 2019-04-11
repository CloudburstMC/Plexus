package com.nukkitx.plexus.network.session;

import com.flowpowered.math.GenericMath;
import com.nukkitx.network.SessionManager;
import com.nukkitx.protocol.bedrock.session.BedrockSession;

import java.util.concurrent.ThreadPoolExecutor;

public class PlexusSessionManager extends SessionManager<BedrockSession<ProxyPlayerSession>> {

    private final int sessionsPerThread;

    public PlexusSessionManager(int sessionsPerThread) {
        this.sessionsPerThread = sessionsPerThread;
    }

    @Override
    protected void onAddSession(BedrockSession<ProxyPlayerSession> session) {
        this.adjustPoolSize();
    }

    @Override
    protected void onRemoveSession(BedrockSession<ProxyPlayerSession> session) {
        this.adjustPoolSize();
    }

    private void adjustPoolSize() {
        if (this.executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor sessionTicker = (ThreadPoolExecutor) this.executor;
            int threads = GenericMath.clamp(this.sessions.size() / this.sessionsPerThread, 1, Runtime.getRuntime().availableProcessors());
            if (sessionTicker.getMaximumPoolSize() != threads) {
                sessionTicker.setMaximumPoolSize(threads);
            }
        }
    }
}
