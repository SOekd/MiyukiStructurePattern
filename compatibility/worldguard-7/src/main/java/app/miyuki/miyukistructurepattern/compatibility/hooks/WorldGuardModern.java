package app.miyuki.miyukistructurepattern.compatibility.hooks;

import app.miyuki.miyukistructurepattern.compatibility.Compatibility;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class WorldGuardModern implements Compatibility {


    @Override
    public boolean supports() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
    }

    @Override
    public boolean canPlace(@NotNull Player player, @NotNull Location location) {

        val world = BukkitAdapter.adapt(location.getWorld());


        val worldContainer = WorldGuard.getInstance().getPlatform()
                .getRegionContainer()
                .get(world);
        if (worldContainer == null) {
            return true;
        }

        val applicableRegions = worldContainer.getApplicableRegions(BukkitAdapter.asBlockVector(location));

        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        return Stream.of(applicableRegions)
                .anyMatch(region -> region.testState(localPlayer, Flags.BLOCK_PLACE));
    }

}
