package app.miyuki.miyukistructurepattern.message;

import app.miyuki.miyukistructurepattern.util.chat.ColorHelper;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
@Getter
public class Message {

    @Nullable
    private final List<String> chat;

    @Nullable
    private final String actionBar;

    @Nullable
    private final String title;

    @Nullable
    private final String subtitle;

    @Nullable
    private final String sound;

    private final int fadeIn;

    private final int stay;

    private final int fadeOut;

    private BukkitAudiences audiences;

    public void init(@NotNull BukkitAudiences audiences) {
        this.audiences = audiences;
    }

    public void send(@NotNull CommandSender sender) {
        send(sender, UnaryOperator.identity());
    }

    public void send(@NotNull CommandSender sender, @Nullable UnaryOperator<String> formatter) {

        formatter = formatter == null ? UnaryOperator.identity() : formatter;

        if (chat != null) {
            for (String line : chat) {
                audiences.sender(sender).sendMessage(ColorHelper.colorize(formatter.apply(line)));
            }
        }

        if (!(sender instanceof Player))
            return;

        if (actionBar != null) {
            audiences.sender(sender).sendActionBar(ColorHelper.colorize(formatter.apply(actionBar)));
        }
        if (title != null && subtitle != null) {

            val titleTime = Title.Times.times(
                    Ticks.duration(fadeIn),
                    Ticks.duration(stay),
                    Ticks.duration(fadeOut)
            );

            val finalTitle = Title.title(ColorHelper.colorize(formatter.apply(title)), ColorHelper.colorize(formatter.apply(subtitle)), titleTime);
            audiences.sender(sender).showTitle(finalTitle);
        }

        if (sound != null) {
            XSound.matchXSound(sound)
                    .map(XSound::parseSound)
                    .ifPresent(bukkitSound -> ((Player) sender).playSound(((Player) sender).getLocation(), bukkitSound, 1, 1));
        }

    }

}
