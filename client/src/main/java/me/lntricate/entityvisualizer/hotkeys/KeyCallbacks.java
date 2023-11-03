package me.lntricate.entityvisualizer.hotkeys;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import me.lntricate.entityvisualizer.FormatUtil;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.gui.GuiConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import static me.lntricate.entityvisualizer.FormatUtil.var;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

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
        Entity entity = mc.crosshairPickEntity;
        if(entity == null)
          return false;

        Map<Triple<String, Vec3, Vec3>, Integer> data = new HashMap<>();
        for(Entity e : mc.level.getEntities(mc.player, entity.getBoundingBox().inflate(0.05)))
        {
          Triple<String, Vec3, Vec3> triple = Triple.of(e.getType().toShortString(), e.position(), e.getDeltaMovement());
          data.put(triple, data.getOrDefault(triple, 0) + 1);
        }

        for(Map.Entry<Triple<String, Vec3, Vec3>, Integer> entry : data.entrySet())
        {
          Triple<String, Vec3, Vec3> triple = entry.getKey();
          String name = triple.getLeft();
          Vec3 p = triple.getMiddle();
          Vec3 m = triple.getRight();
          DecimalFormat df = new DecimalFormat("1");
          df.setMaximumFractionDigits(340);
          mc.gui.handleChat(ChatType.SYSTEM, FormatUtil.format(Configs.Generic.ENTITY_DATA_FORMAT,
            var("name", "s", name),
            var("count", "d", entry.getValue()),
            var("x", "s", df.format(p.x)),
            var("y", "s", df.format(p.y)),
            var("z", "s", df.format(p.z)),
            var("mx", "s", df.format(m.x)),
            var("my", "s", df.format(m.y)),
            var("mz", "s", df.format(m.z))), mc.player.getUUID());
        }
      }
      return true;
    }
  }
}
