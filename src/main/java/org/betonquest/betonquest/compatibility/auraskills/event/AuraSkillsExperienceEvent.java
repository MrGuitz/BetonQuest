package org.betonquest.betonquest.compatibility.auraskills.event;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.api.quest.event.PlayerEvent;
import org.betonquest.betonquest.instruction.variable.VariableNumber;
import org.betonquest.betonquest.instruction.variable.VariableString;
import org.betonquest.betonquest.util.Utils;

/**
 * Gives experience to a player in a skill.
 */
public class AuraSkillsExperienceEvent implements PlayerEvent {
    /**
     * The {@link AuraSkillsApi}.
     */
    private final AuraSkillsApi auraSkillsApi;

    /**
     * The {@link VariableNumber} of experience to give.
     */
    private final VariableNumber amountVar;

    /**
     * The {@link VariableString} of the Skill name to give experience in.
     */
    private final VariableString nameVar;

    /**
     * If the amount is a level and not experience.
     */
    private final boolean isLevel;

    /**
     * Create a new AuraSkills experience event.
     *
     * @param auraSkillsApi the {@link AuraSkillsApi}.
     * @param amountVar     the {@link VariableNumber} amount of experience to give the player in the skill.
     * @param nameVar       the {@link VariableString} of the {@link Skill} name to give experience in.
     * @param isLevel       {@code true} if the amount is a level. Otherwise {@code false} if the amount is experience.
     */
    public AuraSkillsExperienceEvent(final AuraSkillsApi auraSkillsApi, final VariableNumber amountVar,
                                     final VariableString nameVar, final boolean isLevel) {
        this.auraSkillsApi = auraSkillsApi;
        this.amountVar = amountVar;
        this.nameVar = nameVar;
        this.isLevel = isLevel;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        final SkillsUser user = auraSkillsApi.getUser(profile.getPlayerUUID());
        if (user == null) {
            return;
        }

        final NamespacedId namespacedId = NamespacedId.fromDefault(nameVar.getValue(profile));
        final Skill skill = Utils.getNN(auraSkillsApi.getGlobalRegistry().getSkill(namespacedId), "Invalid skill name");
        final int amount = amountVar.getValue(profile).intValue();
        if (!isLevel) {
            user.addSkillXpRaw(skill, amount);
            return;
        }

        final int currentLevel = user.getSkillLevel(skill);
        for (int i = 1; i <= amount; i++) {
            final double requiredXP = auraSkillsApi.getXpRequirements().getXpRequired(skill, currentLevel + i);
            user.addSkillXpRaw(skill, requiredXP);
        }
    }
}
