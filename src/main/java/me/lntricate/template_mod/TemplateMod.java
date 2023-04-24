package me.lntricate.template_mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TemplateMod implements ModInitializer
{
  public static final Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "template_mod";
  public static String MOD_VERSION = "unknown";
  public static String MOD_NAME = "unknown";

  @Override
  public void onInitialize()
  {
    ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
    MOD_NAME = metadata.getName();
    MOD_VERSION = metadata.getVersion().getFriendlyString();
  }
}
