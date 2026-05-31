package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.network.NetworkStuff;
import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Environment(EnvType.SERVER)
@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin
{
  @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
  private void onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci)
  {
    Object thiss = this;
    if(thiss instanceof ServerGamePacketListenerImpl impl && packet.payload() instanceof NetworkStuff.EntityVisualizerPayload p)
    {
      PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener)this, impl.player.serverLevel());
      ServerNetworkHandler.handleData(p.data(), impl.player);
      ci.cancel();
    }
  }
}
