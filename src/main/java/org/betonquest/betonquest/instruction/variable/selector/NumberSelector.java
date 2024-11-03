package org.betonquest.betonquest.instruction.variable.selector;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.quest.condition.number.Operation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A selector that checks if a value fulfills a certain condition.
 */
public interface NumberSelector {
    static NumberSelector parse(final String string, final Instruction instruction) throws InstructionParseException {
        if (string.contains(",")) {
            final String[] parts = string.split(",");
            final VariableNumber[] values = new VariableNumber[parts.length];
            for (int i = 0; i < parts.length; i++) {
                values[i] = instruction.getVarNum(parts[i]);
            }
            return new Enumeration(values);
        }
        if (string.contains("-")) {
            final String[] parts = string.split("-");
            final VariableNumber start = instruction.getVarNum(parts[0]);
            final VariableNumber end = instruction.getVarNum(parts[1]);
            return new Interval(start, end);
        }
        final Pattern pattern = Pattern.compile("(\\W+)(\\d+)");
        final Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            final Operation operator = Operation.fromSymbol(matcher.group(1));
            final VariableNumber number = instruction.getVarNum(matcher.group(2));
            return new Comparison(number, operator);
        }
        throw new InstructionParseException("Selector don't match any valid structure: " + string);
    }

    /**
     * Checks if the value fulfills the condition.
     *
     * @param profile The profile to check
     * @param value   The value to check
     * @return Whether the value fulfills the condition
     * @throws QuestRuntimeException If an error occurs
     */
    boolean isInRange(Profile profile, double value) throws QuestRuntimeException;
}
