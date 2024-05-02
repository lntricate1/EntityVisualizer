package me.lntricate.entityvisualizer.network;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.IEntityHelper;
import me.lntricate.entityvisualizer.helpers.EntityHelper.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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

  public static void send(ServerLevel level)
  {
    if(players.isEmpty())
      return;

    boolean shouldExit = true;
    for(ServerPlayer player : players)
      if(player.level == level)
      {
        shouldExit = false;
        break;
      }
    if(shouldExit)
      return;

    IEntityHelper helper = (IEntityHelper)level;
    ClientboundCustomPayloadPacket moves = getPacketMove(helper.moves());
    ClientboundCustomPayloadPacket ticks = getPacketCuboid(helper.ticks(), 1);
    ClientboundCustomPayloadPacket deaths = getPacketCuboid(helper.deaths(), 2);
    ClientboundCustomPayloadPacket vels = getPacketVel(helper.vels());

    for(ServerPlayer player : players)
      if(player.level == level)
      {
        player.connection.send(moves);
        player.connection.send(ticks);
        player.connection.send(deaths);
        player.connection.send(vels);
      }
  }

  private static ClientboundCustomPayloadPacket getPacketMove(HashSet<Move> moves)
  {
    ListTag id = new ListTag();
    long[] x = new long[moves.size()];
    long[] y = new long[moves.size()];
    long[] z = new long[moves.size()];
    long[] X = new long[moves.size()];
    long[] Y = new long[moves.size()];
    long[] Z = new long[moves.size()];
    byte[] flags = new byte[moves.size()];

    int i = 0;
    for(Move move : moves)
    {
      id.add(StringTag.valueOf(move.id()));
      x[i] = Double.doubleToLongBits(move.x());
      y[i] = Double.doubleToLongBits(move.y());
      z[i] = Double.doubleToLongBits(move.z());
      X[i] = Double.doubleToLongBits(move.X());
      Y[i] = Double.doubleToLongBits(move.Y());
      Z[i] = Double.doubleToLongBits(move.Z());
      byte n = move.noPhysics() ? 1 : (byte)0;
      n += move.xFirst() ? 2 : 0;
      flags[i++] = n;
    }

    CompoundTag tag = new CompoundTag();
    tag.putInt("ID", 0);
    tag.put("id", id);
    tag.putLongArray("x", x);
    tag.putLongArray("y", y);
    tag.putLongArray("z", z);
    tag.putLongArray("X", X);
    tag.putLongArray("Y", Y);
    tag.putLongArray("Z", Z);
    tag.putByteArray("flags", flags);

    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    return new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
  }

  private static ClientboundCustomPayloadPacket getPacketCuboid(HashSet<Cuboid> cuboids, int ID)
  {
    ListTag id = new ListTag();
    long[] x = new long[cuboids.size()];
    long[] y = new long[cuboids.size()];
    long[] z = new long[cuboids.size()];
    int[] w = new int[cuboids.size()];
    int[] h = new int[cuboids.size()];

    int i = 0;
    for(Cuboid cuboid : cuboids)
    {
      id.add(StringTag.valueOf(cuboid.id()));
      x[i] = Double.doubleToLongBits(cuboid.x());
      y[i] = Double.doubleToLongBits(cuboid.y());
      z[i] = Double.doubleToLongBits(cuboid.z());
      w[i] = Float.floatToIntBits(cuboid.w());
      h[i++] = Float.floatToIntBits(cuboid.h());
    }

    CompoundTag tag = new CompoundTag();
    tag.putInt("ID", ID);
    tag.put("id", id);
    tag.putLongArray("x", x);
    tag.putLongArray("y", y);
    tag.putLongArray("z", z);
    tag.putIntArray("w", w);
    tag.putIntArray("h", h);

    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    return new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
  }

  private static ClientboundCustomPayloadPacket getPacketVel(HashSet<Vel> vels)
  {
    ListTag id = new ListTag();
    long[] x = new long[vels.size()];
    long[] y = new long[vels.size()];
    long[] z = new long[vels.size()];
    long[] X = new long[vels.size()];
    long[] Y = new long[vels.size()];
    long[] Z = new long[vels.size()];

    int i = 0;
    for(Vel vel : vels)
    {
      id.add(StringTag.valueOf(vel.id()));
      x[i] = Double.doubleToLongBits(vel.x());
      y[i] = Double.doubleToLongBits(vel.y());
      z[i] = Double.doubleToLongBits(vel.z());
      X[i] = Double.doubleToLongBits(vel.X());
      Y[i] = Double.doubleToLongBits(vel.Y());
      Z[i++] = Double.doubleToLongBits(vel.Z());
    }

    CompoundTag tag = new CompoundTag();
    tag.putInt("ID", 3);
    tag.put("id", id);
    tag.putLongArray("x", x);
    tag.putLongArray("y", y);
    tag.putLongArray("z", z);
    tag.putLongArray("X", X);
    tag.putLongArray("Y", Y);
    tag.putLongArray("Z", Z);

    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    return new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
  }

  public static void sendExposures(ServerLevel level, double px, double py, double pz, HashSet<Cuboid> exposures)
  {
    if(players.isEmpty())
      return;

    boolean shouldExit = true;
    for(ServerPlayer player : players)
      if(player.level == level)
      {
        shouldExit = false;
        break;
      }
    if(shouldExit)
      return;

    ListTag id = new ListTag();
    long[] x = new long[exposures.size()];
    long[] y = new long[exposures.size()];
    long[] z = new long[exposures.size()];
    int[] w = new int[exposures.size()];
    int[] h = new int[exposures.size()];

    int i = 0;
    for(Cuboid exposure : exposures)
    {
      id.add(StringTag.valueOf(exposure.id()));
      x[i] = Double.doubleToLongBits(exposure.x());
      y[i] = Double.doubleToLongBits(exposure.y());
      z[i] = Double.doubleToLongBits(exposure.z());
      w[i] = Float.floatToIntBits(exposure.w());
      h[i++] = Float.floatToIntBits(exposure.h());
    }

    CompoundTag tag = new CompoundTag();
    tag.putInt("ID", 4);
    tag.put("id", id);
    tag.putDouble("px", px);
    tag.putDouble("py", py);
    tag.putDouble("pz", pz);
    tag.putLongArray("x", x);
    tag.putLongArray("y", y);
    tag.putLongArray("z", z);
    tag.putIntArray("w", w);
    tag.putIntArray("h", h);

    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
    for(ServerPlayer player : players)
      if(player.level == level)
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
    tag.putInt("ID", 5);
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
