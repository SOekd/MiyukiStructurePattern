package app.miyuki.miyukistructurepattern.util.block.version;

import app.miyuki.miyukistructurepattern.structure.workload.StructureBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BlockChange {

    void sendFakeBlockChange(@NotNull Player player, @NotNull List<StructureBlock> blocks);

    void setType(@NotNull Block block, @NotNull ItemStack item);

    void clearFakeBlockChange(@NotNull Player player, @NotNull List<Location> locations);
}
