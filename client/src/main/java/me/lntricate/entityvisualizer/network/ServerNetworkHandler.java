package me.lntricate.entityvisualizer.network;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
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

  //#if MC >= 12002
  //$$ public static void handleData(CompoundTag data, ServerPlayer player)
  //#else
  public static void handleData(FriendlyByteBuf data, ServerPlayer player)
  //#endif
  {
    if(data == null)
      return;

    //#if MC >= 12002
    //$$ for(String key : data.getAllKeys())
    //$$   switch(key)
    //$$   {
    //$$     case "HI" -> players.add(player);
    //$$     case "BYE" -> players.remove(player);
    //$$     case "GETENTITYDATA" -> sendRequestedEntities(player, data.getCompound("GETENTITYDATA").getIntArray("ids"));
    //$$   }
    //#else
    switch(data.readVarInt())
    {
      case NetworkStuff.HI -> players.add(player);
      case NetworkStuff.BYE -> players.remove(player);
      case NetworkStuff.DATA -> {
        CompoundTag tag = data.readNbt();
        sendRequestedEntities(player, tag.getIntArray("ids"));
      }
    }
    //#endif
  }

  public static void onPlayerJoin(ServerPlayer player)
  {
    //#if MC >= 12002
    //$$ CompoundTag tag = new CompoundTag();
    //$$ tag.putBoolean("HI", true);
    //$$ player.connection.send(new ClientboundCustomPayloadPacket(new NetworkStuff.EntityVisualizerPayload(tag)));
    //#else
    player.connection.send(new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, (new FriendlyByteBuf(Unpooled.buffer())).writeVarInt(NetworkStuff.HI)));
    //#endif
  }

  public static void onPlayerLeave(ServerPlayer player)
  {
    players.remove(player);
  }

  //#if MC >= 12002
  //$$ private static NetworkStuff.EntityVisualizerPayload payload(String id, CompoundTag data)
  //$$ {
  //$$   CompoundTag rootTag = new CompoundTag();
  //$$   rootTag.put(id, data);
  //$$   return new NetworkStuff.EntityVisualizerPayload(rootTag);
  //$$ }
  //#endif

  public static void sendEntity(ServerLevel level, int id, Vec3 pos, Vec3 vel, boolean self, boolean xFirst, boolean coll)
  {
    if(players.isEmpty())
      return;

    boolean shouldExit = true;
    for(ServerPlayer player : players)
      //#if MC >= 12001
      //$$ if(player.level() == level && player.position().distanceToSqr(pos) < 16384)
      //#else
      if(player.level == level && player.position().distanceToSqr(pos) < 16384)
      //#endif
      {
        shouldExit = false;
        break;
      }
    if(shouldExit)
      return;

    CompoundTag tag = new CompoundTag();
    //#if MC < 12002
    tag.putInt("ID", 0);
    //#endif
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

    //#if MC >= 12002
    //$$ ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload("ENTITY", tag));
    //#else
    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
    //#endif

    for(ServerPlayer player : players)
      //#if MC >= 12001
      //$$ if(player.level() == level && player.position().distanceToSqr(pos) < 16384)
      //#else
      if(player.level == level && player.position().distanceToSqr(pos) < 16384)
      //#endif
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
      //#if MC >= 12001
      //$$ Entity entity = player.level().getEntity(ids[i]);
      //#else
      Entity entity = player.level.getEntity(ids[i]);
      //#endif
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
    //#if MC < 12002
    tag.putInt("ID", 1);
    //#endif
    tag.putIntArray("ids", ids);
    tag.putLongArray("x", x);
    tag.putLongArray("y", y);
    tag.putLongArray("z", z);
    tag.putLongArray("mx", mx);
    tag.putLongArray("my", my);
    tag.putLongArray("mz", mz);
    tag.putIntArray("fuse", fuse);
    //#if MC >= 12002
    //$$ ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload("GETENTITYDATA", tag));
    //#else
    FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
    packetBuf.writeVarInt(NetworkStuff.DATA);
    packetBuf.writeNbt(tag);
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(NetworkStuff.CHANNEL, packetBuf);
    //#endif
    player.connection.send(packet);
  }
}
