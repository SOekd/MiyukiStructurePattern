package app.miyuki.miyukistructurepattern.compatibility;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Compatibility {

    boolean canPlace(@NotNull Player player, @NotNull Location location);

}
