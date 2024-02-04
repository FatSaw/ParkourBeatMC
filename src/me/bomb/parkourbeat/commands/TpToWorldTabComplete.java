package me.bomb.parkourbeat.commands;

import me.bomb.parkourbeat.game.GameStarter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpToWorldTabComplete implements TabCompleter {

    public TpToWorldTabComplete() {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> tabComplete = new ArrayList<>();
        if (sender instanceof Player && args.length == 1) {
            for (String inGameWorld : GameStarter.getWorlds()) {
                if (inGameWorld.toLowerCase().startsWith(args[0].toLowerCase())) {
                    tabComplete.add(inGameWorld);
                }
            }
        }
        return tabComplete;
    }

}
