package app.miyuki.miyukistructurepattern.compatibility.hooks;

import app.miyuki.miyukistructurepattern.compatibility.Compatibility;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.plot.Plot;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotSquaredModern implements Compatibility {

    @Override
    public boolean supports() {
        return Bukkit.getPluginManager().getPlugin("PlotSquared") != null;
    }

    @Override
    public boolean canPlace(@NotNull Player player, @NotNull Location location) {
        val plot = Plot.getPlot(BukkitUtil.adapt(location));

        if (plot == null) {
            return false;
        }

        if (plot.getTopAbs().getY() < location.getBlockY()) {
            return false;
        }

        return plot.isAdded(player.getUniqueId());
    }

}