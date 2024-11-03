package org.betonquest.betonquest.instruction.variable.selector;

import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.quest.condition.number.Operation;

/**
 * A selector that compares a value with another value.
 */
public class Comparison implements NumberSelector {

    /**
     * The value to compare with.
     */
    private final VariableNumber value;

    /**
     * The operation to check.
     */
    private final Operation operator;

    /**
     * Creates a new comparison.
     *
     * @param value    The value to compare with
     * @param operator The operation to check
     */
    public Comparison(final VariableNumber value, final Operation operator) {
        this.value = value;
        this.operator = operator;
    }

    @Override
    public boolean isInRange(final Profile profile, final double value) throws QuestRuntimeException {
        return operator.check(value, this.value.getValue(profile).doubleValue());
    }
}
