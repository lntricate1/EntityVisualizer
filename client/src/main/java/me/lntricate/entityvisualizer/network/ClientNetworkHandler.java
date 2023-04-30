package me.lntricate.entityvisualizer.network;

import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.helpers.EntityPositionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

public class ClientNetworkHandler
{
  private static Minecraft minecraft = Minecraft.getInstance();

  public static void handleData(FriendlyByteBuf data, LocalPlayer player)
  {
    if(data == null)
      return;

    int id = data.readVarInt();
    if(id == NetworkStuff.HI)
      setPacketRecievingState(Configs.Renderers.requireEntityPackets());
    if(id == NetworkStuff.DATA)
      handleEntityPacket(data);
  }

  public static void setPacketRecievingState(boolean state)
  {
    minecraft.player.connection.send(new ServerboundCustomPayloadPacket(NetworkStuff.CHANNEL, (new FriendlyByteBuf(Unpooled.buffer())).writeVarInt(state ? NetworkStuff.HI : NetworkStuff.BYE)));
  }

  private static void handleEntityPacket(FriendlyByteBuf data)
  {
    CompoundTag tag = data.readNbt();
    EntityPositionHelper.onEntityMove(
      tag.getInt("id"),
      tag.getBoolean("self"),
      tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"),
      tag.getBoolean("xFirst"),
      tag.getBoolean("coll"));
  }
}
