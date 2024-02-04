package me.bomb.parkourbeat.game;

import me.bomb.parkourbeat.data.Settings;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class GameCloser extends BukkitRunnable {

    private final World world;
    private final GameStarter game;
    private final HashMap<Player, GameStarter> inGamePlayers;

    GameCloser(World world, GameStarter game, HashMap<Player, GameStarter> inGamePlayers) {
        this.world = world;
        this.game = game;
        this.inGamePlayers = inGamePlayers;
    }

    @Override
    public void run() {
        for (Player player : world.getPlayers()) {
            if (player == null) {
                continue;
            }
            player.sendMessage("Game end!");
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(5.0F);
            player.setExhaustion(0.0F);
            player.setFireTicks(-40);
            inGamePlayers.remove(player, game);
            player.teleport(Settings.exitLocation);
        }
        GameOptions.destroyArena(world.getName());
    }

}
