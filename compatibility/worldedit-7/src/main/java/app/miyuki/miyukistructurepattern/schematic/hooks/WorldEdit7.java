package app.miyuki.miyukistructurepattern.schematic.hooks;

import app.miyuki.miyukistructurepattern.schematic.Schematic;
import app.miyuki.miyukistructurepattern.schematic.SchematicReader;
import app.miyuki.miyukistructurepattern.schematic.SchematicVector;
import app.miyuki.miyukistructurepattern.util.math.MathUtils;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldEdit7 implements SchematicReader {

    private final Map<File, Map<Integer, Schematic>> schematicCache = Maps.newHashMap();

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

        final BlockVector3 minimumPoint = clipboard.getMinimumPoint();
        final BlockVector3 maximumPoint = clipboard.getMaximumPoint();
        final int minX = minimumPoint.getX();
        final int maxX = maximumPoint.getX();
        final int minY = minimumPoint.getY();
        final int maxY = maximumPoint.getY();
        final int minZ = minimumPoint.getZ();
        final int maxZ = maximumPoint.getZ();

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
                    val currentVector = BlockVector3.at(x, y, z);
                    BlockState block = clipboard.getBlock(currentVector);

                    for (int i = 0; i < (roundedDegree / 90); i++) {
                        block = BlockTransformExtent.transform(block, affineTransform);
                    }

                    if (ignoreAir && block.getBlockType().getMaterial().isAir()) {
                        continue;
                    }


                    BlockData blockData = BukkitAdapter.adapt(block);

                    val newVector = affineTransform.apply(Vector3.at(x, y, z));

                    rotatedBlocks.add(new RotatedBlock(newVector, blockData));
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
        Vector3 vector;
        BlockData item;

        RotatedBlock(Vector3 vector, BlockData item) {
            this.vector = vector;
            this.item = item;
        }
    }

    private ClipboardFormat findClipboardFormat(File file) {
        return ClipboardFormats.findByFile(file);
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
        return clipboardReader.read();
    }

}