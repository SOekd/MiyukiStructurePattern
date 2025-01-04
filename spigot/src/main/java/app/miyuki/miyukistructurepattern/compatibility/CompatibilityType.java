package app.miyuki.miyukistructurepattern.compatibility;

import app.miyuki.miyukistructurepattern.structure.Structure;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum CompatibilityType {

    GRIEFDEFENDER("app.miyuki.miyukistructurepattern.compatibility.hooks.GriefDefender"),
    GRIEFPREVENTION("app.miyuki.miyukistructurepattern.compatibility.hooks.GriefPrevention"),
    PLOTSQUARED_LEGACY("app.miyuki.miyukistructurepattern.compatibility.hooks.PlotSquaredLegacy"),
    PLOTSQUARED("app.miyuki.miyukistructurepattern.compatibility.hooks.PlotSquaredModern"),
    WORLDGUARD_LEGACY("app.miyuki.miyukistructurepattern.compatibility.hooks.WorldGuardLegacy"),
    WORLDGUARD("app.miyuki.miyukistructurepattern.compatibility.hooks.WorldGuardModern");

    private static final Map<CompatibilityType, @Nullable Compatibility> COMPATIBILITY_CACHE = Maps.newHashMap();

    private final String className;

    @Nullable
    public Compatibility getCompatibility() {
        if (COMPATIBILITY_CACHE.containsKey(this)) {
            return COMPATIBILITY_CACHE.get(this);
        }
        try {
            Compatibility compatibility = (Compatibility) Class.forName(className).getDeclaredConstructor().newInstance();
            COMPATIBILITY_CACHE.put(this, compatibility);
            return compatibility;
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable Compatibility getCompatibilityTypeByName(@NotNull String name) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name.replace("-", "_")))
                .map(CompatibilityType::getCompatibility)
                .filter(Objects::nonNull)
                .filter(Compatibility::supports)
                .findAny()
                .orElse(null);
    }

    public static List<@NotNull Compatibility> getStructureCompatibilities(@NotNull Structure structure) {
        return structure.getCompatibilities().stream()
                .map(CompatibilityType::getCompatibilityTypeByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
