package me.lntricate.entityvisualizer.malilib.config.options;

import javax.annotation.Nullable;

import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerKeybind;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetHoverInfo;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import me.lntricate.entityvisualizer.malilib.config.EConfigType;
import me.lntricate.entityvisualizer.malilib.config.IEConfigValueGettable;
import me.lntricate.entityvisualizer.malilib.config.IEConfigWidgetable;
import me.lntricate.entityvisualizer.malilib.widgets.EConfigButtonKeybind;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;

public class EConfigHotkey extends ConfigHotkey implements IEConfigValueGettable<IKeybind>, IEConfigWidgetable
{
  @Nullable private EConfigMulti parent;
  @Nullable private ButtonGeneric resetButton;
  @Nullable private ConfigButtonKeybind button;

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

  public void createWidgets(EWidgetConfigOption widgetConfigOption, WidgetListConfigOptionsBase<?, ?> parent, int x, int y, int w, int h, IKeybindConfigGui configGui, @Nullable IDialogHandler dialogHandler)
  {
    resetButton = widgetConfigOption.createResetButton(x, y, this);
    w -= 22;
    EConfigButtonKeybind button = new EConfigButtonKeybind(x, y, w, h, getKeybind(), configGui, widgetConfigOption);

    button.setActionListener(configGui.getButtonPressListener());
    ConfigOptionChangeListenerKeybind listener = new ConfigOptionChangeListenerKeybind(getKeybind(), button, resetButton, configGui);
    configGui.addKeybindChangeListener(listener);
    resetButton.setActionListener(listener);

    widgetConfigOption.addWidgetPublic(button);
    widgetConfigOption.addWidgetPublic(new WidgetKeybindSettings(x + w + 2, y, h, h, getKeybind(), getName(), parent, dialogHandler));
    widgetConfigOption.addWidgetPublic(new WidgetHoverInfo(x, y, w, h, getComment()));
  }

  @Override
  public void onValueChanged()
  {
    super.onValueChanged();
    if(parent != null)
      parent.onValueChanged();
  }

  @Override
  public void createResetButton(EWidgetConfigOption widgetConfigOption, int x, int y, int w, int h)
  {
    widgetConfigOption.addWidgetPublic(resetButton);
  }
}
