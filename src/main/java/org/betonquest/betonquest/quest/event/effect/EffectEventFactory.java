package org.betonquest.betonquest.quest.event.effect;

import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.api.quest.event.PlayerEventFactory;
import org.betonquest.betonquest.api.quest.event.online.OnlineEventAdapter;
import org.betonquest.betonquest.instruction.Instruction;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.quest.PrimaryServerThreadData;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Factory to create effect events from {@link Instruction}s.
 */
public class EffectEventFactory implements PlayerEventFactory {
    /**
     * Logger factory to create a logger for events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the effect event factory.
     *
     * @param loggerFactory logger factory to use
     * @param data          the data for primary server thread access
     */
    public EffectEventFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final PotionEffectType effect = PotionEffectType.getByName(instruction.next());
        if (effect == null) {
            throw new QuestException("Unknown effect type: " + instruction.current());
        }
        try {
            final VariableNumber duration = instruction.get(VariableNumber::new);
            final VariableNumber level = instruction.get(VariableNumber::new);
            final boolean ambient = instruction.hasArgument("ambient");
            final boolean hidden = instruction.hasArgument("hidden");
            final boolean icon = !instruction.hasArgument("noicon");
            return new PrimaryServerThreadEvent(new OnlineEventAdapter(
                    new EffectEvent(effect, duration, level, ambient, hidden, icon),
                    loggerFactory.create(EffectEvent.class),
                    instruction.getPackage()
            ), data);
        } catch (final QuestException e) {
            throw new QuestException("Could not parse effect duration and amplifier", e);
        }
    }
}
