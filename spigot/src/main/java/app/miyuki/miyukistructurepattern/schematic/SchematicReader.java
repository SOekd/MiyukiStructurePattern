package app.miyuki.miyukistructurepattern.schematic;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface SchematicReader {

    void read(@NotNull File file);

}
