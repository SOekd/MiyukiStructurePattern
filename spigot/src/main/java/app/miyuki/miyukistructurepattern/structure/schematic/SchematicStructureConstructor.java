package app.miyuki.miyukistructurepattern.structure.schematic;

import app.miyuki.miyukistructurepattern.compatibility.CompatibilityType;
import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.schematic.Schematic;
import app.miyuki.miyukistructurepattern.schematic.SchematicReader;
import app.miyuki.miyukistructurepattern.structure.StructureAnimationDirectionType;
import app.miyuki.miyukistructurepattern.structure.StructureConstructor;
import app.miyuki.miyukistructurepattern.structure.workload.AnimatedStructureWorkload;
import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.structure.workload.StructureWorkload;
import app.miyuki.miyukistructurepattern.util.block.BlockUtil;
import app.miyuki.miyukistructurepattern.util.math.MathUtils;
import app.miyuki.miyukistructurepattern.workload.Workload;
import app.miyuki.miyukistructurepattern.workload.WorkloadRunnable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SchematicStructureConstructor implements StructureConstructor<SchematicStructure> {

    private final Map<UUID, List<StructureBlock>> cachedPreview = Maps.newConcurrentMap();

    private final Cache<UUID, Map<String, Integer>> cachedRotations = CacheBuilder.newBuilder()
            .maximumSize(2000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    private final WorkloadRunnable workloadRunnable;

    @Nullable
    private final SchematicReader schematicReader;

    private final Configuration configuration;

    @Override
    public void clearPreview(Player player) {
        if (!cachedPreview.containsKey(player.getUniqueId()))
            return;

        val preview = cachedPreview.remove(player.getUniqueId())
                .stream()
                .map(StructureBlock::getLocation)
                .collect(Collectors.toList());

        BlockUtil.clearFakeBlockChange(player, preview);
    }

    @Override
    public void refreshPreview(Player player) {
        val preview = cachedPreview.get(player.getUniqueId());
        if (preview == null)
            return;

        BlockUtil.sendFakeBlockChange(player, preview);
    }

    @SneakyThrows
    private int getCachedOrInsertRotation(@NotNull Player player, @NotNull SchematicStructure structure, @Nullable Integer degree) {
        val cachedRotationOffset = cachedRotations.getIfPresent(player.getUniqueId());
        if (cachedRotationOffset != null && cachedRotationOffset.containsKey(structure.getId())) {
            return cachedRotationOffset.get(structure.getId());
        }

        if (degree == null) {
            val yaw = player.getEyeLocation().getYaw();
            degree = MathUtils.roundToNearestMultiple((int) yaw, 90)
                    + MathUtils.roundToNearestMultiple(structure.getDefaultRotationOffset(), 90);
        }

        val finalRotation = Math.abs(degree % 360);

        cachedRotations.get(player.getUniqueId(), Maps::newHashMap)
                .put(structure.getId(), finalRotation);

        return finalRotation;

    }

    @SneakyThrows
    @Override
    public void preview(Player player, Location origin, SchematicStructure structure) {
        if (schematicReader == null) {
            return;
        }

        val structureFile = structure.getSchematic();


        val cachedRotationOffset = getCachedOrInsertRotation(player, structure, null);

        val schematic = schematicReader.read(structureFile, cachedRotationOffset, true);
        if (schematic == null) {
            return;
        }

        List<StructureBlock> preview = new ArrayList<>();

        for (Schematic.OffsetItem offsetItem : schematic.getOffsetItems()) {

            val vector = offsetItem.getOffset();

            int offsetX = vector.getX();
            int offsetY = vector.getY();
            int offsetZ = vector.getZ();

            ItemStack item = offsetItem.getItem();

            val center = schematic.getCenter();

            double adjustedX = offsetX - center.getX();
            double adjustedZ = offsetZ - center.getZ();
            double adjustedY = offsetY;

            Location blockLocation = origin.clone().add(adjustedX, adjustedY, adjustedZ);

            Block block = blockLocation.getBlock();

            if (!canPlace(structure, player, blockLocation)) {
                if (structure.getCannotPlaceBlock() != null && item.getType() != Material.AIR) {

                    if (structure.isOnlyAir() && block.getType() != Material.AIR) {
                        continue;
                    }

                    preview.add(new StructureBlock(blockLocation, structure.getCannotPlaceBlock()));
                }
                continue;
            }

            if (structure.isOnlyAir() && blockLocation.getBlock().getType() != Material.AIR) {
                continue;
            }

            preview.add(new StructureBlock(blockLocation, item));
        }

        placePreview(player, preview);
    }

    @SneakyThrows
    public void rotate(@NotNull Player player, SchematicStructure structure) {
        clearPreview(player);

        val cachedRotationOffset = getCachedOrInsertRotation(player, structure, null);
        cachedRotations.get(player.getUniqueId(), Maps::newHashMap)
                .put(structure.getId(), Math.abs((cachedRotationOffset + 90) % 360));
    }

    @SneakyThrows
    @Override
    public boolean construct(Player player, Location origin, SchematicStructure structure) {
        if (schematicReader == null) {
            return false;
        }

        val structureFile = structure.getSchematic();

        val cachedRotationOffset = getCachedOrInsertRotation(player, structure, null);

        val schematic = schematicReader.read(structureFile, cachedRotationOffset, true);
        if (schematic == null) {
            return false;
        }

        List<StructureBlock> structureBlocks = new ArrayList<>();

        boolean blockPlaced = false;

        for (Schematic.OffsetItem offsetItem : schematic.getOffsetItems()) {

            val vector = offsetItem.getOffset();

            int offsetX = vector.getX();
            int offsetY = vector.getY();
            int offsetZ = vector.getZ();

            val item = offsetItem.getItem();

            val center = schematic.getCenter();

            double adjustedX = offsetX - center.getX();
            double adjustedZ = offsetZ - center.getZ();
            double adjustedY = offsetY;

            Location blockLocation = origin.clone().add(adjustedX, adjustedY, adjustedZ);

            if (!canPlace(structure, player, blockLocation)) {
                continue;
            }

            if (structure.isOnlyAir() && blockLocation.getBlock().getType() != Material.AIR) {
                continue;
            }

            blockPlaced = true;

            structureBlocks.add(new StructureBlock(blockLocation, item));
        }


        getWorkloads(structureBlocks, structure).forEach(workloadRunnable::addWorkload);

        return blockPlaced;
    }

    private void placePreview(Player player, List<StructureBlock> preview) {
        val oldPreview = cachedPreview.get(player.getUniqueId());

        if (oldPreview != null) {
            Set<Location> newLocations = preview.stream()
                    .map(StructureBlock::getLocation)
                    .collect(Collectors.toSet());

            val locations = oldPreview.stream()
                    .map(StructureBlock::getLocation)
                    .filter(location -> !newLocations.contains(location))
                    .collect(Collectors.toList());

            BlockUtil.clearFakeBlockChange(player, locations);
        }

        BlockUtil.sendFakeBlockChange(player, preview);
        cachedPreview.put(player.getUniqueId(), preview);
    }

    private boolean canPlace(@NotNull SchematicStructure structure, @NotNull Player player, @NotNull Location location) {
        val compatibilities = CompatibilityType.getStructureCompatibilities(structure);
        if (compatibilities.isEmpty()) {
            return true;
        }

        if (location.getWorld().getMaxHeight() < location.getBlockY()) {
            return false;
        }

        return compatibilities.stream().anyMatch(compatibility -> compatibility.canPlace(player, location));
    }

    private List<Workload> getWorkloads(List<StructureBlock> blocks, SchematicStructure structure) {
        val animationEnabled = configuration.getRoot().node("animation", "enabled").getBoolean(true);

        if (!animationEnabled) {

            return blocks.stream()
                    .map(block -> new StructureWorkload(block, structure))
                    .collect(Collectors.toList());
        }

        var delay = configuration.getRoot().node("animation", "delay").getInt(1);
        delay = Math.max(0, delay);

        if (structure.getAnimationDirection() == StructureAnimationDirectionType.HORIZONTAL) {
            StructureBlock.Sorter.sortHorizontally(blocks);
        } else {
            StructureBlock.Sorter.sortVertically(blocks);
        }

        return Collections.singletonList(new AnimatedStructureWorkload(new LinkedList<>(blocks), structure, delay));

    }

}
