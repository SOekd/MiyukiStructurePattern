package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.MiyukiStructurePattern;
import app.miyuki.miyukistructurepattern.configuration.Configuration;
import app.miyuki.miyukistructurepattern.message.MessageLoader;
import app.miyuki.miyukistructurepattern.structure.schematic.SchematicStructure;
import app.miyuki.miyukistructurepattern.structure.schematic.SchematicStructureConstructor;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class StructureListener implements Listener {

    private static final List<Action> PLACE_ACTIONS = ImmutableList.of(
            Action.RIGHT_CLICK_BLOCK,
            Action.RIGHT_CLICK_AIR
    );

    private static final List<Action> ROTATE_ACTIONS = ImmutableList.of(
            Action.LEFT_CLICK_AIR,
            Action.LEFT_CLICK_BLOCK
    );

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

        val distance = Math.max(
                configuration.getRoot().node("distance").getInt(6),
                structure.getDistance()
        );

        val constructor = (StructureConstructor<Structure>) plugin.getStructureConstructor(structure.getType());

        if (!PLACE_ACTIONS.contains(event.getAction())) {

            if (structure.getType() == StructureType.SCHEMATIC && ROTATE_ACTIONS.contains(event.getAction())) {
                SchematicStructureConstructor schematicConstructor = (SchematicStructureConstructor) plugin.getStructureConstructor(StructureType.SCHEMATIC);
                schematicConstructor.rotate(player, (SchematicStructure) structure);

                constructor.preview(player, PlayerUtil.getCorrectTargetLocation(player, distance), structure);
                return;
            }

            if (configuration.getRoot().node("preview", "shift").getBoolean(false) && !player.isSneaking()) {
                return;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> constructor.refreshPreview(player), 1L);
            return;
        }

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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        val player = event.getPlayer();

        Arrays.stream(StructureType.values())
                .map(plugin::getStructureConstructor)
                .forEach(constructor -> constructor.clearPreview(player));
    }

}
