package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.IEntityHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileMixin extends Entity
{
  public ThrowableProjectileMixin(EntityType<? extends ThrowableProjectile> entityType, Level level)
  {
    super((EntityType<? extends Projectile>)entityType, level);
  }

  @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"))
  private void onTickTail(CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    // ((IEntityHelper)level).onTick((Entity)(Object)this);
    if(!isRemoved())
      ((IEntityHelper)level).onMove(position(), getDeltaMovement(), (Entity)(Object)this, true, true);
  }
}
