package app.miyuki.miyukistructurepattern.structure.workload;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

@Data
public class StructureBlock {

    private final Location location;

    private final ItemStack material;

    public static class Sorter {

        public static void sortVertically(List<StructureBlock> structureBlocks) {
            structureBlocks.sort(Comparator
                    .<StructureBlock>comparingDouble(block -> block.getLocation().getY())
                    .thenComparingDouble(block -> block.getLocation().getX())
                    .thenComparingDouble(block -> block.getLocation().getZ()));
        }

        public static void sortHorizontally(List<StructureBlock> structureBlocks) {
            structureBlocks.sort(Comparator
                    .<StructureBlock>comparingDouble(block -> block.getLocation().getX())
                    .thenComparingDouble(block -> block.getLocation().getY())
                    .thenComparingDouble(block -> block.getLocation().getZ()));
        }
    }

}