package app.miyuki.miyukistructurepattern.structure.schematic;

import app.miyuki.miyukistructurepattern.structure.Structure;
import app.miyuki.miyukistructurepattern.structure.StructureAnimationDirectionType;
import app.miyuki.miyukistructurepattern.structure.StructureType;
import fr.mrmicky.fastparticles.ParticleType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
@Setter
public class SchematicStructure extends Structure {

    private final File schematic;

    private final boolean rotationEnabled;

    private final int defaultRotationOffset;

    public SchematicStructure(
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
            @NotNull List<@NotNull String> compatibilities,
            @NotNull File schematic,
            boolean rotationEnabled,
            int defaultRotationOffset
    ) {
        super(
                type,
                id,
                animationDirection,
                onlyAir,
                item,
                cannotPlaceBlock,
                iterations,
                distance,
                sound,
                particle,
                particleAmount,
                compatibilities
        );
        this.schematic = schematic;
        this.rotationEnabled = rotationEnabled;
        this.defaultRotationOffset = defaultRotationOffset;
    }


}
