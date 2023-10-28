package me.lntricate.entityvisualizer.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.config.Configs.Renderers;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
  private static final Set<Vec3> RAYS = new HashSet<>();
  static
  {
    for(int i = 0; i < 16; ++i)
      for(int j = 0; j < 16; ++j)
        for(int k = 0; k < 16; ++k)
          if(i==0 || i==15 || j==0 || j==15 || k==0 || k==15)
          {
            double dx = (float)i / 15F * 2F - 1F;
            double dy = (float)j / 15F * 2F - 1F;
            double dz = (float)k / 15F * 2F - 1F;
            double mag = Math.sqrt(dx*dx + dy*dy + dz*dz);
            RAYS.add(new Vec3(dx / mag * 0.3f, dy / mag * 0.3f, dz / mag * 0.3f));
          }
  }

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

  public static Set<Pair<BlockPos, BlockState>> getAffectedBlocks(double sx, double sy, double sz, float power, ClientLevel level, float random)
  {
    Set<Pair<BlockPos, BlockState>> positions = new HashSet<>();
    for(Vec3 ray : RAYS)
    {
      Vec3 pos = new Vec3(sx, sy, sz);
      for(float rayStrength = power * (0.7F + random * 0.6F); rayStrength > 0F; rayStrength -= 0.22500001F, pos = pos.add(ray))
      {
        BlockPos blockPos = new BlockPos(pos);
        if(!level.isInWorldBounds(blockPos))
          break;

        BlockState blockState = level.getBlockState(blockPos);
        FluidState fluidState = level.getFluidState(blockPos);
        if(!blockState.isAir() || !fluidState.isEmpty())
        {
          float blastRes = Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance());
          rayStrength -= (blastRes + 0.3F) * 0.3F;
        }

        if(rayStrength > 0)
          positions.add(Pair.of(blockPos, blockState));
      }
    }
    return positions;
  }

  public static Pair<Set<Vec3>, Set<Vec3>> getBlockPoints(double sx, double sy, double sz, float power, ClientLevel level)
  {
    Set<Vec3> outMin = new HashSet<>();
    Set<Vec3> outMax = new HashSet<>();
    Vec3 startPos = new Vec3(sx, sy, sz);
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
        if(!Configs.Lists.EXPLOSION_BLOCK_RAYS.shouldRender(block))
          continue;

        if(rayStrengthMin > 0F)
          outMin.add(rayPos);
        else if(rayStrengthMax > 0F)
          outMax.add(rayPos);
      }
    }
    return Pair.of(outMin, outMax);
  }

  public static void explosion(double x, double y, double z)
  {
    if(Renderers.EXPLOSIONS.config.on())
    {
      RenderHandler.addCuboid(x, y, z, Configs.Generic.EXPLOSION_BOX_SIZE.getDoubleValue()/2, Renderers.EXPLOSIONS.config.color1(), Renderers.EXPLOSIONS.config.color2(), Renderers.EXPLOSIONS.config.dur());
      RenderHandler.addText(x, y, z, Component.Serializer.fromJsonLenient("\"BALLS\""), new Color4f(0.5f, 0.5f, 0.5f, 0.5f), 100);
    }
  }

  public static void explosionEntityRays(double x, double y, double z, ClientLevel level, float power)
  {
    if(Renderers.EXPLOSION_ENTITY_RAYS.config.on())
    {
      List<Entity> entities = level.getEntities(null, new AABB(
        x - power*2 - 1, y - power*2 - 1, z - power*2 - 1,
        x + power*2 + 1, y + power*2 + 1, z + power*2 + 1));
      for(Entity entity : entities)
        if(Configs.Lists.EXPLOSION_ENTITY_RAYS.shouldRender(entity.getType()))
          for(Pair<Vec3, Boolean> ray : getExposurePoints(x, y, z, power, entity))
          {
            Vec3 pos = ray.getLeft();
            RenderHandler.addLine(
              pos.x(), pos.y(), pos.z(),
              x, y, z,
              ray.getRight() ? Renderers.EXPLOSION_ENTITY_RAYS.config.color1() : Renderers.EXPLOSION_ENTITY_RAYS.config.color2(),
              Renderers.EXPLOSION_ENTITY_RAYS.config.dur());
          }
    }
  }

  public static void explosionBlockRays(double x, double y, double z, ClientLevel level, float power)
  {
    if(Renderers.EXPLOSION_BLOCK_RAYS.config.on())
    {
      Pair<Set<Vec3>, Set<Vec3>> points = getBlockPoints(x, y, z, power, level);
      for(Vec3 min : points.getLeft())
        RenderHandler.addPoint(min.x, min.y, min.z, Renderers.EXPLOSION_BLOCK_RAYS.config.color1(), Renderers.EXPLOSION_BLOCK_RAYS.config.dur());
      for(Vec3 max : points.getRight())
        RenderHandler.addPoint(max.x, max.y, max.z, Renderers.EXPLOSION_BLOCK_RAYS.config.color2(), Renderers.EXPLOSION_BLOCK_RAYS.config.dur());
    }
  }

  public static void explosionMinBlocks(double x, double y, double z, ClientLevel level, float power)
  {
    if(Renderers.EXPLOSION_MIN_BLOCKS.config.on())
      for(Pair<BlockPos, BlockState> pair : getAffectedBlocks(x, y, z, power, level, 0F))
        if(Configs.Lists.EXPLOSION_MIN_BLOCKS.shouldRender(pair.getRight().getBlock()))
          RenderHandler.addCuboid(pair.getLeft(), Renderers.EXPLOSION_MIN_BLOCKS.config.color1(), Renderers.EXPLOSION_MIN_BLOCKS.config.color2(), Renderers.EXPLOSION_MIN_BLOCKS.config.dur());
  }

  public static void explosionMaxBlocks(double x, double y, double z, ClientLevel level, float power)
  {
    if(Renderers.EXPLOSION_MAX_BLOCKS.config.on())
      for(Pair<BlockPos, BlockState> pair : getAffectedBlocks(x, y, z, power, level, 1F))
        if(Configs.Lists.EXPLOSION_MAX_BLOCKS.shouldRender(pair.getRight().getBlock()))
          RenderHandler.addCuboid(pair.getLeft(), Renderers.EXPLOSION_MAX_BLOCKS.config.color1(), Renderers.EXPLOSION_MAX_BLOCKS.config.color2(), Renderers.EXPLOSION_MAX_BLOCKS.config.dur());
  }

  public static void explosionAffectedBlocks(double x, double y, double z, ClientLevel level, List<BlockPos> toBlow)
  {
    if(Renderers.EXPLOSION_AFFECTED_BLOCKS.config.on())
      for(BlockPos pos : toBlow)
        if(Configs.Lists.EXPLOSION_AFFECTED_BLOCKS.shouldRender(level.getBlockState(pos).getBlock()))
          RenderHandler.addCuboid(pos, Renderers.EXPLOSION_AFFECTED_BLOCKS.config.color1(), Renderers.EXPLOSION_AFFECTED_BLOCKS.config.color2(), Renderers.EXPLOSION_AFFECTED_BLOCKS.config.dur());
  }
}
