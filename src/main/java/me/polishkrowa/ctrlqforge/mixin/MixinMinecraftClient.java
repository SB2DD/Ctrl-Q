package me.polishkrowa.ctrlqforge.mixin;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {

    @ModifyArgs(method = "processKeyBinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;dropItem(Z)Lnet/minecraft/entity/item/EntityItem;"))
    private void injected(Args args) {
        args.set(0, Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157));
    }
}
