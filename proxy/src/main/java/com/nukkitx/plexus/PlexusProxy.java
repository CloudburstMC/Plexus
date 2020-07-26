package com.nukkitx.plexus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nukkitx.event.SimpleEventManager;
import com.nukkitx.plexus.api.ProxiedPlayer;
import com.nukkitx.plexus.api.Proxy;
import com.nukkitx.plexus.network.BedrockProxyListener;
import com.nukkitx.plexus.network.SessionManager;
import com.nukkitx.plugin.SimplePluginManager;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.v407.Bedrock_v407;
import com.nukkitx.service.SimpleServiceManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Log4j2
@RequiredArgsConstructor
public class PlexusProxy extends Proxy {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final YAMLMapper YAML_MAPPER = (YAMLMapper) new YAMLMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final BedrockPacketCodec CODEC = Bedrock_v407.V407_CODEC;

    @Getter(AccessLevel.NONE)
    private final ScheduledExecutorService timerService = Executors.unconfigurableScheduledExecutorService(
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Plexus Ticker").setDaemon(true).build()));

    private final SessionManager sessionManager = new SessionManager();
    private final SimpleServiceManager serviceManager = new SimpleServiceManager();
    private final SimpleEventManager eventManager = new SimpleEventManager();
    private final SimplePluginManager pluginManager = new SimplePluginManager(eventManager);
    @Getter(AccessLevel.NONE)
    private final AtomicBoolean running = new AtomicBoolean();
    private final Path dataPath;
    private final Path pluginPath;
    private final List<BedrockClient> bedrockClients = new ArrayList<>();
    private final ConcurrentMap<String, InetSocketAddress> servers = new ConcurrentHashMap<>();
    private BedrockServer bedrockServer;
    private PlexusProxyConfiguration configuration;

    public void boot() throws Exception {
        Preconditions.checkArgument(running.compareAndSet(false, true), "Plexus has already been booted");
        Thread.currentThread().setName("Main Thread");

        /*          Load config        */
        log.info("Loading configuration...");
        Path configPath = Paths.get(".").resolve("config.yml");
        if (Files.notExists(configPath) || !Files.isRegularFile(configPath)) {
            Files.copy(PlexusProxy.class.getClassLoader().getResourceAsStream("config.yml"), configPath,
                    StandardCopyOption.REPLACE_EXISTING);
        }

        this.configuration = PlexusProxyConfiguration.load(configPath);

        this.configuration.getServers().forEach(this.servers::put);

        /*          Start Server        */
        InetSocketAddress bindAddress = this.configuration.getBindAddress();
        log.info("Binding to {}", bindAddress);
        bedrockServer = new BedrockServer(bindAddress, Runtime.getRuntime().availableProcessors());

        bedrockServer.setHandler(new BedrockProxyListener(this));
        bedrockServer.bind().join();

        this.loop();
    }

    private void loop() {
        while (running.get()) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // Shutdown
        this.bedrockClients.forEach(BedrockClient::close);
        this.bedrockServer.close();
    }

    public InetSocketAddress getDefaultServer() {
        return this.servers.getOrDefault(this.configuration.getDefaultServer(), this.servers.values().iterator().next());
    }

    public BedrockClient newClient() {
        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0",
                ThreadLocalRandom.current().nextInt(20000, 60000));
        BedrockClient client = new BedrockClient(bindAddress);
        this.bedrockClients.add(client);
        client.bind().join();
        return client;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public Set<? extends ProxiedPlayer> getPlayers() {
        return this.sessionManager.allPlayers();
    }

    @Override
    public String getName() {
        return "Plexus";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public void stop() {
        this.stop("Server closed");
    }

    @Override
    public void stop(String reason) {
        if (running.compareAndSet(true, false)) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    public Path getPluginDirectory() {
        return null;
    }

    @Override
    public int getOnlineCount() {
        return this.getPlayers().size();
    }
}
