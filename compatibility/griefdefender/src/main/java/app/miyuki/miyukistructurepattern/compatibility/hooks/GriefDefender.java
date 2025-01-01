package app.miyuki.miyukistructurepattern.compatibility.hooks;

import app.miyuki.miyukistructurepattern.compatibility.Compatibility;
import com.griefdefender.api.claim.TrustTypes;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GriefDefender implements Compatibility {
    @Override
    public boolean canPlace(@NotNull Player player, @NotNull Location location) {

        val claim = com.griefdefender.api.GriefDefender.getCore().getClaimAt(location);

        if (claim == null || claim.isWilderness())
            return false;

        return claim.isUserTrusted(player.getUniqueId(), TrustTypes.BUILDER);
    }
}
