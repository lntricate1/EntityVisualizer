package me.lntricate.entityvisualizer.malilib.config.options;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;
import me.lntricate.entityvisualizer.malilib.widgets.ResetButton;

public class EConfigColor extends ConfigColor implements IEConfigValueGettable<Color4f>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  ResetButton resetButton;

  public EConfigColor(String name, String defaultValue, String comment)
  {
    super(name, defaultValue, comment);
    this.parent = null;
  }

  @Override
  public void setParent(EConfigMulti parent)
  {
    this.parent = parent;
  }

  @Override
  public EConfigType eGetType()
  {
    return EConfigType.COLOR;
  }

  @Override
  public Color4f getValue()
  {
    return getColor();
  }

  @Override
  public void setValue(Object value){}

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, ResetButton resetButton)
  {
    this.resetButton = resetButton;
    widgetConfigOption.addWidgetPublic(new WidgetHoverInfo(x, y, w - 24, h, getComment()));
    widgetConfigOption.addConfigTextFieldEntry(x, y, w - 24, h, this);
  }

  @Override
  public void render(int x, int y, int w, int h)
  {
    // default malilib rendering, has no opacity
    // RenderUtils.drawRect(x + w - 21, y    , 19, 19, 0xFFFFFFFF);
    // RenderUtils.drawRect(x + w - 20, y + 1, 17, 17, 0xFF000000);
    // RenderUtils.drawRect(x + w - 19, y + 2, 15, 15, 0xFF000000 | getIntegerValue());

    x += w - 22;
    h -= 2;
    int X = x + h;
    int Y = y + h;
    RenderUtils.drawRect(x, y, h, 1, 0xFFFFFFFF);
    RenderUtils.drawRect(x, y, 1, h, 0xFFFFFFFF);
    RenderUtils.drawRect(x, Y, h, 1, 0xFFFFFFFF);
    RenderUtils.drawRect(X, y, 1, h+1, 0xFFFFFFFF);
    RenderUtils.drawRect(x+1, y+1, h-1, h-1, getIntegerValue());
  }

  @Override
  public void onValueChanged()
  {
    super.onValueChanged();
    if(parent != null)
      parent.onValueChanged();
    if(resetButton != null)
      resetButton.update();
  }
}
