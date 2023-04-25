package me.lntricate.entityvisualizer.config.options;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IKeybind;

public class BooleanHotkeyGuiWrapper extends ConfigBoolean
{
  protected final IConfigBoolean booleanConfig;
  protected final IKeybind keybind;

  public BooleanHotkeyGuiWrapper(String name, IConfigBoolean booleanConfig, IKeybind keybind)
  {
    super(name, booleanConfig.getDefaultBooleanValue(), booleanConfig.getComment());
    this.booleanConfig = booleanConfig;
    this.keybind = keybind;
  }

  @Override
  public boolean getBooleanValue()
  {
    return this.booleanConfig.getBooleanValue();
  }

  @Override
  public void setBooleanValue(boolean value)
  {
    this.booleanConfig.setBooleanValue(value);
  }

  @Override
  public boolean isModified()
  {
    // Note: calling isModified() for the IHotkey here directly would not work
    // with multi-type configs like the FeatureToggle in Tweakeroo!
    // Thus we need to get the IKeybind and call it for that specifically.
    return this.booleanConfig.isModified() || this.getKeybind().isModified();
  }

  @Override
  public void resetToDefault()
  {
    this.booleanConfig.resetToDefault();
    this.getKeybind().resetToDefault();
  }

  public IConfigBoolean getBooleanConfig()
  {
    return this.booleanConfig;
  }

  public IKeybind getKeybind()
  {
    return this.keybind;
  }
}
