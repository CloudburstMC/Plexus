package com.nukkitx.plexus.api.configuration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface Configuration {

    Configuration save() throws IOException;

    Configuration load() throws IOException;

    boolean exists(@Nonnull String key);

    Set<String> getKeys();

    void set(@Nonnull String key, @Nullable Object value);

    @Nullable
    Object get(@Nonnull String key);

    @Nullable
    Object get(@Nonnull String key, @Nullable Object value);

    void setString(@Nonnull String key, @Nullable String value);

    @Nullable
    String getString(@Nonnull String key);

    @Nullable
    String getString(@Nonnull String key, @Nullable String defaultValue);

    void setInt(@Nonnull String key, @Nullable Integer value);

    @Nullable
    Integer getInt(@Nonnull String key);

    @Nullable
    Integer getInt(@Nonnull String key, @Nullable Integer defaultValue);

    void setLong(@Nonnull String key, @Nullable Long value);

    @Nullable
    Long getLong(@Nonnull String key);

    @Nullable
    Long getLong(@Nonnull String key, @Nullable Long defaultValue);

    void setDouble(@Nonnull String key, @Nullable Double value);

    @Nullable
    Double getDouble(@Nonnull String key);

    @Nullable
    Double getDouble(@Nonnull String key, @Nullable Double defaultValue);

    void setBoolean(@Nonnull String key, @Nullable Boolean value);

    @Nullable
    Boolean getBoolean(@Nonnull String key);

    @Nullable
    Boolean getBoolean(@Nonnull String key, @Nullable Boolean defaultValue);

    <T> void setList(@Nonnull String key, @Nullable List<T> value);

    @Nullable
    <T> List<T> getList(@Nonnull String key);

    @Nullable
    <T> List<T> getList(@Nonnull String key, @Nullable List<T> defaultValue);
}
