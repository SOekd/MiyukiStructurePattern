package app.miyuki.miyukistructurepattern.structure.workload;

import app.miyuki.miyukistructurepattern.structure.Structure;
import app.miyuki.miyukistructurepattern.util.block.BlockUtil;
import app.miyuki.miyukistructurepattern.workload.Workload;
import com.cryptomorin.xseries.XSound;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;

@RequiredArgsConstructor
public class StructureWorkload implements Workload {

    private final StructureBlock<Object> structureBlock;

    private final Structure structure;

    @Override
    public void compute() {
        val blockLocation = structureBlock.getLocation();

        val block = blockLocation.getBlock();

        if (structure.isOnlyAir() && block.getType() != Material.AIR)
            return;

        BlockUtil.setType(structureBlock);

        try {
            val particle = structure.getParticle();
            if (particle != null) {
                particle.spawn(blockLocation.getWorld(), blockLocation, structure.getParticleAmount());
            }
        } catch (Exception ignored) {

        }

        if (structure.getSound() != null) {
            XSound.of(structure.getSound()).ifPresent(sound -> sound.play(blockLocation));
        }
    }
}
