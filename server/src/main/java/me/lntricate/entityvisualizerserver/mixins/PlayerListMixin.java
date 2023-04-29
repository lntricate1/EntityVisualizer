package me.lntricate.entityvisualizerserver.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizerserver.network.ServerNetworkHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class PlayerListMixin
{
  @Inject(method = "placeNewPlayer", at = @At("RETURN"))
  private void onPlayerConnected(Connection connection, ServerPlayer player, CallbackInfo ci)
  {
    ServerNetworkHandler.onPlayerJoin(player);
  }
}
