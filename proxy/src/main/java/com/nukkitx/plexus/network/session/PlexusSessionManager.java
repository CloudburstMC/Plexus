package com.nukkitx.plexus.network.session;

import com.flowpowered.math.GenericMath;
import com.nukkitx.network.SessionManager;
import com.nukkitx.protocol.bedrock.session.BedrockSession;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class PlexusSessionManager extends SessionManager<BedrockSession<ProxyPlayerSession>> {

    private final int sessionsPerThread;

    public PlexusSessionManager(int sessionsPerThread) {
        this.sessionsPerThread = sessionsPerThread;
    }

    @Override
    protected void onAddSession(BedrockSession<ProxyPlayerSession> session) {
        log.info("Connecting " + session);
        this.adjustPoolSize();
    }

    @Override
    protected void onRemoveSession(BedrockSession<ProxyPlayerSession> session) {
        log.info("Disconnected " + session);
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
