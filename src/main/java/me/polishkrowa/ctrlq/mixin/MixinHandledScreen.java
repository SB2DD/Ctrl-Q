package me.polishkrowa.ctrlq.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class) @Environment(EnvType.CLIENT)
public class MixinHandledScreen extends Screen {
    @Shadow @Nullable
    protected Slot focusedSlot;

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;hasControlDown()Z"), cancellable = true)
    private void injected(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        this.onMouseClick(this.focusedSlot, this.focusedSlot.id, InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 341) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 345) ? 1 : 0, SlotActionType.THROW);

        cir.setReturnValue(true);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void injectedd(CallbackInfo ci) {
        if (this.client == null)
            System.out.println("sss");
        this.client.keyboard.setRepeatEvents(true);
    }

    @Shadow(aliases = {"onMouseClick"})
    private void onMouseClick(Slot focusedSlot, int id, int i, SlotActionType aThrow) {}

}
