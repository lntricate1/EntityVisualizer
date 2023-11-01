package me.lntricate.entityvisualizer.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableSet;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.config.Configs.Renderers;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ExplosionHelper
{
  private static final ImmutableSet<Vec3> RAYS;
  static
  {
    ImmutableSet.Builder<Vec3> builder = ImmutableSet.builder();
    for(int i = 0; i < 16; ++i)
      for(int j = 0; j < 16; ++j)
        for(int k = 0; k < 16; ++k)
          if(i==0 || i==15 || j==0 || j==15 || k==0 || k==15)
          {
            double dx = (float)i / 15F * 2F - 1F;
            double dy = (float)j / 15F * 2F - 1F;
            double dz = (float)k / 15F * 2F - 1F;
            double mag = Math.sqrt(dx*dx + dy*dy + dz*dz);
            builder.add(new Vec3(dx / mag * 0.3f, dy / mag * 0.3f, dz / mag * 0.3f));
          }
    RAYS = builder.build();
  }

  public static Map<Vec3, Boolean> getExposurePoints(double sx, double sy, double sz, float power, Entity entity)
  {
    Map<Vec3, Boolean> points = new HashMap<>();
    if(entity.distanceToSqr(sx, sy, sz) > power*power)
      return points;

    Vec3 pos = new Vec3(sx, sy, sz);
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
      boolean hit = entity.level.clip(new ClipContext(vec3, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
      points.put(vec3, hit);
    }
    return points;
  }

  public static Pair<Set<BlockPos>, Set<BlockPos>> getAffectedBlocks(Vec3 startPos, float power, ClientLevel level)
  {
    Set<BlockPos> outMin = new HashSet<>();
    Set<BlockPos> outMax = new HashSet<>();
    for(Vec3 ray : RAYS)
    {
      Vec3 rayPos = startPos.add(ray);
      for(float rayStrengthMin = power * 0.7F, rayStrengthMax = power * 1.3F;
        rayStrengthMax > 0F; rayStrengthMin -= 0.22500001F, rayStrengthMax -= 0.22500001F, rayPos = rayPos.add(ray))
      {
        BlockPos pos = new BlockPos(rayPos);
        if(!level.isInWorldBounds(pos))
          break;

        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        Block block = blockState.getBlock();
        if(!blockState.isAir() || !fluidState.isEmpty())
        {
          float blastRes = (Math.max(block.getExplosionResistance(), fluidState.getExplosionResistance()) + 0.3F) * 0.3F;
          rayStrengthMin -= blastRes;
          rayStrengthMax -= blastRes;
        }

        if(rayStrengthMax <= 0F || outMin.contains(pos) || !Configs.Lists.EXPLOSION_AFFECTED_BLOCKS.shouldRender(block))
          continue;

        if(rayStrengthMin > 0F)
          outMin.add(pos);
        else
          outMax.add(pos);
      }
    }
    outMax.removeAll(outMin);
    return Pair.of(outMin, outMax);
  }

  public static Pair<Set<Vec3>, Set<Vec3>> getBlockPoints(Vec3 startPos, float power, ClientLevel level)
  {
    Set<Vec3> outMin = new HashSet<>();
    Set<Vec3> outMax = new HashSet<>();
    for(Vec3 ray : RAYS)
    {
      Vec3 rayPos = startPos.add(ray);
      float rayStrengthMin = power * 0.7F;
      float rayStrengthMax = power * 1.3F;
      for(; rayStrengthMax > 0F; rayStrengthMin -= 0.22500001F, rayStrengthMax -= 0.22500001F, rayPos = rayPos.add(ray))
      {
        BlockPos pos = new BlockPos(rayPos);
        if(!level.isInWorldBounds(pos))
          break;
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        Block block = blockState.getBlock();
        if(!blockState.isAir() || !fluidState.isEmpty())
        {
          float blastRes = (Math.max(block.getExplosionResistance(), fluidState.getExplosionResistance()) + 0.3F) * 0.3F;
          rayStrengthMin -= blastRes;
          rayStrengthMax -= blastRes;
        }

        if(rayStrengthMax <= 0F || !Configs.Lists.EXPLOSION_BLOCK_RAYS.shouldRender(block))
          continue;

        if(rayStrengthMin > 0F)
          outMin.add(rayPos);
        else
          outMax.add(rayPos);
      }
    }
    return Pair.of(outMin, outMax);
  }

  public static void explosion(double x, double y, double z)
  {
    RenderHandler.addCuboid(x, y, z, Configs.Generic.EXPLOSION_BOX_SIZE.getDoubleValue()/2, Renderers.EXPLOSIONS.config.color1(), Renderers.EXPLOSIONS.config.color2(), Renderers.EXPLOSIONS.config.dur());
  }

  public static void explosionEntityRays(double x, double y, double z, ClientLevel level, float power)
  {
    List<Entity> entities = level.getEntities(null, new AABB(
      x - power*2 - 1, y - power*2 - 1, z - power*2 - 1,
      x + power*2 + 1, y + power*2 + 1, z + power*2 + 1));
    for(Entity entity : entities)
      if(Configs.Lists.EXPLOSION_ENTITY_RAYS.shouldRender(entity.getType()))
        for(Map.Entry<Vec3, Boolean> ray : getExposurePoints(x, y, z, power, entity).entrySet())
        {
          Vec3 pos = ray.getKey();
          RenderHandler.addLine(
            pos.x(), pos.y(), pos.z(),
            x, y, z,
            ray.getValue() ? Renderers.EXPLOSION_ENTITY_RAYS.config.color1() : Renderers.EXPLOSION_ENTITY_RAYS.config.color2(),
            Renderers.EXPLOSION_ENTITY_RAYS.config.dur());
        }
  }

  public static void explosionBlockRays(Vec3 pos, ClientLevel level, float power)
  {
    Pair<Set<Vec3>, Set<Vec3>> points = getBlockPoints(pos, power, level);
    for(Vec3 min : points.getLeft())
      RenderHandler.addPoint(min.x, min.y, min.z, Renderers.EXPLOSION_BLOCK_RAYS.config.color1(), Renderers.EXPLOSION_BLOCK_RAYS.config.dur());
    for(Vec3 max : points.getRight())
      RenderHandler.addPoint(max.x, max.y, max.z, Renderers.EXPLOSION_BLOCK_RAYS.config.color2(), Renderers.EXPLOSION_BLOCK_RAYS.config.dur());
  }

  public static void explosionAffectedBlocks(Vec3 pos, ClientLevel level, float power)
  {
    Pair<Set<BlockPos>, Set<BlockPos>> blocks = getAffectedBlocks(pos, power, level);
    Color4f stroke = new Color4f(0, 0, 0, 0);
    for(BlockPos min : blocks.getLeft())
      RenderHandler.addCuboid(min, Renderers.EXPLOSION_AFFECTED_BLOCKS.config.color1(), stroke, Renderers.EXPLOSION_AFFECTED_BLOCKS.config.dur());
    for(BlockPos max : blocks.getRight())
      RenderHandler.addCuboid(max, Renderers.EXPLOSION_AFFECTED_BLOCKS.config.color2(), stroke, Renderers.EXPLOSION_AFFECTED_BLOCKS.config.dur());
  }
}
