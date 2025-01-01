package app.miyuki.miyukistructurepattern.structure.pattern;

import app.miyuki.miyukistructurepattern.structure.Structure;
import app.miyuki.miyukistructurepattern.structure.StructureAnimationDirectionType;
import app.miyuki.miyukistructurepattern.structure.StructureType;
import com.google.common.collect.Multimap;
import fr.mrmicky.fastparticles.ParticleType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
@Setter
public class PatternStructure extends Structure {

    private final Map<Character, ItemStack> blocks;

    private final Multimap<Integer, String> pattern;

    public PatternStructure(
            StructureType type,
            @NotNull String id,
            StructureAnimationDirectionType animationDirection,
            boolean onlyAir,
            @NotNull ItemStack item,
            @Nullable ItemStack cannotPlaceBlock,
            int iterations,
            int distance,
            @Nullable String sound,
            @Nullable ParticleType particle,
            int particleAmount,
            @NotNull List<@NotNull String> compatibilities,
            @NotNull Map<@NotNull Character, @NotNull ItemStack> blocks,
            @NotNull Multimap<@NotNull Integer, @NotNull String> pattern
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
        this.blocks = blocks;
        this.pattern = pattern;
    }


}
