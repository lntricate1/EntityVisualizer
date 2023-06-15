package me.lntricate.entityvisualizer.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
// import me.lntricate.entityvisualizer.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RenderHandler implements IRenderer, IClientTickHandler
{
  private static RenderHandler INSTANCE = new RenderHandler();
  private static Map<Vec3, Shape> lines = new HashMap<>();
  private static Map<Vec3, Shape> trajectories = new HashMap<>();
  private static Map<Vec3, Shape> cuboids = new HashMap<>();

  public static RenderHandler getInstance()
  {
    return INSTANCE;
  }

  @Override
  public void onRenderWorldLast(PoseStack poseStack, Matrix4f projMatrix)
  {
    Minecraft minecraft = Minecraft.getInstance();
    Vec3 cpos = minecraft.gameRenderer.getMainCamera().getPosition();
    double cx = cpos.x(); double cy = cpos.y(); double cz = cpos.z();

    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderUtils.setupBlend();
    RenderSystem.disableDepthTest();

    // INEFFICIENT but i don't know how else to avoid concurrent access to shapes
    // List<Shape> shapesCopy = List.copyOf(shapes);
    // for(Shape shape : shapesCopy)
      // shape.render(cx, cy, cz);
    for(Shape line : lines.values())
      line.render(cx, cy, cz);
    for(Shape trajectory : trajectories.values())
      trajectory.render(cx, cy, cz);
    for(Shape cuboid : cuboids.values())
      cuboid.render(cx, cy, cz);
  }

  @Override
  public void onClientTick(Minecraft minecraft)
  {
    long time = System.currentTimeMillis();
    Iterator<Shape> ilines = lines.values().iterator();
    while(ilines.hasNext())
    {
      Shape line = ilines.next();
      if(line.removed)
        ilines.remove();
      else
        line.tick(time);
    }
    Iterator<Shape> itrajectories = trajectories.values().iterator();
    while(itrajectories.hasNext())
    {
      Shape trajectory = itrajectories.next();
      if(trajectory.removed)
        itrajectories.remove();
      else
        trajectory.tick(time);
    }
    Iterator<Shape> icuboids = cuboids.values().iterator();
    while(icuboids.hasNext())
    {
      Shape cuboid = icuboids.next();
      if(cuboid.removed)
        icuboids.remove();
      else
        cuboid.tick(time);
    }
  }

  public static void removeStaticShapes()
  {
    for(Shape line : lines.values())
      line.removeStatic();
    for(Shape trajectory : trajectories.values())
      trajectory.removeStatic();
    for(Shape cuboid : cuboids.values())
      cuboid.removeStatic();
  }

  public static void addLine(double x, double y, double z, double X, double Y, double Z, Color4f color, int ticks)
  {
    if(ticks == -1)
      lines.put(new Vec3(x, y, z), new Line(x, y, z, X, Y, Z, color).isStatic());
    else
      lines.put(new Vec3(x, y, z), new Line(x, y, z, X, Y, Z, color).ticks(ticks));
  }

  public static void addTrajectory(double x, double y, double z, double X, double Y, double Z, boolean xFirst, Color4f color, int ticks)
  {
    if(ticks == -1)
      trajectories.put(new Vec3(x, y, z), new Trajectory(x, y, z, X, Y, Z, xFirst, color).isStatic());
    else
      trajectories.put(new Vec3(x, y, z), new Trajectory(x, y, z, X, Y, Z, xFirst, color).ticks(ticks));
  }

  public static void addCuboid(BlockPos pos, Color4f fill, Color4f stroke, int ticks)
  {
    addCuboid(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, fill, stroke, ticks);
  }

  public static void addCuboid(Entity entity, Color4f fill, Color4f stroke, int ticks)
  {
    addCuboid(entity.getX(), entity.getY(), entity.getZ(), entity.getBbWidth()/2, entity.getBbHeight(), fill, stroke, ticks);
  }

  public static void addCuboid(double x, double y, double z, double sizeW, double sizeH, Color4f fill, Color4f stroke, int ticks)
  {
    addCuboid(x - sizeW, y, z - sizeW, x + sizeW, y + sizeH, z + sizeW, fill, stroke, ticks);
  }

  public static void addCuboid(double x, double y, double z, double size, Color4f fill, Color4f stroke, int ticks)
  {
    addCuboid(x - size, y - size, z - size, x + size, y + size, z + size, fill, stroke, ticks);
  }

  public static void addCuboid(double x, double y, double z, double X, double Y, double Z, Color4f fill, Color4f stroke, int ticks)
  {
    if(ticks == -1)
      cuboids.put(new Vec3(x, y, z), new Cuboid(x, y, z, X, Y, Z, stroke, fill).isStatic());
    else
      cuboids.put(new Vec3(x, y, z), new Cuboid(x, y, z, X, Y, Z, stroke, fill).ticks(ticks));
  }

  private static abstract class Shape
  {
    public boolean removed;
    private boolean ticks;
    private boolean isStatic;
    private long removalTime;

    public abstract void render(double cx, double cy, double cz);

    public void remove()
    {
      removed = true;
    }

    public Shape ticks(int ticks)
    {
      this.ticks = true;
      removalTime = System.currentTimeMillis() + 50*ticks;
      return this;
    }

    public Shape isStatic()
    {
      isStatic = true;
      return this;
    }

    public void tick(long time)
    {
      if(ticks && time > removalTime)
        remove();
    }

    public void removeStatic()
    {
      if(isStatic)
        remove();
    }
  }

  private static class Line extends Shape
  {
    private final double x, y, z, X, Y, Z;
    private Color4f color;

    public Line(double x, double y, double z, double X, double Y, double Z, Color4f color)
    {
      this.x = x; this.y = y; this.z = z;
      this.X = X; this.Y = Y; this.Z = Z;
      this.color = color;
    }

    @Override
    public void render(double cx, double cy, double cz)
    {
      Tesselator tessellator = Tesselator.getInstance();
      BufferBuilder buffer = tessellator.getBuilder();
      buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
      buffer.vertex(x-cx, y-cy, z-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(X-cx, Y-cy, Z-cz).color(color.r, color.g, color.b, color.a).endVertex();
      tessellator.end();
    }
  }

  private static class Trajectory extends Shape
  {
    private final Line line1, line2, line3;

    public Trajectory(double x, double y, double z, double X, double Y, double Z, boolean xFirst, Color4f color)
    {
      line1 = new Line(x, y, z, x, Y, z, color);
      if(xFirst)
      {
        line2 = new Line(x, Y, z, X, Y, z, color);
        line3 = new Line(X, Y, z, X, Y, Z, color);
      }
      else
      {
        line2 = new Line(x, Y, z, x, Y, Z, color);
        line3 = new Line(x, Y, Z, X, Y, Z, color);
      }
    }

    @Override
    public void render(double cx, double cy, double cz)
    {
      line1.render(cx, cy, cz);
      line2.render(cx, cy, cz);
      line3.render(cx, cy, cz);
    }
  }

  private static class Quad extends Shape
  {
    private final double x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4;
    private Color4f color;

    public Quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color4f color)
    {
      this.x1 = x1; this.y1 = y1; this.z1 = z1;
      this.x2 = x2; this.y2 = y2; this.z2 = z2;
      this.x3 = x3; this.y3 = y3; this.z3 = z3;
      this.x4 = x4; this.y4 = y4; this.z4 = z4;
      this.color = color;
    }

    @Override
    public void render(double cx, double cy, double cz)
    {
      Tesselator tessellator = Tesselator.getInstance();
      BufferBuilder buffer = tessellator.getBuilder();
      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      buffer.vertex(x1-cx, y1-cy, z1-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x2-cx, y2-cy, z2-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x3-cx, y3-cy, z3-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x4-cx, y4-cy, z4-cz).color(color.r, color.g, color.b, color.a).endVertex();
      tessellator.end();
    }
  }

  private static class Cuboid extends Shape
  {
    private final Set<Shape> shapes;

    public Cuboid(double x, double y, double z, double X, double Y, double Z, Color4f stroke, Color4f fill)
    {
      shapes = new HashSet<>();
      if(fill.a > 0)
      {
        shapes.add(new Quad(x, y, z, x, Y, z, x, Y, Z, x, y, Z, fill));
        shapes.add(new Quad(X, y, z, X, y, Z, X, Y, Z, X, Y, z, fill));

        shapes.add(new Quad(x, y, z, x, y, Z, X, y, Z, X, y, z, fill));
        shapes.add(new Quad(x, Y, z, X, Y, z, X, Y, Z, x, Y, Z, fill));

        shapes.add(new Quad(x, y, z, X, y, z, X, Y, z, x, Y, z, fill));
        shapes.add(new Quad(x, y, Z, x, Y, Z, X, Y, Z, X, y, Z, fill));
      }
      if(stroke.a > 0)
      {
        shapes.add(new Line(x, y, z, X, y, z, stroke));
        shapes.add(new Line(X, y, z, X, Y, z, stroke));
        shapes.add(new Line(X, Y, z, x, Y, z, stroke));
        shapes.add(new Line(x, Y, z, x, y, z, stroke));

        shapes.add(new Line(x, y, z, x, y, Z, stroke));
        shapes.add(new Line(X, y, z, X, y, Z, stroke));
        shapes.add(new Line(X, Y, z, X, Y, Z, stroke));
        shapes.add(new Line(x, Y, z, x, Y, Z, stroke));

        shapes.add(new Line(x, y, Z, X, y, Z, stroke));
        shapes.add(new Line(X, y, Z, X, Y, Z, stroke));
        shapes.add(new Line(X, Y, Z, x, Y, Z, stroke));
        shapes.add(new Line(x, Y, Z, x, y, Z, stroke));
      };
    }

    @Override
    public void render(double cx, double cy, double cz)
    {
      for(Shape shape : shapes)
        shape.render(cx, cy, cz);
    }
  }
}
