package app.miyuki.miyukistructurepattern;

import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.message.MessageLoader;
import app.miyuki.miyukistructurepattern.reload.ReloadCommand;
import app.miyuki.miyukistructurepattern.structure.*;
import app.miyuki.miyukistructurepattern.structure.pattern.PatternStructureConstructor;
import app.miyuki.miyukistructurepattern.structure.schematic.SchematicStructureConstructor;
import app.miyuki.miyukistructurepattern.workload.LinearWorkloadRunnable;
import app.miyuki.miyukistructurepattern.workload.ScheduledWorkloadRunnable;
import app.miyuki.miyukistructurepattern.workload.WorkloadRunnable;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.val;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MiyukiStructurePattern extends JavaPlugin {

    private WorkloadRunnable workloadRunnable;

    private Configuration configuration;

    private Configuration messagesConfiguration;

    private MessageLoader messageLoader;

    private BukkitAudiences audiences;

    private StructureLoader structureLoader;

    private final Map<StructureType, StructureConstructor<? extends Structure>> structureConstructors = new HashMap<>();

    private Metrics metrics;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        audiences = BukkitAudiences.create(this);

        loadConfiguration();

        structureLoader = new StructureLoaderImpl(this, getLogger());
        structureLoader.loadAll();

        if (configuration.getRoot().node("animation", "enabled").getBoolean(false)) {
            workloadRunnable = new ScheduledWorkloadRunnable();
        } else {
            workloadRunnable = new LinearWorkloadRunnable();
        }

        structureConstructors.put(StructureType.PATTERN, new PatternStructureConstructor(workloadRunnable, configuration));
        structureConstructors.put(StructureType.SCHEMATIC, new SchematicStructureConstructor(workloadRunnable, configuration));

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

        messagesConfiguration = createConfiguration("messages.yml", "messages.yml");
        configuration = createConfiguration("configuration.yml", "configuration.yml");
        messageLoader = new MessageLoader(messagesConfiguration, audiences);
    }

    public Configuration createConfiguration(@NotNull String targetPath, @Nullable String resourcePath) {
        return new Configuration(getDataFolder() + "/" + targetPath, resourcePath);
    }

    public StructureConstructor<? extends Structure> getStructureConstructor(StructureType type) {
        return structureConstructors.get(type);
    }

}
