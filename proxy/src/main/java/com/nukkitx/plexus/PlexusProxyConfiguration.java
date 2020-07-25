package com.nukkitx.plexus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nukkitx.plexus.api.ProxyConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

@Getter
@Setter
public final class PlexusProxyConfiguration implements ProxyConfiguration {

    private String motd = "Plexus Proxy";

    @JsonProperty("sub-motd")
    private String subMotd = "https://github.com/CloudburstMC/Plexus";

    @JsonProperty("max-players")
    private int maxPlayerCount = 20;

    @Setter(AccessLevel.NONE)
    @JsonProperty("bind-address")
    private InetSocketAddress bindAddress;

    @JsonProperty("xbox-auth")
    private boolean xboxAuth;

    @Setter(AccessLevel.NONE)
    @JsonProperty("default-server")
    private String defaultServer;

    @Setter(AccessLevel.NONE)
    private Map<String, InetSocketAddress> servers;

    public static PlexusProxyConfiguration load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return PlexusProxy.YAML_MAPPER.readValue(reader, PlexusProxyConfiguration.class);
        }
    }

    public static PlexusProxyConfiguration load(InputStream stream) throws IOException {
        return PlexusProxy.YAML_MAPPER.readValue(stream, PlexusProxyConfiguration.class);
    }

    public static void save(Path path, PlexusProxyConfiguration configuration) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            PlexusProxy.YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, configuration);
        }
    }

//    @Getter
//    @ToString
//    public static class Address {
//        private String address = "127.0.0.1";
//        private int port = 19132;
//
//        public InetSocketAddress getSocketAddress() {
//            return new InetSocketAddress(address, port);
//        }
//    }
}
