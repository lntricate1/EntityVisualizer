package me.lntricate.entityvisualizer.gui;

import java.util.List;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import me.lntricate.entityvisualizer.EntityVisualizerMod;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.malilib.gui.EGuiConfigsBase;

public class GuiConfigs extends EGuiConfigsBase
{
  private static ConfigGuiTab tab = ConfigGuiTab.GENERIC;

  public GuiConfigs()
  {
    super(10, 50, EntityVisualizerMod.MOD_ID, null, "entityvisualizer.gui.title.configs");
  }

  public static enum ConfigGuiTab
  {
    GENERIC("entityvisualizer.gui.button.config_gui.generic", Configs.Generic.getOptions(), 200),
    RENDERERS("entityvisualizer.gui.button.config_gui.renderers", Configs.Renderers.getOptions(), 400),
    LISTS("entityvisualizer.gui.button.config_gui.lists", Configs.Lists.getOptions(), 300);

    private final String translationKey;
    private final ImmutableList<IConfigBase> OPTIONS;
    private final int width;

    private ConfigGuiTab(String translationKey, ImmutableList<IConfigBase> OPTIONS, int width)
    {
      this.translationKey = translationKey;
      this.OPTIONS = OPTIONS;
      this.width = width;
    }

    public String getDisplayName()
    {
      return StringUtils.translate(translationKey);
    }

    public ImmutableList<IConfigBase> getOptions()
    {
      return OPTIONS;
    }

    public int getWidth()
    {
      return width;
    }
  }

  @Override
  public void initGui()
  {
    super.initGui();
    clearOptions();

    int x = 10;
    for(ConfigGuiTab tab : ConfigGuiTab.values())
      x += createButton(x, 26, tab);
  }

  @Override
  protected int getConfigWidth()
  {
    return tab.getWidth();
  }

  @Override
  protected boolean useKeybindSearch()
  {
    return true;
  }

  @Override
  public List<ConfigOptionWrapper> getConfigs()
  {
    return ConfigOptionWrapper.createFor(tab.getOptions());
  }

  private int createButton(int x, int y, ConfigGuiTab tab)
  {
    ButtonGeneric button = new ButtonGeneric(x, y, getStringWidth(tab.getDisplayName()) + 10, 20, tab.getDisplayName());
    button.setEnabled(GuiConfigs.tab != tab);
    addButton(button, new ButtonListenerConfigTabs(tab, this));
    return button.getWidth() + 2;
  }

  private static class ButtonListenerConfigTabs implements IButtonActionListener
  {
    private final GuiConfigs parent;
    private final ConfigGuiTab tab;

    public ButtonListenerConfigTabs(ConfigGuiTab tab, GuiConfigs parent)
    {
      this.tab = tab;
      this.parent = parent;
    }

    @Override
    public void actionPerformedWithButton(ButtonBase button, int mouseButton)
    {
      GuiConfigs.tab = this.tab;

      this.parent.reCreateListWidget();
      this.parent.getListWidget().resetScrollbarPosition();
      this.parent.initGui();
    }
  }
}
