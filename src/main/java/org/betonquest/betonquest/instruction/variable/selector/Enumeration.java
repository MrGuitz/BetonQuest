package org.betonquest.betonquest.instruction.variable.selector;

import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.instruction.variable.VariableNumber;

import java.util.Arrays;

/**
 * A selector that checks if a value is in an enumeration.
 */
public class Enumeration implements NumberSelector {

    /**
     * The values of the enumeration.
     */
    private final VariableNumber[] values;

    /**
     * Creates a new enumeration.
     *
     * @param values The values of the enumeration
     */
    public Enumeration(final VariableNumber... values) {
        this.values = Arrays.copyOf(values, values.length);
    }

    @Override
    public boolean isInRange(final Profile profile, final double value) throws QuestRuntimeException {
        for (final VariableNumber v : values) {
            if (v.getValue(profile).doubleValue() == value) {
                return true;
            }
        }
        return false;
    }
}
