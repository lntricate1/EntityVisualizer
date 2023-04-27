package me.lntricate.entityvisualizer.mixins;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.util.Color4f;
import me.lntricate.entityvisualizer.config.Configs;
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

    // MAKE THIS CUSTOMIZABLE
    double explosionBoxSize = 0.125;
    Color4f explosionBoxStroke = new Color4f(0F, 0F, 0F, 1F);
    Color4f explosionBoxFill = new Color4f(0F, 1F, 1F, 0.26F);

    if(Configs.Renderers.EXPLOSIONS.config.getBooleanValue())
      RenderHandler.addCuboid(x, y, z, explosionBoxSize, explosionBoxStroke, explosionBoxFill, Configs.Renderers.EXPLOSIONS.config.getIntegerValue());

    if(Configs.Renderers.EXPLOSION_ENTITY_RAYS.config.getBooleanValue())
    {
      List<Entity> entities = level.getEntities(null, new AABB(
        x - power*2 - 1, y - power*2 - 1, z - power*2 - 1,
        x + power*2 + 1, y + power*2 + 1, z + power*2 + 1));
      for(Entity entity : entities)
        if(Configs.Lists.EXPLOSION_ENTITY_RAYS.shouldRender(entity))
          for(Pair<Vec3, Boolean> ray : ExplosionHelper.getExposurePoints(x, y, z, power, entity))
          {
            Vec3 pos = ray.getLeft();
            Color4f hitColor = new Color4f(0, 1, 0);
            Color4f missColor = new Color4f(1, 0, 0);
            RenderHandler.addLine(x, y, z, pos.x(), pos.y(), pos.z(), ray.getRight() ? hitColor : missColor, Configs.Renderers.EXPLOSION_ENTITY_RAYS.config.getIntegerValue());
          }
    }

    if(Configs.Renderers.EXPLOSION_MIN_BLOCKS.config.getBooleanValue())
      for(Pair<BlockPos, BlockState> pair : ExplosionHelper.getAffectedBlocks(x, y, z, power, level, 0F))
        if(Configs.Lists.EXPLOSION_MIN_BLOCKS.shouldRender(pair.getRight().getBlock()))
          RenderHandler.addCuboid(pair.getLeft(), explosionBoxStroke, explosionBoxFill, Configs.Renderers.EXPLOSION_MIN_BLOCKS.config.getIntegerValue());

    if(Configs.Renderers.EXPLOSION_MAX_BLOCKS.config.getBooleanValue())
      for(Pair<BlockPos, BlockState> pair : ExplosionHelper.getAffectedBlocks(x, y, z, power, level, 1F))
        if(Configs.Lists.EXPLOSION_MAX_BLOCKS.shouldRender(pair.getRight().getBlock()))
          RenderHandler.addCuboid(pair.getLeft(), explosionBoxStroke, explosionBoxFill, Configs.Renderers.EXPLOSION_MAX_BLOCKS.config.getIntegerValue());

    if(Configs.Renderers.EXPLOSION_AFFECTED_BLOCKS.config.getBooleanValue())
      for(BlockPos pos : packet.getToBlow())
        if(Configs.Lists.EXPLOSION_AFFECTED_BLOCKS.shouldRender(level.getBlockState(pos).getBlock()))
          RenderHandler.addCuboid(pos, explosionBoxStroke, explosionBoxFill, Configs.Renderers.EXPLOSION_AFFECTED_BLOCKS.config.getIntegerValue());
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
      EntityPositionHelper.onEntityDeath(level.getEntity(id));
  }
}
