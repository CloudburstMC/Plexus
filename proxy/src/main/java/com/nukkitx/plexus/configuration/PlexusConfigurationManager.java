package com.nukkitx.plexus.configuration;

import com.nukkitx.plexus.PlexusProxy;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
public class ConfigurationManager {

    private final Path dataPath;
    private final Path pluginFolder;

    //TODO Add plugin support, for now only data configurations supported
    public PlexusConfiguration getConfiguration(String fileName) {
        Path path = this.dataPath.resolve(fileName);
        return new PlexusConfiguration(path);
    }

    public PlexusConfiguration saveDefaultConfiguration(String fileName) throws IOException {
        try {
            URL url = PlexusProxy.class.getResource(fileName);
            Path resourcePath = Paths.get(url.toURI());
            Path targetPath = this.dataPath.resolve(fileName);
            Files.copy(resourcePath, targetPath);
            return new PlexusConfiguration(targetPath);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
