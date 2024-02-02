package me.bomb.parkourbeat.cutscene;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.PacketPlayInFlying.PacketPlayInLook;
import net.minecraft.server.v1_12_R1.PacketPlayInFlying.PacketPlayInPosition;
import net.minecraft.server.v1_12_R1.PacketPlayInFlying.PacketPlayInPositionLook;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public final class CutscenePacketFilter extends ChannelDuplexHandler {

    private static final PacketPlayOutWindowItems packetemptywindowitems;

    static {
        NonNullList<ItemStack> nnl = NonNullList.a();
        ItemStack item = new ItemStack(Item.getById(0));
        for (byte slot = 0; slot < 46; slot++) {
            nnl.add(slot, item);
        }
        packetemptywindowitems = new PacketPlayOutWindowItems(0, nnl);
    }

    CutscenePacketFilter() {
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        if (packet instanceof PacketPlayInSteerVehicle || packet instanceof PacketPlayInFlying
                || packet instanceof PacketPlayInPosition || packet instanceof PacketPlayInPositionLook
                || packet instanceof PacketPlayInLook || packet instanceof PacketPlayInBlockDig
                || packet instanceof PacketPlayInBlockPlace || packet instanceof PacketPlayInArmAnimation
                || packet instanceof PacketPlayInWindowClick || packet instanceof PacketPlayInEntityAction
                || packet instanceof PacketPlayInUseEntity) {
            return;
        }
        super.channelRead(context, packet);
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) throws Exception {
        if (packet instanceof PacketPlayOutWindowItems || packet instanceof PacketPlayOutSetSlot) {
            packet = packetemptywindowitems;
        }
        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
            PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
            info.b(packetdataserializer);
            EnumPlayerInfoAction action = packetdataserializer.a(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.class);
            if (Objects.requireNonNull(action) == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
                HashSet<UUID> uuids = new HashSet<>();
                int i = packetdataserializer.g();
                for (int j = 0; j < i; ++j) {
                    uuids.add(packetdataserializer.i());
                    packetdataserializer.g();
                }
                packetdataserializer.a(action);
                packetdataserializer.d(uuids.size());
                for (UUID uuid : uuids) {
                    packetdataserializer.a(uuid);
                    packetdataserializer.d(3);
                }
                info.a(packetdataserializer);
            }
        }
        super.write(context, packet, channelPromise);
    }
}
