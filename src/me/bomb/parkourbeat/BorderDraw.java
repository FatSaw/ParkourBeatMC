package me.bomb.parkourbeat;

import me.bomb.parkourbeat.location.LocationInside;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class BorderDraw extends BukkitRunnable {

    private final ConcurrentHashMap<Player, HashSet<LocationInside>> particles = new ConcurrentHashMap<>();

    public BorderDraw() {
        runTaskTimerAsynchronously(ParkourBeat.plugin, 5, 5);
    }

    private void put(Player player, HashSet<LocationInside> border) {
        HashSet<LocationInside> particlelocations = particles.get(player);
        if (particlelocations == null) {
            particlelocations = new HashSet<>();
        }
        particlelocations.addAll(border);
        particles.put(player, particlelocations);
    }

    private void remove(Player player, HashSet<LocationInside> border) {
        if (border == null) {
            particles.remove(player);
            return;
        }
        HashSet<LocationInside> particleLocations = particles.get(player);
        if (particleLocations == null) {
            return;
        }
        particleLocations.removeAll(border);
    }

    @Override
    public void run() {
        for (Entry<Player, HashSet<LocationInside>> particLesentry : particles.entrySet()) {
            Player player = particLesentry.getKey();
            HashSet<LocationInside> particleLocations = particLesentry.getValue();
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            for (LocationInside particleLoc : particleLocations) {
                int x = (int) particleLoc.x;
                int y = (int) particleLoc.y;
                int z = (int) particleLoc.z;
                connection.sendPacket(new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, (float) x, (float) y, (float) z, 0f, 0f, 0f, 0f, 0, 1));
            }
        }
    }

}
