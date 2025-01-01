package app.miyuki.miyukistructurepattern.schematic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface SchematicReader {

    @Nullable Schematic read(@NotNull File file, int degree, boolean ignoreAir);

}
