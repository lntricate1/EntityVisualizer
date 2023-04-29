package me.lntricate.entityvisualizer.hotkeys;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.gui.GuiConfigs;
import net.minecraft.client.Minecraft;

public class KeyCallbacks
{
  public static void init()
  {
    Callbacks callback = new Callbacks();
    Configs.Generic.OPEN_CONFIG_GUI.getKeybind().setCallback(callback);
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
      return true;
    }
  }
}
