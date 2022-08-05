package me.polishkrowa.ctrlq.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class) @Environment(EnvType.CLIENT)
public class MixinHandledScreen {
    @Shadow @Nullable public Slot focusedSlot;

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;hasControlDown()Z"), cancellable = true)
    private void injected(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        this.onMouseClick(this.focusedSlot, this.focusedSlot.id, InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 341) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 345) ? 1 : 0, SlotActionType.THROW);
        
        cir.setReturnValue(false);
    }

    @Shadow(aliases = {"onMouseClick"})
    private void onMouseClick(Slot focusedSlot, int id, int i, SlotActionType aThrow) {}

}
