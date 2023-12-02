package me.lntricate.entityvisualizer.mixins;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.lntricate.entityvisualizer.network.NetworkStuff;
import me.lntricate.entityvisualizer.network.NetworkStuff.EntityVisualizerPayload;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin
{
  @Inject(method = "readPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;readUnknownPayload(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/DiscardedPayload;"), cancellable = true)
  private static void onCustomPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf, CallbackInfoReturnable<CustomPacketPayload> cir)
  {
    if(NetworkStuff.CHANNEL.equals(resourceLocation))
      cir.setReturnValue(new EntityVisualizerPayload(friendlyByteBuf));
  }
}
