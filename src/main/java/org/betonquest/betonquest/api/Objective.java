package org.betonquest.betonquest.api;

import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.GlobalObjectives;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.database.Connector;
import org.betonquest.betonquest.database.Saver;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.id.ConditionID;
import org.betonquest.betonquest.id.EventID;
import org.betonquest.betonquest.id.ObjectiveID;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * Superclass for all objectives. You need to extend it in order to create new
 * custom objectives.
 * </p>
 * <p>
 * Registering your objectives is done through
 * {@link org.betonquest.betonquest.BetonQuest#registerObjectives(String, Class)
 * registerObjectives()} method.
 * </p>
 */
@SuppressWarnings({"PMD.CommentRequired", "PMD.AvoidLiteralsInIfCondition", "PMD.TooManyMethods", "PMD.GodClass"})
@CustomLog
public abstract class Objective {

    protected final int notifyInterval;
    protected final boolean notify;
    protected Instruction instruction;
    protected ConditionID[] conditions;
    protected EventID[] events;
    protected boolean persistent;
    protected boolean global;
    protected QREHandler qreHandler = new QREHandler();

    /**
     * Contains all data objects of the players with this objective active.
     */
    protected Map<String, ObjectiveData> dataMap = new HashMap<>();
    /**
     * Should be set with the data class used to hold players' information.
     */
    protected Class<? extends ObjectiveData> template = ObjectiveData.class;

    /**
     * <p>
     * Creates new instance of the objective. The objective should parse
     * instruction string at this point and extract all the data from it.
     * </p>
     * <b>Do not register listeners here!</b> There is a {@link #start()} method
     * for it.
     *
     * @param instruction Instruction object representing the objective; you need to
     *                    extract all required information from it
     * @throws InstructionParseException if the syntax is wrong or any error happens while parsing
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity"})
    public Objective(final Instruction instruction) throws InstructionParseException {
        this.instruction = instruction;
        // extract events and conditions
        final String[] tempEvents1 = instruction.getArray(instruction.getOptional("event"));
        final String[] tempEvents2 = instruction.getArray(instruction.getOptional("events"));
        persistent = instruction.hasArgument("persistent");
        global = instruction.hasArgument("global");
        if (global) {
            GlobalObjectives.add((ObjectiveID) instruction.getID());
        }
        // make them final
        int length = tempEvents1.length + tempEvents2.length;
        events = new EventID[length];
        for (int i = 0; i < length; i++) {
            final String event = i >= tempEvents1.length ? tempEvents2[i - tempEvents1.length] : tempEvents1[i];
            try {
                events[i] = new EventID(instruction.getPackage(), event);
            } catch (final ObjectNotFoundException e) {
                if (length == 1 && "ID is null".equals(e.getMessage())) {
                    throw new InstructionParseException("Error while parsing objective events: No events are defined!", e);
                }
                throw new InstructionParseException("Error while parsing objective events: " + e.getMessage(), e);
            }
        }
        final String[] tempConditions1 = instruction.getArray(instruction.getOptional("condition"));
        final String[] tempConditions2 = instruction.getArray(instruction.getOptional("conditions"));
        length = tempConditions1.length + tempConditions2.length;
        conditions = new ConditionID[length];
        for (int i = 0; i < length; i++) {
            final String condition = i >= tempConditions1.length ? tempConditions2[i - tempConditions1.length]
                    : tempConditions1[i];
            try {
                conditions[i] = new ConditionID(instruction.getPackage(), condition);
            } catch (final ObjectNotFoundException e) {
                throw new InstructionParseException("Error while parsing objective conditions: " + e.getMessage(), e);
            }
        }
        final int customNotifyInterval = instruction.getInt(instruction.getOptional("notify"), 0);
        notify = customNotifyInterval > 0 || instruction.hasArgument("notify");
        notifyInterval = Math.max(1, customNotifyInterval);
    }

    /**
     * This method is called by the plugin when the objective needs to start
     * listening for events. Register your Listeners here!
     */
    public abstract void start();

