package me.lntricate.entityvisualizer.helpers;

public class EntityHelper
{
  public static final record Move(String id, double x, double y, double z, double X, double Y, double Z, boolean noPhysics, boolean xFirst){}
  public static final record Vel(String id, double x, double y, double z, double X, double Y, double Z){}
  public static final record Cuboid(String id, double x, double y, double z, float w, float h){}
}
