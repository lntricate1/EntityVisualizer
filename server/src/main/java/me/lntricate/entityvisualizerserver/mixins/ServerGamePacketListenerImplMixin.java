package me.lntricate.entityvisualizerserver.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizerserver.network.NetworkStuff;
import me.lntricate.entityvisualizerserver.network.ServerNetworkHandler;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
  @Shadow private ServerPlayer player;

  @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
  private void onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci)
  {
    if(packet.getIdentifier().equals(NetworkStuff.CHANNEL))
    {
      PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener)this, (ServerLevel)player.level);
      ServerNetworkHandler.handleData(packet.getData(), player);
      ci.cancel();
    }
  }
}
