package org.betonquest.betonquest.quest.registry;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.bstats.InstructionMetricsSupplier;
import org.betonquest.betonquest.config.PluginMessage;
import org.betonquest.betonquest.feature.registry.FeatureRegistries;
import org.betonquest.betonquest.feature.registry.processor.CancellerProcessor;
import org.betonquest.betonquest.feature.registry.processor.CompassProcessor;
import org.betonquest.betonquest.feature.registry.processor.ConversationProcessor;
import org.betonquest.betonquest.id.ID;
import org.betonquest.betonquest.quest.registry.processor.ConditionProcessor;
import org.betonquest.betonquest.quest.registry.processor.EventProcessor;
import org.betonquest.betonquest.quest.registry.processor.ObjectiveProcessor;
import org.betonquest.betonquest.quest.registry.processor.VariableProcessor;
import org.betonquest.betonquest.schedule.EventScheduling;

import java.util.Collection;
import java.util.Map;

/**
 * Stores the active Quest Types, Conversations, Quest Canceller and Event Scheduler.
 *
 * @param log             The custom {@link BetonQuestLogger} instance for this class.
 * @param eventScheduling Event scheduling module.
 * @param conditions      Condition logic.
 * @param events          Event logic.
 * @param objectives      Objective logic.
 * @param variables       Variable logic.
 * @param questCancellers Quest Canceller logic.
 * @param conversations   Conversation Data logic.
 * @param compasses       Compasses.
 */
public record QuestRegistry(BetonQuestLogger log, EventScheduling eventScheduling, ConditionProcessor conditions,
                            EventProcessor events, ObjectiveProcessor objectives, VariableProcessor variables,
                            CancellerProcessor questCancellers, ConversationProcessor conversations,
                            CompassProcessor compasses) {

    /**
     * Create a new Registry for storing and using Conditions, Events, Objectives, Variables,
     * Conversations and Quest canceller.
     *
     * @param log                 the custom logger for this registry
     * @param loggerFactory       the logger factory used for new custom logger instances
     * @param plugin              the plugin used to create new conversation data
     * @param otherRegistries     the available other types
     * @param questTypeRegistries the available quest types
     * @param pluginMessage       the {@link PluginMessage} instance
     * @return the newly created
     */
    public static QuestRegistry create(final BetonQuestLogger log, final BetonQuestLoggerFactory loggerFactory, final BetonQuest plugin,
                                       final FeatureRegistries otherRegistries, final QuestTypeRegistries questTypeRegistries, final PluginMessage pluginMessage) {
        final EventScheduling eventScheduling = new EventScheduling(loggerFactory.create(EventScheduling.class, "Schedules"), otherRegistries.eventScheduling());
        final ConditionProcessor conditions = new ConditionProcessor(loggerFactory.create(ConditionProcessor.class), questTypeRegistries.condition());
        final EventProcessor events = new EventProcessor(loggerFactory.create(EventProcessor.class), questTypeRegistries.event());
        final ObjectiveProcessor objectives = new ObjectiveProcessor(loggerFactory.create(ObjectiveProcessor.class), questTypeRegistries.objective());
        final VariableProcessor variables = new VariableProcessor(loggerFactory.create(VariableProcessor.class), questTypeRegistries.variable());
        final CancellerProcessor cancellers = new CancellerProcessor(loggerFactory.create(CancellerProcessor.class), loggerFactory, pluginMessage, variables);
        final ConversationProcessor conversations = new ConversationProcessor(loggerFactory.create(ConversationProcessor.class), loggerFactory, plugin, variables,
                otherRegistries.conversationIO(), otherRegistries.interceptor());
        final CompassProcessor compasses = new CompassProcessor(loggerFactory.create(CompassProcessor.class), variables);
        return new QuestRegistry(log, eventScheduling, conditions, events, objectives, variables, cancellers, conversations, compasses);
    }

    /**
     * Loads Conditions, Events, Objectives, Variables, Conversations, Quest Canceller and Event Scheduler.
     * <p>
     * Removes previous data and loads the given QuestPackages.
     *
     * @param packages the quest packages to load
     */
    public void loadData(final Collection<QuestPackage> packages) {
        eventScheduling.stopAll();
        conditions.clear();
        events.clear();
        objectives.clear();
        variables.clear();
        questCancellers.clear();
        conversations.clear();
        compasses.clear();

        for (final QuestPackage pack : packages) {
            final String packName = pack.getQuestPath();
            log.debug(pack, "Loading stuff in package " + packName);
            questCancellers.load(pack);
            events.load(pack);
            conditions.load(pack);
            objectives.load(pack);
            conversations.load(pack);
            eventScheduling.loadData(pack);
            compasses.load(pack);

            log.debug(pack, "Everything in package " + packName + " loaded");
        }

        conversations.checkExternalPointers();

        log.info("There are " + conditions.size() + " conditions, " + events.size() + " events, "
                + objectives.size() + " objectives and " + conversations.size() + " conversations loaded from "
                + packages.size() + " packages.");

        eventScheduling.startAll();
    }

    /**
     * Gets the bstats metric supplier for registered and active quest types.
     *
     * @return instruction metrics for conditions, events, objectives and variables
     */
    public Map<String, InstructionMetricsSupplier<? extends ID>> metricsSupplier() {
        return Map.ofEntries(
                conditions.metricsSupplier(),
                events.metricsSupplier(),
                objectives.metricsSupplier(),
                variables.metricsSupplier()
        );
    }
}
