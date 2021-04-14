package me.polishkrowa.ctrlqforge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ContainerScreen.class)
public class MixinHandledScreen {
    @Shadow
    @Nullable
    public Slot hoveredSlot;

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/inventory/ContainerScreen;hasControlDown()Z"), cancellable = true)
    private void injected(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        //this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber, (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 341) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 345)) ? 1 : 0, ClickType.THROW);

        this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber, InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 341) || InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 345) ? 1 : 0, ClickType.THROW);
        //this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber,  1 , ClickType.THROW);
        cir.setReturnValue(true);
    }

    @Shadow(aliases = {"handleMouseClick"})
    private void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {}

}
