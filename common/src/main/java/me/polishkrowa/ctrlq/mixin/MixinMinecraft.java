package me.polishkrowa.ctrlq.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class) @Environment(EnvType.CLIENT)
public class MixinMinecraft {

    @Redirect(method = "handleKeybinds()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z"))
    private boolean hasCtrlDown(Minecraft instance) {
        var handle = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

}