package me.lntricate.entityvisualizer.helpers;

import java.util.HashMap;
import java.util.List;
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
    if(Configs.Renderers.ENTITY_CREATION.config.getBooleanValue())
    {
      // MAKE CUSTOMIZABLE
      Color4f stroke = new Color4f(0F, 0F, 0F, 1F);
      Color4f fill = new Color4f(0F, 1F, 0F, 0.26F);
      if(Configs.Lists.ENTITY_CREATION.shouldRender(entity))
        RenderHandler.addCuboid(entity, stroke, fill, Configs.Renderers.ENTITY_CREATION.config.getIntegerValue());
    }
    registerPos(entity.getId(), entity.position());
  }

  public static void onEntityMove(Entity entity, boolean isSelfMovementType, double x, double y, double z, boolean nocollide, boolean xFirst)
  {
    if(Configs.Renderers.ENTITY_TICKS.config.getBooleanValue())
    {
      Color4f stroke = new Color4f(0F, 0F, 0F, 1F);
      Color4f fill = new Color4f(0F, 0F, 1F, 0.26F);
      if(Configs.Lists.ENTITY_TICKS.shouldRender(entity))
        RenderHandler.addCuboid(x, y, z, entity.getBbWidth()/2, entity.getBbHeight(), stroke, fill, Configs.Renderers.ENTITY_TICKS.config.getIntegerValue());
    }
    if(Configs.Renderers.ENTITY_TRAJECTORY.config.getBooleanValue())
    {
      Vec3 pos = prevPos.get(entity.getId());
      if(pos != null)
      {
        Color4f stroke = isSelfMovementType ? new Color4f(0F, 0F, 1F, 1F) : new Color4f(0F, 1F, 0F, 1F);
        if(Configs.Lists.ENTITY_TRAJECTORY.shouldRender(entity))
        {
          if(nocollide)
            RenderHandler.addLine(pos.x, pos.y, pos.z, x, y, z, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.getIntegerValue());
          else
            RenderHandler.addTrajectory(pos.x, pos.y, pos.z, x, y, z, xFirst, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.getIntegerValue());
        }
      }
    }
    registerPos(entity.getId(), new Vec3(x, y, z));
  }

  public static void onEntityDeath(Entity entity)
  {
    if(Configs.Renderers.ENTITY_DEATHS.config.getBooleanValue())
    {
      Vec3 pos = prevPos.get(entity.getId());
      if(pos != null)
      {
        Color4f stroke = new Color4f(0F, 0F, 0F, 1F);
        Color4f fill = new Color4f(1F, 0F, 0F, 0.26F);
        if(Configs.Lists.ENTITY_DEATHS.shouldRender(entity))
          RenderHandler.addCuboid(pos.x, pos.y, pos.z, entity.getBbWidth()/2, entity.getBbHeight(), stroke, fill, Configs.Renderers.ENTITY_DEATHS.config.getIntegerValue());
      }
    }
    prevPos.remove(entity.getId());
  }
}
