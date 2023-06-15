package me.lntricate.entityvisualizer.helpers;

import java.util.HashMap;
import java.util.Map;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

  private static void creation(EntityType<?> type, double x, double y, double z)
  {
    if(Configs.Renderers.ENTITY_CREATION.config.on() && Configs.Lists.ENTITY_CREATION.shouldRender(type))
        RenderHandler.addCuboid(x, y, z, type.getWidth()/2, type.getHeight(), Configs.Renderers.ENTITY_CREATION.config.color1(), Configs.Renderers.ENTITY_CREATION.config.color2(), Configs.Renderers.ENTITY_CREATION.config.dur());
  }

  private static void velocity(EntityType<?> type, double x, double y, double z, double mx, double my, double mz)
  {
    if(Configs.Renderers.ENTITY_VELOCITY.config.on() && Configs.Lists.ENTITY_VELOCITY.shouldRender(type))
        RenderHandler.addLine(x, y, z, x + mx, y + my, z + mz, Configs.Renderers.ENTITY_VELOCITY.config.color1(), Configs.Renderers.ENTITY_VELOCITY.config.dur());
  }

  private static void ticks(EntityType<?> type, double x, double y, double z)
  {
    if(Configs.Renderers.ENTITY_TICKS.config.on() && Configs.Lists.ENTITY_TICKS.shouldRender(type))
        RenderHandler.addCuboid(x, y, z, type.getWidth()/2, type.getHeight(), Configs.Renderers.ENTITY_TICKS.config.color1(), Configs.Renderers.ENTITY_TICKS.config.color2(), Configs.Renderers.ENTITY_TICKS.config.dur());
  }

  private static void trajectory(EntityType<?> type, double x, double y, double z, double X, double Y, double Z, boolean self, boolean xFirst, boolean noCollide)
  {
    if(Configs.Renderers.ENTITY_TRAJECTORY.config.on() && Configs.Lists.ENTITY_TRAJECTORY.shouldRender(type))
    {
      Color4f stroke = self ? Configs.Renderers.ENTITY_TRAJECTORY.config.color1() : Configs.Renderers.ENTITY_TRAJECTORY.config.color2();
      if(noCollide)
        RenderHandler.addLine(x, y, z, X, Y, Z, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.dur());
      else
        RenderHandler.addTrajectory(x, y, z, X, Y, Z, xFirst, stroke, Configs.Renderers.ENTITY_TRAJECTORY.config.dur());
    }
  }

  public static void deaths(EntityType<?> type, double x, double y, double z)
  {
    if(Configs.Renderers.ENTITY_DEATHS.config.on() && Configs.Lists.ENTITY_DEATHS.shouldRender(type))
    {
      RenderHandler.addCuboid(x, y, z, type.getWidth()/2, type.getHeight(), Configs.Renderers.ENTITY_DEATHS.config.color1(), Configs.Renderers.ENTITY_DEATHS.config.color2(), Configs.Renderers.ENTITY_DEATHS.config.dur());
    }
  }

  public static void onEntitySpawn(Entity entity)
  {
    EntityType<?> type = entity.getType();

    creation(type, entity.getX(), entity.getY(), entity.getZ());
    velocity(type, entity.getX(), entity.getY(), entity.getZ(), entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z);

    if(Configs.Renderers.requireEntityPackets())
      registerPos(entity.getId(), entity.position());
    registerType(entity.getId(), type);
  }

  public static void onEntityMove(int id, boolean isSelfMovementType, double x, double y, double z, double mx, double my, double mz, boolean xFirst, boolean noCollide)
  {
    EntityType<?> type = types.get(id);
    if(type == null)
    {
      Minecraft mc = Minecraft.getInstance();
      Entity entity = mc.level.getEntity(id);
      if(entity == null)
      {
        registerPos(id, new Vec3(x, y, z));
        return;
      }
      type = entity.getType();
    }
    Vec3 pos = prevPos.get(id);

    ticks(type, x, y, z);
    if(pos != null)
    {
      velocity(type, pos.x, pos.y, pos.z, mx, my, mz);
      trajectory(type, pos.x, pos.y, pos.z, x, y, z, isSelfMovementType, xFirst, noCollide);
    }

    registerPos(id, new Vec3(x, y, z));
  }

  public static void onEntityDeath(int id)
  {
    Vec3 pos = prevPos.get(id);
    if(pos != null)
    {
      EntityType<?> type = types.get(id);
      if(type != null)
        deaths(type, pos.x, pos.y, pos.z);
    }
    prevPos.remove(id);
    types.remove(id);
  }
}
