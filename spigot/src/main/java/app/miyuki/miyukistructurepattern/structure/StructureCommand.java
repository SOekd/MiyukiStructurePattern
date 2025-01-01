package app.miyuki.miyukistructurepattern.structure;

import app.miyuki.miyukistructurepattern.MiyukiStructurePattern;
import app.miyuki.miyukistructurepattern.constants.Permissions;
import app.miyuki.miyukistructurepattern.message.MessageLoader;
import app.miyuki.miyukistructurepattern.util.number.NumberHelper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StructureCommand implements CommandExecutor, TabCompleter {

    @NotNull
    private final MiyukiStructurePattern plugin;

    @NotNull
    private final MessageLoader messageLoader;

    @NotNull
    private final StructureLoader structureLoader;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission(Permissions.GIVE)) {
            messageLoader.load("no-permission").send(sender);
            return true;
        }

        if (args.length != 3) {
            messageLoader.load("give.usage").send(sender);
            return true;
        }

        val target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messageLoader.load("give.player-not-found").send(sender);
            return true;
        }

        val amountValue = args[2];
        if (!NumberHelper.isInteger(amountValue)) {
            messageLoader.load("give.invalid-amount").send(sender);
            return true;
        }

        val amount = Integer.parseInt(amountValue);

        val structureId = args[1].toLowerCase(Locale.ROOT);

        val structure = structureLoader.getLoadedStructure(structureId);
        if (structure == null) {
            messageLoader.load("give.structure-not-found").send(sender);
            return true;
        }

        val item = structure.getItem().clone();

        item.setAmount(amount);

        StructureUtil.setStructureId(item, structureId);

        target.getInventory().addItem(item)
                .forEach((unused, itemStack) -> target.getWorld().dropItem(target.getLocation(), itemStack));

        messageLoader.load("give.success").send(sender, line -> line
                .replace("{player}", target.getName())
                .replace("{structure}", structureId)
        );
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            val written = args[0];
            val completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            StringUtil.copyPartialMatches(written, completions, suggestions);
        }

        if (args.length == 2) {
            val written = args[1];
            val completions = structureLoader.getLoadedStructures().stream()
                    .map(Structure::getId)
                    .map(it -> it.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());
            StringUtil.copyPartialMatches(written, completions, suggestions);
        }

        return suggestions;
    }

}
