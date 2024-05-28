package me.lntricate.entityvisualizer.mixins;

import java.util.HashMap;
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
  @Unique private final HashSet<Move1> move1s = new HashSet<>();
  @Unique private final HashSet<Move2> move2s = new HashSet<>();
  @Unique private final HashSet<Cuboid> ticks = new HashSet<>();
  @Unique private final HashSet<Cuboid> deaths = new HashSet<>();
  @Unique private final HashSet<Vel> vels = new HashSet<>();
  @Unique private final HashMap<Vec3, HashSet<Cuboid>> exposures = new HashMap<>();

  @Override public HashSet<Move> moves(){return moves;}
  @Override public HashSet<Move1> move1s(){return move1s;}
  @Override public HashSet<Move2> move2s(){return move2s;}
  @Override public HashSet<Cuboid> ticks(){return ticks;}
  @Override public HashSet<Cuboid> deaths(){return deaths;}
  @Override public HashSet<Vel> vels(){return vels;}
  @Override public HashMap<Vec3, HashSet<Cuboid>> exposures(){return exposures;}

  @Inject(method = "tick", at = @At("TAIL"))
  private void afterTick(BooleanSupplier booleanSupplier, CallbackInfo ci)
  {
    ServerNetworkHandler.send((ServerLevel)(Object)this);
    moves.clear();
    move1s.clear();
    move2s.clear();
    ticks.clear();
    deaths.clear();
    vels.clear();
    exposures.clear();
  }

  @Override
  public void onMove(Vec3 pos, Vec3 delta, Entity entity, boolean noPhysics, boolean xFirst)
  {
    Move move = new Move(entity.getType().toShortString(), pos.x, pos.y, pos.z, delta.x, delta.y, delta.z, noPhysics, xFirst);
    moves.add(move);
  }

  @Override
  public void onMove(Vec3 pos, Vec3 delta, Vec3 collisionA, double collisionC, Entity entity, boolean xFirst)
  {
    Move1 move = new Move1(entity.getType().toShortString(), pos.x, pos.y, pos.z, delta.x, delta.y, delta.z,
      collisionA.x, collisionA.y, collisionA.z, collisionC, xFirst);
    move1s.add(move);
  }

  @Override
  public void onMove(Vec3 pos, Vec3 delta, Vec3 collisionA, double collisionB, Vec3 collisionC, Entity entity, boolean xFirst)
  {
    Move2 move = new Move2(entity.getType().toShortString(), pos.x, pos.y, pos.z, delta.x, delta.y, delta.z,
      collisionA.x, collisionA.y, collisionA.z, collisionB, collisionC.x, collisionC.y, collisionC.z, xFirst);
    move2s.add(move);
  }

  @Override
  public void onTick(Entity entity) {
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


  @Override
  public void onExposure(Vec3 pos, Entity entity)
  {
    Vec3 epos = entity.position();
    if(exposures.containsKey(pos))
      exposures.get(pos).add(new Cuboid(entity.getType().toShortString(), epos.x, epos.y, epos.z, entity.getBbWidth(), entity.getBbHeight()));
    else
    {
      HashSet<Cuboid> set = new HashSet<>();
      set.add(new Cuboid(entity.getType().toShortString(), epos.x, epos.y, epos.z, entity.getBbWidth(), entity.getBbHeight()));
      exposures.put(pos, set);
    }
  }
}
