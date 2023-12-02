package me.lntricate.entityvisualizer.malilib.config.options;

import java.util.List;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;

public class EConfigRenderer extends EConfigMulti
{
  public EConfigRenderer(String name, String comment, String prettyName, List<IEConfigValueGettable<?>> configs)
  {
    super(name, comment, prettyName, configs);
    key().getKeybind().setCallback(new KeyCallbackToggleBoolean(booleanConfig()));
  }

  public EConfigRenderer(String name, String color, String comment, String prettyName)
  {
    this(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Render toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("color", color, "Render color"),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
  }

  public EConfigRenderer(String name, String color1, String color2, String comment, String prettyName)
  {
    this(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Render toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("stroke", color1, "Render fill color"),
      new EConfigColor("fill", color2, "Render stroke color"),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
  }

  public EConfigRenderer(String name, String color1, String color1Comment, String color2, String color2Comment, String comment, String prettyName)
  {
    this(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Rendering toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("stroke", color1, color1Comment),
      new EConfigColor("fill", color2, color2Comment),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
  }

  public EConfigRenderer(String name, String color1, String color1Comment, String color2, String color2Comment, String color3, String color3Comment, String comment, String prettyName)
  {
    this(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Rendering toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("stroke", color1, color1Comment),
      new EConfigColor("fill", color2, color2Comment),
      new EConfigColor("fill", color3, color3Comment),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
  }

  public EConfigBoolean booleanConfig()
  {
    return (EConfigBoolean)getConfig(0);
  }

  public boolean on()
  {
    return (Boolean)getConfig(0).getValue();
  }

  public int dur()
  {
    return (Integer)getConfig(1).getValue();
  }

  public Color4f color1()
  {
    return (Color4f)getConfig(2).getValue();
  }

  public Color4f color2()
  {
    return (Color4f)getConfig(3).getValue();
  }

  public Color4f color3()
  {
    return (Color4f)getConfig(4).getValue();
  }

  public IHotkey key()
  {
    return (EConfigHotkey)getConfig(size() - 1);
  }
}
