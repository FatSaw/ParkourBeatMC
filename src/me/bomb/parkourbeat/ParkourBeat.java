package me.bomb.parkourbeat;

import me.bomb.parkourbeat.commands.PlayCommand;
import me.bomb.parkourbeat.commands.TpToWorldCommand;
import me.bomb.parkourbeat.commands.TpToWorldTabComplete;
import me.bomb.parkourbeat.data.Settings;
import me.bomb.parkourbeat.game.GameOptions;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class ParkourBeat extends JavaPlugin {
    public static ParkourBeat plugin;

    public void onEnable() {
        Set<String> worldNames = Settings.getWorldNames();
        PluginCommand tpToWorldCommand = getCommand("tptoworld");
        tpToWorldCommand.setExecutor(new TpToWorldCommand(Bukkit.getWorld("world").getSpawnLocation(), worldNames));
        tpToWorldCommand.setTabCompleter(new TpToWorldTabComplete());
        getCommand("play").setExecutor(new PlayCommand());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        plugin = this;
    }

    public void onDisable() {
        for (String arenaName : Settings.getWorldNames()) {
            GameOptions.destroyArena(arenaName);
        }
    }
}
