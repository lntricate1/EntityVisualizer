package me.lntricate.entityvisualizer.network;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.FormatUtil;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.event.RenderHandler;
import me.lntricate.entityvisualizer.helpers.ClientExplosionHelper;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.phys.Vec3;

import static me.lntricate.entityvisualizer.FormatUtil.var;

public class ClientNetworkHandler
{
  private static Minecraft mc = Minecraft.getInstance();
  public static boolean hasServer;

  public static void handleData(FriendlyByteBuf data, LocalPlayer player)
  {
    if(data == null)
      return;

    int id = data.readVarInt();
    if(id == NetworkStuff.HI)
      onServerHi();
    if(id == NetworkStuff.DATA)
    {
      CompoundTag tag = data.readNbt();
      switch(tag.getInt("ID"))
      {
        case 0 -> handleMovePacket(tag);
        case 1 -> handleTickPacket(tag);
        case 2 -> handleDeathPacket(tag);
        case 3 -> handleVelPacket(tag);
        case 4 -> handleExposurePacket(tag);
        case 5 -> handleEntitiesRequestPacket(tag);
      }
    }
  }

  private static void onServerHi()
  {
    setPacketRecievingState(Configs.Renderers.requireEntityPackets());
    hasServer = true;
  }

  public static void setPacketRecievingState(boolean state)
  {
    mc.player.connection.send(new ServerboundCustomPayloadPacket(NetworkStuff.CHANNEL, (new FriendlyByteBuf(Unpooled.buffer())).writeVarInt(state ? NetworkStuff.HI : NetworkStuff.BYE)));
  }

