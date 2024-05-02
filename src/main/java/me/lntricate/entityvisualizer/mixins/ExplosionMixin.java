package me.lntricate.entityvisualizer.mixins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.lntricate.entityvisualizer.helpers.EntityHelper.Cuboid;
import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Explosion.class)
public class ExplosionMixin
{
  @Shadow @Final private Level level;
  @Shadow @Final private double x;
  @Shadow @Final private double y;
  @Shadow @Final private double z;
  @Unique private final HashSet<Cuboid> exposures = new HashSet<>();

  @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;getSeenPercent(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;)F"), locals = LocalCapture.CAPTURE_FAILHARD)
  private void onGetExposure(CallbackInfo ci, Set<BlockPos> set, int i, float q, int k, int l, int r, int s, int t, int u, List<Entity> list, Vec3 vec3, int v, Entity entity)
  {
    if(level.isClientSide())
      return;

    Vec3 pos = entity.position();
    exposures.add(new Cuboid(entity.getType().toShortString(), pos.x, pos.y, pos.z, entity.getBbWidth(), entity.getBbHeight()));
  }

  @Inject(method = "finalizeExplosion", at = @At("HEAD"))
  private void onFinalizeExplosion(boolean bl, CallbackInfo ci)
  {
    if(level.isClientSide() || exposures.isEmpty())
      return;

    ServerNetworkHandler.sendExposures((ServerLevel)level, x, y, z, exposures);
    exposures.clear();
  }
}
