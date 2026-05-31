// Blatantly copied from https://github.com/SpaceWalkerRS/redstone-multimeter-fabric/blob/1.20.2/src/main/java/redstone/multimeter/mixin/common/ServerboundCustomPayloadPacketMixin.java
package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import me.lntricate.entityvisualizer.network.NetworkStuff;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {

  @Inject(method = "readPayload", cancellable = true, at = @At(value = "HEAD"))
  private static void readPayload(ResourceLocation channel, FriendlyByteBuf buffer, CallbackInfoReturnable<CustomPacketPayload> cir) {
    if(channel.equals(NetworkStuff.CHANNEL)) {
      cir.setReturnValue(new NetworkStuff.EntityVisualizerPayload(buffer));
    }
  }
}
