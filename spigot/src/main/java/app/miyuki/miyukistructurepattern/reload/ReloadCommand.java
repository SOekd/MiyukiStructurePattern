package app.miyuki.miyukistructurepattern.reload;

import app.miyuki.miyukistructurepattern.MiyukiStructurePattern;
import app.miyuki.miyukistructurepattern.constants.Permissions;
import app.miyuki.miyukistructurepattern.message.MessageLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ReloadCommand implements CommandExecutor {

    @NotNull
    private final MiyukiStructurePattern plugin;

    @NotNull
    private final MessageLoader messageLoader;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.RELOAD)) {
            messageLoader.load("no-permission").send(sender);
            return true;
        }

        plugin.reload();
        messageLoader.load("reloaded").send(sender);
        return true;
    }

}
