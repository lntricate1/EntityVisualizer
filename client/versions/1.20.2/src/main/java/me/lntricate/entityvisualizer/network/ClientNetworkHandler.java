package me.lntricate.entityvisualizer.network;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import me.lntricate.entityvisualizer.FormatUtil;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.helpers.EntityHelper;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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

    CompoundTag tag = data.readNbt();
    switch(tag.getString("ID"))
    {
      case "hi" -> onServerHi();
      case "tick" -> handleTickPacket(tag);
      case "requestEntities" -> handleRequestEntitiesPacket(tag);
    }
  }

  private static void onServerHi()
  {
    setPacketRecievingState(Configs.Renderers.requireEntityPackets());
    hasServer = true;
  }

  public static void setPacketRecievingState(boolean state)
  {
    CompoundTag tag = new CompoundTag();
    tag.putString("ID", state ? "hi" : "bye");
    mc.player.connection.send(NetworkStuff.serverbound(tag));
  }

  private static void handleTickPacket(CompoundTag tag)
  {
    EntityHelper.registerTick(
      tag.getInt("id"),
      tag.getBoolean("self"),
      tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"),
      tag.getDouble("mx"), tag.getDouble("my"), tag.getDouble("mz"),
      tag.getBoolean("xFirst"),
      tag.getBoolean("coll"));
  }

  private static record Data(String name, Vec3 pos, Vec3 vel, int fuse){};

  private static void handleRequestEntitiesPacket(CompoundTag tag)
  {
    int[] ids = tag.getIntArray("ids");
    long[] x = tag.getLongArray("x");
    long[] y = tag.getLongArray("y");
    long[] z = tag.getLongArray("z");
    long[] mx = tag.getLongArray("mx");
    long[] my = tag.getLongArray("my");
    long[] mz = tag.getLongArray("mz");
    int[] fuse = tag.getIntArray("fuse");

    Map<Data, Integer> datas = new HashMap<>();
    for(int i = 0; i < ids.length; ++i)
    {
      Vec3 pos = new Vec3(Double.longBitsToDouble(x[i]), Double.longBitsToDouble(y[i]), Double.longBitsToDouble(z[i]));
      Vec3 vel = new Vec3(Double.longBitsToDouble(mx[i]), Double.longBitsToDouble(my[i]), Double.longBitsToDouble(mz[i]));
      EntityHelper.registerTick(ids[i], pos, vel);
      Data data = new Data(mc.level.getEntity(ids[i]).getType().toShortString(), pos, vel, fuse[i]);
      datas.put(data, datas.getOrDefault(data, 0) + 1);
    }

    for(Map.Entry<Data, Integer> entry : datas.entrySet())
    {
      Data data = entry.getKey();
      String name = data.name;
      Vec3 p = data.pos;
      Vec3 m = data.vel;
      int f = data.fuse;
      DecimalFormat df = new DecimalFormat("0");
      df.setMaximumFractionDigits(340);
      df.setMinimumFractionDigits(1);
      EConfigString format = f == -1 ? Configs.Generic.ENTITY_DATA_FORMAT : Configs.Generic.ENTITY_DATA_TNT_FORMAT;
      FormatUtil.Variable<?> var = f == -1 ? var("name", "s", name) : var("fuse", "s", f);
      mc.gui.getChat().addMessage(FormatUtil.format(format, var,
        var("count", "d", entry.getValue()),
        var("x", "s", df.format(p.x)),
        var("y", "s", df.format(p.y)),
        var("z", "s", df.format(p.z)),
        var("mx", "s", df.format(m.x)),
        var("my", "s", df.format(m.y)),
        var("mz", "s", df.format(m.z))));
    }
  }
}
