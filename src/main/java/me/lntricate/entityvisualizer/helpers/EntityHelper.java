package me.lntricate.entityvisualizer.helpers;

public class EntityHelper
{
  // Normal movement
  public static final record Move(String id, double x, double y, double z, double dx, double dy, double dz, boolean noPhysics, boolean xFirst){}
  // Step up, second check does not collide up
  public static final record Move1(String id, double x, double y, double z, double dx, double dy, double dz,
    double ax, double ay, double az, double cy, boolean xFirst){}
  // Step up, full
  // x, y, z start pos
  // dx, dy, dz first collision
  // ax, ay, az second collision
  // by third collision
  // cx, cy, cz final collision.
  public static final record Move2(String id, double x, double y, double z, double dx, double dy, double dz,
    double ax, double ay, double az, double by, double cx, double cy, double cz, boolean xFirst){}
  public static final record Vel(String id, double x, double y, double z, double X, double Y, double Z){}
  public static final record Cuboid(String id, double x, double y, double z, float w, float h){}
}
