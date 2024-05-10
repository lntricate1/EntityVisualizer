package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.IEntityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class EntityMixin
{
  @Shadow private Vec3 position;
  @Shadow private Vec3 deltaMovement;
  @Shadow private boolean noPhysics;
  @Shadow private Level level;

  @Unique private Vec3 startpos;

  @Inject(method = "baseTick", at = @At("HEAD"))
  private void onBaseTick(CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onTick((Entity)(Object)this);
  }

  @Inject(method = "move", at = @At("HEAD"))
  private void startMove(MoverType moverType, Vec3 movement, CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    startpos = position;
  }

  @Inject(method = "move", at = @At("TAIL"))
  private void endMove(MoverType moverType, Vec3 movement, CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onMove(startpos, position, (Entity)(Object)this, noPhysics, Math.abs(movement.x) >= Math.abs(movement.z));
  }

  @Inject(method = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
  private void onAccel(Vec3 vel, CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onAccel((Entity)(Object)this, vel);
  }

  @Inject(method = "load", at = @At("TAIL"))
  private void onLoad(CompoundTag tag, CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onAccel((Entity)(Object)this, deltaMovement);
  }

  @Inject(method = "remove", at = @At("HEAD"))
  private void onRemove(RemovalReason reason, CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onDeath((Entity)(Object)this);
  }
}
