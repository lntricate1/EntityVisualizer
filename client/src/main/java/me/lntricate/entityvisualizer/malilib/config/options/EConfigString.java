package me.lntricate.entityvisualizer.malilib.config.options;

import javax.annotation.Nullable;

import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;
import me.lntricate.entityvisualizer.malilib.widgets.ResetButton;

public class EConfigString extends ConfigString implements IEConfigValueGettable<String>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  ResetButton resetButton;

  public EConfigString(String name, String defaultValue, String comment)
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
    return EConfigType.STRING;
  }

  @Override
  public String getValue()
  {
    return getStringValue();
  }

  @Override
  public void setValue(Object value)
  {
    setValueFromString((String)value);
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, ResetButton resetButton)
  {
    this.resetButton = resetButton;
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
      resetButton.update();
  }
}
