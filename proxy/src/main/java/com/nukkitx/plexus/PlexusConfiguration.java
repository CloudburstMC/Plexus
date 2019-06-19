package com.nukkitx.plexus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

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
public final class PlexusConfiguration {

    @JsonProperty("max-players")
    private int maximumPlayers;

    @JsonProperty("bind-address")
    private Address bindAddress;

    @JsonProperty("default-server")
    private String defaultServer;

    private Map<String, Address> servers;

    public static PlexusConfiguration load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return PlexusProxy.YAML_MAPPER.readValue(reader, PlexusConfiguration.class);
        }
    }

    public static PlexusConfiguration load(InputStream stream) throws IOException {
        return PlexusProxy.YAML_MAPPER.readValue(stream, PlexusConfiguration.class);
    }

    public static void save(Path path, PlexusConfiguration configuration) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            PlexusProxy.YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, configuration);
        }
    }

    @Getter
    @ToString
    public static class Address {
        private String address = "127.0.0.1";
        private int port = 19132;

        public InetSocketAddress getSocketAddress() {
            return new InetSocketAddress(address, port);
        }
    }
}
