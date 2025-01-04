package app.miyuki.miyukistructurepattern.compatibility.hooks;

import app.miyuki.miyukistructurepattern.compatibility.Compatibility;
import lombok.val;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GriefPrevention implements Compatibility {

    @Override
    public boolean supports() {
        return Bukkit.getPluginManager().getPlugin("GriefPrevention") != null;
    }

    @Override
    public boolean canPlace(@NotNull Player player, @NotNull Location location) {

        val claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, true, null);

        if (claim == null)
            return false;

        return claim.checkPermission(player, ClaimPermission.Build, null) == null;
    }
}
