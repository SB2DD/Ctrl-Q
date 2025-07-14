package me.polishkrowa.ctrlq;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

/**
 * Ctrl-Q Mod for Minecraft 1.8.9
 * 
 * This mod enables CTRL+Q functionality for dropping entire item stacks, 
 * addressing the common issue on macOS where CMD+Q quits the application 
 * instead of dropping items.
 * 
 * Key Features:
 * - Enables CTRL+Q to drop entire item stacks in both hotbar and inventory
 * - Client-side only implementation - no server installation required
 * - Uses proper Minecraft networking for multiplayer compatibility
 * - Anti-cheat friendly implementation using PlayerController
 * - Comprehensive error handling and logging
 * - Does not interfere with Creative Mode inventory (intentional limitation)
 * 
 * Technical Implementation:
 * - Uses Forge's event system for key input detection
 * - Reflection-based access to GUI slot information for inventory handling
 * - Direct hotbar slot manipulation for non-GUI interactions
 * - Proper network packet handling through PlayerController.windowClick()
 * 
 * @author polishkrowa
 * @version 1.9.3-1.8.9
 * @since 1.8.9
 */
@Mod(modid = CtrlQMod.MODID, version = CtrlQMod.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "1.8.9")
public class CtrlQMod {
    /** Mod identifier used by Forge */
    public static final String MODID = "ctrlq";
    
    /** Current mod version */
    public static final String VERSION = "1.9.3-1.8.9";
    
    /** Logger instance for debug and info messages */
    private static Logger logger;
    
    /** Hotbar slot offset for window click operations */
    private static final int HOTBAR_SLOT_OFFSET = 36;
    
    /** Mouse button ID for CTRL+Drop operations */
    private static final int CTRL_DROP_BUTTON = 1;
    
    /** Click type for drop operations */
    private static final int DROP_CLICK_TYPE = 4;
    
    /** Player inventory window ID */
    private static final int PLAYER_INVENTORY_WINDOW_ID = 0;
    
    /** Field names for reflection access to hovered slot */
    private static final String[] SLOT_FIELD_NAMES = {"theSlot", "field_147006_u"};
    
    /** Creative Mode GUI class name */
    private static final String CREATIVE_GUI_CLASS = "GuiContainerCreative";
    
    /** Cached reflection field for performance */
    private static Field cachedSlotField;

