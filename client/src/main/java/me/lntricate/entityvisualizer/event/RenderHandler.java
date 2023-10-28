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
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs.Generic;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RenderHandler implements IRenderer, IClientTickHandler
{
  private static final Minecraft mc = Minecraft.getInstance();
  private static final Font font = mc.font;
  private static final RenderHandler INSTANCE = new RenderHandler();
  private static record Index(double x, double y, double z, int n){}
  private static final Map<Index, Line> lines = new HashMap<>();
  private static final Map<Index, Quad> quads = new HashMap<>();
  private static final Map<Index, Point> points = new HashMap<>();
  private static final Map<Index, QuadFollow> quadFollows = new HashMap<>();
  private static final Map<Index, Text> texts = new HashMap<>();

  public static RenderHandler getInstance()
  {
    return INSTANCE;
  }

  @Override
  public void onRenderWorldLast(PoseStack poseStack, Matrix4f projMatrix)
  {
    Camera cam = mc.gameRenderer.getMainCamera();
    Vec3 cpos = cam.getPosition();
    Quaternion rot = Vector3f.YP.rotationDegrees(-cam.getYRot());
    rot.mul(Vector3f.XP.rotationDegrees(cam.getXRot()));

    poseStack.pushPose();
    poseStack.mulPose(rot);
    poseStack.translate(cpos.x, 9-cpos.y, cpos.z);
    poseStack.scale(-1, -1, -1);
    Matrix4f matrix = poseStack.last().pose();

    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderUtils.setupBlend();
    RenderSystem.disableDepthTest();
    Tesselator tesselator = Tesselator.getInstance();
    BufferBuilder buffer = tesselator.getBuilder();

    buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    for(Line line : lines.values())
      line.render(matrix, buffer);
    tesselator.end();

    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    for(Quad quad : quads.values())
      quad.render(matrix, buffer);
    for(QuadFollow quadFollow : quadFollows.values())
      quadFollow.render(matrix, buffer, cam);
    tesselator.end();

    buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    for(Point point : points.values())
      point.render(matrix, buffer, cam);
    tesselator.end();

    for(Text text : texts.values())
      text.render(poseStack, buffer, cam);
    poseStack.popPose();
  }

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
    points.put(new Index(x, y, z, 0), new Point((float)x, (float)y, (float)z, (float)Generic.POINT_SIZE.getDoubleValue(), color, mc.level.getGameTime() + ticks));
  }

  public static void addLine(double x, double y, double z, double X, double Y, double Z, Color4f color, int ticks)
  {
    lines.put(new Index(x, y, z, 0), new Line((float)x, (float)y, (float)z, (float)X, (float)Y, (float)Z, color, mc.level.getGameTime() + ticks));
  }

  public static void addTrajectory(double x_, double y_, double z_, double X_, double Y_, double Z_, boolean xFirst, Color4f color, int ticks)
  {
    long time = mc.level.getGameTime() + ticks;
    float x = (float)x_, y = (float)y_, z = (float)z_;
    float X = (float)X_, Y = (float)Y_, Z = (float)Z_;
    lines.put(new Index(x, y, z, 1), new Line(x, y, z, x, Y, z, color, time));
    if(xFirst)
    {
      lines.put(new Index(x, y, z, 2), new Line(x, Y, z, X, Y, z, color, time));
      lines.put(new Index(x, y, z, 3), new Line(X, Y, z, X, Y, Z, color, time));
    }
    else
    {
      lines.put(new Index(x, y, z, 2), new Line(x, Y, z, x, Y, Z, color, time));
      lines.put(new Index(x, y, z, 3), new Line(x, Y, Z, X, Y, Z, color, time));
    }
  }

  public static void addCuboid(double x_, double y_, double z_, double X_, double Y_, double Z_, Color4f fill, Color4f stroke, int ticks)
  {
    long time = mc.level.getGameTime() + ticks;
    float x = (float)x_, y = (float)y_, z = (float)z_;
    float X = (float)X_, Y = (float)Y_, Z = (float)Z_;
    if(fill.a > 0)
    {
      quads.put(new Index(x, y, z, 1), new Quad(x, y, z, x, Y, z, x, Y, Z, x, y, Z, fill, time));
      quads.put(new Index(x, y, z, 2), new Quad(X, y, z, X, y, Z, X, Y, Z, X, Y, z, fill, time));

      quads.put(new Index(x, y, z, 3), new Quad(x, y, z, x, y, Z, X, y, Z, X, y, z, fill, time));
      quads.put(new Index(x, y, z, 4), new Quad(x, Y, z, X, Y, z, X, Y, Z, x, Y, Z, fill, time));

      quads.put(new Index(x, y, z, 5), new Quad(x, y, z, X, y, z, X, Y, z, x, Y, z, fill, time));
      quads.put(new Index(x, y, z, 6), new Quad(x, y, Z, x, Y, Z, X, Y, Z, X, y, Z, fill, time));
    }
    if(stroke.a > 0)
    {
      lines.put(new Index(x, y, z, 1), new Line(x, y, z, X, y, z, stroke, time));
      lines.put(new Index(x, y, z, 2), new Line(X, y, z, X, Y, z, stroke, time));
      lines.put(new Index(x, y, z, 3), new Line(X, Y, z, x, Y, z, stroke, time));
      lines.put(new Index(x, y, z, 4), new Line(x, Y, z, x, y, z, stroke, time));

      lines.put(new Index(x, y, z, 5), new Line(x, y, z, x, y, Z, stroke, time));
      lines.put(new Index(x, y, z, 6), new Line(X, y, z, X, y, Z, stroke, time));
      lines.put(new Index(x, y, z, 7), new Line(X, Y, z, X, Y, Z, stroke, time));
      lines.put(new Index(x, y, z, 8), new Line(x, Y, z, x, Y, Z, stroke, time));

      lines.put(new Index(x, y, z, 9), new Line(x, y, Z, X, y, Z, stroke, time));
      lines.put(new Index(x, y, z, 10), new Line(X, y, Z, X, Y, Z, stroke, time));
      lines.put(new Index(x, y, z, 11), new Line(X, Y, Z, x, Y, Z, stroke, time));
      lines.put(new Index(x, y, z, 12), new Line(x, Y, Z, x, y, Z, stroke, time));
    }
  }

  public static void addText(double x, double y, double z, Component component, Color4f background, int ticks)
  {
    quadFollows.put(new Index(x, y, z, 0), new QuadFollow((float)x, (float)y, (float)z, (font.width(component)+2)/80F, (font.lineHeight+2)/80F, background, mc.level.getGameTime() + ticks));
    texts.put(new Index(x, y, z, 0), new Text((float)x, (float)y, (float)z, component, new Color4f(1F, 1F, 1F), mc.level.getGameTime() + ticks));
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

  private static final record Point(float x, float y, float z, float size, Color4f color, long removalTime)
  {
    public void render(Matrix4f matrix, BufferBuilder buffer, Camera cam)
    {
      Matrix4f m = matrix.copy();
      Quaternion rot = Vector3f.YP.rotationDegrees(-cam.getYRot());
      rot.mul(Vector3f.XP.rotationDegrees(-cam.getXRot()));
      m.translate(new Vector3f(x, -y, z));
      m.multiply(rot);
      buffer.vertex(m, 0,       size/3*2, 0).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(m, -size/2, -size/3,  0).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(m, size/2,  -size/3,  0).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  private static record Line(float x, float y, float z, float X, float Y, float Z, Color4f color, long removalTime)
  {
    public void render(Matrix4f matrix, BufferBuilder buffer)
    {
      buffer.vertex(matrix, x, y, z).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(matrix, X, Y, Z).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  private static final record Quad(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, Color4f color, long removalTime)
  {
    public void render(Matrix4f matrix, BufferBuilder buffer)
    {
      buffer.vertex(matrix, x1, y1, z1).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(matrix, x2, y2, z2).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(matrix, x3, y3, z3).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(matrix, x4, y4, z4).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  private static final record QuadFollow(float x, float y, float z, float w, float h, Color4f color, long removalTime)
  {
    public void render(Matrix4f matrix, BufferBuilder buffer, Camera cam)
    {
      Matrix4f m = matrix.copy();
      Quaternion rot = Vector3f.YP.rotationDegrees(-cam.getYRot());
      rot.mul(Vector3f.XP.rotationDegrees(-cam.getXRot()));
      m.translate(new Vector3f(x, -y, z));
      m.multiply(rot);
      buffer.vertex(m, -w, -h, 0).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(m, w, -h, 0).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(m, w, h, 0).color(color.r, color.g, color.b, color.a).endVertex();
      buffer.vertex(m, -w, h, 0).color(color.r, color.g, color.b, color.a).endVertex();
    }
  }

  private static final record Text(float x, float y, float z, Component component, Color4f color, long removalTime)
  {
    public void render(PoseStack poseStack, BufferBuilder buffer, Camera cam)
    {
      poseStack.pushPose();
      Quaternion rot = Vector3f.YP.rotationDegrees(180-cam.getYRot());
      rot.mul(Vector3f.XP.rotationDegrees(cam.getXRot()));
      poseStack.translate(x, 9-y, z);
      poseStack.scale(0.025F, 0.025F, 0.025F);
      poseStack.mulPose(rot);
      font.draw(poseStack, component, -font.width(component)/2, -font.lineHeight/2, color.intValue);
      poseStack.popPose();
    }
  }
}
