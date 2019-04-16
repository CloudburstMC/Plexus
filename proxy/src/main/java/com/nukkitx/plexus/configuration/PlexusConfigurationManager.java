package com.nukkitx.plexus.configuration;

import com.nukkitx.plexus.PlexusProxy;
import com.nukkitx.plexus.api.configuration.Configuration;
import com.nukkitx.plexus.api.configuration.ConfigurationManager;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@AllArgsConstructor
public class PlexusConfigurationManager implements ConfigurationManager {

    private final Path dataPath;
    private final Path pluginFolder;

    //TODO Add plugin support, for now only data configurations supported
    @Override
    public Configuration loadConfiguration(@Nonnull String fileName) throws IOException {
        Path path = this.dataPath.resolve(fileName);
        return new PlexusConfiguration(path).load();
    }

    @Override
    public Configuration saveDefaultConfiguration(@Nonnull String fileName) throws IOException {
        try {
            URL url = this.getClass().getResource(fileName);
            Path resourcePath = Paths.get(url.toURI());
            Path targetPath = this.dataPath.resolve(fileName);
            Files.copy(resourcePath, targetPath);
            return new PlexusConfiguration(targetPath).load();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T> T loadConfiguration(@Nonnull String fileName, @Nonnull Class<T> configurationClass) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(this.dataPath.resolve(fileName))) {
            return PlexusProxy.YAML_MAPPER.readValue(reader, configurationClass);
        }
    }

    @Override
    public <T> void saveConfiguration(@Nonnull String fileName, T configuration) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(this.dataPath.resolve(fileName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            PlexusProxy.YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, configuration);
        }
    }
}
