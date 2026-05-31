package me.lntricate.entityvisualizer.network;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.FormatUtil;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.helpers.EntityHelper;
import me.lntricate.entityvisualizer.malilib.config.options.EConfigString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.phys.Vec3;

import static me.lntricate.entityvisualizer.FormatUtil.var;

public class ClientNetworkHandler
{
  private static Minecraft mc = Minecraft.getInstance();
  public static boolean hasServer;

  //#if MC >= 12002
  //$$ public static void handleData(CompoundTag data, LocalPlayer player)
  //#else
  public static void handleData(FriendlyByteBuf data, LocalPlayer player)
  //#endif
  {
    if(data == null)
      return;

    //#if MC >= 12002
    //$$ for(String key : data.getAllKeys())
    //$$   switch(key)
    //$$   {
    //$$     case "HI" -> onServerHi();
    //$$     case "ENTITY" -> handleEntityPacket(data.getCompound("ENTITY"));
    //$$     case "GETENTITYDATA" -> handleEntitiesRequestPacket(data.getCompound("GETENTITYDATA"));
    //$$   }
    //#else
    int id = data.readVarInt();
    switch(id)
    {
      case NetworkStuff.HI -> onServerHi();
      case NetworkStuff.DATA -> {
        CompoundTag tag = data.readNbt();
        switch(tag.getInt("ID"))
        {
          case 0 -> handleEntityPacket(tag);
          case 1 -> handleEntitiesRequestPacket(tag);
        }
      }
    }
    //#endif
  }

  private static void onServerHi()
  {
    setPacketRecievingState(Configs.Renderers.requireEntityPackets());
    hasServer = true;
  }

  public static void setPacketRecievingState(boolean state)
  {
    //#if MC >= 12002
    //$$ CompoundTag tag = new CompoundTag();
    //$$ tag.putBoolean(state ? "HI" : "BYE", true);
    //$$ mc.player.connection.send(new ServerboundCustomPayloadPacket(new NetworkStuff.EntityVisualizerPayload(tag)));
    //#else
    mc.player.connection.send(new ServerboundCustomPayloadPacket(NetworkStuff.CHANNEL, (new FriendlyByteBuf(Unpooled.buffer())).writeVarInt(state ? NetworkStuff.HI : NetworkStuff.BYE)));
    //#endif
  }

  private static void handleEntityPacket(CompoundTag tag)
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
        var("mz", "s", df.format(m.z)))
      //#if MC < 11900
        , mc.player.getUUID()
      //#endif
        );
    }
  }
}
