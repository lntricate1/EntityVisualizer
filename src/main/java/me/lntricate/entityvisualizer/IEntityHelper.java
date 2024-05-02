package me.lntricate.entityvisualizer;

import java.util.HashSet;

import me.lntricate.entityvisualizer.helpers.EntityHelper.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IEntityHelper
{
  public void onMove(Vec3 pos1, Vec3 pos2, Entity entity, boolean noPhysics, boolean xFirst);
  public void onAccel(Entity entity, Vec3 vel);
  public void onTick(Entity entity);
  public void onDeath(Entity entity);

  public HashSet<Move> moves();
  public HashSet<Vel> vels();
  public HashSet<Cuboid> ticks();
  public HashSet<Cuboid> deaths();
}
