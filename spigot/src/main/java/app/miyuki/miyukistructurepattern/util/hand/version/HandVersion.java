package app.miyuki.miyukistructurepattern.util.hand.version;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HandVersion {

    boolean isMainHand(@NotNull PlayerInteractEvent event);

    boolean isOffHand(@NotNull PlayerInteractEvent event);

    boolean decrementItemInMainHand(@NotNull Player player);

    boolean decrementItemInOffHand(@NotNull Player player);

    @Nullable ItemStack getItemInMainHand(@NotNull Player player);

    @Nullable ItemStack getItemInOffHand(@NotNull Player player);

}
