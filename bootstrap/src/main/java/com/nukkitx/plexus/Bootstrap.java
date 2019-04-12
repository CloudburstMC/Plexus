package com.nukkitx.plexus;

import joptsimple.*;
import joptsimple.util.PathConverter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class Bootstrap {

    public static void main(String... args) throws IOException {

        // Get current directory path
        Path path = Paths.get(".");

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSpec<Void> helpSpec = parser.accepts("help", "shows this page").forHelp();
        OptionSpec<Path> dataPathSpec = parser.accepts("data-path", "path of main server data e.g. plexus.yml")
                .withRequiredArg()
                .ofType(File.class)
                .withValuesConvertedBy(new PathConverter())
                .defaultsTo(path);
        OptionSpec<Path> pluginPathSpec = parser.accepts("plugin-path", "path to your plugins directory")
                .withRequiredArg()
                .ofType(File.class)
                .withValuesConvertedBy(new PathConverter())
                .defaultsTo(path.resolve("plugins"));

        OptionSet options = parser.parse(args);

        if (options.has(helpSpec)) {
            // Display help page
            parser.printHelpOn(System.out);
            return;
        }

        Path dataPath = options.valueOf(dataPathSpec);
        Path pluginPath = options.valueOf(pluginPathSpec);

        PlexusProxy proxy = new PlexusProxy(dataPath, pluginPath);

        try {
            proxy.boot();
        } catch (Exception e) {
            log.fatal("An exception occurred whilst starting up the server", e);
            System.exit(1);
        }

    }
}
