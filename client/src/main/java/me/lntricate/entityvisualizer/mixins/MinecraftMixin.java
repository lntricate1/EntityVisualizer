package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import me.lntricate.entityvisualizer.helpers.EntityHelper;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
  @Inject(method = "tick", at = @At("HEAD"))
  private void tick(CallbackInfo ci)
  {
    EntityHelper.tick();
  }
}
