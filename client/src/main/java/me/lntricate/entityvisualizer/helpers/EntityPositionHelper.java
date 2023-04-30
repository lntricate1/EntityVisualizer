package me.lntricate.entityvisualizer.helpers;

import java.util.HashMap;
import java.util.Map;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class EntityPositionHelper
{
  private static Map<Integer, Vec3> prevPos = new HashMap<>();
  private static Map<Integer, EntityType<?>> types = new HashMap<>();

  public static void registerPos(int id, Vec3 pos)
  {
    if(prevPos.containsKey(id))
      prevPos.replace(id, pos);
    else
      prevPos.put(id, pos);
  }

  public static void registerType(int id, EntityType<?> type)
  {
    if(types.containsKey(id))
      types.replace(id, type);
    else
      types.put(id, type);
  }

  public static void onEntitySpawn(Entity entity)
  {
    EntityType<?> type = entity.getType();
    if(Configs.Renderers.ENTITY_CREATION.config.on())
      if(Configs.Lists.ENTITY_CREATION.shouldRender(type))
        RenderHandler.addCuboid(entity, Configs.Renderers.ENTITY_CREATION.config.color1(), Configs.Renderers.ENTITY_CREATION.config.color2(), Configs.Renderers.ENTITY_CREATION.config.dur());
    registerPos(entity.getId(), entity.position());
    registerType(entity.getId(), type);
  }

  public static void onEntityMove(int id, boolean isSelfMovementType, double x, double y, double z, boolean xFirst, boolean nocollide)
  {
    EntityType<?> type = types.get(id);
    if(type == null)
    {
      Minecraft mc = Minecraft.getInstance();
      Entity entity = mc.level.getEntity(id);
      if(entity == null)
        return;
      type = entity.getType();
    }

    if(Configs.Renderers.ENTITY_TICKS.config.on())
      if(Configs.Lists.ENTITY_TICKS.shouldRender(type))
        RenderHandler.addCuboid(x, y, z, type.getWidth()/2, type.getHeight(), Configs.Renderers.ENTITY_TICKS.config.color1(), Configs.Renderers.ENTITY_TICKS.config.color2(), Configs.Renderers.ENTITY_TICKS.config.dur());

    if(Configs.Renderers.ENTITY_TRAJECTORY.config.on())
      if(Configs.Lists.ENTITY_TRAJECTORY.shouldRender(type))
      {
        Vec3 pos = prevPos.get(id);
        Color4f stroke = isSelfMovementType ? Configs.Renderers.ENTITY_TRAJECTORY.config.color1() : Configs.Renderers.ENTITY_TRAJECTORY.config.color2();
        if(nocollide)
          RenderHandler.addLine(pos.x, pos.y, pos.z, x, y, z, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.dur());
        else
          RenderHandler.addTrajectory(pos.x, pos.y, pos.z, x, y, z, xFirst, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.dur());
      }
    registerPos(id, new Vec3(x, y, z));
  }

  public static void onEntityDeath(int id)
  {
    if(Configs.Renderers.ENTITY_DEATHS.config.on())
    {
      EntityType<?> type = types.get(id);
      if(type != null)
      {
        Vec3 pos = prevPos.get(id);
        if(Configs.Lists.ENTITY_DEATHS.shouldRender(type))
          RenderHandler.addCuboid(pos.x, pos.y, pos.z, type.getWidth()/2, type.getHeight(), Configs.Renderers.ENTITY_DEATHS.config.color1(), Configs.Renderers.ENTITY_DEATHS.config.color2(), Configs.Renderers.ENTITY_DEATHS.config.dur());
      }
    }
    prevPos.remove(id);
    types.remove(id);
  }
}
