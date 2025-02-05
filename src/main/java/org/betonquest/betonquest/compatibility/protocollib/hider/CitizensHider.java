package org.betonquest.betonquest.compatibility.protocollib.hider;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import org.betonquest.betonquest.api.bukkit.event.npc.NpcExternalVisibilityChangeEvent;
import org.betonquest.betonquest.api.profile.OnlineProfile;
import org.betonquest.betonquest.compatibility.citizens.CitizensAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Hides Citizens NPCs with ProtocolLib.
 */
public final class CitizensHider implements Listener {
    /**
     * The static instance.
     */
    @Nullable
    private static CitizensHider instance;

    /**
     * Hider hiding NPC entities/parts.
     */
    private final EntityHider hider;

    private CitizensHider(final Plugin plugin) {
        hider = new EntityHider(plugin, EntityHider.Policy.BLACKLIST);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Starts (or restarts) the NPCHider. It loads the current configuration for hidden NPCs
     *
     * @param plugin the plugin instance for registering event handler
     */
    public static void start(final Plugin plugin) {
        if (instance != null) {
            instance.stop();
        }
        instance = new CitizensHider(plugin);
    }

    /**
     * Gets the Citizens NPC Hider, if initialized.
     *
     * @return the currently used Hider instance
     */
    @Nullable
    public static CitizensHider getInstance() {
        return instance;
    }

    /**
     * Stops the NPCHider, cleaning up all listeners, runnables etc.
     */
    public void stop() {
        hider.close();
        HandlerList.unregisterAll(this);
    }

    /**
     * Shows the NPC for the player.
     *
     * @param onlineProfile the online profile of the player
     * @param npc           the npc to show
     */
    public void show(final OnlineProfile onlineProfile, final NPC npc) {
        getEntityList(npc).forEach(entity -> hider.showEntity(onlineProfile, entity));
    }

    /**
     * Hides the NPC for the player.
     *
     * @param onlineProfile the online profile of the player
     * @param npc           the npc to hide
     */
    public void hide(final OnlineProfile onlineProfile, final NPC npc) {
        getEntityList(npc).forEach(entity -> hider.hideEntity(onlineProfile, entity));
    }

    private List<Entity> getEntityList(final NPC npc) {
        final List<Entity> entityList = new ArrayList<>();
        entityList.add(npc.getEntity());

        final HologramTrait hologramTrait = npc.getTraitNullable(HologramTrait.class);
        if (hologramTrait != null) {
            final Entity nameEntity = hologramTrait.getNameEntity();
            if (nameEntity != null) {
                entityList.add(nameEntity);
            }
            entityList.addAll(hologramTrait.getHologramEntities());
        }

        return entityList;
    }

    /**
     * Checks whenever the NPC is not visible to the player.
     *
     * @param onlineProfile the profile of the player
     * @param npc           ID of the NPC
     * @return true if the NPC is invisible to that player, false otherwise
     */
    public boolean isInvisible(final OnlineProfile onlineProfile, final NPC npc) {
        return npc.getEntity() != null && !hider.isVisible(onlineProfile, npc.getEntity().getEntityId());
    }

    /**
     * Applies the visibility of a new spawned NPC.
     *
     * @param event the event to listen
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCSpawn(final NPCSpawnEvent event) {
        final NPC npc = event.getNPC();
        if (npc.getOwningRegistry().equals(CitizensAPI.getNPCRegistry())) {
            Bukkit.getPluginManager().callEvent(new NpcExternalVisibilityChangeEvent(new CitizensAdapter(npc)));
        }
    }
}
