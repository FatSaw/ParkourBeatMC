package me.bomb.parkourbeat;

import me.bomb.parkourbeat.data.Settings;
import me.bomb.parkourbeat.game.GameStarter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class EventListener implements Listener {

    private static final Location exitlocation = Settings.exitLocation.clone().add(0, 325, 0);

    @EventHandler
    public void onSpawnLocation(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(Settings.exitLocation);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            event.getChunk().unload(false);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.getWorld() == Settings.lobbyWorld) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        GameStarter.leaveGame(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.teleport(Settings.exitLocation);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(-40);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.PLAYER) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) entity;
        DamageCause cause = event.getCause();
        if (cause == DamageCause.VOID && player.getWorld() == Settings.lobbyWorld) {
            Vector velocity = player.getVelocity();
            player.teleport(exitlocation.clone().setDirection(player.getLocation().getDirection()));
            player.setVelocity(velocity);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

}
