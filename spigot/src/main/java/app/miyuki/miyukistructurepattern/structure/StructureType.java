package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.structure.pattern.PatternStructure;
import app.miyuki.miyukistructurepattern.structure.schematic.SchematicStructure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StructureType {

    PATTERN(PatternStructure.class),
    SCHEMATIC(SchematicStructure.class);

    private final Class<? extends Structure> structureClass;

    public static StructureType findByName(String name) {
        for (StructureType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

}
