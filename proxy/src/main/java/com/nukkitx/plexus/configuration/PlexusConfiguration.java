package com.nukkitx.plexus.configuration;

import com.nukkitx.plexus.api.configuration.Configuration;
import lombok.AllArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@AllArgsConstructor
public class PlexusConfiguration implements Configuration {

    private final static Yaml yaml = new Yaml();
    private Map<String, Object> values = new LinkedHashMap<>();
    private final Path path;

    @Override
    public void save() throws IOException {
        String writingData = yaml.dump(this.values);
        Files.writeString(this.path, writingData);
    }

    @Override
    public void load() throws IOException {
        this.values = yaml.load(Files.newInputStream(this.path));
    }

    @Override
    public boolean exists(@Nonnull String key) {
        return this.values.containsKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return new HashSet<>(this.values.keySet());
    }

    @Override
    public void set(@Nonnull String key, @Nullable Object value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public Object get(@Nonnull String key) {
        return this.values.getOrDefault(key, null);
    }

    @Nullable
    @Override
    public Object get(@Nonnull String key, @Nullable Object value) {
        return this.values.getOrDefault(key, value);
    }

    @Override
    public void setString(@Nonnull String key, @Nullable String value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public String getString(@Nonnull String key) {
        return this.values.get(key) != null ? (String) this.values.get(key) : null;
    }

    @Nullable
    @Override
    public String getString(@Nonnull String key, @Nullable String defaultValue) {
        return defaultValue != null ? (String) this.values.getOrDefault(key, defaultValue) : null;
    }

    @Override
    public void setInt(@Nonnull String key, @Nullable Integer value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public Integer getInt(@Nonnull String key) {
        return this.values.get(key) != null ? (Integer) this.values.get(key) : null;
    }

    @Nullable
    @Override
    public Integer getInt(@Nonnull String key, @Nullable Integer defaultValue) {
        return defaultValue != null ? (Integer) this.values.getOrDefault(key, defaultValue) : null;
    }

    @Override
    public void setLong(@Nonnull String key, @Nullable Long value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public Long getLong(@Nonnull String key) {
        return this.values.get(key) != null ? (Long) this.values.get(key) : null;
    }

    @Nullable
    @Override
    public Long getLong(@Nonnull String key, @Nullable Long defaultValue) {
        return defaultValue != null ? (Long) this.values.getOrDefault(key, defaultValue) : null;
    }

    @Override
    public void setDouble(@Nonnull String key, @Nullable Double value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public Double getDouble(@Nonnull String key) {
        return this.values.get(key) != null ? (Double) this.values.get(key) : null;
    }

    @Nullable
    @Override
    public Double getDouble(@Nonnull String key, @Nullable Double defaultValue) {
        return defaultValue != null ? (Double) this.values.getOrDefault(key, defaultValue) : null;
    }

    @Override
    public void setBoolean(@Nonnull String key, @Nullable Boolean value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public Boolean getBoolean(@Nonnull String key) {
        return this.values.get(key) != null ? (Boolean) this.values.get(key) : null;
    }

    @Nullable
    @Override
    public Boolean getBoolean(@Nonnull String key, @Nullable Boolean defaultValue) {
        return defaultValue != null ? (Boolean) this.values.getOrDefault(key, defaultValue) : null;
    }

    @Override
    public <T> void setList(@Nonnull String key, @Nullable List<T> value) {
        this.values.put(key, value);
    }

    @Nullable
    @Override
    public <T> List<T> getList(@Nonnull String key) {
        return this.values.get(key) != null ? (List<T>) this.values.get(key) : null;
    }

    @Nullable
    @Override
    public <T> List<T> getList(@Nonnull String key, @Nullable List<T> defaultValue) {
        return defaultValue != null ? (List<T>) this.values.getOrDefault(key, defaultValue) : null;
    }
}
