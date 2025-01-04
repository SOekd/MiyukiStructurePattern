package app.miyuki.miyukistructurepattern.util.chunk;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ChunkUtil {

    public <D> Map<Vector3i, List<StructureBlock<D>>> groupStructureBlockByChunk(List<StructureBlock<D>> blocks) {
        Map<Vector3i, List<StructureBlock<D>>> chunkMap = new HashMap<>();

        for (StructureBlock<D> block : blocks) {
            Location loc = block.getLocation();

            int chunkX = loc.getBlockX() >> 4;

            int chunkY = 0;
            if (usesChunkY()) {
                chunkY = loc.getBlockY() >> 4;
            }

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
            if (usesChunkY()) {
                chunkY = loc.getBlockY() >> 4;
            }

            int chunkZ = loc.getBlockZ() >> 4;

            Vector3i chunkCoord = new Vector3i(chunkX, chunkY, chunkZ);

            chunkMap.computeIfAbsent(chunkCoord, k -> new ArrayList<>()).add(loc);
        }

        return chunkMap;
    }

    private boolean usesChunkY() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_16_2);
    }

}
