package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.constants.NBTKey;
import de.tr7zw.changeme.nbtapi.NBT;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class StructureUtil {

    public boolean hasStructureId(@Nullable ItemStack item) {
        return getStructureId(item) != null;
    }

    public void setStructureId(@Nullable ItemStack item, @Nullable String structureId) {
        if (item == null || item.getType() == Material.AIR)
            return;

        NBT.modify(item, nbt -> {
            nbt.setString(NBTKey.STRUCTURE_ID, structureId);
        });
    }

    @Nullable
    public String getStructureId(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return null;

        val structureId = NBT.get(item, compound -> {
            return compound.getString(NBTKey.STRUCTURE_ID);
        });

        if (structureId == null || structureId.isEmpty()) {
            return null;
        }

        return structureId;
    }

}
