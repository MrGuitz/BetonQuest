package org.betonquest.betonquest.quest.event.variable;

import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.QuestTypeAPI;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.id.ObjectiveID;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.betonquest.betonquest.quest.objective.variable.VariableObjective;

/**
 * Event that changes variables that is stored in variable objective.
 */
public class VariableEvent implements PlayerEvent {

    /**
     * Quest Type API.
     */
    private final QuestTypeAPI questTypeAPI;

    /**
     * The variable objective id to change the variable in.
     */
    private final Variable<ObjectiveID> objectiveID;

    /**
     * The key of the variable to store.
     */
    private final Variable<String> key;

    /**
     * The value of the variable to store.
     */
    private final Variable<String> value;

    /**
     * Create a new variable event.
     *
     * @param questTypeAPI the Quest Type API
     * @param objectiveID  the objective id of the variable objective
     * @param key          the key of the variable to store
     * @param value        the value of the variable to store
     */
    public VariableEvent(final QuestTypeAPI questTypeAPI, final Variable<ObjectiveID> objectiveID, final Variable<String> key, final Variable<String> value) {
        this.questTypeAPI = questTypeAPI;
        this.objectiveID = objectiveID;
        this.key = key;
        this.value = value;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        final ObjectiveID resolved = this.objectiveID.getValue(profile);
        final Objective obj = questTypeAPI.getObjective(resolved);
        if (!(obj instanceof final VariableObjective objective)) {
            throw new QuestException(resolved.getFullID() + " is not a variable objective");
        }
        final String keyReplaced = key.getValue(profile);
        final String valueReplaced = value.getValue(profile);
        if (!objective.store(profile, keyReplaced, valueReplaced)) {
            throw new QuestException("Player " + profile.getProfileName() + " does not have '"
                    + resolved + "' objective, cannot store a variable.");
        }
    }
}
