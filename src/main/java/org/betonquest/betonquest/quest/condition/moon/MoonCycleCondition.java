package org.betonquest.betonquest.quest.condition.moon;

import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.condition.nullable.NullableCondition;
import org.betonquest.betonquest.instruction.variable.Variable;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * A condition that checks the moon cycle in the given world.
 */
public class MoonCycleCondition implements NullableCondition {

    /**
     * The world to check the moon cycle in.
     */
    private final Variable<World> variableWorld;

    /**
     * The moon cycle to check for.
     */
    private final Variable<Number> moonCycle;

    /**
     * Checks if the moon cycle in the given world matches the moon cycle of this condition.
     *
     * @param variableWorld the world to check the moon cycle in
     * @param moonCycle     the moon cycle to check for
     */
    public MoonCycleCondition(final Variable<World> variableWorld, final Variable<Number> moonCycle) {
        this.variableWorld = variableWorld;
        this.moonCycle = moonCycle;
    }

    @Override
    public boolean check(@Nullable final Profile profile) throws QuestException {
        final World world = variableWorld.getValue(profile);
        final int moonCycleInt = moonCycle.getValue(profile).intValue();
        if (moonCycleInt < 1 || moonCycleInt > 8) {
            throw new QuestException("Invalid moon cycle key: " + moonCycleInt);
        }
        final int days = (int) (world.getFullTime() / 24_000);
        int phaseInt = days % 8;
        phaseInt += 1;
        return phaseInt == moonCycleInt;
    }
}
