package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.IEntityHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends Entity
{
  public PrimedTntMixin(EntityType<? extends PrimedTnt> entityType, Level level)
  {
    super(entityType, level);
  }

  @Inject(method = "tick", at = @At("HEAD"))
  private void tick(CallbackInfo ci)
  {
    if(level.isClientSide())
      return;

    ((IEntityHelper)level).onTick((Entity)(Object)this);
  }
}
