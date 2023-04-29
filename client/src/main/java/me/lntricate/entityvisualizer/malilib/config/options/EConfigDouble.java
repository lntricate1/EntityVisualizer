package me.lntricate.entityvisualizer.malilib.config.options;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;

public class EConfigDouble extends ConfigDouble implements IEConfigValueGettable<Double>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  ButtonGeneric resetButton;

  public EConfigDouble(String name, double defaultValue, String comment)
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
    return EConfigType.INTEGER;
  }

  @Override
  public Double getValue()
  {
    return getDoubleValue();
  }

  @Override
  public void setValue(Object value)
  {
    setDoubleValue((double)value);
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, @Nullable IDialogHandler dialogHandler)
  {
    widgetConfigOption.addWidgetPublic(new WidgetHoverInfo(x, y, w, h, getComment()));
    widgetConfigOption.addConfigTextFieldEntry(x, y, w, h, this);
  }

  @Override
  public void onValueChanged()
  {
    super.onValueChanged();
    if(parent != null)
      parent.onValueChanged();
    if(resetButton != null)
      resetButton.setEnabled(isModified());
  }

  @Override
  public void createResetButton(EWidgetConfigOption widgetConfigOption, int x, int y, int w, int h)
  {
    resetButton = widgetConfigOption.createResetButton(x, y, this);
    widgetConfigOption.addWidgetPublic(resetButton);
  }
}
