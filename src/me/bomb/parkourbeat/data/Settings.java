package me.bomb.parkourbeat.data;

import me.bomb.parkourbeat.ParkourBeat;
import me.bomb.parkourbeat.TheMath;
import me.bomb.parkourbeat.game.GameOptions;
import me.bomb.parkourbeat.location.LocationPoint;
import me.bomb.parkourbeat.location.LocationZone;
import me.bomb.parkourbeat.location.RouteLocationPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

public class Settings {

    public final static World lobbyWorld;
    public final static Location exitLocation;
    public final static boolean debug;
    public final static Set<String> loadedArenas = new HashSet<String>();
    private final static FilenameFilter mcAFilter = (dir, name) -> name.endsWith(".mca");
    public final static HashMap<String, GameOptions> gameOptions = new HashMap<>();
    public final static ChunkGenerator voidGen = new ChunkGenerator() {
        @Override
        public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
            return new byte[16][];
        }
    };

    static {
        JavaPlugin plugin = JavaPlugin.getPlugin(ParkourBeat.class);
        File workingDirectory = plugin.getDataFolder();
        createFolder(workingDirectory);
        File worldsDirectory = new File(workingDirectory, "worlds");
        createFolder(worldsDirectory);
        YamlConfiguration config = loadConfig(workingDirectory, plugin);
        lobbyWorld = Bukkit.getWorld(config.getString("lobbyworld", "world"));
        exitLocation = lobbyWorld.getSpawnLocation();
        debug = config.getBoolean("debug", false);
        ConfigurationSection gamefieldscs = config.getConfigurationSection("gamefields");
        if (gamefieldscs != null) {
            for (String gamefieldid : gamefieldscs.getKeys(false)) {
                gamefieldid = gamefieldid.toLowerCase();
                ConfigurationSection gamefieldcs = gamefieldscs.getConfigurationSection(gamefieldid);
                if (gamefieldcs == null || !gamefieldcs.isInt("worldcount")) {
                    continue;
                }
                int worldcount = gamefieldcs.getInt("worldcount", 0);
                if (worldcount < 1 || worldcount > 127) {
                    continue;
                }
                String songplaylistname = gamefieldcs.getString("songplaylistname", null);
                String songname = gamefieldcs.getString("songname", null);
                ConfigurationSection runerspawn = gamefieldcs.getConfigurationSection("spawn");
                ConfigurationSection startcs = gamefieldcs.getConfigurationSection("start");
                ConfigurationSection finishcs = gamefieldcs.getConfigurationSection("finish");
                ConfigurationSection gamezonecs = gamefieldcs.getConfigurationSection("gamezone");
                if (runerspawn == null || finishcs == null || gamezonecs == null || !finishcs.isInt("minx") || !finishcs.isInt("miny") || !finishcs.isInt("minz") || !finishcs.isInt("maxx") || !finishcs.isInt("maxy") || !finishcs.isInt("maxz") || !gamezonecs.isInt("minx") || !gamezonecs.isInt("miny") || !gamezonecs.isInt("minz") || !gamezonecs.isInt("maxx") || !gamezonecs.isInt("maxy") || !gamezonecs.isInt("maxz") || !runerspawn.isInt("minx") || !runerspawn.isInt("miny") || !runerspawn.isInt("minz") || !runerspawn.isInt("maxx") || !runerspawn.isInt("maxy") || !runerspawn.isInt("maxz")) {
                    continue;
                }
				/*ConfigurationSection checkpointscs = gamefieldcs.getConfigurationSection("checkpoints");
				List<LocationZones> checkpointzones = null;
				if(checkpointscs!=null) {
					checkpointzones = new ArrayList<LocationZones>();
					for(String checkpointname : checkpointscs.getKeys(false)) {
						ConfigurationSection checkpointzone, registerzone, teleportzone;
						if((checkpointzone=checkpointscs.getConfigurationSection(checkpointname))==null||(registerzone = checkpointzone.getConfigurationSection("register"))==null||(teleportzone = checkpointzone.getConfigurationSection("teleport"))==null||!registerzone.isInt("minx")||!registerzone.isInt("miny")||!registerzone.isInt("minz")||!registerzone.isInt("maxx")||!registerzone.isInt("maxy")||!registerzone.isInt("maxz")||!teleportzone.isInt("minx")||!teleportzone.isInt("miny")||!teleportzone.isInt("minz")||!teleportzone.isInt("maxx")||!teleportzone.isInt("maxy")||!teleportzone.isInt("maxz")) {
							continue;
						}
						checkpointzones.add(new LocationZones(new LocationZone(registerzone.getInt("minx"), registerzone.getInt("miny"), registerzone.getInt("minz"), registerzone.getInt("maxx"), registerzone.getInt("maxy"), registerzone.getInt("maxz")), new LocationZone(teleportzone.getInt("minx"), teleportzone.getInt("miny"), teleportzone.getInt("minz"), teleportzone.getInt("maxx"), teleportzone.getInt("maxy"), teleportzone.getInt("maxz"))));
					}
					if(checkpointzones.isEmpty()) {
						checkpointzones = null;
					}
				}*/
                float spawnyaw = (float) runerspawn.getDouble("yaw", 0.0), spawnpitch = (float) runerspawn.getDouble("pitch", 0.0);
                LocationZone spawnruner = new LocationZone(runerspawn.getInt("minx"), runerspawn.getInt("miny"), runerspawn.getInt("minz"), runerspawn.getInt("maxx"), runerspawn.getInt("maxy"), runerspawn.getInt("maxz"));

                LocationZone startzone = startcs == null ? null : new LocationZone(startcs.getInt("minx"), startcs.getInt("miny"), startcs.getInt("minz"), startcs.getInt("maxx"), startcs.getInt("maxy"), startcs.getInt("maxz"));
                LocationZone gamezone = new LocationZone(gamezonecs.getInt("minx"), gamezonecs.getInt("miny"), gamezonecs.getInt("minz"), gamezonecs.getInt("maxx"), gamezonecs.getInt("maxy"), gamezonecs.getInt("maxz"));
                LocationZone finishzone = new LocationZone(finishcs.getInt("minx"), finishcs.getInt("miny"), finishcs.getInt("minz"), finishcs.getInt("maxx"), finishcs.getInt("maxy"), finishcs.getInt("maxz"));
                LocationPoint[] preview = null;
                ArrayList<LocationPoint> previewlist = new ArrayList<>();
                if (gamefieldcs.contains("preview")) {
                    loadPreviews(gamefieldcs, previewlist);
                }
                if (!previewlist.isEmpty()) {
                    preview = previewlist.toArray(new LocationPoint[0]);
                }
                File gamefielddir = new File(worldsDirectory, gamefieldid);
                File leveldatfile = new File(gamefielddir, "level.dat");
                byte[] readbuf = new byte[32384];
                byte[] leveldat = null;
                try {
                    FileInputStream fis = new FileInputStream(leveldatfile);
                    readbuf = Arrays.copyOf(readbuf, fis.read(readbuf));
                    fis.close();
                    leveldat = readbuf;
                } catch (IOException ignored) {
                }
                File[] regionFiles = gamefielddir.listFiles(mcAFilter);
                HashMap<String, byte[]> regionData = new HashMap<>();
                for (File regionfile : regionFiles) {
                    readbuf = new byte[8388608];
                    try {
                        FileInputStream fis = new FileInputStream(regionfile);
                        readbuf = Arrays.copyOf(readbuf, fis.read(readbuf));
                        fis.close();
                        regionData.put(regionfile.getName(), readbuf);
                    } catch (IOException ignored) {
                    }
                }
                GameOptions gameoption = new GameOptions(songplaylistname, songname, leveldat, regionData, preview, spawnruner, spawnyaw, spawnpitch, startzone, gamezone, finishzone);
                gamefieldid = gamefieldid.concat("_");
                for (byte i = (byte) worldcount; --i > -1; ) {
                    gameOptions.put(gamefieldid.concat(Byte.toString(i)), gameoption);
                }
            }
        }
    }

    private static void loadPreviews(ConfigurationSection gamefieldcs, ArrayList<LocationPoint> previewList) {
        List<String> locations = gamefieldcs.getStringList("preview");
        LocationPoint previousLocation = null;
        for (String location : locations) {
            if (location == null || location.isEmpty()) {
                continue;
            }
            int previousSeparatorIndex = location.indexOf("$");
            int repeatCount = 1;
            if (previousSeparatorIndex > -1) {
                try {
                    repeatCount = Integer.parseInt(location.substring(0, previousSeparatorIndex));
                } catch (NumberFormatException ignored) {
                }
            }
            previousSeparatorIndex = location.indexOf("#");
            if (previousSeparatorIndex < 0) {
                continue;
            }
            boolean relativeLocation = previousSeparatorIndex > 0 && location.charAt(previousSeparatorIndex - 1) == '~';
            int separatorindex = location.indexOf("#", ++previousSeparatorIndex);
            if (separatorindex < 0) {
                continue;
            }

            boolean relativeX = location.charAt(previousSeparatorIndex) == '~';
            if (relativeX) {
                ++previousSeparatorIndex;
            }
            double sx, sy, sz;
            try {
                sx = Double.parseDouble(location.substring(previousSeparatorIndex, separatorindex));
            } catch (NumberFormatException e) {
                continue;
            }
            previousSeparatorIndex = ++separatorindex;
            separatorindex = location.indexOf("#", separatorindex);
            if (separatorindex < 0) {
                continue;
            }
            boolean relativeY = location.charAt(previousSeparatorIndex) == '~';
            if (relativeY) {
                ++previousSeparatorIndex;
            }
            try {
                sy = Double.parseDouble(location.substring(previousSeparatorIndex, separatorindex));
            } catch (NumberFormatException e) {
                continue;
            }
            previousSeparatorIndex = ++separatorindex;
            separatorindex = location.indexOf("#", separatorindex);
            if (separatorindex < 0) {
                continue;
            }
            boolean relativeZ = location.charAt(previousSeparatorIndex) == '~';
            if (relativeZ) {
                ++previousSeparatorIndex;
            }
            try {
                sz = Double.parseDouble(location.substring(previousSeparatorIndex, separatorindex));
            } catch (NumberFormatException e) {
                continue;
            }
            previousSeparatorIndex = ++separatorindex;
            separatorindex = location.indexOf("#", separatorindex);
            if (separatorindex < 0) {
                continue;
            }
            boolean relativeYaw = location.charAt(previousSeparatorIndex) == '~';
            if (relativeYaw) {
                ++previousSeparatorIndex;
            }
            float syaw, spitch;
            try {
                syaw = Float.parseFloat(location.substring(previousSeparatorIndex, separatorindex));
            } catch (NumberFormatException e) {
                continue;
            }
            previousSeparatorIndex = ++separatorindex;
            separatorindex = location.indexOf("#", separatorindex);
            if (separatorindex < 0) {
                continue;
            }
            boolean relativepitch = location.charAt(previousSeparatorIndex) == '~';
            if (relativepitch) {
                ++previousSeparatorIndex;
            }
            try {
                spitch = Float.parseFloat(location.substring(previousSeparatorIndex, separatorindex));
            } catch (NumberFormatException e) {
                continue;
            }
            previousSeparatorIndex = ++separatorindex;
            separatorindex = location.indexOf("#", separatorindex);
            boolean override = separatorindex > -1;
            float soyaw = 0f, sopitch = 0f;
            boolean relativeoyaw = false, relativeopitch = false;
            if (override) {
                relativeoyaw = location.charAt(previousSeparatorIndex) == '~';
                if (relativeoyaw) {
                    ++previousSeparatorIndex;
                }
                try {
                    soyaw = Float.parseFloat(location.substring(previousSeparatorIndex, separatorindex));
                } catch (NumberFormatException e) {
                    continue;
                }
                previousSeparatorIndex = ++separatorindex;
                separatorindex = location.indexOf("#", separatorindex);
                if (separatorindex < 0) {
                    continue;
                }
                relativeopitch = location.charAt(previousSeparatorIndex) == '~';
                if (relativeopitch) {
                    ++previousSeparatorIndex;
                }
                try {
                    sopitch = Float.parseFloat(location.substring(previousSeparatorIndex, separatorindex));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            for (int j = repeatCount; --j > -1; ) {
                double x = sx, y = sy, z = sz;
                float yaw = syaw, pitch = spitch, oyaw = soyaw, opitch = sopitch;
                if (previousLocation != null) {
                    if (relativeYaw) {
                        yaw += previousLocation.getYaw();
                    }
                    if (relativepitch) {
                        pitch += previousLocation.getPitch();
                    }
                    if (relativeLocation) {
                        // Обработка относительных координат
                        if (x == -0.0D) {
                            x = 0.0D;
                        }
                        if (y == -0.0D) {
                            y = 0.0D;
                        }
                        if (z == -0.0D) {
                            z = 0.0D;
                        }

                        // Преобразование углов из градусов в радианы
                        float aPitch = (float) Math.toRadians(pitch);
                        float aYaw = (float) Math.toRadians(yaw);

                        // Вычисление косинусов и синусов углов
                        double cosPitch = TheMath.cos(aPitch);
                        double cosYaw = TheMath.cos(aYaw);
                        double sinYaw = TheMath.sin(aYaw);
                        double sinPitch = TheMath.sin(aPitch);

                        // Вычисление компонентов векторов направления
                        double forwardbz = cosYaw * cosPitch;
                        double forwardbx = sinYaw * cosPitch;
                        double upbz = cosYaw * sinPitch;
                        double upbx = sinYaw * sinPitch;

                        // Нормализация векторов направления
                        double forwards = Math.sqrt(forwardbx * forwardbx + sinPitch * sinPitch + forwardbz * forwardbz);
                        double sides = Math.sqrt(sinYaw * sinYaw + cosYaw * cosYaw);
                        double ups = Math.sqrt(upbx * upbx + cosPitch * cosPitch + upbz * upbz);

                        // Вычисление компонентов смещения по координатам
                        double forwardx = -(forwardbx / forwards) * x;
                        double forwardy = -(sinPitch / forwards) * x;
                        double forwardz = (forwardbz / forwards) * x;
                        double sidedx = (cosYaw / sides) * z;
                        double sidedz = (sinYaw / sides) * z;
                        double updx = -(upbx / ups) * y;
                        double updy = (cosPitch / ups) * y;
                        double updz = (upbz / ups) * y;

                        // Обновление координат по смещениям
                        x = previousLocation.getX();
                        y = previousLocation.getY();
                        z = previousLocation.getZ();
                        x += forwardx + sidedx + updx;
                        y += forwardy + updy;
                        z += forwardz + sidedz + updz;
                    } else {
                        // Обработка абсолютных координат
                        if (relativeX) {
                            x += previousLocation.getX();
                        }
                        if (relativeY) {
                            y += previousLocation.getY();
                        }
                        if (relativeZ) {
                            z += previousLocation.getZ();
                        }
                    }
                }
                LocationPoint locationpoint;
                if (!override) {
                    locationpoint = new LocationPoint(x, y, z, yaw, pitch);
                    previewList.add(locationpoint);
                    previousLocation = locationpoint;
                    continue;
                }
                if (previousLocation instanceof RouteLocationPoint) {
                    RouteLocationPoint previousroutelocation = (RouteLocationPoint) previousLocation;
                    if (relativeoyaw) {
                        oyaw += previousroutelocation.getYaw();
                    }
                    if (relativeopitch) {
                        opitch += previousroutelocation.getPitch();
                    }
                }
                locationpoint = new RouteLocationPoint(x, y, z, yaw, pitch, oyaw, opitch);
                previewList.add(locationpoint);
                previousLocation = locationpoint;
            }
        }
    }


    private static YamlConfiguration loadConfig(File workingdirectory, JavaPlugin plugin) {
        YamlConfiguration config;
        File configfile = new File(workingdirectory, "gameoptions.yml");
        if (!configfile.exists()) {
            plugin.saveResource("gameoptions.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configfile);
        return config;
    }

    private static void createFolder(File workingdirectory) {
        if (!workingdirectory.exists()) {
            workingdirectory.mkdirs();
        }
    }

    public static Set<String> getWorldNames() {
        return gameOptions.keySet();
    }

    protected static int getArenaCount() {
        return gameOptions.size();
    }

}
