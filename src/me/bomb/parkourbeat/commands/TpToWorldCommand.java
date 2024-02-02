package me.bomb.parkourbeat.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class TpToWorldCommand implements CommandExecutor {
    private final Location mainworldlocation;
    private final Set<String> worlds;

    public TpToWorldCommand(Location mainworldlocation, Set<String> worlds) {
        this.mainworldlocation = mainworldlocation;
        this.worlds = worlds;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                String worlDid = args[0].toLowerCase();

                World world = Bukkit.getWorld(worlDid);
                if (world == null) {
                    sender.sendMessage("World not loaded!");
                    return true;
                }
                player.teleport(world.getSpawnLocation());
                if (worlds.contains(worlDid)) {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("Teleported to parkourbeat world ".concat(world.getName()));
                } else {
                    sender.sendMessage("World not loaded!");
                }
            } else {
                player.teleport(mainworldlocation);
                player.sendMessage("Teleported to main world");
                player.setGameMode(GameMode.ADVENTURE);
            }
        } else {
            sender.sendMessage("Command only for players!");
        }
        return true;
    }

}
