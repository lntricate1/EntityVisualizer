package me.lntricate.entityvisualizer.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs.Lists;
import me.lntricate.entityvisualizer.config.Configs.Renderers;
import me.lntricate.entityvisualizer.event.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class EntityHelper
{
  public static record EntityData(String type, float w, float h){};
  public static record Pos(double x, double y, double z){};
  private static final Minecraft mc = Minecraft.getInstance();
  private static final Map<Integer, Vec3> prevPos = new HashMap<>();
  private static final Map<Integer, EntityData> datas = new HashMap<>();
  private static final Set<Vec3> memPos = new HashSet<>();
  private static long memTick = 0L;

  @Nullable
  public static Vec3 pos(int id)
  {
    if(prevPos.containsKey(id))
      return prevPos.get(id);

    Entity entity = mc.level.getEntity(id);
    return entity == null ? null : entity.position();
  }

  public static Vec3 pos(Entity entity)
  {
    int id = entity.getId();
    return prevPos.containsKey(id) ? prevPos.get(id) : entity.position();
  }

  public static EntityData data(int id)
  {
    if(datas.containsKey(id))
      return datas.get(id);

    Entity entity = mc.level.getEntity(id);
    if(entity == null)
      return new EntityData("UNKNOWN", 0.98f, 0.98f);

    EntityType<?> type = entity.getType();
    return new EntityData(type.toShortString(), type.getWidth(), type.getHeight());
  }

  public static void tick()
  {
    if(mc.level == null)
      return;

    long tick = mc.level.getGameTime();
    if(tick == memTick)
      return;

    memTick = mc.level.getGameTime();
    memPos.clear();
  }

  public static void registerAdd(Entity entity)
  {
    int id = entity.getId();
    Vec3 pos = entity.position();
    prevPos.put(id, pos);
    EntityType<?> type = entity.getType();
    EntityData data = new EntityData(type.toShortString(), type.getWidth(), type.getHeight());
    datas.put(id, data);
    if(memPos.contains(pos))
      return;

    if(Renderers.ENTITY_CREATION.config.on() && Lists.ENTITY_CREATION.shouldRender(data.type))
      creation(data.w, data.h, pos.x, pos.y, pos.z);

    if(Renderers.ENTITY_VELOCITY.config.on() && Lists.ENTITY_VELOCITY.shouldRender(data.type))
    {
      Vec3 v = entity.getDeltaMovement();
      velocity(pos.x, pos.y, pos.z, v.x, v.y, v.z);
    }

    memPos.add(pos);
  }

  public static void registerTick(int id, boolean self, double x, double y, double z, double mx, double my, double mz, boolean xFirst, boolean noCollide)
  {
    Vec3 pos = new Vec3(x, y, z);
    if(memPos.contains(pos))
      return;

    EntityData data = data(id);

    if(Renderers.ENTITY_TICKS.config.on())
      if(Lists.ENTITY_TICKS.shouldRender(data.type))
        ticks(data.w, data.h, x, y, z);

    if(Renderers.ENTITY_TRAJECTORY.config.on() && prevPos.containsKey(id))
      if(Lists.ENTITY_TRAJECTORY.shouldRender(data.type))
      {
        Vec3 prev = prevPos.get(id);
        trajectory(x, y, z, prev.x, prev.y, prev.z, self, xFirst, noCollide);
      }

    if(Renderers.ENTITY_VELOCITY.config.on())
      if(Lists.ENTITY_VELOCITY.shouldRender(data.type))
        velocity(x, y, z, mx, my, mz);

    memPos.add(pos);
    prevPos.put(id, pos);
  }

  public static void registerDeath(int id)
  {
    Vec3 pos = pos(id);
    prevPos.remove(id);
    if(pos == null || memPos.contains(pos))
    {
      datas.remove(id);
      return;
    }

    EntityData data = data(id);
    datas.remove(id);

    if(Renderers.ENTITY_DEATHS.config.on())
      if(Lists.ENTITY_DEATHS.shouldRender(data.type))
        deaths(data.w, data.h, pos.x, pos.y, pos.z);

    memPos.add(pos);
    prevPos.remove(id);
  }

  private static void creation(float w, float h, double x, double y, double z)
  {
    RenderHandler.addCuboid(x, y, z, w/2, h, Renderers.ENTITY_CREATION.config.color1(), Renderers.ENTITY_CREATION.config.color2(), Renderers.ENTITY_CREATION.config.dur());
  }

  private static void velocity(double x, double y, double z, double mx, double my, double mz)
  {
    RenderHandler.addLine(x, y, z, x + mx, y + my, z + mz, Renderers.ENTITY_VELOCITY.config.color1(), Renderers.ENTITY_VELOCITY.config.dur());
  }

  private static void ticks(float w, float h, double x, double y, double z)
  {
    RenderHandler.addCuboid(x, y, z, w/2, h, Renderers.ENTITY_TICKS.config.color1(), Renderers.ENTITY_TICKS.config.color2(), Renderers.ENTITY_TICKS.config.dur());
  }

  private static void trajectory(double x, double y, double z, double X, double Y, double Z, boolean self, boolean xFirst, boolean noCollide)
  {
    Color4f stroke = self ? Renderers.ENTITY_TRAJECTORY.config.color1() : Renderers.ENTITY_TRAJECTORY.config.color2();
    if(noCollide || !self)
      RenderHandler.addLine(x, y, z, X, Y, Z, stroke, Renderers.ENTITY_TRAJECTORY.config.dur());
    else
      RenderHandler.addTrajectory(x, y, z, X, Y, Z, xFirst, stroke, Renderers.ENTITY_TRAJECTORY.config.dur());
  }

  public static void deaths(float w, float h, double x, double y, double z)
  {
    RenderHandler.addCuboid(x, y, z, w/2, h, Renderers.ENTITY_DEATHS.config.color1(), Renderers.ENTITY_DEATHS.config.color2(), Renderers.ENTITY_DEATHS.config.dur());
  }
}
