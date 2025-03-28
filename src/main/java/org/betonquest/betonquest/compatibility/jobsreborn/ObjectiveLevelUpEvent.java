package org.betonquest.betonquest.compatibility.jobsreborn;

import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.api.profile.Profile;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.instruction.Instruction;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@SuppressWarnings("PMD.CommentRequired")
public class ObjectiveLevelUpEvent extends Objective implements Listener {
    /**
     * Job to level up.
     */
    private final VariableJob job;

    public ObjectiveLevelUpEvent(final Instruction instructions) throws QuestException {
        super(instructions);
        this.job = instructions.get(VariableJob::new);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJobsLevelUpEvent(final JobsLevelUpEvent event) throws QuestException {
        final OnlineProfile profile = profileProvider.getProfile(event.getPlayer().getPlayer());
        if (event.getJob().isSame(this.job.getValue(profile)) && containsPlayer(profile) && checkConditions(profile)) {
            completeObjective(profile);
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "";
    }

    @Override
    public String getProperty(final String name, final Profile profile) {
        return "";
    }
}
