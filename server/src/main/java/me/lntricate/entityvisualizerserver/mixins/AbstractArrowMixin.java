package me.lntricate.entityvisualizerserver.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizerserver.network.ServerNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin
{
  @Inject(method = "tick", at = @At("TAIL"))
  private void onTick(CallbackInfo ci)
  {
    ServerNetworkHandler.sendEntity((ServerLevel)((Entity)(Object)this).level, ((Entity)(Object)this).getId(), ((Entity)(Object)this).position(), true, true, true);
  }
}
