package me.lntricate.entityvisualizer.malilib.config.options;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.config.gui.EConfigOptionChangeListenerKeybind;
import me.lntricate.entityvisualizer.malilib.widgets.EConfigButtonKeybind;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;
import me.lntricate.entityvisualizer.malilib.widgets.ResetButton;

public class EConfigHotkey extends ConfigHotkey implements IEConfigValueGettable<IKeybind>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;

  public EConfigHotkey(String name, String defaultValue, String comment)
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
    return EConfigType.HOTKEY;
  }

  @Override
  public IKeybind getValue()
  {
    return getKeybind();
  }

  @Override
  public void setValue(Object value){}

  // Very sussy masa
  @Override
  public void resetToDefault()
  {
    super.resetToDefault();
    onValueChanged();
  }

  @Override
  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, ResetButton resetButton)
  {
    w -= 22;
    EConfigButtonKeybind button = new EConfigButtonKeybind(x, y, w, h, getKeybind(), configGui, widgetConfigOption);

    button.setActionListener(configGui.getButtonPressListener());
    EConfigOptionChangeListenerKeybind listener = new EConfigOptionChangeListenerKeybind(getKeybind(), button, resetButton, configGui);
    configGui.addKeybindChangeListener(listener);

    widgetConfigOption.addWidgetPublic(button);
    widgetConfigOption.addWidgetPublic(new WidgetKeybindSettings(x + w + 2, y, h, h, getKeybind(), getName(), parent, configGui.getDialogHandler()));
    widgetConfigOption.addWidgetPublic(new WidgetHoverInfo(x, y, w, h, getComment()));
  }

  @Override
  public void onValueChanged()
  {
    super.onValueChanged();
    if(parent != null)
      parent.onValueChanged();
  }
}
