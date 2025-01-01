package app.miyuki.miyukistructurepattern.message;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MessageBuilder {

    @Nullable
    private List<String> chat;

    @Nullable
    private String actionBar;

    @Nullable
    private String title;

    @Nullable
    private String subtitle;

    @Nullable
    private String sound;

    private int fadeIn;

    private int stay;

    private int fadeOut;

    public MessageBuilder chat(@Nullable List<String> chat) {
        this.chat = chat;
        return this;
    }

    public MessageBuilder actionBar(@Nullable String actionBar) {
        this.actionBar = actionBar;
        return this;
    }

    public MessageBuilder title(@Nullable String title) {
        this.title = title;
        return this;
    }

    public MessageBuilder subtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public MessageBuilder sound(@Nullable String sound) {
        this.sound = sound;
        return this;
    }

    public MessageBuilder fadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
        return this;
    }

    public MessageBuilder stay(int stay) {
        this.stay = stay;
        return this;
    }

    public MessageBuilder fadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
        return this;
    }

    public Message build() {
        return new Message(chat, actionBar, title, subtitle, sound, fadeIn, stay, fadeOut);
    }

}
