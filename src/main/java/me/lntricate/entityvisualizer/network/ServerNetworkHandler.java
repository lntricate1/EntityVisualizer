package me.lntricate.entityvisualizer.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.IEntityHelper;
import me.lntricate.entityvisualizer.helpers.EntityHelper.*;
import me.lntricate.entityvisualizer.mixins.EntityAccessor;
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
    ClientboundCustomPayloadPacket moves = getPacketMove(helper.moves(), helper.move1s(), helper.move2s());
    ClientboundCustomPayloadPacket ticks = getPacketCuboid(helper.ticks(), 1);
    ClientboundCustomPayloadPacket deaths = getPacketCuboid(helper.deaths(), 2);
    ClientboundCustomPayloadPacket vels = getPacketVel(helper.vels());
    ClientboundCustomPayloadPacket[] exposures = getPacketsExposures(helper.exposures());

    for(ServerPlayer player : players)
      if(player.level == level)
      {
        player.connection.send(moves);
        player.connection.send(ticks);
        player.connection.send(deaths);
        player.connection.send(vels);
        for(ClientboundCustomPayloadPacket packet : exposures)
          player.connection.send(packet);
      }
  }

  private static ClientboundCustomPayloadPacket getPacketMove(HashSet<Move> moves, HashSet<Move1> move1s, HashSet<Move2> move2s)
  {
    int size2 = move2s.size();
    int size1 = size2 + move1s.size();
    int size0 = size1 + moves.size();
    ListTag id = new ListTag();
    long[] x = new long[size0];
    long[] y = new long[size0];
    long[] z = new long[size0];
    long[] dx = new long[size0];
    long[] dy = new long[size0];
    long[] dz = new long[size0];
    byte[] flags = new byte[size0];
    long[] ax = new long[size1];
    long[] ay = new long[size1];
    long[] az = new long[size1];
    long[] by = new long[size2];
    long[] cx = new long[size2];
    long[] cy = new long[size1];
    long[] cz = new long[size2];

    int i = 0;
    for(Move2 move2 : move2s)
    {
      id.add(StringTag.valueOf(move2.id()));
      x[i] = Double.doubleToLongBits(move2.x());
      y[i] = Double.doubleToLongBits(move2.y());
      z[i] = Double.doubleToLongBits(move2.z());
      dx[i] = Double.doubleToLongBits(move2.dx());
      dy[i] = Double.doubleToLongBits(move2.dy());
      dz[i] = Double.doubleToLongBits(move2.dz());
      ax[i] = Double.doubleToLongBits(move2.ax());
      ay[i] = Double.doubleToLongBits(move2.ay());
      az[i] = Double.doubleToLongBits(move2.az());
      by[i] = Double.doubleToLongBits(move2.by());
      cx[i] = Double.doubleToLongBits(move2.cx());
      cy[i] = Double.doubleToLongBits(move2.cy());
      cz[i] = Double.doubleToLongBits(move2.cz());
      flags[i++] = move2.xFirst() ? 2 : (byte)0;
    }

    for(Move1 move1 : move1s)
    {
      id.add(StringTag.valueOf(move1.id()));
      x[i] = Double.doubleToLongBits(move1.x());
      y[i] = Double.doubleToLongBits(move1.y());
      z[i] = Double.doubleToLongBits(move1.z());
      dx[i] = Double.doubleToLongBits(move1.dx());
      dy[i] = Double.doubleToLongBits(move1.dy());
      dz[i] = Double.doubleToLongBits(move1.dz());
      ax[i] = Double.doubleToLongBits(move1.ax());
      ay[i] = Double.doubleToLongBits(move1.ay());
      az[i] = Double.doubleToLongBits(move1.az());
      cy[i] = Double.doubleToLongBits(move1.cy());
      flags[i++] = move1.xFirst() ? 2 : (byte)0;
    }

    for(Move move : moves)
    {
      id.add(StringTag.valueOf(move.id()));
      x[i] = Double.doubleToLongBits(move.x());
      y[i] = Double.doubleToLongBits(move.y());
      z[i] = Double.doubleToLongBits(move.z());
      dx[i] = Double.doubleToLongBits(move.dx());
      dy[i] = Double.doubleToLongBits(move.dy());
      dz[i] = Double.doubleToLongBits(move.dz());
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
    tag.putLongArray("dx", dx);
    tag.putLongArray("dy", dy);
    tag.putLongArray("dz", dz);
    tag.putLongArray("ax", ax);
    tag.putLongArray("ay", ay);
    tag.putLongArray("az", az);
    tag.putLongArray("by", by);
    tag.putLongArray("cx", cx);
    tag.putLongArray("cy", cy);
    tag.putLongArray("cz", cz);
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

  private static ClientboundCustomPayloadPacket[] getPacketsExposures(HashMap<Vec3, HashSet<Cuboid>> exposuresMap)
  {
    ClientboundCustomPayloadPacket[] packets = new ClientboundCustomPayloadPacket[exposuresMap.size()];
    int j = 0;
    for(Map.Entry<Vec3, HashSet<Cuboid>> entry : exposuresMap.entrySet())
    {
      HashSet<Cuboid> exposures = entry.getValue();
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
      Vec3 pos = entry.getKey();
      tag.putDouble("px", pos.x);
      tag.putDouble("py", pos.y);
      tag.putDouble("pz", pos.z);
      tag.putLongArray("x", x);
      tag.putLongArray("y", y);
      tag.putLongArray("z", z);
      tag.putIntArray("w", w);
      tag.putIntArray("h", h);

      FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
      packetBuf.writeVarInt(NetworkStuff.DATA);
      packetBuf.writeNbt(tag);
      packets[j++] = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
    }

    return packets;
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
    long[] px = new long[ids.length];
    long[] py = new long[ids.length];
    long[] pz = new long[ids.length];
    for(int i = 0; i < ids.length; ++i)
    {
      Entity entity = player.level.getEntity(ids[i]);
      EntityAccessor eAccess = (EntityAccessor)entity;
      Vec3 m = entity.getDeltaMovement();
      x[i] = Double.doubleToLongBits(entity.getX());
      y[i] = Double.doubleToLongBits(entity.getY());
      z[i] = Double.doubleToLongBits(entity.getZ());
      mx[i] = Double.doubleToLongBits(m.x);
      my[i] = Double.doubleToLongBits(m.y);
      mz[i] = Double.doubleToLongBits(m.z);
      fuse[i] = entity instanceof PrimedTnt tnt ? tnt.getFuse() : -1;
      double[] pistonDeltas = eAccess.getPistonDeltas();
      px[i] = Double.doubleToLongBits(pistonDeltas[0]);
      py[i] = Double.doubleToLongBits(pistonDeltas[1]);
      pz[i] = Double.doubleToLongBits(pistonDeltas[2]);
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
    tag.putLongArray("px", px);
    tag.putLongArray("py", py);
    tag.putLongArray("pz", pz);
    tag.putIntArray("fuse", fuse);
    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
    player.connection.send(packet);
  }
}
