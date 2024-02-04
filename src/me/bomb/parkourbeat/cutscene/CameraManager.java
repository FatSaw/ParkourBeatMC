package me.bomb.parkourbeat.cutscene;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.bomb.parkourbeat.location.LocationPoint;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CameraManager extends Thread {

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

    private final World world;
    private final LocationPoint[] preview;
    private final EntityPlayer entityplayer;

    private CameraManager(World world, LocationPoint[] preview, EntityPlayer entityplayer) {
        this.world = world;
        this.preview = preview;
        this.entityplayer = entityplayer;
        start();
    }

    public static CameraManager playCutscene(World world, LocationPoint[] preview, Player player) {
        if (world == null || preview == null || preview.length < 1 || player == null || !player.isOnline()) {
            return null;
        }
        return new CameraManager(world, preview, ((CraftPlayer) player).getHandle());
    }

    public void run() {
        ChannelPipeline pipeline = entityplayer.playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", "cutscene", new CutscenePacketFilter());

        LocationPoint previouslocation = preview[0];
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) world).getHandle());
        stand.setLocation(previouslocation.getX(), previouslocation.getY() - 1.777d, previouslocation.getZ(), previouslocation.getYaw(), previouslocation.getPitch());
        stand.setInvisible(true);
        stand.h(-1);
        PacketPlayOutSpawnEntityLiving posel = new PacketPlayOutSpawnEntityLiving(stand);
        PacketPlayOutCamera poc = new PacketPlayOutCamera(stand);
        PacketPlayOutEntityDestroy poed = new PacketPlayOutEntityDestroy(-1);

        PlayerConnection connection = entityplayer.playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, entityplayer));
        connection.sendPacket(packetemptywindowitems);
        connection.sendPacket(packetgamestatechange);
        connection.sendPacket(posel);
        connection.sendPacket(poc);

        short k = 0;
        byte sleep = 50;
        while (++k < preview.length) {
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {
                }
            }
            long time = System.currentTimeMillis();
            sleep = 50;
            LocationPoint location = preview[k];
            boolean hasmove = previouslocation.hasMove(location);
            if (hasmove) previouslocation = location;
            stand.locX = location.getX();
            stand.locY = location.getY();
            stand.locZ = location.getZ();
            stand.yaw = location.getYaw();
            stand.pitch = location.getPitch();
            Packet<?> movepacket = hasmove ? new PacketPlayOutEntityTeleport(stand) : new PacketPlayOutEntity.PacketPlayOutEntityLook(-1, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)), (byte) ((int) (location.getPitch() * 256.0F / 360.0F)), false);
            connection.sendPacket(movepacket);
            long timedif;
            timedif = -time;
            if (timedif < 0)
                timedif = 0;
            if (timedif > 50)
                timedif = 50;
            sleep -= (byte) timedif;
        }
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
        }
        Channel channel = connection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("cutscene");
            return null;
        });
        connection.sendPacket(poed);
        connection.sendPacket(new PacketPlayOutCamera(entityplayer));
        connection.sendPacket(new PacketPlayOutGameStateChange(3, entityplayer.playerInteractManager.getGameMode().getId()));
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, entityplayer));
        connection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        entityplayer.updateInventory(entityplayer.defaultContainer);
    }

}
