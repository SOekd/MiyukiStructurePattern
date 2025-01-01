package app.miyuki.miyukistructurepattern.schematic;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SchematicReaderType {

    WORLDEDIT_6("app.miyuki.miyukistructurepattern.schematic.hooks.WorldEdit6"),
    WORLDEDIT_7("app.miyuki.miyukistructurepattern.schematic.hooks.WorldEdit7");

    private final String className;

    public SchematicReader getSchematicReader() {
        try {
            return (SchematicReader) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}