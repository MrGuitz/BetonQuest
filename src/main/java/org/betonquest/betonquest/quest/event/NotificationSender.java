package org.betonquest.betonquest.quest.event;

import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.config.PluginMessage;

/**
 * Allows sending notifications to a player.
 */
@FunctionalInterface
public interface NotificationSender {

    /**
     * Send the notification.
     *
     * @param profile   the {@link Profile} of the player to receive the notification
     * @param variables the variables to use in the notification
     */
    void sendNotification(Profile profile, PluginMessage.Replacement... variables);
}
