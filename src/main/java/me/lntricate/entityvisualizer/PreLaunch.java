package me.lntricate.entityvisualizer;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;

public class PreLaunch implements PreLaunchEntrypoint
{
  @Override
  public void onPreLaunch()
  {
    MixinExtrasBootstrap.init();
  }
}
