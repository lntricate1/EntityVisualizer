package me.lntricate.entityvisualizer.hotkeys;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import io.netty.buffer.Unpooled;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.gui.GuiConfigs;
import me.lntricate.entityvisualizer.network.NetworkStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class KeyCallbacks
{
  public static void init()
  {
    Callbacks callback = new Callbacks();
    Configs.Generic.OPEN_CONFIG_GUI.getKeybind().setCallback(callback);
    Configs.Generic.ENTITY_DATA.getKeybind().setCallback(callback);
  }

  public static class Callbacks implements IHotkeyCallback
  {
    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key)
    {
      Minecraft mc = Minecraft.getInstance();
      if(mc.player == null)
        return false;

      if(key == Configs.Generic.OPEN_CONFIG_GUI.getKeybind())
        GuiBase.openGui(new GuiConfigs());

      if(key == Configs.Generic.ENTITY_DATA.getKeybind())
      {
        Vec3 pos = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle().scale(Configs.Generic.ENTITY_DATA_RANGE.getDoubleValue()).add(pos);
        List<Entity> entities = mc.level.getEntities(mc.player, new AABB(pos, look), (Entity entity) -> entity.getBoundingBox().clip(pos, look).isPresent());
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putIntArray("ids", entities.stream().map((Entity e) -> e.getId()).toList());
        mc.player.connection.send(new ServerboundCustomPayloadPacket(NetworkStuff.CHANNEL, (new FriendlyByteBuf(Unpooled.buffer())).writeVarInt(NetworkStuff.DATA).writeNbt(compoundTag)));
      }
      return true;
    }
  }
}
