package me.bomb.parkourbeat.game;

import me.bomb.amusic.AMusic;
import me.bomb.parkourbeat.cutscene.CameraManager;
import me.bomb.parkourbeat.ParkourBeat;
import me.bomb.parkourbeat.data.Settings;
import me.bomb.parkourbeat.location.LocationInside;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameStarter extends BukkitRunnable {

    private final static HashMap<Player, GameStarter> inGamePlayers = new HashMap<>();
    private final Location spawnLoc;
    private final World world;
    private final Player player;
    private final GameTicker gameTicker;
    private final GameCloser gameCloser;
    private boolean playListLoaded, cameraManagerStarted, ready;
    private CameraManager cameraManager;
    private final GameOptions gameOptions;

    public GameStarter(String arenaName, Player player) throws NullPointerException {
        if (arenaName == null) {
            throw new NullPointerException("arenaName cannot be null");
        }
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }
        gameOptions = GameOptions.initArena(arenaName);
        if (gameOptions == null) {
            throw new NullPointerException("gameoptions cannot be null");
        }
        inGamePlayers.put(player, this);
        world = Bukkit.getWorld(arenaName);
        this.player = player;
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(-40);
        LocationInside loc = gameOptions.spawnZone.randomInside(0.5D);
        spawnLoc = new Location(world, loc.x, loc.y, loc.z, gameOptions.yaw, gameOptions.pitch);
        player.teleport(spawnLoc);

        gameCloser = new GameCloser(world, this, inGamePlayers);
        gameTicker = new GameTicker(player, world, gameCloser, gameOptions.gameZone, gameOptions.finishZone);
        if (!gameOptions.songPlayListName.equals(AMusic.getPackName(player))) {
            AMusic.loadPack(player, gameOptions.songPlayListName, false);
        }
        runTaskTimerAsynchronously(ParkourBeat.plugin, 0L, 5L);
    }

    public static void leaveGame(Player player) {
        inGamePlayers.remove(player);
        player.teleport(Settings.exitLocation);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public static Set<String> getWorlds() {
        Set<String> worlds = new HashSet<>();
        for (Player player : inGamePlayers.keySet()) {
            worlds.add(player.getWorld().getName());
        }
        return worlds;
    }

    @Override
    public void run() {
        if (player.getWorld() == world && player.isOnline()) {
            if (!playListLoaded) {
                playListLoaded = gameOptions.songPlayListName.equals(AMusic.getPackName(player));
                return;
            }
            if (!cameraManagerStarted) {
                cameraManagerStarted = true;
                cameraManager = CameraManager.playCutscene(world, gameOptions.preview, player);
            }
            if (cameraManager != null && cameraManager.isAlive()) {
                return;
            }
            cameraManager = null;

            if (gameOptions.startZone == null) {
                player.sendMessage("Game autostart!");
                AMusic.setRepeatMode(player, null);
                AMusic.playSound(player, gameOptions.songName);
                player.teleport(spawnLoc);
                gameTicker.runTaskTimer(ParkourBeat.plugin, 50L, 1L);
            } else {
                if (!ready) {
                    ready = true;
                    player.sendMessage("Game ready!");
                }
                Location loc = player.getLocation();
                if (gameOptions.startZone.isOutside(loc.getX(), loc.getY(), loc.getZ())) {
                    return;
                }
                player.sendMessage("Game start!");
                AMusic.setRepeatMode(player, null);
                AMusic.playSound(player, gameOptions.songName);
                gameTicker.runTaskTimer(ParkourBeat.plugin, 0L, 1L);
            }
        } else {
            gameCloser.runTaskLater(ParkourBeat.plugin, 50L);
        }
        cancel();
    }
}
