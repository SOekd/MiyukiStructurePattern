package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.MiyukiStructurePattern;
import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.message.MessageLoader;
import app.miyuki.miyukistructurepattern.util.hand.HandUtil;
import app.miyuki.miyukistructurepattern.util.player.PlayerUtil;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public class StructureListener implements Listener {

    private static final List<Action> ALLOWED_ACTIONS = ImmutableList.of(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR);

    @NotNull
    private MiyukiStructurePattern plugin;

    @NotNull
    private final Configuration configuration;

    @NotNull
    private final StructureLoader structureLoader;

    @NotNull
    private final MessageLoader messageLoader;

    @EventHandler
    public void onStructureUse(PlayerInteractEvent event) {

        val player = event.getPlayer();
        val item = event.getItem();
        if (item == null || item.getType() == Material.AIR)
            return;

        boolean isMainHand = HandUtil.isMainHand(event);

        String structureId = StructureUtil.getStructureId(item);
        if (structureId == null)
            return;

        val structure = structureLoader.getLoadedStructure(structureId);

        if (structure == null)
            return;

        val constructor = (StructureConstructor<Structure>) plugin.getStructureConstructor(structure.getType());

        if (!ALLOWED_ACTIONS.contains(event.getAction())) {
            if (configuration.getRoot().node("preview", "shift").getBoolean(false) && !player.isSneaking()) {
                return;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> constructor.refreshPreview(player), 1L);
            return;
        }

        val distance = Math.max(
                configuration.getRoot().node("distance").getInt(6),
                structure.getDistance()
        );

        if (constructor.construct(player, PlayerUtil.getCorrectTargetLocation(player, distance), structure)) {
            if (isMainHand) {
                HandUtil.decrementItemInMainHand(player);
            } else {
                HandUtil.decrementItemInOffHand(player);
            }

            messageLoader.load("structure-placed").send(player);
        }

    }

    @EventHandler
    public void onBlockStructureUse(PlayerInteractEvent event) {
        if (StructureUtil.hasStructureId(event.getItem())) {
            event.setCancelled(true);
        }
    }

}
