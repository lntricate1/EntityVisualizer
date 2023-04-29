package me.lntricate.entityvisualizer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.lntricate.entityvisualizer.gui.GuiConfigs;

public class ModMenuImpl implements ModMenuApi
{
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory()
  {
    return (screen) ->
    {
      GuiConfigs gui = new GuiConfigs();
      gui.setParent(screen);
      return gui;
    };
  }
}
