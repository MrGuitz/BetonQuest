package org.betonquest.betonquest.compatibility.worldguard;

import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.condition.online.OnlineCondition;
import org.betonquest.betonquest.instruction.variable.VariableString;

/**
 * Checks if the player is in specified region.
 */
public class RegionCondition implements OnlineCondition {

    /**
     * Region name.
     */
    private final VariableString name;

    /**
     * Creates a new region condition.
     *
     * @param name the name of the region
     */
    public RegionCondition(final VariableString name) {
        this.name = name;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        return WorldGuardIntegrator.isInsideRegion(profile.getPlayer().getLocation(), name.getValue(profile));
    }
}
