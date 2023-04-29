package me.lntricate.entityvisualizer.malilib.config;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;

public interface IEConfigWidgetable
{
  public EConfigType eGetType();
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, @Nullable IDialogHandler dialogHandler);
  public void createResetButton(EWidgetConfigOption widgetConfigOption, int x, int y, int w, int h);
  public default void render(int x, int y, int w, int h){}
}
