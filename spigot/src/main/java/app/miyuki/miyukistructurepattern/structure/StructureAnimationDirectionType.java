package app.miyuki.miyukistructurepattern.structure;

import org.jetbrains.annotations.NotNull;

public enum StructureAnimationDirectionType {

    VERTICAL,
    HORIZONTAL;

    public static StructureAnimationDirectionType findByName(@NotNull String name) {
        for (StructureAnimationDirectionType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return HORIZONTAL;
    }

}
