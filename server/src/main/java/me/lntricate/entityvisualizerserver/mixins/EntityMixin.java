package me.lntricate.entityvisualizerserver.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizerserver.network.ServerNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class EntityMixin
{
  @Shadow private Vec3 position;
  @Shadow private Level level;
  @Shadow private int id;

  @Inject(method = "move", at = @At("TAIL"))
  private void endMove(MoverType moverType, Vec3 movement, CallbackInfo ci)
  {
    if(!level.isClientSide())
      ServerNetworkHandler.sendEntity((ServerLevel)level, id, position, moverType == MoverType.SELF || moverType == MoverType.PLAYER, movement.x >= movement.z);
  }
}
