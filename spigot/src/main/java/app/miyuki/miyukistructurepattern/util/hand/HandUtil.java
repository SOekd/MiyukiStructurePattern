package app.miyuki.miyukistructurepattern.util.hand;

import app.miyuki.miyukistructurepattern.util.hand.version.HandVersion;
import app.miyuki.miyukistructurepattern.util.hand.version.LegacyHandVersion;
import app.miyuki.miyukistructurepattern.util.hand.version.ModernHandVersion;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

@UtilityClass
public class HandUtil {

    private static final HandVersion HAND_VERSION;

    static {
        Field[] fields = EquipmentSlot.class.getFields();
        boolean hasHand = false;
        boolean hasOffHand = false;

        for (Field field : fields) {
            if (field.getName().equals("HAND")) {
                hasHand = true;
            } else if (field.getName().equals("OFF_HAND")) {
                hasOffHand = true;
            }
        }

        if (hasHand && hasOffHand) {
            HAND_VERSION = new ModernHandVersion();
        } else {
            HAND_VERSION = new LegacyHandVersion();
        }
    }

    public boolean isMainHand(@NotNull PlayerInteractEvent event) {
        return HAND_VERSION.isMainHand(event);
    }

    public boolean isOffHand(@NotNull PlayerInteractEvent event) {
        return HAND_VERSION.isOffHand(event);
    }

    public boolean decrementItemInMainHand(@NotNull Player player) {
        return HAND_VERSION.decrementItemInMainHand(player);
    }

    public boolean decrementItemInOffHand(@NotNull Player player) {
        return HAND_VERSION.decrementItemInOffHand(player);
    }

    public @Nullable ItemStack getItemInMainHand(@NotNull Player player) {
        return HAND_VERSION.getItemInMainHand(player);
    }

    public @Nullable ItemStack getItemInOffHand(@NotNull Player player) {
        return HAND_VERSION.getItemInOffHand(player);
    }

}
