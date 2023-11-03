package me.lntricate.entityvisualizer.malilib.config.options;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.button.ConfigButtonStringList;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;
import me.lntricate.entityvisualizer.malilib.widgets.ResetButton;

public class EConfigStringList extends ConfigStringList implements IEConfigValueGettable<List<String>>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  ResetButton resetButton;

  public EConfigStringList(String name, ImmutableList<String> defaultValue, String comment)
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
  public List<String> getValue()
  {
    return getStrings();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Object value)
  {
    setStrings((List<String>)value);
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, ResetButton resetButton)
  {
    this.resetButton = resetButton;
    widgetConfigOption.addWidgetPublic(new ConfigButtonStringList(x, y, w, h, this, configGui, configGui.getDialogHandler()));
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
