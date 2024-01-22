package me.bomb.parkourbeat;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

final class GameCloser extends BukkitRunnable {
	
	private final World world;
	private final GameStarter game;
	private final HashMap<Player,GameStarter> ingameplayers;
	protected GameCloser(World world, GameStarter game, HashMap<Player,GameStarter> ingameplayers) {
		this.world = world;
		this.game = game;
		this.ingameplayers = ingameplayers;
	}
	
	@Override
	public void run() {
		for(Player player : world.getPlayers()) {
			if(player==null) {
				continue;
			}
			player.sendMessage("Game end!");
			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setSaturation(5.0F);
			player.setExhaustion(0.0F);
			player.setFireTicks(-40);
			ingameplayers.remove(player, game);
			player.teleport(GameOptions.exitlocation);
		}
		GameOptions.destroyArena(world.getName());
	}

}
