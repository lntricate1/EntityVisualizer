package me.lntricate.entityvisualizer.network;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;

public class ServerNetworkHandler
{
  private static Set<ServerPlayer> players = new HashSet<>();

  public static void handleData(FriendlyByteBuf data, ServerPlayer player)
  {
    if(data == null)
      return;

    switch(data.readVarInt())
    {
      case NetworkStuff.HI:
        players.add(player);
        break;
      case NetworkStuff.BYE:
        players.remove(player);
        break;
      case NetworkStuff.DATA:
        CompoundTag tag = data.readNbt();
        sendRequestedEntities(player, tag.getIntArray("ids"));
        break;
    }
  }

  public static void onPlayerJoin(ServerPlayer player)
  {
    player.connection.send(new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, (new FriendlyByteBuf(Unpooled.buffer())).writeVarInt(NetworkStuff.HI)));
  }

  public static void onPlayerLeave(ServerPlayer player)
  {
    players.remove(player);
  }

  public static void sendEntity(ServerLevel level, int id, Vec3 pos, Vec3 vel, boolean self, boolean xFirst, boolean coll)
  {
    if(players.isEmpty())
      return;

    boolean shouldExit = true;
    for(ServerPlayer player : players)
      if(player.level == level && player.position().distanceToSqr(pos) < 16384)
      {
        shouldExit = false;
        break;
      }
    if(shouldExit)
      return;

    CompoundTag tag = new CompoundTag();
    tag.putInt("ID", 0);
    tag.putInt("id", id);
    tag.putDouble("x", pos.x);
    tag.putDouble("y", pos.y);
    tag.putDouble("z", pos.z);
    tag.putDouble("mx", vel.x);
    tag.putDouble("my", vel.y);
    tag.putDouble("mz", vel.z);
    tag.putBoolean("self", self);
    tag.putBoolean("xFirst", xFirst);
    tag.putBoolean("coll", coll);

    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);

    for(ServerPlayer player : players)
      if(player.level == level && player.position().distanceToSqr(pos) < 16384)
        player.connection.send(packet);
  }

  public static void sendRequestedEntities(ServerPlayer player, int[] ids)
  {
    long[] x = new long[ids.length];
    long[] y = new long[ids.length];
    long[] z = new long[ids.length];
    long[] mx = new long[ids.length];
    long[] my = new long[ids.length];
    long[] mz = new long[ids.length];
    int[] fuse = new int[ids.length];
    for(int i = 0; i < ids.length; ++i)
    {
      Entity entity = player.level.getEntity(ids[i]);
      Vec3 m = entity.getDeltaMovement();
      x[i] = Double.doubleToLongBits(entity.getX());
      y[i] = Double.doubleToLongBits(entity.getY());
      z[i] = Double.doubleToLongBits(entity.getZ());
      mx[i] = Double.doubleToLongBits(m.x);
      my[i] = Double.doubleToLongBits(m.y);
      mz[i] = Double.doubleToLongBits(m.z);
      fuse[i] = entity instanceof PrimedTnt tnt ? tnt.getFuse() : -1;
    }
    CompoundTag tag = new CompoundTag();
    tag.putInt("ID", 1);
    tag.putIntArray("ids", ids);
    tag.putLongArray("x", x);
    tag.putLongArray("y", y);
    tag.putLongArray("z", z);
    tag.putLongArray("mx", mx);
    tag.putLongArray("my", my);
    tag.putLongArray("mz", mz);
    tag.putIntArray("fuse", fuse);
    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
    player.connection.send(packet);
  }
}
