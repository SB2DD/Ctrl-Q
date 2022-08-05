package me.polishkrowa.ctrlq.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ContainerScreen.class) @Environment(EnvType.CLIENT)
public class MixinHandledScreen extends Screen {

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @ModifyArgs(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/ContainerScreen;onMouseClick(Lnet/minecraft/container/Slot;IILnet/minecraft/container/SlotActionType;)V"))
    private void injected(Args args) {
        args.set(2, InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), 341) || InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), 345) ? 1 : 0);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void injectedd(CallbackInfo ci) {
        this.minecraft.keyboard.enableRepeatEvents(true);
    }

}
