package me.lntricate.entityvisualizer.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ExplosionHelper
{
  public static List<Pair<Vec3, Boolean>> getExposurePoints(double sx, double sy, double sz, float power, Entity entity)
  {
    ArrayList<Pair<Vec3, Boolean>> points = new ArrayList<>();
    if(entity.distanceToSqr(sx, sy, sz) > power*power)
      return points;

    AABB aabb = entity.getBoundingBox();
    double ax = 1D / ((aabb.maxX - aabb.minX) * 2D + 1D);
    double ay = 1D / ((aabb.maxY - aabb.minY) * 2D + 1D);
    double az = 1D / ((aabb.maxZ - aabb.minZ) * 2D + 1D);
    double bx = (1D - Math.floor(1D / ax) * ax) / 2D;
    double bz = (1D - Math.floor(1D / az) * az) / 2D;
    for(float x = 0; x <= 1; x += ax)
      for(float y = 0; y <= 1; y += ay)
        for(float z = 0; z <= 1; z += az)
    {
      double lx = Mth.lerp(x, aabb.minX, aabb.maxX);
      double ly = Mth.lerp(y, aabb.minY, aabb.maxY);
      double lz = Mth.lerp(z, aabb.minZ, aabb.maxZ);
      Vec3 vec3 = new Vec3(lx + bx, ly, lz + bz);
      boolean hit = entity.level.clip(new ClipContext(vec3, new Vec3(sx, sy, sz), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
      points.add(Pair.of(vec3, hit));
    }
    return points;
  }

  public static Set<Pair<BlockPos, BlockState>> getAffectedBlocks(double sx, double sy, double sz, float power, Level level, float random/* , float blastRes */)
  {
    Set<Pair<BlockPos, BlockState>> positions = new HashSet<>();
    for(int i = 0; i < 16; ++i)
      for(int j = 0; j < 16; ++j)
        for(int k = 0; k < 16; ++k)
          if(i==0 || i==15 || j==0 || j==15 || k==0 || k==15)
    {
      double dx = (float)i / 15F * 2F - 1F;
      double dy = (float)j / 15F * 2F - 1F;
      double dz = (float)k / 15F * 2F - 1F;
      double mag = Math.sqrt(dx*dx + dy*dy + dz*dz);
      dx = dx / mag * 0.3F;
      dy = dy / mag * 0.3F;
      dz = dz / mag * 0.3F;
      double x = sx;
      double y = sy;
      double z = sz;
      for(float h = power * (0.7F + random * 0.6F); h > 0F; h -= 0.22500001F)
      {
        BlockPos blockPos = new BlockPos(x, y, z);
        if(!level.isInWorldBounds(blockPos)) break;
        BlockState blockState = level.getBlockState(blockPos);
        FluidState fluidState = level.getFluidState(blockPos);
        Optional<Float> optional = blockState.isAir() && fluidState.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance()));
        if(optional.isPresent())
        {
          h -= (optional.get() + 0.3F) * 0.3F;
        }
        if(h > 0)
          positions.add(Pair.of(blockPos, blockState));
        x += dx;
        y += dy;
        z += dz;
      }
    }
    return positions;
  }

  public static Set<Vec3> getBlockRays(double sx, double sy, double sz, float power)
  {
    Set<Vec3> positions = new HashSet<>();
    for(int i = 0; i < 16; ++i)
      for(int j = 0; j < 16; ++j)
        for(int k = 0; k < 16; ++k)
          if(i==0 || i==15 || j==0 || j==15 || k==0 || k==15)
    {
      double dx = (float)i / 15F * 2F - 1F;
      double dy = (float)j / 15F * 2F - 1F;
      double dz = (float)k / 15F * 2F - 1F;
      double mag = Math.sqrt(dx*dx + dy*dy + dz*dz);
      int length = (int)Math.ceil(power * (0.7F + 0.6F) / 0.22500001F);
      positions.add(new Vec3(sx + dx / mag * 0.3F * length, sy + dy / mag * 0.3F * length, sz + dz / mag * 0.3F * length));
    }
    return positions;
  }
}
