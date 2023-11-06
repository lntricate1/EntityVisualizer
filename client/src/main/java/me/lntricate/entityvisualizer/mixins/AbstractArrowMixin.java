package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.SERVER)
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity
{
  public AbstractArrowMixin(EntityType<? extends AbstractArrow> entityType, Level level)
  {
    super((EntityType<? extends Projectile>)entityType, level);
  }

  @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"))
  private void onTick(CallbackInfo ci)
  {
    Vec3 vel = getDeltaMovement();
    ServerNetworkHandler.sendEntity((ServerLevel)level, getId(), position().add(vel), vel, true, true, true);
  }
}
