package app.miyuki.miyukistructurepattern.structure.schematic;

import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.structure.StructureConstructor;
import app.miyuki.miyukistructurepattern.structure.pattern.PatternStructure;
import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.workload.WorkloadRunnable;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SchematicStructureConstructor implements StructureConstructor<PatternStructure> {

    private final Map<UUID, List<StructureBlock>> cachedPreview = Maps.newConcurrentMap();

    private final WorkloadRunnable workloadRunnable;

    private final Configuration configuration;


    @Override
    public void clearPreview(Player player) {

    }

    @Override
    public void preview(Player player, Location origin, PatternStructure structure) {

    }

    @Override
    public void refreshPreview(Player player) {

    }

    @Override
    public boolean construct(Player player, Location origin, PatternStructure structure) {
        return false;
    }
}
