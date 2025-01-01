package app.miyuki.miyukistructurepattern.schematic.hooks;

import app.miyuki.miyukistructurepattern.schematic.Schematic;
import app.miyuki.miyukistructurepattern.schematic.SchematicReader;
import app.miyuki.miyukistructurepattern.schematic.SchematicVector;
import app.miyuki.miyukistructurepattern.util.math.MathUtils;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockData;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.World;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldEdit6 implements SchematicReader {

    private static MethodHandle getMaterialHandle;

    private final Map<Integer, Material> materialMap = Maps.newHashMap();

    private final Map<File, Map<Integer, Schematic>> schematicCache = Maps.newHashMap();

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Method getMaterialMethod = Material.class.getMethod("getMaterial", int.class);
            getMaterialHandle = lookup.unreflect(getMaterialMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            getMaterialHandle = null;
        }
    }

    @SneakyThrows
    @Override
    public @Nullable Schematic read(@NotNull File file, int degree, boolean ignoreAir) {
        var roundedDegree = MathUtils.roundToNearestMultiple(degree, 90);
        roundedDegree = Math.abs(roundedDegree);

        if (schematicCache.containsKey(file)) {
            val schematic = schematicCache.get(file).get(roundedDegree);
            if (schematic != null) {
                return schematic;
            }
        }

        if (!file.exists()) {
            return null;
        }

        val clipboard = getClipboard(file);
        if (clipboard == null) {
            return null;
        }

        final Vector minimumPoint = clipboard.getMinimumPoint();
        final Vector maximumPoint = clipboard.getMaximumPoint();
        final int minX = (int) minimumPoint.getX();
        final int maxX = (int) maximumPoint.getX();
        final int minY = (int) minimumPoint.getY();
        final int maxY = (int) maximumPoint.getY();
        final int minZ = (int) minimumPoint.getZ();
        final int maxZ = (int) maximumPoint.getZ();

        int totalX = 0;
        int totalZ = 0;
        int blockCount = 0;
        int minYFound = Integer.MAX_VALUE;

        List<Schematic.OffsetItem> offsetItems = new ArrayList<>();

        val affineTransform = new AffineTransform().rotateY(roundedDegree);

        List<RotatedBlock> rotatedBlocks = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    val currentVector = new Vector(x, y, z);
                    val block = clipboard.getBlock(currentVector);

                    int blockId = block.getId();
                    int blockData = block.getData();

                    for (int i = 0; i < (roundedDegree / 90); i++) {
                        blockData = BlockData.rotate90(blockId, block.getData());
                    }

                    val material = materialMap.computeIfAbsent(blockId, id -> {
                        try {
                            return (Material) getMaterialHandle.invoke(id);
                        } catch (Throwable throwable) {
                            return Material.AIR;
                        }
                    });

                    if (ignoreAir && material == Material.AIR) {
                        continue;
                    }

                    val item = new ItemStack(material, 1, (short) blockData);

                    val newVector = affineTransform.apply(currentVector);

                    rotatedBlocks.add(new RotatedBlock(newVector, item));
                }
            }
        }

        int newMinX = rotatedBlocks.stream().mapToInt(rb -> (int) rb.vector.getX()).min().orElse(0);
        int newMinY = rotatedBlocks.stream().mapToInt(rb -> (int) rb.vector.getY()).min().orElse(0);
        int newMinZ = rotatedBlocks.stream().mapToInt(rb -> (int) rb.vector.getZ()).min().orElse(0);

        for (RotatedBlock rotatedBlock : rotatedBlocks) {
            int offsetX = (int) (rotatedBlock.vector.getX() - newMinX);
            int offsetY = (int) (rotatedBlock.vector.getY() - newMinY);
            int offsetZ = (int) (rotatedBlock.vector.getZ() - newMinZ);

            offsetItems.add(new Schematic.OffsetItem(new SchematicVector(offsetX, offsetY, offsetZ), rotatedBlock.item));

            totalX += offsetX;
            totalZ += offsetZ;
            if (offsetY < minYFound) {
                minYFound = offsetY;
            }
            blockCount++;
        }

        if (blockCount <= 0) {
            return null;
        }

        double centerX = (double) totalX / blockCount;
        double centerZ = (double) totalZ / blockCount;
        double centerY = minYFound;

        val schematic = new Schematic(offsetItems, new SchematicVector((int) centerX, (int) centerY, (int) centerZ));

        schematicCache.computeIfAbsent(file, k -> Maps.newHashMap()).put(roundedDegree, schematic);

        return schematic;
    }

    private static class RotatedBlock {
        Vector vector;
        ItemStack item;

        RotatedBlock(Vector vector, ItemStack item) {
            this.vector = vector;
            this.item = item;
        }
    }

    private ClipboardFormat findClipboardFormat(File file) {
        return ClipboardFormat.findByFile(file);
    }

    private World getAnyWorld() {
        return BukkitUtil.getLocalWorld(Bukkit.getWorlds().get(0));
    }

    @SneakyThrows
    private @Nullable Clipboard getClipboard(File file) {
        @Cleanup val inputStream = new FileInputStream(file);
        @Cleanup val bufferedInputStream = new BufferedInputStream(inputStream);

        val clipboardFormat = findClipboardFormat(file);
        if (clipboardFormat == null) {
            return null;
        }

        val clipboardReader = clipboardFormat.getReader(bufferedInputStream);
        val worldData = getAnyWorld().getWorldData();
        return clipboardReader.read(worldData);
    }


}