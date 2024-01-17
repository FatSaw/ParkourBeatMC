package me.bomb.parkourbeat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class TptoworldTabComplete implements TabCompleter {
	
	protected TptoworldTabComplete() {
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> tabcomplete = new ArrayList<String>();
		if (sender instanceof Player&&args.length == 1) {
			for(String ingameworld : GameStarter.getWorlds()) {
				if(ingameworld.toLowerCase().startsWith(args[0].toLowerCase())) {
					tabcomplete.add(ingameworld);
				}
			}
		}
		return tabcomplete;
	}

}
