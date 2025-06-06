package org.betonquest.betonquest.quest.event.run;

import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.nullable.NullableEvent;
import org.betonquest.betonquest.kernel.processor.adapter.EventAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Allows for running multiple events.
 */
public class RunEvent implements NullableEvent {

    /**
     * Events that the run event will execute.
     */
    private final List<EventAdapter> events;

    /**
     * Create a run event from the given instruction.
     *
     * @param events events to run
     */
    public RunEvent(final List<EventAdapter> events) {
        this.events = events;
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        for (final EventAdapter event : events) {
            event.fire(profile);
        }
    }
}
