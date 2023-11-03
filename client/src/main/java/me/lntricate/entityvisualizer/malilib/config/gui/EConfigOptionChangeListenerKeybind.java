package me.lntricate.entityvisualizer.malilib.config.gui;

import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerKeybind;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import me.lntricate.entityvisualizer.malilib.widgets.EConfigButtonKeybind;
import me.lntricate.entityvisualizer.malilib.widgets.ResetButton;

public class EConfigOptionChangeListenerKeybind extends ConfigOptionChangeListenerKeybind
{
  private final IKeybindConfigGui host;
  private final EConfigButtonKeybind buttonHotkey;
  private final ResetButton resetButton;
  private final IKeybind keybind;

  public EConfigOptionChangeListenerKeybind(IKeybind keybind, EConfigButtonKeybind buttonHotkey, ResetButton resetButton, IKeybindConfigGui host)
  {
    super(keybind, buttonHotkey, resetButton, host);
    this.buttonHotkey = buttonHotkey;
    this.resetButton = resetButton;
    this.keybind = keybind;
    this.host = host;
  }

  @Override
  public void actionPerformedWithButton(ButtonBase button, int mouseButton)
  {
    keybind.resetToDefault();
    resetButton.update();
    buttonHotkey.updateDisplayString();
    host.getButtonPressListener().actionPerformedWithButton(button, mouseButton);
  }
}
