package me.lntricate.entityvisualizer.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;

public class RenderHandler implements IRenderer, IClientTickHandler
{
  private static RenderHandler INSTANCE = new RenderHandler();
  private static List<Shape> shapes = new ArrayList<>();

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
    RenderSystem.disableDepthTest();

    // INEFFICIENT but i don't know how else to avoid concurrent access to shapes
    List<Shape> shapesCopy = List.copyOf(shapes);
    for(Shape shape : shapesCopy)
    {
      shape.render(cx, cy, cz);
    }
  }

  @Override
  public void onClientTick(Minecraft minecraft)
  {
    long time = System.currentTimeMillis();
    Iterator<Shape> iter = shapes.iterator();
    while(iter.hasNext())
    {
      Shape shape = iter.next();
      if(shape.removed)
        iter.remove();
      else
        shape.tick(time);
    }
  }

  public static void removeStaticShapes()
  {
    for(Shape shape : shapes)
      shape.removeStatic();
  }

  public static void addLine(double x, double y, double z, double X, double Y, double Z, Color4f color, int ticks)
  {
    if(ticks == -1)
      shapes.add(new Line(x, y, z, X, Y, Z, color).isStatic());
    else
      shapes.add(new Line(x, y, z, X, Y, Z, color).ticks(ticks));
  }

  public static void addCuboid(double x, double y, double z, double size, Color4f stroke, Color4f fill, int ticks)
  {
    addCuboid(x + size, y + size, z + size, x - size, y - size, z - size, stroke, fill, ticks);
  }

  public static void addCuboid(double x, double y, double z, double X, double Y, double Z, Color4f stroke, Color4f fill, int ticks)
  {
    if(ticks == -1)
      shapes.add(new Cuboid(x, y, z, X, Y, Z, stroke, fill).isStatic());
    else
      shapes.add(new Cuboid(x, y, z, X, Y, Z, stroke, fill).ticks(ticks));
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
    private final Shape[] shapes;

    public Cuboid(double x, double y, double z, double X, double Y, double Z, Color4f stroke, Color4f fill)
    {
      shapes = new Shape[]
      {
        new Quad(x, y, z, x, Y, z, x, Y, Z, x, y, Z, fill),
        new Quad(X, y, z, X, y, Z, X, Y, Z, X, Y, z, fill),

        new Quad(x, y, z, x, y, Z, X, y, Z, X, y, z, fill),
        new Quad(x, Y, z, X, Y, z, X, Y, Z, x, Y, Z, fill),

        new Quad(x, y, z, X, y, z, X, Y, z, x, Y, z, fill),
        new Quad(x, y, Z, x, Y, Z, X, Y, Z, X, y, Z, fill),

        new Line(x, y, z, X, y, z, stroke),
        new Line(X, y, z, X, Y, z, stroke),
        new Line(X, Y, z, x, Y, z, stroke),
        new Line(x, Y, z, x, y, z, stroke),

        new Line(x, y, z, x, y, Z, stroke),
        new Line(X, y, z, X, y, Z, stroke),
        new Line(X, Y, z, X, Y, Z, stroke),
        new Line(x, Y, z, x, Y, Z, stroke),

        new Line(x, y, Z, X, y, Z, stroke),
        new Line(X, y, Z, X, Y, Z, stroke),
        new Line(X, Y, Z, x, Y, Z, stroke),
        new Line(x, Y, Z, x, y, Z, stroke)
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
