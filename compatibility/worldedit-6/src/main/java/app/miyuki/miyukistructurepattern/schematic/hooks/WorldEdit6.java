package app.miyuki.miyukistructurepattern.schematic.hooks;

import app.miyuki.miyukistructurepattern.schematic.SchematicReader;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.world.World;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class WorldEdit6 implements SchematicReader {

    @SneakyThrows
    @Override
    public void read(@NotNull File file) {

        @Cleanup FileInputStream inputStream = new FileInputStream(file);
        @Cleanup BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        val schematicFormat = findClipboardFormat(file);
        val clipboardReader = schematicFormat.getReader(bufferedInputStream);

        val clipboard = clipboardReader.read(getAnyWorld().getWorldData());

        final Vector minimumPoint = clipboard.getMinimumPoint();
        final Vector maximumPoint = clipboard.getMaximumPoint();
        final int minX = (int) minimumPoint.getX();
        final int maxX = (int) maximumPoint.getX();
        final int minY = (int) minimumPoint.getY();
        final int maxY = (int) maximumPoint.getY();
        final int minZ = (int) minimumPoint.getZ();
        final int maxZ = (int) maximumPoint.getZ();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    val block = clipboard.getBlock(new Vector(x, y, z));

                    // what i do? i don't know
                    int blockId = block.getId();

                }
            }
        }

    }

    private ClipboardFormat findClipboardFormat(File file) {
        return ClipboardFormat.findByFile(file);
    }

    private World getAnyWorld() {
        return BukkitUtil.getLocalWorld(Bukkit.getWorlds().get(0));
    }

}