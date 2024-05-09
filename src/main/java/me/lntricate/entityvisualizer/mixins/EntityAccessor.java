package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor
{
  @Accessor
  public double[] getPistonDeltas();
}
