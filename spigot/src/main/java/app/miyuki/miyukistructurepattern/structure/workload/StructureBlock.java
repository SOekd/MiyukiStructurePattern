package app.miyuki.miyukistructurepattern.structure.workload;

import lombok.Data;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;

@Data
public class StructureBlock<D> {

    private final Location location;

    private final D itemData;

    public static class Sorter {

        public static <D> void sortVertically(List<StructureBlock<D>> structureBlocks) {
            structureBlocks.sort(Comparator
                    .<StructureBlock<D>>comparingDouble(block -> block.getLocation().getY())
                    .thenComparingDouble(block -> block.getLocation().getX())
                    .thenComparingDouble(block -> block.getLocation().getZ()));
        }

        public static <D> void sortHorizontally(List<StructureBlock<D>> structureBlocks) {
            structureBlocks.sort(Comparator
                    .<StructureBlock<D>>comparingDouble(block -> block.getLocation().getX())
                    .thenComparingDouble(block -> block.getLocation().getY())
                    .thenComparingDouble(block -> block.getLocation().getZ()));
        }
    }

}