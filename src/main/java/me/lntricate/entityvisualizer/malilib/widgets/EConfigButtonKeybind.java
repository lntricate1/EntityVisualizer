package me.lntricate.entityvisualizer.malilib.widgets;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.hotkeys.IKeybind;

public class EConfigButtonKeybind extends ConfigButtonKeybind
{
  private final EWidgetConfigOption widget;

  public EConfigButtonKeybind(int x, int y, int w, int h, IKeybind keybind, @Nullable IKeybindConfigGui host, EWidgetConfigOption widget)
  {
    super(x, y, w, h, keybind, host);
    this.widget = widget;
  }

  @Override
  public void onClearSelection()
  {
    super.onClearSelection();
    widget.modify();
  }
}
