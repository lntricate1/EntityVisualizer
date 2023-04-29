package me.lntricate.entityvisualizer.helpers;

import java.util.HashMap;
import java.util.Map;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityPositionHelper
{
  private static Map<Integer, Vec3> prevPos = new HashMap<>();

  public static void registerPos(int id, Vec3 pos)
  {
    if(prevPos.containsKey(id))
      prevPos.replace(id, pos);
    else
      prevPos.put(id, pos);
  }

  public static void onEntitySpawn(Entity entity)
  {
    if(Configs.Renderers.ENTITY_CREATION.config.on())
    {
      if(Configs.Lists.ENTITY_CREATION.shouldRender(entity))
        RenderHandler.addCuboid(entity, Configs.Renderers.ENTITY_CREATION.config.color1(), Configs.Renderers.ENTITY_CREATION.config.color2(), Configs.Renderers.ENTITY_CREATION.config.dur());
    }
    registerPos(entity.getId(), entity.position());
  }

  public static void onEntityMove(Entity entity, boolean isSelfMovementType, double x, double y, double z, boolean nocollide, boolean xFirst)
  {
    if(Configs.Renderers.ENTITY_TICKS.config.on())
    {
      if(Configs.Lists.ENTITY_TICKS.shouldRender(entity))
        RenderHandler.addCuboid(x, y, z, entity.getBbWidth()/2, entity.getBbHeight(), Configs.Renderers.ENTITY_TICKS.config.color1(), Configs.Renderers.ENTITY_TICKS.config.color2(), Configs.Renderers.ENTITY_TICKS.config.dur());
    }
    if(Configs.Renderers.ENTITY_TRAJECTORY.config.on())
    {
      Vec3 pos = prevPos.get(entity.getId());
      if(pos != null && Configs.Lists.ENTITY_TRAJECTORY.shouldRender(entity))
      {
        Color4f stroke = isSelfMovementType ? Configs.Renderers.ENTITY_TRAJECTORY.config.color1() : Configs.Renderers.ENTITY_TRAJECTORY.config.color2();
        if(nocollide)
          RenderHandler.addLine(pos.x, pos.y, pos.z, x, y, z, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.dur());
        else
          RenderHandler.addTrajectory(pos.x, pos.y, pos.z, x, y, z, xFirst, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.dur());
      }
    }
    registerPos(entity.getId(), new Vec3(x, y, z));
  }

  public static void onEntityDeath(Entity entity)
  {
    if(Configs.Renderers.ENTITY_DEATHS.config.on())
    {
      Vec3 pos = prevPos.get(entity.getId());
      if(pos != null)
      {
        if(Configs.Lists.ENTITY_DEATHS.shouldRender(entity))
          RenderHandler.addCuboid(pos.x, pos.y, pos.z, entity.getBbWidth()/2, entity.getBbHeight(), Configs.Renderers.ENTITY_DEATHS.config.color1(), Configs.Renderers.ENTITY_DEATHS.config.color2(), Configs.Renderers.ENTITY_DEATHS.config.dur());
      }
    }
    prevPos.remove(entity.getId());
  }
}