    /**
     * This method is called by the plugin when the objective starts for a specific player.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void start(final Profile profile) {
        //Empty
    }

    /**
     * This method is called by the plugin when the objective needs to be
     * stopped. You have to unregister all Listeners here.
     */
    public abstract void stop();

    /**
     * This method is called by the plugin when the objective stop for a specific player.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void stop(final Profile profile) {
        //Empty
    }

    /**
     * This method should return the default data instruction for the objective,
     * ready to be parsed by the ObjectiveData class.
     *
     * @return the default data instruction string
     */
    public abstract String getDefaultDataInstruction();

    /**
     * This method should return the default data instruction for the objective,
     * ready to be parsed by the ObjectiveData class.
     * Reimplement this method if you need player context (e.g. for variable parsing) when creating the data instruction.
     *
     * @param profile the {@link Profile} of the player
     * @return the default data instruction string
     */
    public String getDefaultDataInstruction(final Profile profile) {
        return getDefaultDataInstruction();
    }

    /**
     * This method should return various properties of the objective, formatted
     * as readable Strings. An example would be "5h 5min" for "time_left"
     * keyword in "delay" objective or "12" for keyword "mobs_killed" in
     * "mobkill" objective. The method is not abstract since not all objectives
     * need to have properties, i.e. "die" objective. By default it returns an
     * empty string.
     *
     * @param name    the name of the property you need to return; you can parse it
     *                to extract additional information
     * @param profile the {@link Profile} of the player for whom the property is to be returned
     * @return the property with given name
     */
    abstract public String getProperty(String name, Profile profile);

    /**
     * This method fires events for the objective and removes it from player's
     * list of active objectives. Use it when you detect that the objective has
     * been completed. It deletes the objective using delete() method.
     *
     * @param profile the {@link Profile} of the player for whom the objective is to be completed
     */
    public final void completeObjective(final Profile profile) {
        // remove the objective from player's list
        completeObjectiveForPlayer(profile);
        BetonQuest.getInstance().getPlayerData(profile).removeRawObjective((ObjectiveID) instruction.getID());
        if (persistent) {
            BetonQuest.getInstance().getPlayerData(profile).addNewRawObjective((ObjectiveID) instruction.getID());
            createObjectiveForPlayer(profile, getDefaultDataInstruction(profile));
        }
        LOG.debug(instruction.getPackage(),
                "Objective \"" + instruction.getID().getFullID() + "\" has been completed for player "
                        + profile.getPlayerName()
                        + ", firing events.");
        // fire all events
        for (final EventID event : events) {
            BetonQuest.event(profile, event);
        }
        LOG.debug(instruction.getPackage(),
                "Firing events in objective \"" + instruction.getID().getFullID() + "\" for player "
                        + profile.getPlayerName()
                        + " finished");
    }

    /**
     * Checks if all conditions has been met. Use it when the player has done
     * something that modifies data (e.g. killing zombies). If conditions are
     * met, you can safely modify the data.
     *
     * @param profile the {@link Profile} of the player for whom the conditions are to be checked
     * @return if all conditions of this objective has been met
     */
    public final boolean checkConditions(final Profile profile) {
        LOG.debug(instruction.getPackage(), "Condition check in \"" + instruction.getID().getFullID()
                + "\" objective for player " + profile.getPlayerName());
        return BetonQuest.conditions(profile, conditions);
    }

    /**
     * Send notification for progress with the objective.
     *
     * @param messageName message name to use in messages.yml
     * @param profile     the {@link Profile} of the player
     * @param variables   variables for putting into the message
     */
    protected void sendNotify(final Profile profile, final String messageName, final Object... variables) {
        try {
            final String[] stringVariables = Arrays.stream(variables)
                    .map(String::valueOf)
                    .toArray(String[]::new);
            Config.sendNotify(instruction.getPackage().getPackagePath(), profile, messageName, stringVariables, messageName + ",info");
        } catch (final QuestRuntimeException exception) {
            try {
                LOG.warn(instruction.getPackage(), "The notify system was unable to play a sound for the '" + messageName + "' category in '" + instruction.getObjective().getFullID() + "'. Error was: '" + exception.getMessage() + "'");
            } catch (final InstructionParseException e) {
                LOG.reportException(instruction.getPackage(), e);
            }
        }
    }

