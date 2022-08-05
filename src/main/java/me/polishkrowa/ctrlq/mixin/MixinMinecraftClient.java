package me.polishkrowa.ctrlq.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MinecraftClient.class) @Environment(EnvType.CLIENT)
public class MixinMinecraftClient {
    @Shadow @Nullable public ClientPlayerEntity player;

    @ModifyArgs(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Lnet/minecraft/entity/ItemEntity;"))
    private void injected(Args args) {
        args.set(0,InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), 341) || InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), 345));
    }

}
