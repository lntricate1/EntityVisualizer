package me.lntricate.entityvisualizer.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Environment(EnvType.SERVER)
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{}
