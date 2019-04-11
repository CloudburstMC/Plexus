package com.nukkitx.plexus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nukkitx.event.SimpleEventManager;
import com.nukkitx.network.NetworkListener;
import com.nukkitx.plexus.api.Proxy;
import com.nukkitx.plexus.configuration.PlexusConfigurationManager;
import com.nukkitx.plexus.network.NetworkManager;
import com.nukkitx.plugin.SimplePluginManager;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.v340.Bedrock_v340;
import com.nukkitx.service.SimpleServiceManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@RequiredArgsConstructor
public class PlexusProxy implements Proxy {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final YAMLMapper YAML_MAPPER = (YAMLMapper) new YAMLMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    public static final BedrockPacketCodec MINECRAFT_CODEC = Bedrock_v340.V340_CODEC;
    //public static final String NAME;
    //public static final SemVer API_VERSION;
    //public static final String PLEXUS_VERSION;
    //public static final SemVer MINECRAFT_VERSION;
    @Getter(AccessLevel.NONE)
    private final ScheduledExecutorService timerService = Executors.unconfigurableScheduledExecutorService(
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Plexus Ticker").setDaemon(true).build()));

    private final SimpleServiceManager serviceManager = new SimpleServiceManager();
    private final SimpleEventManager eventManager = new SimpleEventManager();
    private final SimplePluginManager pluginManager = new SimplePluginManager(eventManager);
    @Getter(AccessLevel.NONE)
    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();
    @Getter(AccessLevel.NONE)
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Path dataPath;
    private final Path pluginPath;

    private NetworkManager networkManager;

    private final PlexusConfigurationManager configurationManager = new PlexusConfigurationManager(this.dataPath, this.pluginPath);

    public void boot() throws Exception {
        Preconditions.checkArgument(!running.get(), "Plexus has already been booted");
        Thread.currentThread().setName("Main Thread");

        this.networkManager = new NetworkManager();
    }

    public void shutdown() {
        this.networkManager.close();
    }

    public boolean isRunning() {
        return running.get();
    }
}
