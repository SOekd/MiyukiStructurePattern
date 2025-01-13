package app.miyuki.miyukistructurepattern.structure.pattern;

import app.miyuki.miyukistructurepattern.compatibility.CompatibilityType;
import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.constants.Permissions;
import app.miyuki.miyukistructurepattern.structure.StructureAnimationDirectionType;
import app.miyuki.miyukistructurepattern.structure.StructureConstructor;
import app.miyuki.miyukistructurepattern.structure.workload.AnimatedStructureWorkload;
import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.structure.workload.StructureWorkload;
import app.miyuki.miyukistructurepattern.util.block.BlockUtil;
import app.miyuki.miyukistructurepattern.workload.Workload;
import app.miyuki.miyukistructurepattern.workload.WorkloadRunnable;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PatternStructureConstructor implements StructureConstructor<PatternStructure> {

    private final Map<UUID, List<StructureBlock<Object>>> cachedPreview = Maps.newConcurrentMap();

    private final WorkloadRunnable workloadRunnable;

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

    @Override
    public void preview(Player player, Location origin, PatternStructure structure) {
        val blocks = structure.getBlocks();

        val pattern = structure.getPattern();

        List<StructureBlock<Object>> preview = new ArrayList<>();

        for (int iteration = 0; iteration < structure.getIterations(); iteration++) {
            for (int y : pattern.keySet().stream().mapToInt(Integer::intValue).sorted().toArray()) {
                List<String> rows = new ArrayList<>(pattern.get(y));
                int centerY = rows.size() / 2;

                for (int i = 0; i < rows.size(); i++) {
                    String row = rows.get(i);
                    int centerX = row.length() / 2;
                    for (int x = 0; x < row.length(); x++) {
                        char blockTypeKey = row.charAt(x);

                        ItemStack blockMaterial = blocks.getOrDefault(blockTypeKey, new ItemStack(Material.AIR));

                        int offsetX = x - centerX;
                        int offsetY = i - centerY;

                        Location blockLocation = origin.clone().add(offsetX, y - 1.0, offsetY);

                        Block block = blockLocation.getBlock();

                        if (!canPlace(structure, player, blockLocation)) {
                            if (structure.getCannotPlaceBlock() != null && blockMaterial.getType() != Material.AIR) {

                                if (structure.isOnlyAir() && block.getType() != Material.AIR) {
                                    continue;
                                }

                                preview.add(new StructureBlock<>(blockLocation, BlockUtil.extractData(structure.getCannotPlaceBlock())));
                            }
                            continue;
                        }

                        preview.add(new StructureBlock<>(blockLocation, BlockUtil.extractData(blockMaterial)));
                    }
                }
            }

            origin.add(0, pattern.keySet().size(), 0);
        }

        placePreview(player, preview);
    }

    @Override
    public boolean construct(Player player, Location origin, PatternStructure structure) {
        val blocks = structure.getBlocks();

        val pattern = structure.getPattern();

        List<StructureBlock<Object>> structureBlocks = new ArrayList<>();

        boolean placeBlock = false;
        for (int iteration = 0; iteration < structure.getIterations(); iteration++) {
            for (int y : pattern.keySet().stream().mapToInt(Integer::intValue).sorted().toArray()) {
                List<String> rows = new ArrayList<>(pattern.get(y));
                int centerY = rows.size() / 2;

                for (int i = 0; i < rows.size(); i++) {
                    String row = rows.get(i);
                    int centerX = row.length() / 2;
                    for (int x = 0; x < row.length(); x++) {
                        char blockTypeKey = row.charAt(x);

                        ItemStack blockMaterial = blocks.getOrDefault(blockTypeKey, new ItemStack(Material.AIR));

                        int offsetX = x - centerX;
                        int offsetY = i - centerY;

                        Location blockLocation = origin.clone().add(offsetX, y - 1.0, offsetY);

                        if (!canPlace(structure, player, blockLocation)) {
                            continue;
                        }

                        placeBlock = true;

                        structureBlocks.add(new StructureBlock<>(blockLocation, BlockUtil.extractData(blockMaterial)));
                    }
                }
            }

            origin.add(0, pattern.keySet().size(), 0);
        }

        getWorkloads(structureBlocks, structure).forEach(workloadRunnable::addWorkload);

        return placeBlock;
    }

    private void placePreview(Player player, List<StructureBlock<Object>> preview) {
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

    private boolean canPlace(@NotNull PatternStructure structure, @NotNull Player player, @NotNull Location location) {
        val compatibilities = CompatibilityType.getStructureCompatibilities(structure);

        if (location.getWorld().getMaxHeight() < location.getBlockY()) {
            return false;
        }

        if (compatibilities.isEmpty()) {
            return true;
        }

        if (player.hasPermission(Permissions.BYPASS_PROTECTION)) {
            return true;
        }

        return compatibilities.stream().anyMatch(compatibility -> compatibility.canPlace(player, location));
    }

    private List<Workload> getWorkloads(List<StructureBlock<Object>> blocks, PatternStructure structure) {
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