    /**
     * Pre-initialization phase of the mod loading process.
     * This is where we register our event handlers with the Forge event bus.
     * 
     * @param event The pre-initialization event provided by Forge
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        logger.info("Ctrl-Q mod successfully initialized for Minecraft 1.8.9");
    }


    /**
     * Handles CTRL+Q key combinations when the player is in-game with no GUI open.
     * This method is triggered by Forge's InputEvent system whenever a key is pressed
     * while the player is interacting with the hotbar.
     * 
     * The method checks for:
     * 1. No GUI is currently open (mc.currentScreen == null)
     * 2. Player exists and is valid
     * 3. Drop key is currently being pressed
     * 4. CTRL key is being held down
     * 
     * When all conditions are met, it delegates to handleHotbarCtrlQ() for actual processing.
     * 
     * @param event The input event triggered by Forge when keys are pressed
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.currentScreen != null || mc.thePlayer == null) return;
        
        if (mc.gameSettings.keyBindDrop.isKeyDown() && Keyboard.getEventKeyState() && isCtrlKeyPressed()) {
            logger.info("CTRL+Drop key combination detected in hotbar context");
            handleHotbarCtrlQ(mc);
        }
    }
    
    /**
     * Handles CTRL+Q key combinations when the player has an inventory GUI open.
     * This method is triggered by Forge's GUI event system and processes key inputs
     * specifically for container interfaces like chests, player inventory, etc.
     * 
     * The method performs several checks:
     * 1. Excludes Creative Mode inventory (not supported due to complexity)
     * 2. Verifies the GUI is a container-type interface
     * 3. Matches the pressed key against the configured drop key
     * 4. Confirms CTRL is being held down
     * 
     * When all conditions are met, it delegates to handleInventoryCtrlQ() and cancels
     * the event to prevent normal drop behavior (single item instead of stack).
     * 
     * @param event The GUI keyboard event triggered when keys are pressed in inventory
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        String guiClassName = event.gui.getClass().getSimpleName();
        logger.debug("GUI keyboard event fired for: " + guiClassName);
        
        if (CREATIVE_GUI_CLASS.equals(guiClassName)) {
            logger.debug("Creative Mode inventory detected - skipping (not supported)");
            return;
        }
        
        if (!(event.gui instanceof GuiContainer)) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        int pressedKey = Keyboard.getEventKey();
        int configuredDropKey = mc.gameSettings.keyBindDrop.getKeyCode();
        
        logger.debug("Key pressed: " + pressedKey + ", configured drop key: " + configuredDropKey);
        
        if (pressedKey == configuredDropKey && Keyboard.getEventKeyState() && 
            isCtrlKeyPressed() && mc.thePlayer != null) {
            logger.info("CTRL+Drop key combination detected in inventory context");
            handleInventoryCtrlQ((GuiContainer) event.gui);
            event.setCanceled(true);
        }
    }
    
    /**
     * Utility method to check if either CTRL key is currently being pressed.
     * Checks both left and right CTRL keys to maximize compatibility across
     * different keyboard layouts and user preferences.
     * 
     * This method uses LWJGL's direct keyboard state checking rather than
     * Minecraft's key binding system to ensure reliable detection regardless
     * of user configuration.
     * 
     * @return true if either left or right CTRL key is currently pressed
     */
    private boolean isCtrlKeyPressed() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }
    
    /**
     * Handles the actual CTRL+Q functionality for inventory container GUIs.
     * This method uses Java reflection to access the currently hovered slot
     * and then uses Minecraft's networking system to drop the entire stack.
     * 
     * The process involves:
     * 1. Using reflection to access the private 'theSlot' field from GuiContainer
     * 2. Checking if a slot is currently being hovered and contains items
     * 3. Validating the slot number is within container bounds
     * 4. Using PlayerController.windowClick() with DROP mode and CTRL modifier
     * 5. Comprehensive error handling for edge cases
     * 
     * The reflection approach is necessary because Minecraft 1.8.9 doesn't provide
     * public API access to the currently hovered slot information.
     * 
     * @param gui The inventory container GUI where the CTRL+Q was triggered
     */
    private void handleInventoryCtrlQ(GuiContainer gui) {
        try {
            Slot slot = getHoveredSlot(gui);
            if (slot == null) {
                logger.debug("No slot currently being hovered");
                return;
            }
            
            if (!slot.getHasStack()) {
                logger.debug("No item stack present in hovered slot");
                return;
            }
            
            if (!isValidSlot(slot, gui)) {
                logger.warn("Invalid slot number " + slot.slotNumber + 
                           " for container with " + gui.inventorySlots.inventorySlots.size() + " slots");
                return;
            }
            
            logger.info("Attempting to drop entire stack from slot " + slot.slotNumber + 
                       " in " + gui.getClass().getSimpleName());
            
            performWindowClick(gui.inventorySlots.windowId, slot.slotNumber);
            logger.info("Successfully executed stack drop via PlayerController");
            
        } catch (Exception e) {
            logger.error("Failed to handle inventory CTRL+Q operation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handles the actual CTRL+Q functionality for hotbar interactions.
     * This method operates when no GUI is open and the player is holding
     * items in their hotbar. It drops the entire stack from the currently
     * selected hotbar slot.
     * 
     * The process involves:
     * 1. Getting the currently selected hotbar slot (0-8)
     * 2. Checking if that slot contains any items
     * 3. Converting hotbar slot to window slot ID (36-44 for hotbar slots 0-8)
     * 4. Using PlayerController.windowClick() with proper parameters
     * 5. Comprehensive error handling and logging
     * 
     * The window slot ID conversion is necessary because Minecraft's networking
     * system uses a different slot numbering scheme than the player inventory.
     * 
     * @param mc The Minecraft client instance
     */
    private void handleHotbarCtrlQ(Minecraft mc) {
        try {
            int selectedSlot = mc.thePlayer.inventory.currentItem;
            ItemStack heldStack = mc.thePlayer.inventory.getStackInSlot(selectedSlot);
            
            if (heldStack == null) {
                logger.debug("No item stack present in current hotbar slot");
                return;
            }
            
            logger.info("Attempting to drop stack of " + heldStack.stackSize + 
                       " items from hotbar slot " + selectedSlot);
            
            int windowSlotId = HOTBAR_SLOT_OFFSET + selectedSlot;
            performWindowClick(PLAYER_INVENTORY_WINDOW_ID, windowSlotId);
            logger.info("Successfully executed hotbar stack drop via PlayerController");
            
        } catch (Exception e) {
            logger.error("Failed to handle hotbar CTRL+Q operation: " + e.getMessage(), e);
        }
    }
    
    private Slot getHoveredSlot(GuiContainer gui) throws Exception {
        Field slotField = getSlotField();
        Object theSlot = slotField.get(gui);
        return (Slot) theSlot;
    }
    
    private Field getSlotField() throws NoSuchFieldException {
        if (cachedSlotField != null) {
            return cachedSlotField;
        }
        
        for (String fieldName : SLOT_FIELD_NAMES) {
            try {
                cachedSlotField = GuiContainer.class.getDeclaredField(fieldName);
                cachedSlotField.setAccessible(true);
                return cachedSlotField;
            } catch (NoSuchFieldException ignored) {
                // Try next field name
            }
        }
        throw new NoSuchFieldException("Could not find slot field in GuiContainer");
    }
    
    private boolean isValidSlot(Slot slot, GuiContainer gui) {
        return slot.slotNumber >= 0 && slot.slotNumber < gui.inventorySlots.inventorySlots.size();
    }
    
    private void performWindowClick(int windowId, int slotId) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.playerController.windowClick(windowId, slotId, CTRL_DROP_BUTTON, DROP_CLICK_TYPE, mc.thePlayer);
    }
}