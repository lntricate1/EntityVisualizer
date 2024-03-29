package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.helpers.EntityHelper;
import me.lntricate.entityvisualizer.helpers.ExplosionHelper;
import me.lntricate.entityvisualizer.network.ClientNetworkHandler;
import me.lntricate.entityvisualizer.network.NetworkStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin
{
  @Shadow @Final private Minecraft minecraft;
  @Shadow private ClientLevel level;

  private final String mainThreadInjectionPoint = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V";

  @Inject(method = "handleCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getIdentifier()Lnet/minecraft/resources/ResourceLocation;"), cancellable = true)
  private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci)
  {
    if(packet.getIdentifier().equals(NetworkStuff.CHANNEL))
    {
      ClientNetworkHandler.handleData(packet.getData(), minecraft.player);
      ci.cancel();
    }
  }

  @Inject(method = "handleExplosion", at = @At(value = "INVOKE", target = mainThreadInjectionPoint, shift = At.Shift.AFTER))
  private void onExplosion(ClientboundExplodePacket packet, CallbackInfo ci)
  {
    ExplosionHelper.registerExplosion(packet, level);
  }

  @Inject(method = "handleAddEntity", at = @At("TAIL"))
  private void onEntitySpawn(ClientboundAddEntityPacket packet, CallbackInfo ci)
  {
    EntityHelper.registerAdd(level.getEntity(packet.getId()));
  }

  @Inject(method = "handleRemoveEntities", at = @At(value = "INVOKE", target = mainThreadInjectionPoint, shift = At.Shift.AFTER))
  private void onEntityKill(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci)
  {
    for(int id : packet.getEntityIds())
      EntityHelper.registerDeath(id);
  }
}
