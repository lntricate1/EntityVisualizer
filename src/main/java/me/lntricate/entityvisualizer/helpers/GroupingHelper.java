package me.lntricate.entityvisualizer.helpers;

public class GroupingHelper
{
  private double lastX, lastY, lastZ = 0;
  private long lastTick = 0;
  private int threshold = 10;
  private int count = 1;
  public static GroupingHelper entity = new GroupingHelper();
  public static GroupingHelper explosion = new GroupingHelper();

  public boolean registerPos(double x, double y, double z, long tick)
  {
    if(isNewPos(x, y, z, tick))
    {
      lastX = x; lastY = y; lastZ = z; lastTick = tick;
      count = 1;
      return true;
    }

    count ++;
    return false;
  }

  public boolean isNewPosIncrement(double x, double y, double z, long tick)
  {
    if(isNewPos(x, y, z, tick))
      return true;

    count ++;
    lastTick = tick;
    return false;
  }

  public void registerPosNoCheck(double x, double y, double z, long tick)
  {
    lastX = x; lastY = y; lastZ = z; lastTick = tick;
    count = 1;
  }

  public boolean isNewPos(double x, double y, double z, long tick)
  {
    return x != lastX || y != lastY || z != lastZ || tick - lastTick > threshold;
  }

  public int getCount()
  {
    return count;
  }
}

