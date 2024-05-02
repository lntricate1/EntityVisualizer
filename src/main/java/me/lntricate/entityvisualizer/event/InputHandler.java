package me.lntricate.entityvisualizer.event;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import me.lntricate.entityvisualizer.EntityVisualizerMod;
import me.lntricate.entityvisualizer.config.Configs;

public class InputHandler implements IKeybindProvider, IMouseInputHandler
{
  private static final InputHandler INSTANCE = new InputHandler();

  private InputHandler()
  {
    super();
  }

  public static InputHandler getInstance()
  {
    return INSTANCE;
  }

  @Override
  public void addKeysToMap(IKeybindManager manager)
  {
    for(IHotkey hotkey : Configs.Generic.getHotkeys())
      manager.addKeybindToMap(hotkey.getKeybind());

    for(IHotkey renderer : Configs.Renderers.getHotkeys())
      manager.addKeybindToMap(renderer.getKeybind());
  }

  @Override
  public void addHotkeys(IKeybindManager manager)
  {
    manager.addHotkeysForCategory(EntityVisualizerMod.MOD_NAME, "entityvisualizer.hotkeys.category.generic_hotkeys", Configs.Generic.getHotkeys());
    manager.addHotkeysForCategory(EntityVisualizerMod.MOD_NAME, "entityvisualizer.hotkeys.category.renderer_hotkeys", Configs.Renderers.getHotkeys());
  }
}
