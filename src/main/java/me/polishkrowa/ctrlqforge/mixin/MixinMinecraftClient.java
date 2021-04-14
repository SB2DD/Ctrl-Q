package me.polishkrowa.ctrlqforge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Hand;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(method = "processKeyBinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;drop(Z)Z"), cancellable = true)
    private void injected(CallbackInfo ci) {
        if (!this.player.isSpectator() && this.player.drop((InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 341) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 345)))) {
            this.player.swingArm(Hand.MAIN_HAND);
        }
        ci.cancel();
    }
//(InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 341) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 345))
}
