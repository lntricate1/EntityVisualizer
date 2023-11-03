package me.lntricate.entityvisualizer.malilib.config.options;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;
import me.lntricate.entityvisualizer.malilib.widgets.ResetButton;

public class EConfigOptionList extends ConfigOptionList implements IEConfigValueGettable<IConfigOptionListEntry>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  ResetButton resetButton;

  public EConfigOptionList(String name, IConfigOptionListEntry defaultValue, String comment)
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
    return EConfigType.OPTION_LIST;
  }

  @Override
  public IConfigOptionListEntry getValue()
  {
    return getOptionListValue();
  }

  @Override
  public void setValue(Object value)
  {
    setOptionListValue((IConfigOptionListEntry)value);
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, ResetButton resetButton)
  {
    this.resetButton = resetButton;
    widgetConfigOption.addWidgetPublic(new ConfigButtonOptionList(x, y, w, h, this));
    widgetConfigOption.addWidgetPublic(new WidgetHoverInfo(x, y, w, h, getComment()));
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