  private static void handleMovePacket(CompoundTag tag)
  {
    if(!Configs.Renderers.ENTITY_TRAJECTORY.config.on())
      return;

    ListTag id = (ListTag)tag.get("id");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    long[] dx = tag.getLongArray("dx");
    long[] dy = tag.getLongArray("dy");
    long[] dz = tag.getLongArray("dz");
    long[] ax = tag.getLongArray("ax");
    long[] ay = tag.getLongArray("ay");
    long[] az = tag.getLongArray("az");
    long[] bx = tag.getLongArray("bx");
    long[] by = tag.getLongArray("by");
    long[] bz = tag.getLongArray("bz");
    long[] cy = tag.getLongArray("cy");
    byte[] flags = tag.getByteArray("flags");

    int i = 0;
    for(; i < bx.length; i++)
      RenderHandler.addMove(id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]),
        Double.longBitsToDouble(dx[i]), Double.longBitsToDouble(dy[i]), Double.longBitsToDouble(dz[i]),
        Double.longBitsToDouble(ax[i]), Double.longBitsToDouble(ay[i]), Double.longBitsToDouble(az[i]),
        Double.longBitsToDouble(bx[i]), Double.longBitsToDouble(by[i]), Double.longBitsToDouble(bz[i]),
        Double.longBitsToDouble(cy[i]), flags[i] == 2);

    for(; i < ax.length; i++)
      RenderHandler.addMove(id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]),
        Double.longBitsToDouble(dx[i]), Double.longBitsToDouble(dy[i]), Double.longBitsToDouble(dz[i]),
        Double.longBitsToDouble(ax[i]), Double.longBitsToDouble(ay[i]), Double.longBitsToDouble(az[i]),
        Double.longBitsToDouble(by[i]), Double.longBitsToDouble(cy[i]), flags[i] == 2);

    for(; i < x.length; i++)
      RenderHandler.addMove(id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]),
        Double.longBitsToDouble(dx[i]), Double.longBitsToDouble(dy[i]), Double.longBitsToDouble(dz[i]),
        (flags[i] & 1) == 1, (flags[i] & 2) == 2);
  }

  private static void handleTickPacket(CompoundTag tag)
  {
    if(!Configs.Renderers.ENTITY_TICKS.config.on())
      return;

    ListTag id = (ListTag)tag.get("id");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    int[] w = tag.getIntArray("w");
    int[] h = tag.getIntArray("h");

    for(int i = 0; i < x.length; i++)
      RenderHandler.addTick(id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]), Float.intBitsToFloat(w[i]), Float.intBitsToFloat(h[i]));
  }

  private static void handleDeathPacket(CompoundTag tag)
  {
    if(!Configs.Renderers.ENTITY_DEATHS.config.on())
      return;

    ListTag id = (ListTag)tag.get("id");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    int[] w = tag.getIntArray("w");
    int[] h = tag.getIntArray("h");

    for(int i = 0; i < x.length; i++)
      RenderHandler.addDeath(id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]), Float.intBitsToFloat(w[i]), Float.intBitsToFloat(h[i]));
  }

  private static void handleVelPacket(CompoundTag tag)
  {
    if(!Configs.Renderers.ENTITY_VELOCITY.config.on())
      return;

    ListTag id = (ListTag)tag.get("id");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    long[] X = tag.getLongArray("X");
    long[] Y = tag.getLongArray("Y");
    long[] Z = tag.getLongArray("Z");

    for(int i = 0; i < x.length; i++)
      RenderHandler.addVel(id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]), Double.longBitsToDouble(X[i]), Double.longBitsToDouble(Y[i]), Double.longBitsToDouble(Z[i]));
  }

  private static void handleExposurePacket(CompoundTag tag)
  {
    if(!Configs.Renderers.EXPLOSION_ENTITY_RAYS.config.on())
      return;

    double px = tag.getDouble("px");
    double py = tag.getDouble("py");
    double pz = tag.getDouble("pz");
    ListTag id = (ListTag)tag.get("id");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    int[] w = tag.getIntArray("w");
    int[] h = tag.getIntArray("h");

    for(int i = 0; i < x.length; i++)
      ClientExplosionHelper.explosionEntityRays(mc.level, px, py, pz, id.getString(i), Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]), Float.intBitsToFloat(w[i]), Float.intBitsToFloat(h[i]));
  }

  private static record Data(String name, Vec3 pos, Vec3 vel, int fuse, Vec3 pistonDeltas){};

  private static void handleEntitiesRequestPacket(CompoundTag tag)
  {
    int[] ids = tag.getIntArray("ids");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    long[] mx = tag.getLongArray("mx");
    long[] my = tag.getLongArray("my");
    long[] mz = tag.getLongArray("mz");
    int[] fuse = tag.getIntArray("fuse");
    long[] px = tag.getLongArray("px");
    long[] py = tag.getLongArray("py");
    long[] pz = tag.getLongArray("pz");

    Map<Data, Integer> datas = new HashMap<>();
    for(int i = 0; i < ids.length; ++i)
    {
      Vec3 pos = new Vec3(Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]));
      Vec3 vel = new Vec3(Double.longBitsToDouble(mx[i]), Double.longBitsToDouble(my[i]), Double.longBitsToDouble(mz[i]));
      Vec3 pistonDeltas = new Vec3(Double.longBitsToDouble(px[i]), Double.longBitsToDouble(py[i]), Double.longBitsToDouble(pz[i]));
      Data data = new Data(mc.level.getEntity(ids[i]).getType().toShortString(), pos, vel, fuse[i], pistonDeltas);
      datas.put(data, datas.getOrDefault(data, 0) + 1);
    }

    for(Map.Entry<Data, Integer> entry : datas.entrySet())
    {
      Data data = entry.getKey();
      String name = data.name;
      Vec3 p = data.pos;
      Vec3 m = data.vel;
      Vec3 pd = data.pistonDeltas;
      int f = data.fuse;
      DecimalFormat df = new DecimalFormat("0");
      df.setMaximumFractionDigits(340);
      df.setMinimumFractionDigits(1);
      EConfigString format = f == -1 ? Configs.Generic.ENTITY_DATA_FORMAT : Configs.Generic.ENTITY_DATA_TNT_FORMAT;
      FormatUtil.Variable<?> var = f == -1 ? var("name", "s", name) : var("fuse", "s", f);
      //#if MC >= 11900
      //$$ mc.gui.getChat().addMessage(FormatUtil.format(format, var,
      //#else
      mc.gui.handleChat(ChatType.SYSTEM, FormatUtil.format(format, var,
      //#endif
        var("count", "d", entry.getValue()),
        var("x", "s", df.format(p.x)),
        var("y", "s", df.format(p.y)),
        var("z", "s", df.format(p.z)),
        var("mx", "s", df.format(m.x)),
        var("my", "s", df.format(m.y)),
        var("mz", "s", df.format(m.z)),
        var("px", "s", df.format(pd.x)),
        var("py", "s", df.format(pd.y)),
        var("pz", "s", df.format(pd.z)))
      //#if MC < 11900
        , mc.player.getUUID()
      //#endif
        );
    }
  }
}
