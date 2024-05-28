package me.lntricate.entityvisualizer.mixins.carpet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import carpet.helpers.OptimizedExplosion;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.mixins.ExplosionAccessor;
import me.lntricate.entityvisualizer.IEntityHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

@Mixin(OptimizedExplosion.class)
public class OptimizedExplosionMixin
{
  @Inject(method = "doExplosionA", at = @At("HEAD"))
  private static void onExplosionA(Explosion e, ExplosionLogHelper eLogger, CallbackInfo ci, @Share("level") LocalRef<IEntityHelper> level, @Share("pos") LocalRef<Vec3> pos)
  {
    ExplosionAccessor eAccess = (ExplosionAccessor)e;
    //#if MC >= 11800
    //$$ level.set((IEntityHelper)(ServerLevel)eAccess.getLevel());
    //#else
    level.set((IEntityHelper)(ServerLevel)eAccess.getWorld());
    //#endif
    double x = eAccess.getX();
    double y = eAccess.getY();
    double z = eAccess.getZ();
    pos.set(new Vec3(x, y, z));
  }

  @Inject(method = "doExplosionA", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/MutablePair;setLeft(Ljava/lang/Object;)V"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
  private static void onGetExposure(Explosion e, ExplosionLogHelper eLogger, CallbackInfo ci, ExplosionAccessor eAccess, boolean eventNeeded, float f3, int k1, int l1, int i2, int i1, int j2, int j1, Vec3 vec3d, Entity explodingEntity, int k2, Entity entity, @Share("level") LocalRef<IEntityHelper> level, @Share("pos") LocalRef<Vec3> pos)
  {
    level.get().onExposure(pos.get(), entity);
  }
}
