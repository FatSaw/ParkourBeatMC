package me.bomb.parkourbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayCommand implements CommandExecutor {
	private final Random random = new Random();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Set<String> worldnames = GameOptions.getWorldNames();
			List<String> avilableworlds = new ArrayList<String>();
			Set<String> ingameworlds = Game.getWorlds();
			for(String worldname : worldnames) {
				if(!ingameworlds.contains(worldname)) {
					avilableworlds.add(worldname);
				}
			}
			String freearenaname = avilableworlds.get(random.nextInt(avilableworlds.size()));
			new Game(freearenaname, player);
			sender.sendMessage("Trying to start game...");
		} else {
			sender.sendMessage("Command only for players!");
		}
		return true;
	}

}