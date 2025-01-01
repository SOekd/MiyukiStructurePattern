package app.miyuki.miyukistructurepattern.configuration;

import app.miyuki.miyukistructurepattern.configuration.exception.ConfigurationLoadException;
import app.miyuki.miyukistructurepattern.configuration.exception.ConfigurationSaveException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
public class Configuration {

    private final String path;

    private final Consumer<TypeSerializerCollection.Builder> serializerBuilder;

    private CommentedConfigurationNode root;

    private final YamlConfigurationLoader loader;

    @SneakyThrows
    public Configuration(@Nullable String path, @Nullable String internalPath, @Nullable Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        this.serializerBuilder = serializerBuilder;
        if (path == null && internalPath == null)
            throw new IllegalArgumentException("Path and internal path cannot be null at the same time");

        this.path = path != null ? path : internalPath;

        if (internalPath != null && path == null) {
            loader = loadInternal(internalPath);
        } else {
            this.loader = loadExternal(internalPath);
        }

        reload();
    }

    public Configuration(@NotNull String path) {
        this(path, null, null);
    }

    public Configuration(@Nullable String path, @Nullable String internalPath) {
        this(path, internalPath, null);
    }

    public Configuration(@NotNull String path, @Nullable Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        this(path, null, serializerBuilder);
    }

    private @NotNull YamlConfigurationLoader loadInternal(@NotNull String internalPath) {
        return YamlConfigurationLoader.builder()
                .indent(2)
                .defaultOptions(opts -> opts.serializers(serializerBuilder != null ? serializerBuilder : build -> {
                }))
                .nodeStyle(NodeStyle.BLOCK)
                .source(() -> new BufferedReader(new InputStreamReader(Objects.requireNonNull(getResource(internalPath)), StandardCharsets.UTF_8)))
                .build();
    }

    @SneakyThrows
    private @NotNull YamlConfigurationLoader loadExternal(@Nullable String internalPath) {
        val finalPath = Paths.get(path);

        ensureParentPathCreation(finalPath);
        copyAndCreateFile(finalPath, internalPath);

        return YamlConfigurationLoader.builder()
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .defaultOptions(opts -> opts.serializers(serializerBuilder != null ? serializerBuilder : build -> {
                }))
                .path(finalPath)
                .build();
    }

    private void copyAndCreateFile(@NotNull Path target, @Nullable String internalPath) throws IOException {
        if (Files.exists(target))
            return;

        if (internalPath == null) {
            Files.createFile(target);
            return;
        }

        try (InputStream in = getResource(internalPath)) {
            Files.copy(Objects.requireNonNull(in), target);
        }
    }

    private void ensureParentPathCreation(Path path) throws IOException {
        val parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    public void reload() {
        try {
            this.root = loader.load();
        } catch (ConfigurateException exception) {
            exception.printStackTrace();
            throw new ConfigurationLoadException("Failed to reload config file at " + path, exception);
        }
    }

    public void save() {
        try {
            loader.save(root);
        } catch (ConfigurateException exception) {
            exception.printStackTrace();
            throw new ConfigurationSaveException("Failed to save config file at " + path, exception);
        }
    }

    private @Nullable InputStream getResource(@NotNull String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

}
