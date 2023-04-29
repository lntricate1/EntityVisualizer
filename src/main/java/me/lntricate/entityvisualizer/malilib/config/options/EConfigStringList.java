package me.lntricate.entityvisualizer.malilib.config.options;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonStringList;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;

public class EConfigStringList extends ConfigStringList implements IEConfigValueGettable<List<String>>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  ButtonGeneric resetButton;

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
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, @Nullable IDialogHandler dialogHandler)
  {
    widgetConfigOption.addWidgetPublic(new WidgetHoverInfo(x, y, w, h, getComment()));
    widgetConfigOption.addWidgetPublic(new ConfigButtonStringList(x, y, w, h, this, configGui, dialogHandler));
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
