package org.betonquest.betonquest.quest.event.compass;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.BetonQuestLogger;
import org.betonquest.betonquest.api.QuestCompassTargetChangeEvent;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.location.CompoundLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Locale;

/**
 * Event for the compass event.
 */
public class CompassEvent implements Event {

    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private static final BetonQuestLogger LOG = BetonQuestLogger.create();

    /**
     * The action to perform.
     */
    private final Action action;

    /**
     * The compass name.
     */
    private final String compass;

    /**
     * The target location.
     */
    private final CompoundLocation compassLocation;

    /**
     * The quest package.
     */
    private final QuestPackage questPackage;

    /**
     * Creates a compass event, that targets a specific location.
     *
     * @param action          the action to perform
     * @param compass         the compass name
     * @param compassLocation the target location
     * @param questPackage    the quest package
     */
    public CompassEvent(final Action action, final String compass, final CompoundLocation compassLocation, final QuestPackage questPackage) {
        this.action = action;
        this.compass = compass;
        this.compassLocation = compassLocation;
        this.questPackage = questPackage;
    }

    @Override
    public void execute(final Profile profile) throws QuestRuntimeException {
        if (action == Action.SET) {
            executeSet(profile);
            return;
        }
        try {
            final Instruction tagInstruction = new Instruction(questPackage, null, "tag " + action.toString().toLowerCase(Locale.ROOT) + " compass-" + compass);
            BetonQuest.getInstance().getEventFactory("tag").parseEventInstruction(tagInstruction).handle(profile);
        } catch (final InstructionParseException e) {
            LOG.warn(questPackage, "Failed to tag " + profile + " with compass point: " + compass, e);
        }
    }

    private void executeSet(final Profile profile) {
        if (profile.getOnlineProfile().isEmpty()) {
            return;
        }
        try {
            final Location location = compassLocation.getLocation(profile);
            final QuestCompassTargetChangeEvent event = new QuestCompassTargetChangeEvent(profile, location);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            profile.getOnlineProfile().get().getPlayer().setCompassTarget(location);
        } catch (final QuestRuntimeException e) {
            LOG.warn(questPackage, "Failed to set compass: " + compass, e);
        }
    }
}
