package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin
{
  @Inject(method = "handleExplosion", at = @At("TAIL"))
  private void onExplosion(ClientboundExplodePacket packet, CallbackInfo ci)
  {
    double x = packet.getX();
    double y = packet.getY();
    double z = packet.getZ();

    // MAKE THIS CUSTOMIZABLE
    double explosionBoxSize = 0.125;
    Color4f explosionBoxStroke = new Color4f(0F, 0F, 0F, 1F);
    Color4f explosionBoxFill = new Color4f(0F, 1F, 1F, 0.26F);
    if(Configs.Renderers.EXPLOSIONS.config.getBooleanValue())
    {
      RenderHandler.addCuboid(x, y, z, explosionBoxSize, explosionBoxStroke, explosionBoxFill, Configs.Renderers.EXPLOSIONS.config.getIntegerValue());
    }
  }
}
