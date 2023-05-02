package me.lntricate.entityvisualizer.malilib.config.options;

import java.util.List;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import fi.dy.masa.malilib.util.Color4f;

public class EConfigRenderer extends EConfigMulti
{
  public EConfigRenderer(String name, String color, String comment, String prettyName)
  {
    super(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Render toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("color", color, "Render color"),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
    ((EConfigHotkey)getConfig(3)).getKeybind().setCallback(new KeyCallbackToggleBoolean((EConfigBoolean)getConfig(0)));
  }

  public EConfigRenderer(String name, String color1, String color2, String comment, String prettyName)
  {
    super(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Render toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("stroke", color1, "Render fill color"),
      new EConfigColor("fill", color2, "Render stroke color"),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
    ((EConfigHotkey)getConfig(4)).getKeybind().setCallback(new KeyCallbackToggleBoolean((EConfigBoolean)getConfig(0)));
  }

  public EConfigRenderer(String name, String color1, String color1Comment, String color2, String color2Comment, String comment, String prettyName)
  {
    super(name, comment, prettyName, List.of(
      new EConfigBoolean("toggle", false, "Rendering toggle"),
      new EConfigInteger("duration", 100, "Render duration (ticks)"),
      new EConfigColor("stroke", color1, color1Comment),
      new EConfigColor("fill", color2, color2Comment),
      new EConfigHotkey("hotkey", "", "Render toggle hotkey")
    ));
    ((EConfigHotkey)getConfig(4)).getKeybind().setCallback(new KeyCallbackToggleBoolean((EConfigBoolean)getConfig(0)));
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

  public IHotkey key()
  {
    return (EConfigHotkey)getConfig(size() - 1);
  }
}
