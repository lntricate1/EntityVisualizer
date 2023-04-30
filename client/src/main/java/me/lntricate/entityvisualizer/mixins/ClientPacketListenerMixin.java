package me.lntricate.entityvisualizer.mixins;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.config.Configs;
import me.lntricate.entityvisualizer.config.Configs.Lists;
import me.lntricate.entityvisualizer.config.Configs.Renderers;
import me.lntricate.entityvisualizer.event.RenderHandler;
import me.lntricate.entityvisualizer.helpers.EntityPositionHelper;
import me.lntricate.entityvisualizer.helpers.ExplosionHelper;
import me.lntricate.entityvisualizer.network.ClientNetworkHandler;
import me.lntricate.entityvisualizer.network.NetworkStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin
{
  @Shadow @Final private Minecraft minecraft;
  @Shadow private ClientLevel level;

  private final String mainThreadInjectionPoint = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V";

  @Inject(method = "handleCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getIdentifier()Lnet/minecraft/resources/ResourceLocation;"), cancellable = true)
  private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci)
  {
    if(packet.getIdentifier().equals(NetworkStuff.CHANNEL))
    {
      ClientNetworkHandler.handleData(packet.getData(), minecraft.player);
      ci.cancel();
    }
  }

  @Inject(method = "handleExplosion", at = @At(value = "INVOKE", target = mainThreadInjectionPoint, shift = At.Shift.AFTER))
  private void onExplosion(ClientboundExplodePacket packet, CallbackInfo ci)
  {
    double x = packet.getX();
    double y = packet.getY();
    double z = packet.getZ();
    float power = packet.getPower();

    if(Renderers.EXPLOSIONS.config.on())
      RenderHandler.addCuboid(x, y, z, Configs.Generic.EXPLOSION_BOX_SIZE.getDoubleValue()/2, Renderers.EXPLOSIONS.config.color1(), Renderers.EXPLOSIONS.config.color2(), Renderers.EXPLOSIONS.config.dur());

    if(Renderers.EXPLOSION_ENTITY_RAYS.config.on())
    {
      List<Entity> entities = level.getEntities(null, new AABB(
        x - power*2 - 1, y - power*2 - 1, z - power*2 - 1,
        x + power*2 + 1, y + power*2 + 1, z + power*2 + 1));
      for(Entity entity : entities)
        if(Lists.EXPLOSION_ENTITY_RAYS.shouldRender(entity.getType()))
          for(Pair<Vec3, Boolean> ray : ExplosionHelper.getExposurePoints(x, y, z, power, entity))
          {
            Vec3 pos = ray.getLeft();
            RenderHandler.addLine(
              x, y, z,
              pos.x(), pos.y(), pos.z(),
              ray.getRight() ? Renderers.EXPLOSION_ENTITY_RAYS.config.color1() : Renderers.EXPLOSION_ENTITY_RAYS.config.color2(),
              Renderers.EXPLOSION_ENTITY_RAYS.config.dur());
          }
    }

    if(Renderers.EXPLOSION_BLOCK_RAYS.config.on())
      for(Vec3 ray : ExplosionHelper.getBlockRays(x, y, z, power))
        RenderHandler.addLine(x, y, z, ray.x, ray.y, ray.z, Renderers.EXPLOSION_BLOCK_RAYS.config.color1(), Renderers.EXPLOSION_BLOCK_RAYS.config.dur());

    if(Renderers.EXPLOSION_MIN_BLOCKS.config.on())
      for(Pair<BlockPos, BlockState> pair : ExplosionHelper.getAffectedBlocks(x, y, z, power, level, 0F))
      {
        if(Lists.EXPLOSION_MIN_BLOCKS.shouldRender(pair.getRight().getBlock()))
          RenderHandler.addCuboid(pair.getLeft(), Renderers.EXPLOSION_MIN_BLOCKS.config.color1(), Renderers.EXPLOSION_MIN_BLOCKS.config.color2(), Renderers.EXPLOSION_MIN_BLOCKS.config.dur());
      }

    if(Renderers.EXPLOSION_MAX_BLOCKS.config.on())
      for(Pair<BlockPos, BlockState> pair : ExplosionHelper.getAffectedBlocks(x, y, z, power, level, 1F))
        if(Lists.EXPLOSION_MAX_BLOCKS.shouldRender(pair.getRight().getBlock()))
          RenderHandler.addCuboid(pair.getLeft(), Renderers.EXPLOSION_MAX_BLOCKS.config.color1(), Renderers.EXPLOSION_MAX_BLOCKS.config.color2(), Renderers.EXPLOSION_MAX_BLOCKS.config.dur());

    if(Renderers.EXPLOSION_AFFECTED_BLOCKS.config.on())
      for(BlockPos pos : packet.getToBlow())
        if(Lists.EXPLOSION_AFFECTED_BLOCKS.shouldRender(level.getBlockState(pos).getBlock()))
          RenderHandler.addCuboid(pos, Renderers.EXPLOSION_AFFECTED_BLOCKS.config.color1(), Renderers.EXPLOSION_AFFECTED_BLOCKS.config.color2(), Renderers.EXPLOSION_AFFECTED_BLOCKS.config.dur());
  }

  @Inject(method = "handleAddEntity", at = @At("TAIL"))
  private void onEntitySpawn(ClientboundAddEntityPacket packet, CallbackInfo ci)
  {
    EntityPositionHelper.onEntitySpawn(level.getEntity(packet.getId()));
  }

  @Inject(method = "handleRemoveEntities", at = @At(value = "INVOKE", target = mainThreadInjectionPoint, shift = At.Shift.AFTER))
  private void onEntityKill(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci)
  {
    for(int id : packet.getEntityIds())
      EntityPositionHelper.onEntityDeath(id);
  }
}
