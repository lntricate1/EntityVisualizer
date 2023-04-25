package me.lntricate.entityvisualizer.event;

import com.google.common.collect.ImmutableList;

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
    for(Configs.Renderers renderer : Configs.Renderers.values())
      manager.addKeybindToMap(renderer.config.getKeybind());

    for(IHotkey hotkey : Configs.Generic.HOTKEY_LIST)
      manager.addKeybindToMap(hotkey.getKeybind());
  }

  @Override
  public void addHotkeys(IKeybindManager manager)
  {
    manager.addHotkeysForCategory(EntityVisualizerMod.MOD_NAME, "entityvisualizer.hotkeys.category.renderer_hotkeys", ImmutableList.copyOf(Configs.Renderers.getHotkeys()));
    manager.addHotkeysForCategory(EntityVisualizerMod.MOD_NAME, "entityvisualizer.hotkeys.category.generic_hotkeys", Configs.Generic.HOTKEY_LIST);
  }
}
