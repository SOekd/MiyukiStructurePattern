package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.MiyukiStructurePattern;
import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.structure.pattern.PatternStructure;
import app.miyuki.miyukistructurepattern.structure.schematic.SchematicStructure;
import app.miyuki.miyukistructurepattern.util.item.NodeToItemAdapter;
import app.miyuki.miyukistructurepattern.util.item.material.MaterialUtils;
import com.cryptomorin.xseries.XSound;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fr.mrmicky.fastparticles.ParticleType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class StructureLoaderImpl implements StructureLoader {

    private final Map<String, Structure> loadedStructures = Maps.newHashMap();

    private final MiyukiStructurePattern plugin;

    private final Logger logger;

    @Override
    public Structure getLoadedStructure(String id) {
        return loadedStructures.get(id.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<Structure> getLoadedStructures() {
        return new ArrayList<>(loadedStructures.values());
    }

    @Override
    public void loadAll() {

        loadedStructures.clear();

        val structuresPath = plugin.getDataFolder().toPath().resolve("structures");

        if (!structuresPath.toFile().exists()) {
            structuresPath.toFile().mkdirs();
        }

        try (val files = Files.walk(structuresPath)) {

            files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .forEach(path -> {
                        val configuration = new Configuration(path.toFile().toString());
                        loadFromConfiguration(configuration);
                    });

        } catch (IOException exception) {
            logger.warning("Error while loading structures: " + exception.getMessage());
        }
    }

    @SneakyThrows
    private void loadFromConfiguration(@NotNull Configuration configuration) {
        for (Map.Entry<Object, CommentedConfigurationNode> structureEntry : configuration.getRoot().childrenMap().entrySet()) {


            val key = structureEntry.getKey().toString().toLowerCase(Locale.ROOT);
            val node = structureEntry.getValue();

            val type = StructureType.findByName(node.node("type").getString(""));
            if (type == null) {
                logger.warning("Invalid structure type for structure " + key);
                continue;
            }

            val onlyAir = node.node("only-air").getBoolean(true);
            val iterations = node.node("iterations").getInt(1);
            val distance = node.node("distance").getInt(1);
            val item = NodeToItemAdapter.adapt(node.node("item"));
            if (item == null) {
                logger.warning("Invalid item for structure " + key);
                continue;
            }

            val animationDirection = node.node("animation-direction").getString("HORIZONTAL");
            val direction = StructureAnimationDirectionType.findByName(animationDirection);

            val cannotPlaceItem = MaterialUtils.findMaterialByNameAndCreateItem(node.node("cannot-place").getString("STONE"));
            val particleName = node.node("particles", "particle").getString();

            ParticleType particleType;
            if (particleName != null && !particleName.isEmpty()) {
                try {
                    particleType = ParticleType.of(particleName.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid particle type for structure " + key);
                    particleType = null;
                }
            } else {
                particleType = null;
            }
            val particlesAmount = node.node("particles", "amount").getInt(10);

            var sound = node.node("sound").getString();
            if (sound != null && !XSound.of(sound).isPresent()) {
                logger.warning("Invalid sound for structure " + key);
                sound = null;
            }

            Map<Character, ItemStack> blocks = Maps.newHashMap();

            val compatibility = node.node("compatibility").getList(String.class, new ArrayList<>());

            if (type == StructureType.PATTERN) {
                for (Map.Entry<Object, CommentedConfigurationNode> blockEntry : node.node("blocks").childrenMap().entrySet()) {
                    val character = blockEntry.getKey().toString().charAt(0);
                    val itemStack = MaterialUtils.findMaterialByNameAndCreateItem(blockEntry.getValue().getString("STONE"));

                    if (itemStack == null) {
                        logger.warning("Invalid material for character " + character + " in structure " + key);
                        continue;
                    }

                    blocks.put(character, itemStack);
                }

                val patternNode = node.node("pattern").childrenMap();

                Multimap<Integer, String> pattern = ArrayListMultimap.create();

                for (Map.Entry<Object, CommentedConfigurationNode> patternEntry : patternNode.entrySet()) {
                    val y = Integer.parseInt(patternEntry.getKey().toString());
                    val row = patternEntry.getValue().getList(String.class, new ArrayList<>());

                    pattern.putAll(y, row);
                }
                loadedStructures.put(
                        key.toLowerCase(Locale.ROOT),
                        new PatternStructure(type, key, direction, onlyAir, item, cannotPlaceItem, iterations, distance, sound, particleType, particlesAmount, compatibility, blocks, pattern)
                );
            }

            if (type == StructureType.SCHEMATIC) {

                val schematicFileName = node.node("schematic").getString();
                if (schematicFileName == null) {
                    logger.warning("Invalid schematic file for structure " + key);
                    continue;
                }

                val schematicsFolder = plugin.getDataFolder().toPath().resolve("schematics");
                if (!schematicsFolder.toFile().exists()) {
                    schematicsFolder.toFile().mkdirs();
                }

                val schematicFile = new File(schematicsFolder.toFile(), schematicFileName);
                if (!schematicFile.exists()) {
                    logger.warning("Schematic file " + schematicFileName + " not found for structure " + key);
                    continue;
                }


                val rotationEnabled = node.node("rotation", "enabled").getBoolean(false);
                val rotationDefaultOffsetDegrees = node.node("rotation", "default-offset").getInt(0);

                loadedStructures.put(
                        key.toLowerCase(Locale.ROOT),
                        new SchematicStructure(type, key, direction, onlyAir, item, cannotPlaceItem, iterations, distance, sound, particleType, particlesAmount, compatibility, schematicFile, rotationEnabled, rotationDefaultOffsetDegrees)
                );

            }

        }
    }

}
