package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.lntricate.entityvisualizer.network.ServerNetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 12002
//$$ import net.minecraft.server.network.CommonListenerCookie;
//#endif
import net.minecraft.server.players.PlayerList;

@Environment(EnvType.SERVER)
@Mixin(PlayerList.class)
public class PlayerListMixin
{
  @Inject(method = "placeNewPlayer", at = @At("RETURN"))
  //#if MC >= 12002
  //$$ private void onPlayerConnected(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci)
  //#else
  private void onPlayerConnected(Connection connection, ServerPlayer player, CallbackInfo ci)
  //#endif
  {
    ServerNetworkHandler.onPlayerJoin(player);
  }

  @Inject(method = "remove", at = @At("RETURN"))
  private void onPlayerDisconnected(ServerPlayer player, CallbackInfo ci)
  {
    ServerNetworkHandler.onPlayerLeave(player);
  }
}
