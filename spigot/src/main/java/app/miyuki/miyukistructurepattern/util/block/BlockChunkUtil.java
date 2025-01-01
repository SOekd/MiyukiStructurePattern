package app.miyuki.miyukistructurepattern.util.block;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class BlockChunkUtil {

    public static Map<Vector3i, List<StructureBlock>> groupStructureBlockByChunk(List<StructureBlock> blocks) {
        Map<Vector3i, List<StructureBlock>> chunkMap = new HashMap<>();

        for (StructureBlock block : blocks) {
            Location loc = block.getLocation();

            int chunkX = loc.getBlockX() >> 4;

            // 1.16.5 maybe it's needed, i'll check it later
            int chunkY = 0;

            int chunkZ = loc.getBlockZ() >> 4;

            Vector3i chunkCoord = new Vector3i(chunkX, chunkY, chunkZ);

            chunkMap.computeIfAbsent(chunkCoord, k -> new ArrayList<>()).add(block);
        }

        return chunkMap;
    }

    public Map<Vector3i, List<Location>> groupLocationByChunk(List<Location> locations) {
        Map<Vector3i, List<Location>> chunkMap = new HashMap<>();

        for (Location loc : locations) {
            int chunkX = loc.getBlockX() >> 4;
            int chunkY = 0;
            int chunkZ = loc.getBlockZ() >> 4;

            Vector3i chunkCoord = new Vector3i(chunkX, chunkY, chunkZ);

            chunkMap.computeIfAbsent(chunkCoord, k -> new ArrayList<>()).add(loc);
        }

        return chunkMap;
    }

}
