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
     * Main initialization phase of the mod loading process.
     * Currently unused as all initialization happens in preInit.
     * 
     * @param event The initialization event provided by Forge
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // No additional initialization required for this mod
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
        
        // Only process hotbar interactions when no GUI is displayed
        if (mc.currentScreen == null && mc.thePlayer != null) {
            // Check if the configured drop key is currently being pressed
            // Keyboard.getEventKeyState() ensures we only trigger on key press, not release
            if (mc.gameSettings.keyBindDrop.isKeyDown() && Keyboard.getEventKeyState()) {
                boolean isCtrlPressed = isCtrlKeyPressed();
                
                if (isCtrlPressed) {
                    logger.info("CTRL+Drop key combination detected in hotbar context");
                    handleHotbarCtrlQ(mc);
                }
            }
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
        // Log GUI interactions for debugging purposes
        logger.debug("GUI keyboard event fired for: " + event.gui.getClass().getSimpleName());
        
        // Creative Mode inventory is not supported due to its complex item handling
        if (event.gui.getClass().getSimpleName().equals("GuiContainerCreative")) {
            logger.debug("Creative Mode inventory detected - skipping (not supported)");
            return;
        }
        
        // Only process events for container-based GUIs (inventories, chests, furnaces, etc.)
        if (event.gui instanceof GuiContainer) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // Get the key code that was pressed and compare with drop key binding
            int pressedKey = Keyboard.getEventKey();
            int configuredDropKey = mc.gameSettings.keyBindDrop.getKeyCode();
            
            logger.debug("Key pressed: " + pressedKey + ", configured drop key: " + configuredDropKey);
            
            // Check if the pressed key matches the player's drop key binding
            if (pressedKey == configuredDropKey && Keyboard.getEventKeyState()) {
                boolean isCtrlPressed = isCtrlKeyPressed();
                logger.debug("Drop key detected, CTRL pressed: " + isCtrlPressed);
                
                if (isCtrlPressed && mc.thePlayer != null) {
                    logger.info("CTRL+Drop key combination detected in inventory context");
                    handleInventoryCtrlQ((GuiContainer) event.gui);
                    // Cancel the event to prevent normal single-item drop behavior
                    event.setCanceled(true);
                }
            }
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
            // Access the currently hovered slot using reflection
            // Try both deobfuscated and obfuscated field names for maximum compatibility
            java.lang.reflect.Field theSlotField;
            try {
                // Try deobfuscated name first (development environment)
                theSlotField = GuiContainer.class.getDeclaredField("theSlot");
            } catch (NoSuchFieldException e) {
                // Fall back to obfuscated name (production environment)
                theSlotField = GuiContainer.class.getDeclaredField("field_147006_u");
            }
            
            // Make the private field accessible
            theSlotField.setAccessible(true);
            Object theSlot = theSlotField.get(gui);
            
            if (theSlot != null) {
                Slot slot = (Slot) theSlot;
                
                // Verify the slot contains items before attempting to drop
                if (slot.getHasStack()) {
                    logger.info("Attempting to drop entire stack from slot " + slot.slotNumber + 
                               " in " + gui.getClass().getSimpleName());
                    
                    // Validate slot number is within container bounds to prevent errors
                    if (slot.slotNumber >= 0 && slot.slotNumber < gui.inventorySlots.inventorySlots.size()) {
                        Minecraft mc = Minecraft.getMinecraft();
                        
                        // Use PlayerController for proper network handling and anti-cheat compatibility
                        // Parameters: windowId, slotId, mouseButton (1=CTRL), clickType (4=DROP), player
                        mc.playerController.windowClick(
                            gui.inventorySlots.windowId,  // Container window ID
                            slot.slotNumber,              // Specific slot to interact with
                            1,                            // Mouse button 1 with CTRL modifier
                            4,                            // Click type 4 = DROP mode
                            mc.thePlayer                  // Player performing the action
                        );
                        
                        logger.info("Successfully executed stack drop via PlayerController");
                    } else {
                        logger.warn("Invalid slot number " + slot.slotNumber + 
                                   " for container with " + gui.inventorySlots.inventorySlots.size() + " slots");
                    }
                } else {
                    logger.debug("No item stack present in hovered slot");
                }
            } else {
                logger.debug("No slot currently being hovered");
            }
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
            // Get the currently selected hotbar slot (0-8)
            int selectedSlot = mc.thePlayer.inventory.currentItem;
            ItemStack heldStack = mc.thePlayer.inventory.getStackInSlot(selectedSlot);
            
            if (heldStack != null) {
                logger.info("Attempting to drop stack of " + heldStack.stackSize + 
                           " items from hotbar slot " + selectedSlot);
                
                // Convert hotbar slot (0-8) to window slot ID (36-44)
                // This is required because Minecraft's network protocol uses different numbering
                int windowSlotId = 36 + selectedSlot;
                
                // Use PlayerController for proper network handling and anti-cheat compatibility
                // Parameters: windowId (0 for player inventory), slotId, mouseButton (1=CTRL), clickType (4=DROP), player
                mc.playerController.windowClick(
                    0,                // Window ID 0 = player inventory
                    windowSlotId,     // Converted slot ID for networking
                    1,                // Mouse button 1 with CTRL modifier
                    4,                // Click type 4 = DROP mode
                    mc.thePlayer      // Player performing the action
                );
                
                logger.info("Successfully executed hotbar stack drop via PlayerController");
            } else {
                logger.debug("No item stack present in current hotbar slot");
            }
        } catch (Exception e) {
            logger.error("Failed to handle hotbar CTRL+Q operation: " + e.getMessage(), e);
        }
    }
}