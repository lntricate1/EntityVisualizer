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
//#if MC >= 12002
//$$ import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
//$$ import net.minecraft.client.multiplayer.CommonListenerCookie;
//$$ import net.minecraft.network.Connection;
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//#endif
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

@Mixin(ClientPacketListener.class)
//#if MC >= 12002
//$$ public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl
//#else
public class ClientPacketListenerMixin
//#endif
{
  @Shadow private ClientLevel level;

  //#if MC >= 12002
  //$$ protected ClientPacketListenerMixin(final Minecraft minecraft, final Connection connection, final CommonListenerCookie commonListenerCookie)
  //$$ {
  //$$   super(minecraft, connection, commonListenerCookie);
  //$$ }
  //#else
  @Shadow @Final private Minecraft minecraft;
  //#endif

  private final String mainThreadInjectionPoint = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V";

  //#if MC >= 12002
  //$$ @Inject(method = "handleUnknownCustomPayload", at = @At(value = "HEAD"), cancellable = true)
  //$$ private void onCustomPayload(CustomPacketPayload packet, CallbackInfo ci)
  //$$ {
  //$$   if(packet instanceof NetworkStuff.EntityVisualizerPayload p)
  //$$   {
  //$$     ClientNetworkHandler.handleData(p.data(), minecraft.player);
  //$$     ci.cancel();
  //$$   }
  //$$ }
  //#else
  @Inject(method = "handleCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getIdentifier()Lnet/minecraft/resources/ResourceLocation;"), cancellable = true)
  private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci)
  {
    if(packet.getIdentifier().equals(NetworkStuff.CHANNEL))
    {
      ClientNetworkHandler.handleData(packet.getData(), minecraft.player);
      ci.cancel();
    }
  }
  //#endif

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
