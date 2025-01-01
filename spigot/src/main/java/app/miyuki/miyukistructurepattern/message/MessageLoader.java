package app.miyuki.miyukistructurepattern.message;

import app.miyuki.miyukistructurepattern.configuration.Configuration;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class MessageLoader {

    private final Configuration messages;

    private final BukkitAudiences audiences;

    private final Map<String, MessageBuilder> cachedMessages = Maps.newHashMap();

    @NotNull
    public Message load(@NotNull String path) {
        if (cachedMessages.containsKey(path)) {
            val message = cachedMessages.get(path).build();

            message.init(audiences);

            return message;
        }

        MessageBuilder messageBuilder = new MessageBuilder();

        val messageNode = messages.getRoot().node((Object[]) path.split(Pattern.quote(".")));

        loadChatMessage(messageBuilder, messageNode.copy());
        loadActionBarMessage(messageBuilder, messageNode.copy());
        loadTitleMessage(messageBuilder, messageNode.copy());
        loadSoundMessage(messageBuilder, messageNode.copy());

        cachedMessages.put(path, messageBuilder);

        val message = messageBuilder.build();
        message.init(audiences);
        return message;
    }

    @SneakyThrows(SerializationException.class)
    private void loadChatMessage(@NotNull MessageBuilder messageBuilder, @NotNull CommentedConfigurationNode node) {
        if (node.isList()) {
            val message = node.getList(String.class);
            if (message != null)
                messageBuilder.chat(message);
        } else if (node.getString() != null) {
            messageBuilder.chat(Collections.singletonList(node.getString()));
        } else if (node.hasChild("chat")) {
            loadChatMessage(messageBuilder, node.node("chat"));
        }
    }

    private void loadActionBarMessage(@NotNull MessageBuilder messageBuilder, @NotNull CommentedConfigurationNode node) {
        val actionBar = node.node("action-bar").getString();
        if (actionBar != null)
            messageBuilder.actionBar(actionBar);
    }

    private void loadTitleMessage(@NotNull MessageBuilder messageBuilder, @NotNull CommentedConfigurationNode node) {
        val titleNode = node.node("title");
        val title = titleNode.node("title").getString();
        val subtitle = titleNode.node("subtitle").getString();
        val fadeIn = titleNode.node("fade-in").getInt(10);
        val stay = titleNode.node("stay").getInt(70);
        val fadeOut = titleNode.node("fade-out").getInt(20);

        if (title != null && subtitle != null)
            messageBuilder
                    .title(title)
                    .subtitle(subtitle)
                    .fadeIn(fadeIn)
                    .stay(stay)
                    .fadeOut(fadeOut);
    }

    private void loadSoundMessage(@NotNull MessageBuilder messageBuilder, @NotNull CommentedConfigurationNode node) {
        val sound = node.node("sound").getString();
        if (sound != null)
            messageBuilder.sound(sound);
    }

    public void reload() {
        cachedMessages.clear();
        messages.reload();
    }

}
