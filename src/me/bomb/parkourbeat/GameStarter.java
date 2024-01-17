package me.bomb.parkourbeat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.bomb.amusic.AMusic;

final class GameStarter extends BukkitRunnable {
	
	private final static HashMap<Player,GameStarter> ingameplayers = new HashMap<Player,GameStarter>();
	private final Location spawnloc;
	private final World world;
	private final String songplaylistname;
	private final LocationZone spawnruner, gamezone, finish;
	private final Player player;
	private final LocationPoint[] preview;
	private final GameTicker gameticker;
	private final GameCloser gamecloser;
	private List<String> soundnames = null;
	private CameraManager cameramanager;
	
	protected GameStarter(String arenaname, Player player) {
		ingameplayers.put(player, this);
		GameOptions gameoptions = GameOptions.initArena(arenaname);
		world = Bukkit.getWorld(arenaname);
		this.player = player;
		this.spawnruner = gameoptions.spawnruner;
		this.gamezone = gameoptions.gamezone;
		this.preview = gameoptions.preview;
		this.finish = gameoptions.finishzone;
		this.songplaylistname = gameoptions.songplaylistname;
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setSaturation(5.0F);
		player.setExhaustion(0.0F);
		LocationInside loc = spawnruner.randomInside(0.5D);
		spawnloc = new Location(world, loc.x, loc.y, loc.z);
		player.teleport(spawnloc);
		
		gamecloser = new GameCloser(world, this, ingameplayers);
		gameticker = new GameTicker(player, world, gamecloser, gamezone, finish);
		if(!songplaylistname.equals(AMusic.getPackName(player))) {
			AMusic.loadPack(player, songplaylistname, false);
		}
		runTaskTimerAsynchronously(ParkourBeat.plugin, 0L, 20L);
	}

	protected static void leaveGame(Player player) {
		ingameplayers.remove(player);
		player.teleport(GameOptions.exitlocation);
		player.setGameMode(GameMode.ADVENTURE);
	}
	
	protected static Set<String> getWorlds() {
		Set<String> worlds = new HashSet<String>();
		for(Player player : ingameplayers.keySet()) {
			worlds.add(player.getWorld().getName());
		}
		return worlds;
	}
	@Override
	public void run() {
		if(soundnames==null) {
			if(songplaylistname.equals(AMusic.getPackName(player))) {
				soundnames = AMusic.getPlaylistSoundnames(player);
			}
			return;
		}
		if(cameramanager == null) {
			cameramanager = CameraManager.playCutscene(world, preview, player);
		}
		if(cameramanager.isAlive()) {
			return;
		}
		player.sendMessage("GameStart!");
		AMusic.playSound(player, soundnames.get(0));
		player.teleport(spawnloc);
		gameticker.runTaskTimer(ParkourBeat.plugin, 50, 1L);
		cancel();
	}
}
