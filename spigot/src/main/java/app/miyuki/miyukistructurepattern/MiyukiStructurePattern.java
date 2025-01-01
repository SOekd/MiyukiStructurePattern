package app.miyuki.miyukistructurepattern;

import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.message.MessageLoader;
import app.miyuki.miyukistructurepattern.reload.ReloadCommand;
import app.miyuki.miyukistructurepattern.schematic.SchematicReaderHook;
import app.miyuki.miyukistructurepattern.structure.*;
import app.miyuki.miyukistructurepattern.structure.pattern.PatternStructureConstructor;
import app.miyuki.miyukistructurepattern.structure.schematic.SchematicStructureConstructor;
import app.miyuki.miyukistructurepattern.workload.LinearWorkloadRunnable;
import app.miyuki.miyukistructurepattern.workload.ScheduledWorkloadRunnable;
import app.miyuki.miyukistructurepattern.workload.WorkloadRunnable;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.SneakyThrows;
import lombok.val;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MiyukiStructurePattern extends JavaPlugin {

    private Configuration configuration;

    private MessageLoader messageLoader;

    private BukkitAudiences audiences;

    private StructureLoader structureLoader;

    private final Map<StructureType, StructureConstructor<? extends Structure>> structureConstructors = new HashMap<>();

    private Metrics metrics;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false);
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        audiences = BukkitAudiences.create(this);

        loadConfiguration();

        copySampleSchematic();

        structureLoader = new StructureLoaderImpl(this, getLogger());
        structureLoader.loadAll();

        WorkloadRunnable workloadRunnable;
        if (configuration.getRoot().node("animation", "enabled").getBoolean(false)) {
            workloadRunnable = new ScheduledWorkloadRunnable();
        } else {
            workloadRunnable = new LinearWorkloadRunnable();
        }

        val schematicReader = SchematicReaderHook.hook();
        if (schematicReader == null) {
            getLogger().severe("WorldEdit or FAWE is not installed, schematic support is disabled.");
        }

        structureConstructors.put(StructureType.PATTERN, new PatternStructureConstructor(workloadRunnable, configuration));
        structureConstructors.put(StructureType.SCHEMATIC, new SchematicStructureConstructor(workloadRunnable, schematicReader, configuration));

        Bukkit.getScheduler().runTaskTimer(this, workloadRunnable, 0L, 1L);

        Bukkit.getPluginManager().registerEvents(new StructureListener(this, configuration, structureLoader, messageLoader), this);

        val structureCommand = new StructureCommand(this, messageLoader, structureLoader);
        getCommand("givestructurepattern").setExecutor(structureCommand);
        getCommand("givestructurepattern").setTabCompleter(structureCommand);

        getCommand("reloadstructurepattern").setExecutor(new ReloadCommand(this, messageLoader));


        new StructurePreviewTask(this, configuration, structureLoader);

        metrics = new Metrics(this, 21480);
    }

    public void reload() {
        messageLoader.reload();
        structureLoader.loadAll();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        audiences.close();
        metrics.shutdown();
    }

    private void loadConfiguration() {

        if (getDataFolder().toPath().resolve("structures").toFile().mkdirs()) {
            createConfiguration("structures/structures.yml", "structures.yml");
        }

        Configuration messagesConfiguration = createConfiguration("messages.yml", "messages.yml");
        configuration = createConfiguration("configuration.yml", "configuration.yml");
        messageLoader = new MessageLoader(messagesConfiguration, audiences);
    }

    public Configuration createConfiguration(@NotNull String targetPath, @Nullable String resourcePath) {
        return new Configuration(getDataFolder() + "/" + targetPath, resourcePath);
    }

    public StructureConstructor<? extends Structure> getStructureConstructor(StructureType type) {
        return structureConstructors.get(type);
    }

    @SneakyThrows
    private void copySampleSchematic() {
        val schematicFile = getDataFolder().toPath().resolve("schematics").resolve("house.schematic").toFile();
        if (schematicFile.exists()) {
            return;
        }

        val parent = schematicFile.getParentFile();
        if (parent.exists()) {
            return;
        }

        parent.mkdirs();

        try (InputStream inputStream = getResource("house.schematic")) {
            Files.copy(Objects.requireNonNull(inputStream), schematicFile.toPath());
        }
    }

}