    /**
     * Adds this new objective to the player. Also updates the database with the
     * objective.
     *
     * @param profile the {@link Profile} of the player
     */
    public final void newPlayer(final Profile profile) {
        final String defaultInstruction = getDefaultDataInstruction(profile);
        createObjectiveForPlayer(profile, defaultInstruction);
        BetonQuest.getInstance().getPlayerData(profile).addObjToDB(instruction.getID().getFullID(), defaultInstruction);
    }

    /**
     * Start a new objective for the player.
     *
     * @param profile           the {@link Profile} of the player
     * @param instructionString the objective data instruction
     * @see #resumeObjectiveForPlayer(Profile, String)
     */
    public final void createObjectiveForPlayer(final Profile profile, final String instructionString) {
        startObjective(profile, instructionString, ObjectiveState.NEW);
    }

    /**
     * Resume a paused objective for the player.
     *
     * @param profile           the {@link Profile} of the player that has the objective
     * @param instructionString the objective data instruction
     * @see #createObjectiveForPlayer(Profile, String)
     */
    public final void resumeObjectiveForPlayer(final Profile profile, final String instructionString) {
        startObjective(profile, instructionString, ObjectiveState.PAUSED);
    }

    /**
     * Start a objective for the player. This lower level method allows to set the previous state directly. If possible
     * prefer {@link #createObjectiveForPlayer(Profile, String)} and {@link #resumeObjectiveForPlayer(Profile, String)}.
     *
     * @param profile           the {@link Profile} of the player that has the objective
     * @param instructionString the objective data instruction
     * @param previousState     the objective's previous state
     */
    public final void startObjective(final Profile profile, final String instructionString, final ObjectiveState previousState) {
        synchronized (this) {
            createObjectiveData(profile, instructionString)
                    .ifPresent(data -> startObjectiveWithEvent(profile, data, previousState));
        }
    }

    private Optional<ObjectiveData> createObjectiveData(final Profile profile, final String instructionString) {
        try {
            return Optional.of(constructObjectiveDataUnsafe(profile, instructionString));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException exception) {
            handleObjectiveDataConstructionError(profile, exception);
            return Optional.empty();
        }
    }

    private void handleObjectiveDataConstructionError(final Profile profile, final ReflectiveOperationException exception) {
        if (exception.getCause() instanceof InstructionParseException) {
            LOG.warn(instruction.getPackage(), "Error while loading " + this.instruction.getID().getFullID() + " objective data for player "
                    + profile.getPlayerName() + ": " + exception.getCause().getMessage(), exception);
        } else {
            LOG.reportException(instruction.getPackage(), exception);
        }
    }

