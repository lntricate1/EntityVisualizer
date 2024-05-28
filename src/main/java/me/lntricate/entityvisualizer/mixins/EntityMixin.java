package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import me.lntricate.entityvisualizer.IEntityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
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

  @Inject(method = "baseTick", at = @At("HEAD"))
  private void onBaseTick(CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onTick((Entity)(Object)this);
  }

  @Inject(method = "collide", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;collideBoundingBoxHeuristically(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/shapes/CollisionContext;Lnet/minecraft/util/RewindableStream;)Lnet/minecraft/world/phys/Vec3;", ordinal = 2))
  private void beforeCollisionC(Vec3 movement, CallbackInfoReturnable<Vec3> cir,
    @Local(ordinal = 2) Vec3 collisionA, @Local(ordinal = 3) Vec3 collisionB,
    @Share("collisionA") LocalRef<Vec3> collisionARef, @Share("collisionB") LocalRef<Vec3> collisionBRef)
  {
    collisionARef.set(collisionA);
    collisionBRef.set(collisionB);
  }

  @Inject(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;horizontalDistanceSqr()D", ordinal = 1, shift = Shift.BY, by = 3))
  private void afterCollisionC(Vec3 movement, CallbackInfoReturnable<Vec3> cir,
    @Share("didCollisionC") LocalBooleanRef didCollisionCRef)
  {
    didCollisionCRef.set(true);
  }

  @Inject(method = "collide", at = @At(value = "RETURN", ordinal = 0))
  private void onReturn1(Vec3 movement, CallbackInfoReturnable<Vec3> cir,
    @Local(ordinal = 1) Vec3 delta,
    @Share("collisionA") LocalRef<Vec3> collisionARef, @Share("collisionB") LocalRef<Vec3> collisionBRef, @Share("didCollisionC") LocalBooleanRef didCollisionCRef)
  {
    if(level.isClientSide())
      return;

    boolean xFirst = Math.abs(movement.x) >= Math.abs(movement.z);
    if(didCollisionCRef.get())
      ((IEntityHelper)level).onMove(position, delta, collisionARef.get(), collisionBRef.get().y, cir.getReturnValue(), (Entity)(Object)this, xFirst);
    else
      ((IEntityHelper)level).onMove(position, delta, collisionARef.get(), cir.getReturnValue().y, (Entity)(Object)this, xFirst);
    didCollisionCRef.set(false);
  }

  @Inject(method = "collide", at = @At(value = "RETURN", ordinal = 1))
  private void onReturn2(Vec3 movement, CallbackInfoReturnable<Vec3> cir)
  {
    if(level.isClientSide())
      return;

    boolean xFirst = Math.abs(movement.x) >= Math.abs(movement.z);
    ((IEntityHelper)level).onMove(position, cir.getReturnValue(), (Entity)(Object)this, noPhysics, xFirst);
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
