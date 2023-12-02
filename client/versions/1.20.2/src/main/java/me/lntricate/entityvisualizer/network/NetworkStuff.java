package me.lntricate.entityvisualizer.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class NetworkStuff
{
  public static final ResourceLocation CHANNEL = new ResourceLocation("entityvisualizer:hello");
  public static final int HI = 1;
  public static final int BYE = -1;
  public static final int DATA = 2;

  public record EntityVisualizerPayload(CompoundTag tag) implements CustomPacketPayload
  {
    public EntityVisualizerPayload(FriendlyByteBuf friendlyByteBuf)
    {
      this(friendlyByteBuf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf)
    {
      friendlyByteBuf.writeNbt(tag);
    }

    @Override
    public ResourceLocation id()
    {
      return CHANNEL;
    }
  }

  public static ClientboundCustomPayloadPacket clientbound(CompoundTag tag)
  {
    return new ClientboundCustomPayloadPacket(new EntityVisualizerPayload(tag));
  }

  public static ServerboundCustomPayloadPacket serverbound(CompoundTag tag)
  {
    return new ServerboundCustomPayloadPacket(new EntityVisualizerPayload(tag));
  }
}