    private ObjectiveData constructObjectiveDataUnsafe(final Profile profile, final String instructionString)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final String fullId = this.instruction.getID().getFullID();
        return template.getConstructor(String.class, Profile.class, String.class)
                .newInstance(instructionString, profile, fullId);
    }

    private void startObjectiveWithEvent(final Profile profile, final ObjectiveData data, final ObjectiveState previousState) {
        runObjectiveChangeEvent(profile, previousState, ObjectiveState.ACTIVE);
        activateObjective(profile, data);
    }

    /**
     * Complete an active objective for the player. It will only remove it from the player and not run any completion
     * events, run {@link #completeObjective(Profile)} instead! It does also not remove it from the database.
     *
     * @param profile the {@link Profile} of the player that has the objective
     * @see #cancelObjectiveForPlayer(Profile)
     * @see #pauseObjectiveForPlayer(Profile)
     */
    public final void completeObjectiveForPlayer(final Profile profile) {
        stopObjective(profile, ObjectiveState.COMPLETED);
    }

    /**
     * Cancel an active objective for the player. It will only remove it from the player and not remove it from the
     * database.
     *
     * @param profile the {@link Profile} of the player that has the objective
     * @see #completeObjectiveForPlayer(Profile)
     * @see #pauseObjectiveForPlayer(Profile)
     */
    public final void cancelObjectiveForPlayer(final Profile profile) {
        stopObjective(profile, ObjectiveState.CANCELED);
    }

    /**
     * Pause an active objective for the player.
     *
     * @param profile the {@link Profile} of the player that has the objective
     * @see #completeObjectiveForPlayer(Profile)
     * @see #cancelObjectiveForPlayer(Profile)
     */
    public final void pauseObjectiveForPlayer(final Profile profile) {
        stopObjective(profile, ObjectiveState.PAUSED);
    }

    /**
     * Stops a objective for the player. This lower level method allows to set the previous state directly. If possible
     * prefer {@link #completeObjectiveForPlayer(Profile)}, {@link #cancelObjectiveForPlayer(Profile)} and
     * {@link #pauseObjectiveForPlayer(Profile)}.
     *
     * @param profile  the {@link Profile} of the player that has the objective
     * @param newState the objective's new state
     */
    public final void stopObjective(final Profile profile, final ObjectiveState newState) {
        synchronized (this) {
            stopObjectiveWithEvent(profile, newState);
        }
    }

    private void stopObjectiveWithEvent(final Profile profile, final ObjectiveState newState) {
        runObjectiveChangeEvent(profile, ObjectiveState.ACTIVE, newState);
        deactivateObjective(profile);
    }

    private void runObjectiveChangeEvent(final Profile profile, final ObjectiveState previousState, final ObjectiveState newState) {
        BetonQuest.getInstance()
                .callSyncBukkitEvent(new PlayerObjectiveChangeEvent(profile.getPlayer(), this, newState, previousState));
    }

    private void activateObjective(final Profile profile, final ObjectiveData data) {
        if (dataMap.isEmpty()) {
            start();
        }
        dataMap.put(profile.getPlayerId(), data);
        start(profile);
    }

    private void deactivateObjective(final Profile profile) {
        stop(profile);
        dataMap.remove(profile);
        if (dataMap.isEmpty()) {
            stop();
        }
    }

    /**
     * Checks if the player has this objective.
     *
     * @param profile the {@link Profile} of the player
     * @return true if the player has this objective
     */
    public final boolean containsPlayer(final Profile profile) {
        return dataMap.containsKey(profile.getPlayerId());
    }

    /**
     * Returns the data of the specified player.
     *
     * @param profile the {@link Profile} of the player
     * @return the data string for this objective
     */
    public final String getData(final Profile profile) {
        final ObjectiveData data = dataMap.get(profile.getPlayerId());
        if (data == null) {
            return null;
        }
        return data.toString();
    }

    /**
     * Returns the label of this objective. Don't worry about it, it's only used
     * by the rest of BetonQuest's logic.
     *
     * @return the label of the objective
     */
    public final String getLabel() {
        return instruction.getID().getFullID();
    }

    /**
     * Sets the label of this objective. Don't worry about it, it's only used by
     * the rest of BetonQuest's logic.
     *
     * @param rename new ID of the objective
     */
    public void setLabel(final ObjectiveID rename) {
        instruction = new Instruction(instruction.getPackage(), rename, instruction.toString());
    }

    /**
     * Should be called at the end of the use of this objective, for example
     * when reloading the plugin. It will unregister listeners and save all
     * player's data to their "inactive" map.
     */
    public void close() {
        stop();
        for (final Map.Entry<String, ObjectiveData> entry : dataMap.entrySet()) {
            final Profile profile = PlayerConverter.getID(Bukkit.getOfflinePlayer(entry.getKey()));
            stop(profile);
            BetonQuest.getInstance().getPlayerData(profile).addRawObjective(instruction.getID().getFullID(),
                    entry.getValue().toString());
        }
    }

    /**
     * Returns whether the objective is global.
     *
     * @return true if the objective is global, false otherwise
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Show the states of the player objectives.
     */
    public enum ObjectiveState {

        /**
         * The objective is new and does not exist before.
         */
        NEW,

        /**
         * The objective is active.
         */
        ACTIVE,

        /**
         * The objective is complete.
         */
        COMPLETED,

        /**
         * The objective is paused.
         */
        PAUSED,

        /**
         * The objective is canceled.
         */
        CANCELED,
    }

    /**
     * A task that may throw a {@link QuestRuntimeException}.
     */
    protected interface QREThrowing {

        void run() throws QuestRuntimeException;
    }

    /**
     * Stores player's data for the objective.
     */
    protected static class ObjectiveData {

        protected String instruction;
        protected Profile profile;
        protected String objID;

        /**
         * The ObjectiveData object is loaded from the database and the
         * constructor needs to parse the data in the instruction, so it can be
         * later retrieved and modified by your objective code.
         *
         * @param instruction the instruction of the data object; parse it to get all
         *                    required information
         * @param profile     the {@link Profile} of the player
         * @param objID       ID of the objective, used by BetonQuest to store this
         *                    ObjectiveData in the database
         */
        public ObjectiveData(final String instruction, final Profile profile, final String objID) {
            this.instruction = instruction;
            this.profile = profile;
            this.objID = objID;
        }

        /**
         * This method should return the whole instruction string, which can be
         * successfully parsed by the constructor. This method is used by
         * BetonQuest to save the ObjectiveData to the database. That's why the
         * output syntax here must be compatible with input syntax in the
         * constructor.
         *
         * @return the instruction string
         */
        @Override
        public String toString() {
            return instruction;
        }

        /**
         * <p>
         * Should be called when the data inside ObjectiveData changes. It will
         * update the database with the changes.
         * </p>
         *
         * <p>
         * If you forget it, the objective will still work for players who don't
         * leave the server. However, if someone leaves before completing, they
         * will have to start this objective from scratch.
         * </p>
         */
        @SuppressWarnings("PMD.DoNotUseThreads")
        protected void update() {
            final Saver saver = BetonQuest.getInstance().getSaver();
            saver.add(new Saver.Record(Connector.UpdateType.REMOVE_OBJECTIVES, profile.getPlayerId(), objID));
            saver.add(new Saver.Record(Connector.UpdateType.ADD_OBJECTIVES, profile.getPlayerId(), objID, toString()));
            final QuestDataUpdateEvent event = new QuestDataUpdateEvent(profile, objID, toString());
            final Server server = BetonQuest.getInstance().getServer();
            server.getScheduler().runTask(BetonQuest.getInstance(), () -> server.getPluginManager().callEvent(event));
            // update the journal so all possible variables display correct
            // information
            BetonQuest.getInstance().getPlayerData(profile).getJournal().update();
        }

    }

    /**
     * Can handle thrown{@link QuestRuntimeException} and rate limits them so
     * they don't spam console that hard.
     */
    protected class QREHandler {

        /**
         * Interval in which errors are logged.
         */
        public static final int ERROR_RATE_LIMIT_MILLIS = 5000;

        public long last;

        public QREHandler() {
        }

        /**
         * Runs a task and logs occurring quest runtime exceptions with a rate
         * limit.
         *
         * @param qreThrowing a task that may throw a quest runtime exception
         */
        public void handle(final QREThrowing qreThrowing) {
            try {
                qreThrowing.run();
            } catch (final QuestRuntimeException e) {
                if (System.currentTimeMillis() - last < ERROR_RATE_LIMIT_MILLIS) {
                    return;
                }
                last = System.currentTimeMillis();
                LOG.warn(instruction.getPackage(), "Error while handling '" + instruction.getID() + "' objective: " + e.getMessage(), e);
            }
        }
    }
}
