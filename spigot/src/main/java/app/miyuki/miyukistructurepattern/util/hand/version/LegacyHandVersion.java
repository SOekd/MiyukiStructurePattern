package app.miyuki.miyukistructurepattern.util.hand.version;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegacyHandVersion implements HandVersion {

    @Override
    public boolean isMainHand(@NotNull PlayerInteractEvent event) {
        return true;
    }

    @Override
    public boolean isOffHand(@NotNull PlayerInteractEvent event) {
        return false;
    }

    @Override
    public boolean decrementItemInMainHand(@NotNull Player player) {
        val item = player.getItemInHand();

        if (item.getAmount() == 1) {
            player.setItemInHand(null);
            return true;
        }

        item.setAmount(item.getAmount() - 1);
        return true;
    }

    @Override
    public boolean decrementItemInOffHand(@NotNull Player player) {
        return false;
    }

    @Override
    public @Nullable ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getItemInHand();
    }

    @Override
    public @Nullable ItemStack getItemInOffHand(@NotNull Player player) {
        return null;
    }

}
