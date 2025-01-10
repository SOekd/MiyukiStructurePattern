package app.miyuki.miyukistructurepattern.compatibility.hooks;

import app.miyuki.miyukistructurepattern.compatibility.Compatibility;
import com.intellectualcrafters.plot.object.Plot;
import com.plotsquared.bukkit.util.BukkitUtil;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotSquaredLegacy implements Compatibility {

    @Override
    public boolean supports() {
        val plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        return Bukkit.getPluginManager().getPlugin("PlotSquared") != null
                && plugin.getDescription().getVersion().startsWith("3.");
    }

    @Override
    public boolean canPlace(@NotNull Player player, @NotNull Location location) {
        val plot = Plot.getPlot(BukkitUtil.getLocation(location));
        return plot != null && plot.isAdded(player.getUniqueId());
    }

}