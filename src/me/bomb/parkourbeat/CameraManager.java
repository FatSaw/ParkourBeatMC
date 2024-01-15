package me.bomb.parkourbeat;

import java.util.ArrayList;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NonNullList;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutCamera;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.PacketPlayOutGameStateChange;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_12_R1.PacketPlayOutWindowItems;
import net.minecraft.server.v1_12_R1.PacketPlayOutAbilities;

final class CameraManager extends Thread {
	
	private static final PacketPlayOutWindowItems packetemptywindowitems;
	private static final PacketPlayOutGameStateChange packetgamestatechange;
	
	static {
		NonNullList<ItemStack> nnl = NonNullList.a();
		ItemStack item = new ItemStack(Item.getById(0));
		for (byte slot = 0; slot < 46; slot++) {
			nnl.add(slot, item);
		}
		packetemptywindowitems = new PacketPlayOutWindowItems(0, nnl);
		packetgamestatechange = new PacketPlayOutGameStateChange(3, -1);
	}
	
	protected static CameraManager playCutscene(World world, LocationPoint[] preview, ArrayList<Player> players) {
		ArrayList<EntityPlayer> eplayers = new ArrayList<EntityPlayer>();
		for(Player player : players) {
			if(!player.isOnline()) {
				continue;
			}
			eplayers.add(((CraftPlayer)player).getHandle());
		}
		return new CameraManager(world, preview, eplayers);
	}
	
	private final World world;
	private final LocationPoint[] preview;
	private final ArrayList<EntityPlayer> players;
	
	private CameraManager(World world, LocationPoint[] preview, ArrayList<EntityPlayer> players) {
		this.world = world;
		this.preview = preview;
		this.players = players;
		if(world != null && preview != null && preview.length > 0 && players != null && !players.isEmpty()) {
			start();
		}
	}
	
	public void run() {
		for(EntityPlayer entityplayer : players) {
			ChannelPipeline pipeline = entityplayer.playerConnection.networkManager.channel.pipeline();
			pipeline.addBefore("packet_handler", "cutscene", new CutscenePacketFilter());
		}
		LocationPoint previouslocation = preview[0];
		EntityArmorStand stand = new EntityArmorStand(((CraftWorld)world).getHandle());
		stand.setLocation(previouslocation.getX(), previouslocation.getY() - 1.777d, previouslocation.getZ(), previouslocation.getYaw(), previouslocation.getPitch());
		stand.setInvisible(true);
		stand.h(-1);
		PacketPlayOutSpawnEntityLiving posel = new PacketPlayOutSpawnEntityLiving(stand);
		PacketPlayOutCamera poc = new PacketPlayOutCamera(stand);
		PacketPlayOutEntityDestroy poed = new PacketPlayOutEntityDestroy(-1);
		for(EntityPlayer entityplayer : players) {
			PlayerConnection connection = entityplayer.playerConnection;
			connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, entityplayer));
			connection.sendPacket(packetemptywindowitems);
			connection.sendPacket(packetgamestatechange);
			connection.sendPacket(posel);
			connection.sendPacket(poc);
		}
		short k = 0;
		byte sleep = 50;
		while (++k < preview.length) {
			if(sleep>0) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
				}
			}
			long time = System.currentTimeMillis();
			sleep = 50;
			LocationPoint location = preview[k];
			boolean hasmove = previouslocation.hasMove(location);
			if(hasmove) previouslocation = location;
			stand.locX = location.getX();
			stand.locY = location.getY();
			stand.locZ = location.getZ();
			stand.yaw = location.getYaw();
			stand.pitch = location.getPitch();
			Packet<?> movepacket = hasmove ? new PacketPlayOutEntityTeleport(stand) : new PacketPlayOutEntity.PacketPlayOutEntityLook(-1,(byte)  ((int)(location.getYaw() * 256.0F / 360.0F)), (byte) ((int)(location.getPitch() * 256.0F / 360.0F)), false);
			for(EntityPlayer entityplayer : players) {
				PlayerConnection connection = entityplayer.playerConnection;
				connection.sendPacket(movepacket);
			}
			long timedif = System.currentTimeMillis();
			timedif =- time;
			if (timedif < 0)
				timedif = 0;
			if (timedif > 50)
				timedif = 50;
			sleep -= timedif;
		}
		if(sleep>0) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			}
		}
		for(EntityPlayer entityplayer : players) {
			PlayerConnection connection = entityplayer.playerConnection;
			Channel channel = connection.networkManager.channel;
			channel.eventLoop().submit(() -> {
				channel.pipeline().remove("cutscene");
				return null;
			});
			connection.sendPacket(poed);
			connection.sendPacket(new PacketPlayOutCamera(entityplayer));
			connection.sendPacket(new PacketPlayOutGameStateChange(3,entityplayer.playerInteractManager.getGameMode().getId()));
			connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE,entityplayer));
			connection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
			entityplayer.updateInventory(entityplayer.defaultContainer);
		}
	}
	
}
