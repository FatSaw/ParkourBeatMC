package me.bomb.parkourbeat.game;

import me.bomb.amusic.AMusic;
import me.bomb.parkourbeat.ParkourBeat;
import me.bomb.parkourbeat.location.LocationZone;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTicker extends BukkitRunnable {

    private final Player player;
    private final World world;
    private final GameCloser gamecloser;
    private final LocationZone gamezone, finish;

    GameTicker(Player player, World world, GameCloser gamecloser, LocationZone gamezone, LocationZone finish) {
        this.player = player;
        this.world = world;
        this.gamecloser = gamecloser;
        this.gamezone = gamezone;
        this.finish = finish;
    }

    public void run() {
        Location loc = player.getLocation();
        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        boolean notFall = gamezone.isOutside(x, y, z);
        if (player.getWorld() != world || notFall) {
            gamecloser.runTaskLater(ParkourBeat.plugin, 50L);
            cancel();
            AMusic.stopSound(player);
            return;
        }

        if (finish.isInside(x, y, z)) {
            gamecloser.runTaskLater(ParkourBeat.plugin, 50L);
            cancel();
            AMusic.stopSound(player);
            player.sendMessage("You win!");
        }
    }

}
