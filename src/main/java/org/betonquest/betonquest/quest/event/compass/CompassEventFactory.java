package org.betonquest.betonquest.quest.event.compass;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.EventFactory;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadEvent;
import org.betonquest.betonquest.utils.location.CompoundLocation;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Factory for the compass event.
 */
public class CompassEventFactory implements EventFactory {

    /**
     * Server to use for syncing to the primary server thread.
     */
    private final Server server;

    /**
     * Scheduler to use for syncing to the primary server thread.
     */
    private final BukkitScheduler scheduler;

    /**
     * Plugin to use for syncing to the primary server thread.
     */
    private final Plugin plugin;

    /**
     * Create the compass event factory.
     *
     * @param server    server to use
     * @param scheduler scheduler to use
     * @param plugin    plugin to use
     */
    public CompassEventFactory(final Server server, final BukkitScheduler scheduler, final Plugin plugin) {
        this.server = server;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    @Override
    public Event parseEvent(final Instruction instruction) throws InstructionParseException {
        final Action action = instruction.getEnum(Action.class);
        final String compass = instruction.next();
        CompoundLocation compassLocation = null;

        // Check if compass is valid
        for (final QuestPackage pack : Config.getPackages().values()) {
            final ConfigurationSection section = pack.getConfig().getConfigurationSection("compass");
            if (section != null && section.contains(compass)) {
                compassLocation = new CompoundLocation(pack, pack.getString("compass." + compass + ".location"));
                break;
            }
        }
        if (compassLocation == null) {
            throw new InstructionParseException("Invalid compass location: " + compass);
        }
        return new PrimaryServerThreadEvent(
                new CompassEvent(action, compass, compassLocation, instruction.getPackage()),
                server, scheduler, plugin);
    }
}
