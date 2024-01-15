package me.bomb.parkourbeat;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ParkourBeat extends JavaPlugin {
	protected static ParkourBeat plugin;
	public void onEnable() {
		Set<String> worldnames = GameOptions.getWorldNames();
		PluginCommand tptoworldcommand = getCommand("tptoworld");
		tptoworldcommand.setExecutor(new TptoworldCommand(Bukkit.getWorld("world").getSpawnLocation(), worldnames));
		tptoworldcommand.setTabCompleter(new TptoworldTabComplete());
		getCommand("play").setExecutor(new PlayCommand());
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		plugin = this;
	}
	public void onDisable() {
		for(String arenaname : GameOptions.getWorldNames()) {
			GameOptions.destroyArena(arenaname);
		}
	}
}
