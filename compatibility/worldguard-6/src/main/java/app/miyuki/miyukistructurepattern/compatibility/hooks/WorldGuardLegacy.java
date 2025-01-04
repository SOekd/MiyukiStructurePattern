package app.miyuki.miyukistructurepattern.compatibility.hooks;

import app.miyuki.miyukistructurepattern.compatibility.Compatibility;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class WorldGuardLegacy implements Compatibility {


    @Override
    public boolean supports() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
    }

    @Override
    public boolean canPlace(@NotNull Player player, @NotNull Location location) {
        val applicableRegions = getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);

        val localPlayer = getWorldGuard().wrapPlayer(player);

        return Stream.of(applicableRegions)
                .anyMatch(region -> region.testState(localPlayer, DefaultFlag.BLOCK_PLACE));
    }

    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

}
