package app.miyuki.miyukistructurepattern.util.chat;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ColorHelper {

    @Getter
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private static final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .build();

    @NotNull
    public Component colorize(@NotNull String text) {
        Component legacyComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(text);

        val serializedLegacy = LegacyComponentSerializer.legacySection().serialize(legacyComponent);

        legacyComponent = LegacyComponentSerializer.legacySection().deserialize(serializedLegacy);

        val message = miniMessage.serialize(legacyComponent)
                .replace("\\<", "<")
                .replace("\\\\<", "<");

        return miniMessage.deserialize(message);
    }

    @NotNull
    public String legacyColorize(@NotNull Component component) {
        return legacyComponentSerializer.serialize(component);
    }

    @NotNull
    public String legacyColorize(@NotNull String text) {
        return legacyColorize(colorize(text));
    }

}
