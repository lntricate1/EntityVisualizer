package me.lntricate.entityvisualizer.malilib.gui;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.gui.ButtonPressDirtyListenerSimple;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerKeybind;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IConfigInfoProvider;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetConfigOption;
import me.lntricate.entityvisualizer.malilib.widgets.EWidgetListConfigOptions;
import net.minecraft.client.gui.screens.Screen;

public abstract class EGuiConfigsBase extends EGuiListBase<ConfigOptionWrapper, EWidgetConfigOption, EWidgetListConfigOptions> implements IKeybindConfigGui
{
  protected final List<ConfigOptionChangeListenerKeybind> hotkeyChangeListeners = new ArrayList<>();
  protected final ButtonPressDirtyListenerSimple dirtyListener = new ButtonPressDirtyListenerSimple();
  protected final String modId;
  protected final List<String> initialConfigValues = new ArrayList<>();
  protected ConfigButtonKeybind activeKeybindButton;
  protected int configWidth = 204;
  @Nullable protected Screen parentScreen;
  @Nullable protected IConfigInfoProvider hoverInfoProvider;
  @Nullable protected IDialogHandler dialogHandler;

  public EGuiConfigsBase(int listX, int listY, String modId, @Nullable Screen parent, String titleKey, Object... args)
  {
    super(listX, listY);

    this.modId = modId;
    this.parentScreen = parent;
    this.title = StringUtils.translate(titleKey, args);
  }

  @Override
  protected int getBrowserWidth()
  {
    return this.width - 20;
  }

  @Override
  protected int getBrowserHeight()
  {
    return this.height - 80;
  }

  protected boolean useKeybindSearch()
  {
    return false;
  }

  protected int getConfigWidth()
  {
    return this.configWidth;
  }

  public void setParentGui(Screen parent)
  {
    this.parentScreen = parent;
  }

  public EGuiConfigsBase setConfigWidth(int configWidth)
  {
    this.configWidth = configWidth;
    return this;
  }

  public EGuiConfigsBase setHoverInfoProvider(IConfigInfoProvider provider)
  {
    this.hoverInfoProvider = provider;
    return this;
  }

  @Override
  public IDialogHandler getDialogHandler()
  {
    return this.dialogHandler;
  }

  public void setDialogHandler(IDialogHandler handler)
  {
    this.dialogHandler = handler;
  }

  @Override
  public String getModId()
  {
    return this.modId;
  }

  @Override
  @Nullable
  public IConfigInfoProvider getHoverInfoProvider()
  {
    return this.hoverInfoProvider;
  }

  @Override
  protected EWidgetListConfigOptions createListWidget(int listX, int listY)
  {
    return new EWidgetListConfigOptions(listX, listY,
      this.getBrowserWidth(), this.getBrowserHeight(), this.getConfigWidth(), this.getBlitOffset(), this.useKeybindSearch(), this);
  }

  @Override
  public void initGui()
  {
    super.initGui();

    minecraft.keyboardHandler.setSendRepeatsToGui(true);
  }

  @Override
  public void removed()
  {
    if (this.getListWidget().wereConfigsModified())
    {
      this.getListWidget().applyPendingModifications();
      this.onSettingsChanged();
      this.getListWidget().clearConfigsModifiedFlag();
    }

    minecraft.keyboardHandler.setSendRepeatsToGui(false);
  }

  protected void onSettingsChanged()
  {
    ConfigManager.getInstance().onConfigsChanged(this.modId);

    if (this.hotkeyChangeListeners.size() > 0)
      InputEventHandler.getKeybindManager().updateUsedKeys();
  }

  @Override
  public boolean onKeyTyped(int keyCode, int scanCode, int modifiers)
  {
    if (this.activeKeybindButton != null)
    {
      this.activeKeybindButton.onKeyPressed(keyCode);
      return true;
    }
    else
    {
      if (this.getListWidget().onKeyTyped(keyCode, scanCode, modifiers))
        return true;

      if (keyCode == KeyCodes.KEY_ESCAPE && this.parentScreen != GuiUtils.getCurrentScreen())
      {
        GuiBase.openGui(this.parentScreen);
        return true;
      }

      return false;
    }
  }

  @Override
  public boolean onCharTyped(char charIn, int modifiers)
  {
    if (this.activeKeybindButton != null)
    {
      // Prevents the chars leaking into the search box, if we didn't pretend to handle them here
      return true;
    }

    if (this.getListWidget().onCharTyped(charIn, modifiers))
      return true;

    return super.onCharTyped(charIn, modifiers);
  }

  @Override
  public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
  {
    if (super.onMouseClicked(mouseX, mouseY, mouseButton))
      return true;

    // When clicking on not-a-button, clear the selection
    if (this.activeKeybindButton != null)
    {
      this.activeKeybindButton.onClearSelection();
      this.setActiveKeybindButton(null);
      return true;
    }

    return false;
  }

  @Override
  public void clearOptions()
  {
    this.setActiveKeybindButton(null);
    this.hotkeyChangeListeners.clear();
  }

  @Override
  public void addKeybindChangeListener(ConfigOptionChangeListenerKeybind listener)
  {
    this.hotkeyChangeListeners.add(listener);
  }

  @Override
  public ButtonPressDirtyListenerSimple getButtonPressListener()
  {
    return this.dirtyListener;
  }

  @Override
  public void setActiveKeybindButton(@Nullable ConfigButtonKeybind button)
  {
    if (this.activeKeybindButton != null)
    {
      this.activeKeybindButton.onClearSelection();
      this.updateKeybindButtons();
    }

    this.activeKeybindButton = button;

    if (this.activeKeybindButton != null)
      this.activeKeybindButton.onSelected();
  }

  protected void updateKeybindButtons()
  {
    for (ConfigOptionChangeListenerKeybind listener : this.hotkeyChangeListeners)
      listener.updateButtons();
  }
}
