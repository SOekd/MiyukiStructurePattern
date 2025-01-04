package app.miyuki.miyukistructurepattern.util.block;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import app.miyuki.miyukistructurepattern.util.block.version.BlockChange;
import app.miyuki.miyukistructurepattern.util.block.version.LegacyBlockChange;
import app.miyuki.miyukistructurepattern.util.block.version.ModernBlockChange;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@UtilityClass
public class BlockUtil {

    private static final BlockChange<Object> BLOCK_CHANGE;

    static {
        BlockChange<?> tempBlockChange;

        try {
            Class.forName("org.bukkit.block.data.BlockData");
            tempBlockChange = new ModernBlockChange();
        } catch (ClassNotFoundException e) {
            tempBlockChange = new LegacyBlockChange();
        }

        BLOCK_CHANGE = (BlockChange<Object>) tempBlockChange;
    }

    public void sendFakeBlockChange(@NotNull Player player, @NotNull List<StructureBlock<Object>> blocks) {
        BLOCK_CHANGE.sendFakeBlockChange(player, blocks);
    }

    public void clearFakeBlockChange(@NotNull Player player, @NotNull List<Location> locations) {
        BLOCK_CHANGE.clearFakeBlockChange(player, locations);
    }

    public Object extractData(Block block) {
        return BLOCK_CHANGE.extractData(block);
    }

    public Object extractData(ItemStack itemStack) {
        return BLOCK_CHANGE.extractData(itemStack);
    }

    public boolean isAir(Object itemData) {
        return BLOCK_CHANGE.isAir(itemData);
    }

    public void setType(@NotNull StructureBlock<Object> block) {
        BLOCK_CHANGE.setType(block);
    }
}
