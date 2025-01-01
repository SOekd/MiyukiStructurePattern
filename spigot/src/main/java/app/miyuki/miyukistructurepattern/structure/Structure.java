package app.miyuki.miyukistructurepattern.structure;

import fr.mrmicky.fastparticles.ParticleType;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class Structure {

    private final StructureType type;

    private final String id;

    private final boolean onlyAir;

    private final StructureAnimationDirectionType animationDirection;

    private final ItemStack item;

    private final ItemStack cannotPlaceBlock;

    private final int iterations;

    private final int distance;

    private final String sound;

    private final ParticleType particle;

    private final int particleAmount;

    private final List<String> compatibilities;

    public Structure(
            @NotNull StructureType type,
            @NotNull String id,
            @NotNull StructureAnimationDirectionType animationDirection,
            boolean onlyAir,
            @NotNull ItemStack item,
            @Nullable ItemStack cannotPlaceBlock,
            int iterations,
            int distance,
            @Nullable String sound,
            @Nullable ParticleType particle,
            int particleAmount,
            @NotNull List<@NotNull String> compatibilities
    ) {
        this.type = type;
        this.id = id;
        this.animationDirection = animationDirection;
        this.onlyAir = onlyAir;
        this.item = item;
        this.cannotPlaceBlock = cannotPlaceBlock;
        this.iterations = iterations;
        this.distance = distance;
        this.sound = sound;
        this.particle = particle;
        this.particleAmount = particleAmount;
        this.compatibilities = compatibilities;
    }

}
