package me.bomb.parkourbeat;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.PlayerConnection;

final class BorderDraw extends BukkitRunnable {
	
	private final ConcurrentHashMap<Player, HashSet<LocationInside>> particles = new ConcurrentHashMap<Player, HashSet<LocationInside>>();
	
	public BorderDraw() {
		runTaskTimerAsynchronously(ParkourBeat.plugin, 5, 5);
	}
	
	protected void put(Player player, HashSet<LocationInside> border) {
		HashSet<LocationInside> particlelocations = particles.get(player);
		if(particlelocations == null) {
			particlelocations = new HashSet<LocationInside>();
		}
		particlelocations.addAll(border);
		particles.put(player, particlelocations);
	}
	protected void remove(Player player, HashSet<LocationInside> border) {
		if(border == null) {
			particles.remove(player);
			return;
		}
		HashSet<LocationInside> particlelocations = particles.get(player);
		if(particlelocations == null) {
			return;
		}
		particlelocations.removeAll(border);
	}

	@Override
	public void run() {
		for(Entry<Player, HashSet<LocationInside>> particlesentry : particles.entrySet()) {
			Player player = particlesentry.getKey();
			HashSet<LocationInside> particlelocations = particlesentry.getValue();
			PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
			for (LocationInside particleloc : particlelocations) {
				int x = (int) particleloc.x;
				int y = (int) particleloc.y;
				int z = (int) particleloc.z;
	    		connection.sendPacket(new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,(float) x,(float) y,(float) z, 0f, 0f, 0f, 0f, 0, 1));
			}
		}
	}

}
