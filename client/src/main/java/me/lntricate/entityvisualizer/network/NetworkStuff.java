package me.lntricate.entityvisualizer.network;

//#if MC >= 12002
//$$ import net.minecraft.nbt.CompoundTag;
//$$ import net.minecraft.network.FriendlyByteBuf;
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//#endif
import net.minecraft.resources.ResourceLocation;

public class NetworkStuff
{
  public static final ResourceLocation CHANNEL = new ResourceLocation("entityvisualizer:hello");
  public static final int HI = 1;
  public static final int BYE = -1;
  public static final int DATA = 2;

  //#if MC >= 12002
  //$$ public record EntityVisualizerPayload(CompoundTag data) implements CustomPacketPayload
  //$$ {
  //$$   public EntityVisualizerPayload(FriendlyByteBuf input)
  //$$   {
  //$$     this(input.readNbt());
  //$$   }

  //$$   @Override
  //$$   public void write(FriendlyByteBuf output)
  //$$   {
  //$$     output.writeNbt(data);
  //$$   }

  //$$   @Override
  //$$   public ResourceLocation id()
  //$$   {
  //$$     return CHANNEL;
  //$$   }
  //$$ }
  //#endif
}
