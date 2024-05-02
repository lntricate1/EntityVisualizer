package me.lntricate.entityvisualizer;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;

public class ClientEntityVisualizerMod implements ClientModInitializer
{
  @Override
  public void onInitializeClient()
  {
    InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
  }
}
