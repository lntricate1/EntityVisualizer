package me.lntricate.entityvisualizer.config.options;

import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.minecraft.util.Mth;

public class ConfigGenericRenderer extends ConfigBooleanHotkeyed implements IConfigInteger
{
  protected final int minValue;
  protected final int maxValue;
  protected final int defaultValue;
  protected int value;
  private boolean useSlider;

  public ConfigGenericRenderer(String name, String comment, String prettyName)
  {
    this(name, false, 100, 0, 1200, false, "", comment, prettyName);
  }

  public ConfigGenericRenderer(String name, boolean defaultBoolean, int defaultInteger, int minValue, int maxValue, boolean useSlider, String defaultHotkey, String comment, String prettyName)
  {
    this(name, defaultBoolean, defaultInteger, minValue, maxValue, useSlider, defaultHotkey, KeybindSettings.DEFAULT, comment, prettyName);
  }

  public ConfigGenericRenderer(String name, boolean defaultBoolean, int defaultInteger, int minValue, int maxValue, boolean useSlider, String defaultHotkey, KeybindSettings settings, String comment, String prettyName)
  {
    super(name, defaultBoolean, defaultHotkey, settings, comment, prettyName);

    this.minValue = minValue;
    this.maxValue = maxValue;
    defaultValue = defaultInteger;
    value = defaultValue;
    this.useSlider = useSlider;
  }

  @Override
  public boolean shouldUseSlider()
  {
    return useSlider;
  }

  @Override
  public int getIntegerValue()
  {
    return value;
  }

  @Override
  public int getDefaultIntegerValue()
  {
    return defaultValue;
  }

  @Override
  public void setIntegerValue(int value)
  {
    int oldValue = this.value;
    this.value = getClampedValue(value);
    if(oldValue != this.value)
      onValueChanged();
  }

  @Override
  public int getMinIntegerValue()
  {
    return minValue;
  }

  @Override
  public int getMaxIntegerValue()
  {
    return maxValue;
  }

  protected int getClampedValue(int value)
  {
    return Mth.clamp(value, this.minValue, this.maxValue);
  }

  @Override
  public boolean isModified()
  {
    return this.value != this.defaultValue || super.isModified();
  }

  @Override
  public boolean isModified(String newValue)
  {
    try
    {
      return Integer.parseInt(newValue) != defaultValue;
    }
    catch(Exception e)
    {}
    return true;
  }

  @Override
  public void resetToDefault()
  {
    super.resetToDefault();
    value = defaultValue;
  }
}
