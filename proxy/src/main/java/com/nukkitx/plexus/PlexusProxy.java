package com.nukkitx.plexus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nukkitx.event.SimpleEventManager;
import com.nukkitx.plexus.api.Proxy;
import com.nukkitx.plexus.network.SessionManager;
import com.nukkitx.plugin.SimplePluginManager;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.v354.Bedrock_v354;
import com.nukkitx.service.SimpleServiceManager;
import io.netty.buffer.PooledByteBufAllocator;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Log4j2
@RequiredArgsConstructor
public class PlexusProxy implements Proxy {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final YAMLMapper YAML_MAPPER = (YAMLMapper) new YAMLMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final BedrockPacketCodec CODEC = Bedrock_v354.V354_CODEC;
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
    private PlexusConfiguration configuration;

    public void boot() throws Exception {
        Preconditions.checkArgument(!running.get(), "Plexus has already been booted");
        Thread.currentThread().setName("Main Thread");

        /*          Load config        */
        log.info("Loading configuration...");
        Path configPath = Paths.get(".").resolve("config.yml");
        if (Files.notExists(configPath) || !Files.isRegularFile(configPath)) {
            Files.copy(PlexusProxy.class.getClassLoader().getResourceAsStream("config.yml"), configPath,
                    StandardCopyOption.REPLACE_EXISTING);
        }

        this.configuration = PlexusConfiguration.load(configPath);

        this.configuration.getServers().forEach((s, address) -> this.servers.put(s, address.getSocketAddress()));

        /*          Start Server        */
        InetSocketAddress bindAddress = this.configuration.getBindAddress().getSocketAddress();
        bedrockServer = new BedrockServer(bindAddress, Runtime.getRuntime().availableProcessors());
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

    public void shutdown() {
        if (running.compareAndSet(false, true)) {
            synchronized (this) {
                this.notify();
            }
        }
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
}
