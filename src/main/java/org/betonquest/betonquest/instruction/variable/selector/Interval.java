package org.betonquest.betonquest.instruction.variable.selector;

import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.instruction.variable.VariableNumber;

/**
 * A selector that checks if a value is in a certain interval.
 */
public class Interval implements NumberSelector {

    /**
     * The start value of the interval.
     */
    private final VariableNumber startValue;

    /**
     * The end value of the interval.
     */
    private final VariableNumber endValue;

    /**
     * Creates a new interval.
     *
     * @param startValue The start value of the interval
     * @param endValue   The end value of the interval
     */
    public Interval(final VariableNumber startValue, final VariableNumber endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
    }

    @Override
    public boolean isInRange(final Profile profile, final double value) throws QuestRuntimeException {
        final double value1 = startValue.getValue(profile).doubleValue();
        final double value2 = endValue.getValue(profile).doubleValue();
        if (value1 > value2) {
            return value >= value2 && value <= value1;
        }
        return value >= value1 && value <= value2;
    }
}
