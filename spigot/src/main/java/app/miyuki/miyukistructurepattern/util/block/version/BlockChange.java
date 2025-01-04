package app.miyuki.miyukistructurepattern.util.block.version;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BlockChange<D> {

    void sendFakeBlockChange(@NotNull Player player, @NotNull List<StructureBlock<D>> blocks);

    void setType(@NotNull StructureBlock<D> item);

    void clearFakeBlockChange(@NotNull Player player, @NotNull List<Location> locations);

    @NotNull D extractData(@NotNull Block block);

    @NotNull D extractData(@NotNull ItemStack itemStack);

    boolean isAir(@NotNull D itemData);

}
