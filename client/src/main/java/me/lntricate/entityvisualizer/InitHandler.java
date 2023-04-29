package me.lntricate.entityvisualizer;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.event.InputHandler;
import me.lntricate.entityvisualizer.event.RenderHandler;
import me.lntricate.entityvisualizer.hotkeys.KeyCallbacks;

public class InitHandler implements IInitializationHandler
{
  @Override
  public void registerModHandlers()
  {
    ConfigManager.getInstance().registerConfigHandler(EntityVisualizerMod.MOD_ID, new Configs());
    InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
    InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());
    KeyCallbacks.init();

    RenderEventHandler.getInstance().registerWorldLastRenderer(RenderHandler.getInstance());
    TickHandler.getInstance().registerClientTickHandler(RenderHandler.getInstance());
  }
}
