package me.lntricate.entityvisualizer;

import java.util.HashMap;
import java.util.HashSet;

import me.lntricate.entityvisualizer.helpers.EntityHelper.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IEntityHelper
{
  public void onMove(Vec3 pos1, Vec3 delta, Entity entity, boolean noPhysics, boolean xFirst);
  public void onMove(Vec3 pos1, Vec3 delta, Vec3 collisionA, double cy, Entity entity, boolean xFirst);
  public void onMove(Vec3 pos1, Vec3 delta, Vec3 collisionA, double by, Vec3 collisionC, Entity entity, boolean xFirst);
  public void onAccel(Entity entity, Vec3 vel);
  public void onTick(Entity entity);
  public void onDeath(Entity entity);
  public void onExposure(Vec3 pos, Entity entity);

  public HashSet<Move> moves();
  public HashSet<Move1> move1s();
  public HashSet<Move2> move2s();
  public HashSet<Vel> vels();
  public HashSet<Cuboid> ticks();
  public HashSet<Cuboid> deaths();
  public HashMap<Vec3, HashSet<Cuboid>> exposures();
}
