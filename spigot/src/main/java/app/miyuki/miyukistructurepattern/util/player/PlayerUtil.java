package app.miyuki.miyukistructurepattern.util.player;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

@UtilityClass
public class PlayerUtil {


    public Block getTargetBlock(Player player, int range) {
        val iterator = new BlockIterator(player, range);
        Block lastBlock = iterator.next();
        while (iterator.hasNext()) {
            lastBlock = iterator.next();
            if (lastBlock.getType() != Material.AIR) {
                break;
            }
        }
        return lastBlock;
    }

    public Location getTargetLocation(Player player, int range) {
        return getTargetBlock(player, range).getLocation();
    }

    public Location getCorrectTargetLocation(Player player, int range) {
        val targetBlock = player.getLastTwoTargetBlocks(null, range);
        if (targetBlock.size() != 2) {
            return getTargetLocation(player, range);
        } else {
            return targetBlock.get(0).getLocation();
        }
    }


}
