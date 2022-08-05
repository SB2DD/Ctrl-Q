package me.polishkrowa.ctrlqforge.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiContainer.class)
public class MixinHandledScreen extends GuiScreen {

    @ModifyArgs(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;handleMouseClick(Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V",ordinal = 1))
    private void injected(Args args) {
        args.set(2, Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)? 1 : 0);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void injectedd(Container inventorySlotsIn, CallbackInfo ci) {
        Keyboard.enableRepeatEvents(true);
    }


    @Inject(method = "onGuiClosed()V", at = @At(value = "TAIL"))
    private void injecteddd(CallbackInfo ci) {
        Keyboard.enableRepeatEvents(false);
    }
}
