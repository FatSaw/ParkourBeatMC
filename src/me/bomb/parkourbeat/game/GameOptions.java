package me.bomb.parkourbeat.game;

import me.bomb.parkourbeat.data.Settings;
import me.bomb.parkourbeat.location.LocationPoint;
import me.bomb.parkourbeat.location.LocationZone;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Stream;

public class GameOptions {

    protected final String songPlayListName, songName;
    protected final LocationPoint[] preview;
    protected final LocationZone spawnZone, startZone, gameZone, finishZone;
    private final byte[] levelDat;
    private final HashMap<String, byte[]> regionData;
    protected float yaw, pitch;

    public GameOptions(String songPlayListName, String songName, byte[] levelDat, HashMap<String, byte[]> regionData, LocationPoint[] preview, LocationZone spawnZone, float yaw, float pitch, LocationZone startZone, LocationZone gameZone, LocationZone finishZone) {
        this.songPlayListName = songPlayListName;
        this.songName = songName;
        this.regionData = regionData;
        this.levelDat = levelDat;
        this.preview = preview;
        this.spawnZone = spawnZone;
        this.yaw = yaw;
        this.pitch = pitch;
        this.startZone = startZone;
        this.gameZone = gameZone;
        this.finishZone = finishZone;
    }

    protected static GameOptions initArena(String arenaName) {
        GameOptions gameOption = Settings.gameOptions.get(arenaName);
        if (gameOption == null || !Settings.loadedArenas.add(arenaName)) {
            return null;
        }
        File worldDir = new File(arenaName);
        deleteFolder(worldDir);
        File regionDir = new File(worldDir, "region");
        regionDir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(new File(worldDir, "level.dat"), false);
            fos.write(gameOption.levelDat);
            fos.close();
        } catch (IOException e) {
            deleteFolder(worldDir);
            Settings.loadedArenas.remove(arenaName);
            return null;
        }
        for (String filename : gameOption.regionData.keySet()) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(regionDir, filename), false);
                fos.write(gameOption.regionData.get(filename));
                fos.close();
            } catch (IOException ignored) {

            }
        }
        WorldCreator worldcreator = new WorldCreator(arenaName);
        worldcreator.generator(Settings.voidGen);
        worldcreator.generateStructures(false);
        World world = worldcreator.createWorld();
        world.setKeepSpawnInMemory(false);
        world.setAutoSave(false);
        world.setPVP(false);
        world.setSpawnFlags(true, true);
        world.getWorldBorder().setSize(2048);
        return gameOption;
    }

    public static boolean destroyArena(String arenaName) {
        if (!Settings.loadedArenas.remove(arenaName)) {
            return false;
        }
        World world = Bukkit.getWorld(arenaName);
        if (world == null) {
            return false;
        }
        for (Player player : world.getPlayers()) {
            player.teleport(Settings.exitLocation);
        }
        File worldDirectory = world.getWorldFolder();
        return Bukkit.unloadWorld(world, false) && deleteFolder(worldDirectory);
    }

    private static boolean deleteFolder(File file) {
        try (Stream<Path> files = Files.walk(file.toPath())) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}