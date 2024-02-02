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

    protected final String songplaylistname, songname;
    protected final LocationPoint[] preview;
    protected final LocationZone spawnzone, startzone, gamezone, finishzone;
    private final byte[] leveldat;
    private final HashMap<String, byte[]> regiondata;
    protected float yaw, pitch;

    public GameOptions(String songplaylistname, String songname, byte[] leveldat, HashMap<String, byte[]> regiondata, LocationPoint[] preview, LocationZone spawnzone, float yaw, float pitch, LocationZone startzone, LocationZone gamezone, LocationZone finishzone) {
        this.songplaylistname = songplaylistname;
        this.songname = songname;
        this.regiondata = regiondata;
        this.leveldat = leveldat;
        this.preview = preview;
        this.spawnzone = spawnzone;
        this.yaw = yaw;
        this.pitch = pitch;
        this.startzone = startzone;
        this.gamezone = gamezone;
        this.finishzone = finishzone;
    }

    protected static GameOptions initArena(String arenaname) {
        GameOptions gameOption = Settings.gameoptions.get(arenaname);
        if (gameOption == null || !Settings.loadedarenas.add(arenaname)) {
            return null;
        }
        File worldDir = new File(arenaname);
        deleteFolder(worldDir);
        File regionDir = new File(worldDir, "region");
        regionDir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(new File(worldDir, "level.dat"), false);
            fos.write(gameOption.leveldat);
            fos.close();
        } catch (IOException e) {
            deleteFolder(worldDir);
            Settings.loadedarenas.remove(arenaname);
            return null;
        }
        for (String filename : gameOption.regiondata.keySet()) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(regionDir, filename), false);
                fos.write(gameOption.regiondata.get(filename));
                fos.close();
            } catch (IOException ignored) {

            }
        }
        WorldCreator worldcreator = new WorldCreator(arenaname);
        worldcreator.generator(Settings.voidgen);
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
        if (!Settings.loadedarenas.remove(arenaName)) {
            return false;
        }
        World world = Bukkit.getWorld(arenaName);
        if (world == null) {
            return false;
        }
        for (Player player : world.getPlayers()) {
            player.teleport(Settings.exitlocation);
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