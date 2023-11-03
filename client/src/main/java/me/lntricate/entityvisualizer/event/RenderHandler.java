package me.lntricate.entityvisualizer.event;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs.Generic;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
// import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
// import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RenderHandler implements IRenderer, IClientTickHandler
{
  private static final Minecraft mc = Minecraft.getInstance();
  // private static final Font font = mc.font;
  private static final RenderHandler INSTANCE = new RenderHandler();
  private static record Index(double x, double y, double z, Shape s, int n){}
  private static enum Shape{LINE, TRAJECTORY, CUBOID, POINT}
  private static final Map<Index, Line> lines = new HashMap<>();
  private static final Map<Index, Quad> quads = new HashMap<>();
  private static final Map<Index, Point> points = new HashMap<>();
  // private static final Map<Index, QuadFollow> quadFollows = new HashMap<>();
  // private static final Map<Index, Text> texts = new HashMap<>();

  public static RenderHandler getInstance()
  {
    return INSTANCE;
  }

  @Override
  public void onRenderWorldLast(PoseStack poseStack, Matrix4f projMatrix)
  {
    Camera cam = mc.gameRenderer.getMainCamera();
    Vec3 cpos = cam.getPosition();
    double x = cpos.x, y = cpos.y, z = cpos.z;
    Vector3f left = cam.getLeftVector();
    Vector3f up = cam.getUpVector();

    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderUtils.setupBlend();
    RenderSystem.disableDepthTest();
    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder buffer = tesselator.getBuilder();

    buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    for(Line line : lines.values())
      line.render(buffer, x, y, z);
    tesselator.end();

    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    for(Quad quad : quads.values())
      quad.render(buffer, x, y, z);
    // if(!quadFollows.isEmpty())
    //   renderQuadFollows(buffer, left, up, x, y, z);
    tesselator.end();

    buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    if(!points.isEmpty())
      renderPoints(buffer, left, up, x, y, z);
    tesselator.end();

    // for(Text text : texts.values())
    //   text.render(poseStack, buffer, cam);
    // poseStack.popPose();
  }

  private static void renderPoints(BufferBuilder buffer, Vector3f left, Vector3f up, double x, double y, double z)
  {
    double size = Generic.POINT_SIZE.getDoubleValue();
    double size2 = size/2;
    double ux = up.x()*size2, uy = up.y()*size/3, uz = up.z()*size2;
    double lx = left.x()*size2, lz = left.z()*size2;
    double ax = ux-x, ay = 2*uy-y, az = uz-z, bx = lx-x, by = -uy-y, bz = lz-z, cx = -lx-x, cz = -lz-z;
    for(Point point : points.values())
      point.render(buffer, ax, ay, az, bx, by, bz, cx, cz);
  }

  // private static void renderQuadFollows(BufferBuilder buffer, Vector3f left, Vector3f up, double x, double y, double z)
  // {
  //   double ux = up.x(), uy = up.y(), uz = up.z();
  //   double lx = left.x(), lz = left.z();
  //   for(QuadFollow quadFollow : quadFollows.values())
  //     quadFollow.render(buffer, ux, uy, uz, lx, lz, x, y, z);
  // }

  @Override
  public void onClientTick(Minecraft minecraft)
  {
    if(mc.level == null)
      return;

    long time = mc.level.getGameTime();
    lines.values().removeIf((Line line) -> time > line.removalTime);
    quads.values().removeIf((Quad quad) -> time > quad.removalTime);
    points.values().removeIf((Point point) -> time > point.removalTime);
  }

  public static void addPoint(double x, double y, double z, Color4f color, int ticks)
  {
    points.put(new Index(x, y, z, Shape.POINT, 0), new Point(x, y, z, color, mc.level.getGameTime() + ticks));
  }

  public static void addLine(double x, double y, double z, double X, double Y, double Z, Color4f color, int ticks)
  {
    lines.put(new Index(x, y, z, Shape.LINE, 0), new Line(x, y, z, X, Y, Z, color, mc.level.getGameTime() + ticks));
  }

  public static void addTrajectory(double x, double y, double z, double X, double Y, double Z, boolean xFirst, Color4f color, int ticks)
  {
    long time = mc.level.getGameTime() + ticks;
    lines.put(new Index(x, y, z, Shape.TRAJECTORY, 0), new Line(x, y, z, x, Y, z, color, time));
    if(xFirst)
    {
      lines.put(new Index(x, y, z, Shape.TRAJECTORY, 1), new Line(x, Y, z, X, Y, z, color, time));
      lines.put(new Index(x, y, z, Shape.TRAJECTORY, 2), new Line(X, Y, z, X, Y, Z, color, time));
    }
    else
    {
      lines.put(new Index(x, y, z, Shape.TRAJECTORY, 1), new Line(x, Y, z, x, Y, Z, color, time));
      lines.put(new Index(x, y, z, Shape.TRAJECTORY, 2), new Line(x, Y, Z, X, Y, Z, color, time));
    }
  }

  public static void addCuboid(double x, double y, double z, double X, double Y, double Z, Color4f fill, Color4f stroke, int ticks)
  {
    long time = mc.level.getGameTime() + ticks;
    if(fill.a > 0)
    {
      quads.put(new Index(x, y, z, Shape.CUBOID, 0), new Quad(x, y, z, x, Y, z, x, Y, Z, x, y, Z, fill, time));
      quads.put(new Index(x, y, z, Shape.CUBOID, 1), new Quad(X, y, z, X, y, Z, X, Y, Z, X, Y, z, fill, time));

      quads.put(new Index(x, y, z, Shape.CUBOID, 2), new Quad(x, y, z, x, y, Z, X, y, Z, X, y, z, fill, time));
      quads.put(new Index(x, y, z, Shape.CUBOID, 3), new Quad(x, Y, z, X, Y, z, X, Y, Z, x, Y, Z, fill, time));

      quads.put(new Index(x, y, z, Shape.CUBOID, 4), new Quad(x, y, z, X, y, z, X, Y, z, x, Y, z, fill, time));
      quads.put(new Index(x, y, z, Shape.CUBOID, 5), new Quad(x, y, Z, x, Y, Z, X, Y, Z, X, y, Z, fill, time));
    }
    if(stroke.a > 0)
    {
      lines.put(new Index(x, y, z, Shape.CUBOID, 6), new Line(x, y, z, X, y, z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 7), new Line(X, y, z, X, Y, z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 8), new Line(X, Y, z, x, Y, z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 9), new Line(x, Y, z, x, y, z, stroke, time));

      lines.put(new Index(x, y, z, Shape.CUBOID, 10), new Line(x, y, z, x, y, Z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 11), new Line(X, y, z, X, y, Z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 12), new Line(X, Y, z, X, Y, Z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 13), new Line(x, Y, z, x, Y, Z, stroke, time));

      lines.put(new Index(x, y, z, Shape.CUBOID, 14), new Line(x, y, Z, X, y, Z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 15), new Line(X, y, Z, X, Y, Z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 16), new Line(X, Y, Z, x, Y, Z, stroke, time));
      lines.put(new Index(x, y, z, Shape.CUBOID, 17), new Line(x, Y, Z, x, y, Z, stroke, time));
    }
  }

  // public static void addText(double x, double y, double z, Component component, Color4f background, int ticks)
  // {
  //   quadFollows.put(new Index(x, y, z, 0), new QuadFollow((float)x, (float)y, (float)z, (font.width(component)+2)/80F, (font.lineHeight+2)/80F, background, mc.level.getGameTime() + ticks));
  //   texts.put(new Index(x, y, z, 0), new Text((float)x, (float)y, (float)z, component, new Color4f(1F, 1F, 1F), mc.level.getGameTime() + ticks));
  // }

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

  private static final record Point(double x, double y, double z, Color4f color, long removalTime)
  {
    public void render(BufferBuilder buffer, double ax, double ay, double az, double bx, double by, double bz, double cx, double cz)
    {
      buffer.vertex(x+ax, y+ay, z+az).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x+bx, y+by, z+bz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x+cx, y+by, z+cz).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  private static record Line(double x, double y, double z, double X, double Y, double Z, Color4f color, long removalTime)
  {
    public void render(BufferBuilder buffer, double cx, double cy, double cz)
    {
      buffer.vertex(x-cx, y-cy, z-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(X-cx, Y-cy, Z-cz).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  private static final record Quad(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color4f color, long removalTime)
  {
    public void render(BufferBuilder buffer, double cx, double cy, double cz)
    {
      buffer.vertex(x1-cx, y1-cy, z1-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x2-cx, y2-cy, z2-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x3-cx, y3-cy, z3-cz).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(x4-cx, y4-cy, z4-cz).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  // private static final record QuadFollow(float x, float y, float z, float w, float h, Color4f color, long removalTime)
  // {
  //   public void render(BufferBuilder buffer, double ux, double uy, double uz, double lx, double lz, double cx, double cy, double cz)
  //   {
  //     ux *= h; uy *= h; uz *= h;
  //     lx *= w; lz *= w;
  //     double dx = ux+lx, dy = uy, dz = uz+lz;
  //     buffer.vertex(dx-cx, dy-cy, dz-cz).color(color.r, color.g, color.b, color.a).endVertex();
  //     buffer.vertex(-dx-cx, dy-cy, -dz-cx).color(color.r, color.g, color.b, color.a).endVertex();
  //     buffer.vertex(-dx-cx, -dy-cy, -dz-cx).color(color.r, color.g, color.b, color.a).endVertex();
  //     buffer.vertex(-dx-cx, -dy-cy, dz-cx).color(color.r, color.g, color.b, color.a).endVertex();
  //   }
  // }

  // private static final record Text(float x, float y, float z, Component component, Color4f color, long removalTime)
  // {
  //   public void render(PoseStack poseStack, BufferBuilder buffer, Camera cam)
  //   {
  //     poseStack.pushPose();
  //     Quaternion rot = Vector3f.YP.rotationDegrees(180-cam.getYRot());
  //     rot.mul(Vector3f.XP.rotationDegrees(cam.getXRot()));
  //     poseStack.translate(x, -y, z);
  //     poseStack.scale(0.025F, 0.025F, 0.025F);
  //     poseStack.mulPose(rot);
  //     font.draw(poseStack, component, -font.width(component)/2, -font.lineHeight/2, color.intValue);
  //     poseStack.popPose();
  //   }
  // }
}
