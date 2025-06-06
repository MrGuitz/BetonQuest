package org.betonquest.betonquest.conversation.interceptor;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.bukkit.entity.Player;

@SuppressWarnings("PMD.CommentRequired")
public class NonInterceptingInterceptor implements Interceptor {

    protected final Player player;

    public NonInterceptingInterceptor(final OnlineProfile onlineProfile) {
        this.player = onlineProfile.getPlayer();
    }

    @Override
    public void sendMessage(final String message) {
        player.spigot().sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(final BaseComponent... message) {
        player.spigot().sendMessage(message);
    }

    @Override
    public void sendMessage(final Component message) {
        player.sendMessage(message);
    }

    @Override
    public void end() {
        // Empty
    }
}
