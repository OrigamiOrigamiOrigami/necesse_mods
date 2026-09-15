package origami.autotrash.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.inventory.Inventory;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventory;
import net.bytebuddy.asm.Advice;
import origami.autotrash.AutoTrashMod;

/**
 * When a blacklisted item lands in the player main inventory, move it into the
 * trash slot (not delete), so it can still be recovered.
 */
@ModMethodPatch(target = Inventory.class, name = "setItem",
        arguments = {int.class, InventoryItem.class, boolean.class})
public class AutoTrashInventoryPatch {

    public static final ThreadLocal<Boolean> BUSY = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    @Advice.OnMethodExit
    static void onExit(@Advice.This Inventory inventory,
                       @Advice.Argument(0) int slot,
                       @Advice.Argument(1) InventoryItem item) {
        if (BUSY.get()) {
            return;
        }
        if (!AutoTrashMod.isEnabled() || AutoTrashMod.SETTINGS == null) {
            return;
        }
        if (item == null || item.item == null || item.getAmount() <= 0) {
            return;
        }
        if (!(inventory instanceof PlayerInventory)) {
            return;
        }
        PlayerInventory pInv = (PlayerInventory) inventory;
        if (pInv.player == null || pInv.player.getInv() == null) {
            return;
        }
        // Only main inventory (includes hotbar). Skip trash/cloud/equipment.
        if (pInv != pInv.player.getInv().main) {
            return;
        }
        if (!pInv.player.isServer()) {
            return;
        }
        if (pInv.isItemLocked(slot)) {
            return;
        }
        String id = item.item.getStringID();
        if (!AutoTrashMod.SETTINGS.isBlacklisted(id)) {
            return;
        }

        PlayerInventory trash = pInv.player.getInv().trash;
        if (trash == null || trash.getSize() <= 0) {
            return;
        }

        BUSY.set(Boolean.TRUE);
        try {
            // Move into the visible trash slot so the player can undo mistakes.
            InventoryItem moving = item.copy();
            pInv.clearSlot(slot);
            trash.setItem(0, moving);
        } finally {
            BUSY.set(Boolean.FALSE);
        }
    }
}
