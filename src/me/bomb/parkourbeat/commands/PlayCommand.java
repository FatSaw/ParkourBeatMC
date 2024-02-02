package me.bomb.parkourbeat.commands;

import me.bomb.parkourbeat.data.Settings;
import me.bomb.parkourbeat.game.GameOptions;
import me.bomb.parkourbeat.game.GameStarter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PlayCommand implements CommandExecutor {

    private final Random random = new Random();

    public PlayCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Set<String> worldNames = Settings.getWorldNames();
            List<String> availableWorlds = new ArrayList<String>();
            Set<String> inGameWorlds = GameStarter.getWorlds();
            for (String worldName : worldNames) {
                if (!inGameWorlds.contains(worldName)) {
                    availableWorlds.add(worldName);
                }
            }
            if (availableWorlds.isEmpty()) {
                sender.sendMessage("No free arena worlds!");
            } else {
                String freeArenaName = availableWorlds.get(random.nextInt(availableWorlds.size()));

                try {
                    new GameStarter(freeArenaName, player);
                    sender.sendMessage("Trying to start game...");
                } catch (NullPointerException e) {
                    sender.sendMessage("Game start error: " + e.getMessage());
                }
            }
        } else {
            sender.sendMessage("Command only for players!");
        }
        return true;
    }

}
