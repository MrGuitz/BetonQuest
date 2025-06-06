package org.betonquest.betonquest.kernel.processor.feature;

import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.betonquest.betonquest.api.logger.BetonQuestLogger;
import org.betonquest.betonquest.api.message.Message;
import org.betonquest.betonquest.api.quest.QuestException;
import org.betonquest.betonquest.feature.journal.JournalMainPageEntry;
import org.betonquest.betonquest.id.ConditionID;
import org.betonquest.betonquest.id.JournalMainPageID;
import org.betonquest.betonquest.kernel.processor.SectionProcessor;
import org.betonquest.betonquest.message.ParsedSectionMessageCreator;
import org.betonquest.betonquest.variables.GlobalVariableResolver;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads and stores Journal Main Pages.
 */
public class JournalMainPageProcessor extends SectionProcessor<JournalMainPageID, JournalMainPageEntry> {

    /**
     * Message creator to parse messages.
     */
    private final ParsedSectionMessageCreator messageCreator;

    /**
     * Create a new QuestProcessor to store and execute type logic.
     *
     * @param log            the custom logger for this class
     * @param messageCreator the message creator to parse messages
     */
    public JournalMainPageProcessor(final BetonQuestLogger log, final ParsedSectionMessageCreator messageCreator) {
        super(log, "Journal Main Page", "journal_main_page");
        this.messageCreator = messageCreator;
    }

    @Override
    protected JournalMainPageEntry loadSection(final QuestPackage pack, final ConfigurationSection section) throws QuestException {
        final int priority = section.getInt("priority", -1);
        if (priority < 0) {
            throw new QuestException("Priority of journal main page needs to be at least 0!");
        }
        final String rawConditions = GlobalVariableResolver.resolve(pack, section.getString("conditions"));
        final List<ConditionID> conditions;
        if (rawConditions == null || rawConditions.isEmpty()) {
            conditions = List.of();
        } else {
            final String[] split = rawConditions.split(",");
            conditions = new ArrayList<>(split.length);
            for (final String conditionString : split) {
                if (!conditionString.isEmpty()) {
                    conditions.add(new ConditionID(pack, conditionString));
                }
            }
        }
        final Message text = messageCreator.parseFromSection(pack, section, "text");
        return new JournalMainPageEntry(priority, List.copyOf(conditions), text);
    }

    @Override
    protected JournalMainPageID getIdentifier(final QuestPackage pack, final String identifier) throws QuestException {
        return new JournalMainPageID(pack, identifier);
    }
}
