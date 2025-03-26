package org.betonquest.betonquest.feature;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.api.message.MessageParserRegistry;
import org.betonquest.betonquest.api.quest.QuestTypeAPI;
import org.betonquest.betonquest.conversation.InventoryConvIO;
import org.betonquest.betonquest.conversation.SimpleConvIO;
import org.betonquest.betonquest.conversation.SlowTellrawConvIO;
import org.betonquest.betonquest.conversation.TellrawConvIO;
import org.betonquest.betonquest.conversation.interceptor.NonInterceptingInterceptorFactory;
import org.betonquest.betonquest.conversation.interceptor.SimpleInterceptorFactory;
import org.betonquest.betonquest.kernel.registry.feature.ConversationIORegistry;
import org.betonquest.betonquest.kernel.registry.feature.FeatureRegistries;
import org.betonquest.betonquest.kernel.registry.feature.InterceptorRegistry;
import org.betonquest.betonquest.kernel.registry.feature.NotifyIORegistry;
import org.betonquest.betonquest.kernel.registry.feature.ScheduleRegistry;
import org.betonquest.betonquest.message.parser.LegacyParser;
import org.betonquest.betonquest.message.parser.MineDownMessageParser;
import org.betonquest.betonquest.message.parser.MiniMessageParser;
import org.betonquest.betonquest.notify.ActionBarNotifyIO;
import org.betonquest.betonquest.notify.AdvancementNotifyIO;
import org.betonquest.betonquest.notify.BossBarNotifyIO;
import org.betonquest.betonquest.notify.ChatNotifyIO;
import org.betonquest.betonquest.notify.SoundIO;
import org.betonquest.betonquest.notify.SubTitleNotifyIO;
import org.betonquest.betonquest.notify.SuppressNotifyIO;
import org.betonquest.betonquest.notify.TitleNotifyIO;
import org.betonquest.betonquest.notify.TotemNotifyIO;
import org.betonquest.betonquest.schedule.LastExecutionCache;
import org.betonquest.betonquest.schedule.impl.realtime.cron.RealtimeCronSchedule;
import org.betonquest.betonquest.schedule.impl.realtime.cron.RealtimeCronScheduler;
import org.betonquest.betonquest.schedule.impl.realtime.daily.RealtimeDailySchedule;
import org.betonquest.betonquest.schedule.impl.realtime.daily.RealtimeDailyScheduler;
import org.bukkit.ChatColor;

/**
 * Registers the stuff that is not built from Instructions.
 */
public class CoreFeatureFactories {
    /**
     * Factory to create new class specific loggers.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Cache to catch up missed schedulers.
     */
    private final LastExecutionCache lastExecutionCache;

    /**
     * Quest Type API.
     */
    private final QuestTypeAPI questTypeAPI;

    /**
     * Create a new Core Other Factories class for registering.
     *
     * @param loggerFactory      the factory to create new class specific loggers
     * @param lastExecutionCache the cache to catch up missed schedulers
     * @param questTypeAPI       the class for executing events
     */
    public CoreFeatureFactories(final BetonQuestLoggerFactory loggerFactory, final LastExecutionCache lastExecutionCache,
                                final QuestTypeAPI questTypeAPI) {
        this.loggerFactory = loggerFactory;
        this.lastExecutionCache = lastExecutionCache;
        this.questTypeAPI = questTypeAPI;
    }

    /**
     * Registers the Factories.
     *
     * @param registries containing the registry to register in
     */
    public void register(final FeatureRegistries registries) {
        final ConversationIORegistry conversationIOTypes = registries.conversationIO();
        conversationIOTypes.register("simple", SimpleConvIO.class);
        conversationIOTypes.register("tellraw", TellrawConvIO.class);
        conversationIOTypes.register("chest", InventoryConvIO.class);
        conversationIOTypes.register("combined", InventoryConvIO.Combined.class);
        conversationIOTypes.register("slowtellraw", SlowTellrawConvIO.class);

        final InterceptorRegistry interceptorTypes = registries.interceptor();
        interceptorTypes.register("simple", new SimpleInterceptorFactory());
        interceptorTypes.register("none", new NonInterceptingInterceptorFactory());

        final NotifyIORegistry notifyIOTypes = registries.notifyIO();
        notifyIOTypes.register("suppress", SuppressNotifyIO.class);
        notifyIOTypes.register("chat", ChatNotifyIO.class);
        notifyIOTypes.register("advancement", AdvancementNotifyIO.class);
        notifyIOTypes.register("actionbar", ActionBarNotifyIO.class);
        notifyIOTypes.register("bossbar", BossBarNotifyIO.class);
        notifyIOTypes.register("title", TitleNotifyIO.class);
        notifyIOTypes.register("totem", TotemNotifyIO.class);
        notifyIOTypes.register("subtitle", SubTitleNotifyIO.class);
        notifyIOTypes.register("sound", SoundIO.class);

        final ScheduleRegistry eventSchedulingTypes = registries.eventScheduling();
        eventSchedulingTypes.register("realtime-daily", RealtimeDailySchedule.class, new RealtimeDailyScheduler(
                loggerFactory.create(RealtimeDailyScheduler.class, "Schedules"), questTypeAPI, lastExecutionCache));
        eventSchedulingTypes.register("realtime-cron", RealtimeCronSchedule.class, new RealtimeCronScheduler(
                loggerFactory.create(RealtimeCronScheduler.class, "Schedules"), questTypeAPI, lastExecutionCache));

        final MessageParserRegistry messageParserRegistry = registries.messageParser();
        registerMessageParsers(messageParserRegistry);
    }

    private void registerMessageParsers(final MessageParserRegistry messageParserRegistry) {
        final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .extractUrls()
                .build();
        messageParserRegistry.register("legacy", new LegacyParser(legacySerializer));
        final MiniMessage miniMessage = MiniMessage.miniMessage();
        messageParserRegistry.register("minimessage", new MiniMessageParser(miniMessage));
        final MiniMessage legacyMiniMessage = MiniMessage.builder()
                .preProcessor(input -> {
                    final TextComponent deserialize = legacySerializer.deserialize(ChatColor.translateAlternateColorCodes('&', input.replaceAll("(?<!\\\\)\\\\n", "\n")));
                    final String serialize = miniMessage.serialize(deserialize);
                    return serialize.replaceAll("\\\\<", "<");
                })
                .build();
        messageParserRegistry.register("legacyminimessage", new MiniMessageParser(legacyMiniMessage));
        messageParserRegistry.register("minedown", new MineDownMessageParser());
    }
}
