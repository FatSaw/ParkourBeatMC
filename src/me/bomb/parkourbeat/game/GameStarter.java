package me.bomb.parkourbeat.game;

import me.bomb.amusic.AMusic;
import me.bomb.parkourbeat.cutscene.CameraManager;
import me.bomb.parkourbeat.ParkourBeat;
import me.bomb.parkourbeat.data.Settings;
import me.bomb.parkourbeat.location.LocationInside;
import me.bomb.parkourbeat.location.LocationPoint;
import me.bomb.parkourbeat.location.LocationZone;
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
    private final Location spawnloc;
    private final World world;
    private final String songPlayListName, songName;
    private final LocationZone spawnZone, startZone, gameZone, finish;
    private final Player player;
    private final LocationPoint[] preview;
    private final GameTicker gameTicker;
    private final GameCloser gameCloser;
    private boolean playListLoaded, cameraManagerStarted, ready;
    private CameraManager cameraManager;

    public GameStarter(String arenaname, Player player) throws NullPointerException {
        if (arenaname == null) {
            throw new NullPointerException("arenaname cannot be null");
        }
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }
        GameOptions gameoptions = GameOptions.initArena(arenaname);
        if (gameoptions == null) {
            throw new NullPointerException("gameoptions cannot be null");
        }
        inGamePlayers.put(player, this);
        world = Bukkit.getWorld(arenaname);
        this.player = player;
        this.spawnZone = gameoptions.spawnzone;
        this.startZone = gameoptions.startzone;
        this.gameZone = gameoptions.gamezone;
        this.preview = gameoptions.preview;
        this.finish = gameoptions.finishzone;
        this.songPlayListName = gameoptions.songplaylistname;
        this.songName = gameoptions.songname;
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(-40);
        LocationInside loc = spawnZone.randomInside(0.5D);
        spawnloc = new Location(world, loc.x, loc.y, loc.z, gameoptions.yaw, gameoptions.pitch);
        player.teleport(spawnloc);

        gameCloser = new GameCloser(world, this, inGamePlayers);
        gameTicker = new GameTicker(player, world, gameCloser, gameZone, finish);
        if (!songPlayListName.equals(AMusic.getPackName(player))) {
            AMusic.loadPack(player, songPlayListName, false);
        }
        runTaskTimerAsynchronously(ParkourBeat.plugin, 0L, 5L);
    }

    public static void leaveGame(Player player) {
        inGamePlayers.remove(player);
        player.teleport(Settings.exitlocation);
        player.setGameMode(GameMode.ADVENTURE);
    }

    public static Set<String> getWorlds() {
        Set<String> worlds = new HashSet<String>();
        for (Player player : inGamePlayers.keySet()) {
            worlds.add(player.getWorld().getName());
        }
        return worlds;
    }

    @Override
    public void run() {
        if (player.getWorld() == world && player.isOnline()) {
            if (!playListLoaded) {
                playListLoaded = songPlayListName.equals(AMusic.getPackName(player));
                return;
            }
            if (!cameraManagerStarted) {
                cameraManagerStarted = true;
                cameraManager = CameraManager.playCutscene(world, preview, player);
            }
            if (cameraManager != null && cameraManager.isAlive()) {
                return;
            }
            cameraManager = null;

            if (startZone == null) {
                player.sendMessage("Game autostart!");
                AMusic.setRepeatMode(player, null);
                AMusic.playSound(player, songName);
                player.teleport(spawnloc);
                gameTicker.runTaskTimer(ParkourBeat.plugin, 50L, 1L);
            } else {
                if (!ready) {
                    ready = true;
                    player.sendMessage("Game ready!");
                }
                Location loc = player.getLocation();
                if (startZone.isOutside(loc.getX(), loc.getY(), loc.getZ())) {
                    return;
                }
                player.sendMessage("Game start!");
                AMusic.setRepeatMode(player, null);
                AMusic.playSound(player, songName);
                gameTicker.runTaskTimer(ParkourBeat.plugin, 0L, 1L);
            }
        } else {
            gameCloser.runTaskLater(ParkourBeat.plugin, 50L);
        }
        cancel();
    }
}
