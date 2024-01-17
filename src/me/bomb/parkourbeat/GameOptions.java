package me.bomb.parkourbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

final class GameOptions {
	private final static FilenameFilter mcafilter = new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith(".mca");
	    }
	};
	private final static VoidGenerator voidgen = new VoidGenerator();
	private final static HashMap<String,GameOptions> gameoptions = new HashMap<String,GameOptions>();
	protected final static Set<String> loadedarenas = new HashSet<String>();
	

	protected final static World lobbyworld;
	protected final static Location exitlocation;
	protected final static boolean debug;
	private final byte[] leveldat;
	protected final String songplaylistname;
	private final HashMap<String,byte[]> regiondata;
	protected final LocationPoint[] preview;
	protected final List<LocationZones> checkpointzones;
	protected final LocationZone spawnruner;
	protected final LocationZone gamezone;
	protected final LocationZone finishzone;
	
	private GameOptions(String songplaylistname, byte[] leveldat, HashMap<String,byte[]> regiondata, LocationPoint[] preview, List<LocationZones> checkpointzones, LocationZone spawnruner, LocationZone gamezone, LocationZone finishzone) {
		this.songplaylistname = songplaylistname;
		this.regiondata = regiondata;
		this.leveldat = leveldat;
		this.preview = preview;
		this.checkpointzones = checkpointzones;
		this.spawnruner = spawnruner;
		this.gamezone = gamezone;
		this.finishzone = finishzone;
	}
	static {
		JavaPlugin plugin = JavaPlugin.getPlugin(ParkourBeat.class);
		File workingdirectory = plugin.getDataFolder();
		if(!workingdirectory.exists()) {
			workingdirectory.mkdirs();
		}
		File worldsdirectory = new File(workingdirectory, "worlds");
		if(!worldsdirectory.exists()) {
			worldsdirectory.mkdirs();
		}
		YamlConfiguration config = null;
		File configfile = new File(workingdirectory, "gameoptions.yml");
		if (!configfile.exists()) {
			try {
				byte[] buf = new byte[2048];
				InputStream in = plugin.getResource("gameoptions.yml");
				if (in != null) {
					buf = Arrays.copyOf(buf, in.read(buf));
					in.close();
					OutputStream out = new FileOutputStream(configfile);
					if (out != null) {
						out.write(buf);
						out.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(configfile);
		lobbyworld = Bukkit.getWorld(config.getString("lobbyworld", "world"));
		exitlocation = lobbyworld.getSpawnLocation();
		debug = config.getBoolean("debug", false);
		ConfigurationSection gamefieldscs = config.getConfigurationSection("gamefields");
		if(gamefieldscs!=null) {
			for(String gamefieldid : gamefieldscs.getKeys(false)) {
				gamefieldid = gamefieldid.toLowerCase();
				ConfigurationSection gamefieldcs = gamefieldscs.getConfigurationSection(gamefieldid);
				if(gamefieldcs == null || !gamefieldcs.isInt("worldcount")) {
					continue;
				}
				int worldcount = gamefieldcs.getInt("worldcount", 0);
				if(worldcount<1 || worldcount > 127) {
					continue;
				}
				String songplaylistname = gamefieldcs.getString("songplaylistname", null);
				ConfigurationSection runerspawn = gamefieldcs.getConfigurationSection("spawn");
				ConfigurationSection finishcs = gamefieldcs.getConfigurationSection("finish");
				ConfigurationSection gamezonecs = gamefieldcs.getConfigurationSection("gamezone");
				if(runerspawn == null || finishcs == null || gamezonecs == null || !finishcs.isInt("minx") || !finishcs.isInt("miny") || !finishcs.isInt("minz") || !finishcs.isInt("maxx") || !finishcs.isInt("maxy") || !finishcs.isInt("maxz") || !gamezonecs.isInt("minx") || !gamezonecs.isInt("miny") || !gamezonecs.isInt("minz") || !gamezonecs.isInt("maxx") || !gamezonecs.isInt("maxy") || !gamezonecs.isInt("maxz") || !runerspawn.isInt("minx") || !runerspawn.isInt("miny") || !runerspawn.isInt("minz") || !runerspawn.isInt("maxx") || !runerspawn.isInt("maxy") || !runerspawn.isInt("maxz")) {
					continue;
				}
				ConfigurationSection checkpointscs = gamefieldcs.getConfigurationSection("checkpoints");
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
				}
				LocationZone spawnruner = new LocationZone(runerspawn.getInt("minx"), runerspawn.getInt("miny"), runerspawn.getInt("minz"), runerspawn.getInt("maxx"), runerspawn.getInt("maxy"), runerspawn.getInt("maxz"));
				LocationZone gamezone = new LocationZone(gamezonecs.getInt("minx"), gamezonecs.getInt("miny"), gamezonecs.getInt("minz"), gamezonecs.getInt("maxx"), gamezonecs.getInt("maxy"), gamezonecs.getInt("maxz"));
				LocationZone finishzone = new LocationZone(finishcs.getInt("minx"), finishcs.getInt("miny"), finishcs.getInt("minz"), finishcs.getInt("maxx"), finishcs.getInt("maxy"), finishcs.getInt("maxz"));
				LocationPoint[] preview = null;
				ArrayList<LocationPoint> previewlist = new ArrayList<LocationPoint>();
				if(gamefieldcs.contains("preview")) {
					List<String> locations = gamefieldcs.getStringList("preview");
					LocationPoint previouslocation = null;
					for(String location : locations) {
						if(location==null||location.isEmpty()) {
							continue;
						}
						int previousseparatorindex = location.indexOf("$");
						int repeatcount = 1;
						if(previousseparatorindex>-1) {
							try {
								repeatcount = Integer.valueOf(location.substring(0, previousseparatorindex));
							} catch (NumberFormatException e) {
							}
						}
						previousseparatorindex = location.indexOf("#");
						if(previousseparatorindex<0) {
							continue;
						}
						boolean relativelocation = previousseparatorindex>0 && location.charAt(previousseparatorindex-1) == '~';
						int separatorindex = location.indexOf("#", ++previousseparatorindex);
						if(separatorindex<0) {
							continue;
						}
						boolean relativex = location.charAt(previousseparatorindex) == '~';
						if(relativex) {
							++previousseparatorindex;
						}
						double sx,sy,sz;
						try {
							sx = Double.parseDouble(location.substring(previousseparatorindex, separatorindex));
						} catch (NumberFormatException e) {
							continue;
						}
						previousseparatorindex = ++separatorindex;
						separatorindex = location.indexOf("#", separatorindex);
						if(separatorindex<0) {
							continue;
						}
						boolean relativey = location.charAt(previousseparatorindex) == '~';
						if(relativey) {
							++previousseparatorindex;
						}
						try {
							sy = Double.parseDouble(location.substring(previousseparatorindex, separatorindex));
						} catch (NumberFormatException e) {
							continue;
						}
						previousseparatorindex = ++separatorindex;
						separatorindex = location.indexOf("#", separatorindex);
						if(separatorindex<0) {
							continue;
						}
						boolean relativez = location.charAt(previousseparatorindex) == '~';
						if(relativez) {
							++previousseparatorindex;
						}
						try {
							sz = Double.parseDouble(location.substring(previousseparatorindex, separatorindex));
						} catch (NumberFormatException e) {
							continue;
						}
						previousseparatorindex = ++separatorindex;
						separatorindex = location.indexOf("#", separatorindex);
						if(separatorindex<0) {
							continue;
						}
						boolean relativeyaw = location.charAt(previousseparatorindex) == '~';
						if(relativeyaw) {
							++previousseparatorindex;
						}
						float syaw,spitch;
						try {
							syaw = Float.parseFloat(location.substring(previousseparatorindex, separatorindex));
						} catch (NumberFormatException e) {
							continue;
						}
						previousseparatorindex = ++separatorindex;
						separatorindex = location.indexOf("#", separatorindex);
						if(separatorindex<0) {
							continue;
						}
						boolean relativepitch = location.charAt(previousseparatorindex) == '~';
						if(relativepitch) {
							++previousseparatorindex;
						}
						try {
							spitch = Float.parseFloat(location.substring(previousseparatorindex, separatorindex));
						} catch (NumberFormatException e) {
							continue;
						}
						previousseparatorindex = ++separatorindex;
						separatorindex = location.indexOf("#", separatorindex);
						boolean override = separatorindex>-1;
						float soyaw = 0f,sopitch = 0f;
						boolean relativeoyaw = false,relativeopitch = false;
						if(override) {
							relativeoyaw = location.charAt(previousseparatorindex) == '~';
							if(relativeoyaw) {
								++previousseparatorindex;
							}
							try {
								soyaw = Float.parseFloat(location.substring(previousseparatorindex, separatorindex));
							} catch (NumberFormatException e) {
								continue;
							}
							previousseparatorindex = ++separatorindex;
							separatorindex = location.indexOf("#", separatorindex);
							if(separatorindex<0) {
								continue;
							}
							relativeopitch = location.charAt(previousseparatorindex) == '~';
							if(relativeopitch) {
								++previousseparatorindex;
							}
							try {
								sopitch = Float.parseFloat(location.substring(previousseparatorindex, separatorindex));
							} catch (NumberFormatException e) {
								continue;
							}
							previousseparatorindex = ++separatorindex;
							separatorindex = location.indexOf("#", separatorindex);
						}
						for(int j = repeatcount; --j > -1;) {
							double x = sx, y = sy, z = sz;
							float yaw = syaw, pitch = spitch, oyaw = soyaw, opitch = sopitch;
							if (previouslocation!=null) {
								if (relativeyaw) {
									yaw += previouslocation.getYaw();
								}
								if (relativepitch) {
									pitch += previouslocation.getPitch();
								}
								if (relativelocation) {
									if (x == -0.0D) {
										x = 0.0D;
									}
									if (y == -0.0D) {
										y = 0.0D;
									}
									if (z == -0.0D) {
										z = 0.0D;
									}
									float apitch = (float) Math.toRadians(pitch),ayaw = (float) Math.toRadians(yaw);
								    double cospitch = TheMath.cos(apitch),cosyaw = TheMath.cos(ayaw),sinyaw = TheMath.sin(ayaw),sinpitch = TheMath.sin(apitch), forwardbz = cosyaw * cospitch,forwardbx = sinyaw * cospitch, upbz = cosyaw * sinpitch,upbx = sinyaw * sinpitch, forwards = Math.sqrt(forwardbx*forwardbx+sinpitch*sinpitch+forwardbz*forwardbz),sides = Math.sqrt(sinyaw*sinyaw+cosyaw*cosyaw),ups = Math.sqrt(upbx*upbx+cospitch*cospitch+upbz*upbz), forwardx = -(forwardbx/forwards)*x,forwardy = -(sinpitch/forwards)*x,forwardz = (forwardbz/forwards)*x, sidedx = (cosyaw/sides)*z,sidedz = (sinyaw/sides)*z, updx = -(upbx/ups)*y,updy = (cospitch/ups)*y,updz = (upbz/ups)*y;
									x = previouslocation.getX();
									y = previouslocation.getY();
									z = previouslocation.getZ();
								    x+=forwardx+sidedx+updx;
								    y+=forwardy+updy;
								    z+=forwardz+sidedz+updz;
								} else {
									if (relativex) {
										x += previouslocation.getX();
									}
									if (relativey) {
										y += previouslocation.getY();
									}
									if (relativez) {
										z += previouslocation.getZ();
									}
								}
							}
							LocationPoint locationpoint;
							if(!override) {
								locationpoint = new LocationPoint(x, y, z, yaw, pitch);
								previewlist.add(locationpoint);
								previouslocation = locationpoint;
								continue;
							}
							if (previouslocation!=null&&previouslocation instanceof RouteLocationPoint) {
								RouteLocationPoint previousroutelocation = (RouteLocationPoint) previouslocation;
								if (relativeoyaw) {
									oyaw += previousroutelocation.getYaw();
								}
								if (relativeopitch) {
									opitch += previousroutelocation.getPitch();
								}
							}
							locationpoint = new RouteLocationPoint(x, y, z, yaw, pitch, oyaw, opitch);
							previewlist.add(locationpoint);
							previouslocation = locationpoint;
						}
					}
				}
				if(previewlist.size()>0) {
					preview = previewlist.toArray(new LocationPoint[previewlist.size()]);
				}
				File gamefielddir = new File(worldsdirectory, gamefieldid);
				File leveldatfile = new File(gamefielddir, "level.dat");
				byte[] readbuf = new byte[8096];
				byte[] leveldat = null;
				try {
					FileInputStream fis = new FileInputStream(leveldatfile);
					readbuf = Arrays.copyOf(readbuf, fis.read(readbuf));
					fis.close();
					leveldat = readbuf;
				} catch (IOException e) {
				}
				File[] regionfiles = gamefielddir.listFiles(mcafilter);
				HashMap<String,byte[]> regiondata = new HashMap<String,byte[]>();
				for(File regionfile : regionfiles) {
					readbuf = new byte[8388608];
					try {
						FileInputStream fis = new FileInputStream(regionfile);
						readbuf = Arrays.copyOf(readbuf, fis.read(readbuf));
						fis.close();
						regiondata.put(regionfile.getName(), readbuf);
					} catch (IOException e) {
					}
				}
				GameOptions gameoption = new GameOptions(songplaylistname, leveldat, regiondata, preview, checkpointzones, spawnruner, gamezone, finishzone);
				gamefieldid = gamefieldid.concat("_");
				for(byte i = (byte) worldcount;--i>-1;) {
					gameoptions.put(gamefieldid.concat(Byte.toString((byte)i)), gameoption);
				}
			}
		}
	}
	
	protected static GameOptions initArena(String arenaname) {
		GameOptions gameoption = gameoptions.get(arenaname);
		if(gameoption==null || !loadedarenas.add(arenaname)) {
			return null;
		}
		File worlddir = new File(arenaname);
		deleteFolder(worlddir);
		File regiondir = new File(worlddir, "region");
		regiondir.mkdirs();
		try {
			FileOutputStream fos = new FileOutputStream(new File(worlddir, "level.dat"), false);
			fos.write(gameoption.leveldat);
			fos.close();
		} catch (IOException e) {
			deleteFolder(worlddir);
			loadedarenas.remove(arenaname);
			return null;
		}
		for(String filename : gameoption.regiondata.keySet()) {
			try {
				FileOutputStream fos = new FileOutputStream(new File(regiondir, filename), false);
				fos.write(gameoption.regiondata.get(filename));
				fos.close();
			} catch (IOException e) {
				continue;
			}
		}
		WorldCreator worldcreator = new WorldCreator(arenaname);
		worldcreator.generator(voidgen);
		worldcreator.generateStructures(false);
		World world = worldcreator.createWorld();
		world.setKeepSpawnInMemory(false);
		world.setAutoSave(false);
		world.setPVP(false);
		world.setSpawnFlags(true, true);
		world.getWorldBorder().setSize(2048);
		return gameoption;
	}
	
	protected static boolean destroyArena(String arenaname) {
		if(!loadedarenas.remove(arenaname)) {
			return false;
		}
		World world = Bukkit.getWorld(arenaname);
		if(world == null) {
			return false;
		}
		for(Player player : world.getPlayers()) {
			player.teleport(exitlocation);
		}
		File worlddir = world.getWorldFolder();
		return Bukkit.unloadWorld(world, false) && deleteFolder(worlddir);
	}
	
	private static boolean deleteFolder(File file) {
        try (Stream<Path> files = Files.walk(file.toPath())) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
	
	protected static Set<String> getWorldNames() {
		return gameoptions.keySet();
	}
	
	protected static int getArenaCount() {
		return gameoptions.size();
	}
	
}