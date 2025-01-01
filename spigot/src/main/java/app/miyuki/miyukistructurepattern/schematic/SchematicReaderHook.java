package app.miyuki.miyukistructurepattern.schematic;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class SchematicReaderHook {

    @Nullable
    public SchematicReader hook() {
        val worldEdit = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            return null;
        }

        val version = worldEdit.getDescription().getVersion();
        boolean isWorldEdit6 = version.startsWith("6.");

        return isWorldEdit6 ?
                SchematicReaderType.WORLDEDIT_6.getSchematicReader() :
                SchematicReaderType.WORLDEDIT_7.getSchematicReader();
    }

}
