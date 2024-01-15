package me.bomb.parkourbeat;

import java.util.ArrayList;
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

public class Game extends BukkitRunnable {
	private final static Location exitlocation = Bukkit.getWorld("world").getSpawnLocation();
	private final static HashSet<Game> games = new HashSet<Game>();
	private final static HashMap<Player,Game> ingameplayers = new HashMap<Player,Game>();
	private final World world;
	private byte lives;
	private int checkpointnum = 0;
	private LocationZone checkpoint;
	private final LocationZone gamezone, finish;
	private final Player player;
	private final LocationPoint[] preview;
	private final List<LocationZones> checkpoints;
	private String soundname;
	private final String arenaname;
	
	protected Game(String arenaname, Player player) {
		this.arenaname = arenaname;
		GameOptions gameoptions = GameOptions.initArena(arenaname);
		World world = Bukkit.getWorld(arenaname);
		this.world = world;
		this.lives = gameoptions.lives;
		this.player = player;
		this.checkpoint = gameoptions.spawnruner;
		this.gamezone = gameoptions.gamezone;
		this.preview = gameoptions.preview;
		this.finish = gameoptions.finishzone;
		this.checkpoints = gameoptions.checkpointzones;
		String songplaylistname = gameoptions.songplaylistname;
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setSaturation(5.0F);
		player.setExhaustion(0.0F);
		LocationInside loc = checkpoint.randomInside(0.5D);
		player.teleport(new Location(world, loc.x, loc.y, loc.z));
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(player);
		CameraManager cameramanager = CameraManager.playCutscene(world, preview, players);
		boolean alongwait = false;
		if(!songplaylistname.equals(AMusic.getPackName(player))) {
			alongwait = true;
			AMusic.loadPack(player, songplaylistname, false);
		}
		final boolean longwait = alongwait;
		new BukkitRunnable() {
			List<String> soundnames;
			@Override
			public void run() {
				if((soundnames = AMusic.getPlaylistSoundnames(player)) != null && !cameramanager.isAlive()) {
					Game.this.runTaskLater(ParkourBeat.plugin, longwait ? 100L : 50L);
					Game.this.soundname = soundnames.get(0);
					cancel();
				}
			}
		}.runTaskTimerAsynchronously(ParkourBeat.plugin, 0L, 20L);
	}
	protected static void leaveGame(Player player) {
		if(player==null) {
			return;
		}
		ingameplayers.remove(player);
		player.teleport(exitlocation);
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
		ingameplayers.put(player, this);
		player.sendMessage("GameStarted!");
		AMusic.playSound(player, soundname);
		LocationInside loc = checkpoint.randomInside(0.5D);
		player.teleport(new Location(world, loc.x, loc.y, loc.z));
		BukkitRunnable gamecloser = new BukkitRunnable() {
			@Override
			public void run() {
				games.remove(Game.this);
				for(Player player : world.getPlayers()) {
					if(player==null) {
						continue;
					}
					player.sendMessage("Game ended!");
					player.setGameMode(GameMode.ADVENTURE);
					player.setHealth(player.getMaxHealth());
					player.setFoodLevel(20);
					player.setSaturation(5.0F);
					player.setExhaustion(0.0F);
					leaveGame(player);
				}
				GameOptions.destroyArena(arenaname);
			}
		};
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player.getWorld() != world || lives < 0) {
					gamecloser.runTaskLater(ParkourBeat.plugin, 150L);
					cancel();
					ingameplayers.remove(player);
					AMusic.stopSound(player);
					return;
				}
				Location loc = player.getLocation();
				double x = loc.getX(), y = loc.getY(), z = loc.getZ();
				if (!gamezone.isInside(x, y, z)) {
					if(--lives<0) {
						player.sendMessage("You loose!");
						AMusic.stopSound(player);
					} else {
						World world = player.getWorld();
						LocationInside loci = checkpoint.randomInside(0.5D);
						player.teleport(new Location(world, loci.x, loci.y, loci.z));
						player.sendMessage("Remaining lives: " + lives + "!");
					}
					return;
				}
				if(checkpoints!=null&&checkpointnum<checkpoints.size()) {
					LocationZones checkpoint = checkpoints.get(checkpointnum);
					if(checkpoint.register.isInside(x, y, z)) {
						++Game.this.checkpointnum;
						Game.this.checkpoint = checkpoint.respawn;

						player.sendMessage("Checkpoint!");
					}
				}
				if (finish.isInside(x, y, z)) {
					lives = -1;
					player.sendMessage("You win!");
					AMusic.stopSound(player);
				}
			}
		}.runTaskTimer(ParkourBeat.plugin, 0L, 1L);
		games.add(this);
	}
}
