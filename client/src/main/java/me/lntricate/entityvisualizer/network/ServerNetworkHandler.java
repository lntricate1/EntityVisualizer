package me.lntricate.entityvisualizer.network;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
}
