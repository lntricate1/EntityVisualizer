package me.lntricate.entityvisualizer.mixins;

import java.util.HashSet;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.IEntityHelper;
import me.lntricate.entityvisualizer.helpers.EntityHelper.*;
import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements IEntityHelper
{
  @Unique private final HashSet<Move> moves = new HashSet<>();
  @Unique private final HashSet<Cuboid> ticks = new HashSet<>();
  @Unique private final HashSet<Cuboid> deaths = new HashSet<>();
  @Unique private final HashSet<Vel> vels = new HashSet<>();

  @Override public HashSet<Move> moves(){return moves;}
  @Override public HashSet<Cuboid> ticks(){return ticks;}
  @Override public HashSet<Cuboid> deaths(){return deaths;}
  @Override public HashSet<Vel> vels(){return vels;}

  @Inject(method = "tick", at = @At("TAIL"))
  private void afterTick(BooleanSupplier booleanSupplier, CallbackInfo ci)
  {
    ServerNetworkHandler.send((ServerLevel)(Object)this);
    moves.clear();
    ticks.clear();
    deaths.clear();
    vels.clear();
  }

  @Override
  public void onMove(Vec3 pos1, Vec3 pos2, Entity entity, boolean noPhysics, boolean xFirst)
  {
    Move move = new Move(entity.getType().toShortString(), pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, noPhysics, xFirst);
    moves.add(move);
  }

  @Override
  public void onTick(Entity entity)
  {
    Vec3 pos = entity.position();
    Cuboid tick = new Cuboid(entity.getType().toShortString(), pos.x, pos.y, pos.z, entity.getBbWidth(), entity.getBbHeight());
    ticks.add(tick);
  }

  @Override
  public void onAccel(Entity entity, Vec3 vel)
  {
    Vec3 pos = entity.position();
    Vel vel1 = new Vel(entity.getType().toShortString(), pos.x, pos.y, pos.z, pos.x + vel.x, pos.y + vel.y, pos.z + vel.z);
    vels.add(vel1);
  }

  @Override
  public void onDeath(Entity entity)
  {
    Vec3 pos = entity.position();
    Cuboid death = new Cuboid(entity.getType().toShortString(), pos.x, pos.y, pos.z, entity.getBbWidth(), entity.getBbHeight());
    deaths.add(death);
  }
}
