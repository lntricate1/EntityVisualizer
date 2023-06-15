package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;

@Mixin(ThrowableProjectile.class)
public class ThrowableProjectileMixin
{
  @Inject(method = "tick", at = @At("TAIL"))
  private void onTick(CallbackInfo ci)
  {
    ServerNetworkHandler.sendEntity((ServerLevel)((Entity)(Object)this).level, ((Entity)(Object)this).getId(), ((Entity)(Object)this).position(), true, true, true);
  }
}
