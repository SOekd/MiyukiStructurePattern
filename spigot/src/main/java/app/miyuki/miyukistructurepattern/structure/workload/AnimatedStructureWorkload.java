package app.miyuki.miyukistructurepattern.structure.workload;

import app.miyuki.miyukistructurepattern.structure.Structure;
import app.miyuki.miyukistructurepattern.util.block.BlockUtil;
import app.miyuki.miyukistructurepattern.workload.ScheduledWorkload;
import com.cryptomorin.xseries.XSound;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;

import java.util.Queue;

@RequiredArgsConstructor
public class AnimatedStructureWorkload implements ScheduledWorkload {

    private final Queue<StructureBlock<Object>> blocks;

    private final Structure structure;

    private final int animationSpeed;

    private long nextBlockPlace;

    @Override
    public void compute() {

        if (System.currentTimeMillis() < nextBlockPlace) {
            return;
        }

        nextBlockPlace = System.currentTimeMillis() + animationSpeed;

        val structureBlock = blocks.poll();
        if (structureBlock == null) {
            return;
        }

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

    @Override
    public boolean shouldBeRescheduled() {
        return !blocks.isEmpty();
    }

}
