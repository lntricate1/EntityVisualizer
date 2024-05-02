package me.lntricate.entityvisualizer.mixins.carpet;

import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import carpet.helpers.OptimizedExplosion;
import carpet.logging.logHelpers.ExplosionLogHelper;
import carpet.mixins.ExplosionAccessor;
import me.lntricate.entityvisualizer.helpers.EntityHelper.Cuboid;
import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

@Mixin(OptimizedExplosion.class)
public class OptimizedExplosionMixin
{
  @Unique private static final HashSet<Cuboid> exposures = new HashSet<>();

  @Inject(method = "doExplosionA", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/MutablePair;setLeft(Ljava/lang/Object;)V"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
  private static void onGetExposure(Explosion e, ExplosionLogHelper eLogger, CallbackInfo ci, ExplosionAccessor eAccess, boolean eventNeeded, float f3, int k1, int l1, int i2, int i1, int j2, int j1, Vec3 vec3d, Entity explodingEntity, int k2, Entity entity)
  {
    Vec3 pos = entity.position();
    exposures.add(new Cuboid(entity.getType().toShortString(), pos.x, pos.y, pos.z, entity.getBbWidth(), entity.getBbHeight()));
  }

  @Inject(method = "doExplosionB", at = @At("HEAD"), remap = false)
  private static void onFinalizeExplosion(Explosion e, boolean spawnParticles, CallbackInfo ci)
  {
    if(exposures.isEmpty())
      return;

    ExplosionAccessor eAccess = (ExplosionAccessor)e;
    //#if MC >= 11800
    //$$ ServerLevel level = (ServerLevel)eAccess.getLevel();
    //#else
    ServerLevel level = (ServerLevel)eAccess.getWorld();
    //#endif
    double x = eAccess.getX();
    double y = eAccess.getY();
    double z = eAccess.getZ();
    ServerNetworkHandler.sendExposures(level, x, y, z, exposures);
    exposures.clear();
  }
}
