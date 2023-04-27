package me.lntricate.entityvisualizer.network;

import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.helpers.EntityPositionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

public class ClientNetworkHandler
{
  private static Minecraft minecraft = Minecraft.getInstance();

  public static void handleData(FriendlyByteBuf data, LocalPlayer player)
  {
    if(data == null)
      return;

    int id = data.readVarInt();
    if(id == NetworkStuff.HI)
      setPacketRecievingState(Configs.Renderers.ENTITY_TICKS.config.getBooleanValue() || Configs.Renderers.ENTITY_TRAJECTORY.config.getBooleanValue());
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
    Entity entity = minecraft.level.getEntity(tag.getInt("id"));
    boolean nocollide = entity.noPhysics || entity instanceof Projectile;
    EntityPositionHelper.onEntityMove(
      entity,
      tag.getBoolean("self"),
      tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"),
      nocollide, tag.getBoolean("xFirst"));
  }
}
