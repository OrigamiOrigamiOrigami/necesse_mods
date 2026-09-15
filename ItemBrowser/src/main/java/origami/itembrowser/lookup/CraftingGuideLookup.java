package origami.itembrowser.lookup;

import necesse.engine.network.server.ServerClient;
import necesse.engine.state.MainGame;
import necesse.gfx.forms.ComponentListContainer;
import necesse.gfx.forms.FormManager;
import necesse.gfx.forms.components.FormComponent;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventory;
import necesse.inventory.PlayerInventoryManager;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.container.item.CraftingGuideContainer;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Helpers for Crafting Guide (合成指南) quick-lookup via hotkey.
 */
public final class CraftingGuideLookup {

    public static final String GUIDE_ITEM_ID = "craftingguide";

    /** Guides whose ingredient slot holds our U-key filter (must not be given to the player). */
    private static final Set<CraftingGuideContainer> PHANTOM_FILTERS =
            Collections.newSetFromMap(new WeakHashMap<CraftingGuideContainer, Boolean>());

    private CraftingGuideLookup() {
    }

    public static void markPhantomFilter(CraftingGuideContainer guide) {
        if (guide != null) {
            PHANTOM_FILTERS.add(guide);
        }
    }

    public static boolean isPhantomFilter(CraftingGuideContainer guide) {
        return guide != null && PHANTOM_FILTERS.contains(guide);
    }

    /** Clears the filter flag. Returns true if this guide had a phantom filter. */
    public static boolean consumePhantomFilter(CraftingGuideContainer guide) {
        return guide != null && PHANTOM_FILTERS.remove(guide);
    }

    public static InventoryItem findHoveredItem(MainGame mainGame) {
        if (mainGame == null || mainGame.formManager == null) {
            return null;
        }
        return findHoveredIn(mainGame.formManager);
    }

    private static InventoryItem findHoveredIn(Object node) {
        if (node instanceof FormContainerSlot) {
            FormContainerSlot slot = (FormContainerSlot) node;
            if (slot.isHovering() && slot.isActive() && slot.getContainerSlot() != null) {
                InventoryItem item = slot.getContainerSlot().getItem();
                if (item != null && item.item != null) {
                    return item;
                }
            }
        }
        if (node instanceof ComponentListContainer) {
            @SuppressWarnings("unchecked")
            ComponentListContainer<? extends FormComponent> list =
                    (ComponentListContainer<? extends FormComponent>) node;
            for (FormComponent child : list.getComponents()) {
                InventoryItem found = findHoveredIn(child);
                if (found != null) {
                    return found;
                }
            }
        }
        if (node instanceof FormManager) {
            FormManager fm = (FormManager) node;
            for (FormComponent child : fm.getComponents()) {
                InventoryItem found = findHoveredIn(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public static boolean hasCraftingGuide(ServerClient client) {
        return findCraftingGuideSlot(client) != null;
    }

    public static PlayerInventorySlot findCraftingGuideSlot(ServerClient client) {
        if (client == null || client.playerMob == null) {
            return null;
        }
        PlayerInventoryManager inv = client.playerMob.getInv();
        PlayerInventorySlot slot = findInInventory(inv.main);
        if (slot != null) {
            return slot;
        }
        return findInInventory(inv.cloud);
    }

    private static PlayerInventorySlot findInInventory(PlayerInventory inventory) {
        if (inventory == null) {
            return null;
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            InventoryItem item = inventory.getItem(i);
            if (item != null && GUIDE_ITEM_ID.equals(item.item.getStringID())) {
                return new PlayerInventorySlot(inventory, i);
            }
        }
        return null;
    }
}
