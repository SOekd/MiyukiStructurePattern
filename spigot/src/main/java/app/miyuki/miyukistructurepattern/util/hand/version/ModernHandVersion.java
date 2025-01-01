package app.miyuki.miyukistructurepattern.util.hand.version;

import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModernHandVersion implements HandVersion {

    @Override
    public boolean isMainHand(@NotNull PlayerInteractEvent event) {
        val hand = event.getHand();
        if (hand == null)
            return false;

        return hand == EquipmentSlot.HAND;
    }

    @Override
    public boolean isOffHand(@NotNull PlayerInteractEvent event) {
        val hand = event.getHand();
        if (hand == null)
            return false;

        return hand == EquipmentSlot.OFF_HAND;
    }

    @Override
    public boolean decrementItemInMainHand(@NotNull Player player) {
        val item = player.getInventory().getItemInMainHand();

        if (item.getAmount() == 1) {
            player.getInventory().setItemInMainHand(null);
            return true;
        }

        item.setAmount(item.getAmount() - 1);
        return true;
    }

    @Override
    public boolean decrementItemInOffHand(@NotNull Player player) {
        val item = player.getInventory().getItemInOffHand();

        if (item.getAmount() == 1) {
            player.getInventory().setItemInOffHand(null);
            return true;
        }

        item.setAmount(item.getAmount() - 1);
        return true;
    }

    @Override
    public @Nullable ItemStack getItemInMainHand(@NotNull Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public @Nullable ItemStack getItemInOffHand(@NotNull Player player) {
        return player.getInventory().getItemInOffHand();
    }

}
