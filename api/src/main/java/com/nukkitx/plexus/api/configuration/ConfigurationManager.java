package com.nukkitx.plexus.api.configuration;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface ConfigurationManager {

    Configuration loadConfiguration(@Nonnull String fileName) throws IOException;

    Configuration saveDefaultConfiguration(@Nonnull String fileName) throws IOException;
}
