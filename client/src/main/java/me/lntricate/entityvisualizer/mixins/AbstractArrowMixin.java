package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin
{
  @Unique private Vec3 vel;

  @Inject(method = "tick", at = @At("HEAD"))
  private void onTickHead(CallbackInfo ci)
  {
    vel = ((Entity)(Object)this).getDeltaMovement();
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void onTickTail(CallbackInfo ci)
  {
    if(!((Entity)(Object)this).level.isClientSide())
      ServerNetworkHandler.sendEntity((ServerLevel)((Entity)(Object)this).level, ((Entity)(Object)this).getId(), ((Entity)(Object)this).position(), vel, true, true, true);
  }
}
