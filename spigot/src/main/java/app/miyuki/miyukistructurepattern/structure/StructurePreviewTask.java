package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.MiyukiStructurePattern;
import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.util.hand.HandUtil;
import app.miyuki.miyukistructurepattern.util.player.PlayerUtil;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class StructurePreviewTask implements Runnable {


    private final MiyukiStructurePattern plugin;

    private final Configuration configuration;

    private final StructureLoader structureLoader;

    public StructurePreviewTask(MiyukiStructurePattern plugin, Configuration configuration, StructureLoader structureLoader) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.structureLoader = structureLoader;

        val configurationRoot = configuration.getRoot().node("preview");

        if (!configurationRoot.node("enabled").getBoolean(true))
            return;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, 0L, configurationRoot.node("update-ticks").getInt(5));
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (configuration.getRoot().node("preview", "shift").getBoolean(false) && !player.isSneaking())
                continue;

            val hand = HandUtil.getItemInMainHand(player);
            val offHand = HandUtil.getItemInOffHand(player);

            if (preview(player, offHand)) {
                return;
            }
            if (preview(player, hand)) {
                return;
            }

            Arrays.stream(StructureType.values())
                    .map(plugin::getStructureConstructor)
                    .forEach(constructor -> constructor.clearPreview(player));
        }
    }

    private boolean preview(Player player, ItemStack hand) {
        String structureId = StructureUtil.getStructureId(hand);

        if (structureId == null)
            return false;

        Structure structure = structureLoader.getLoadedStructure(structureId);
        if (structure == null)
            return false;

        val distance = Math.max(
                configuration.getRoot().node("distance").getInt(6),
                structure.getDistance()
        );

        val constructor = (StructureConstructor<Structure>) plugin.getStructureConstructor(structure.getType());

        constructor.preview(player, PlayerUtil.getCorrectTargetLocation(player, distance), structure);
        return true;
    }


}
