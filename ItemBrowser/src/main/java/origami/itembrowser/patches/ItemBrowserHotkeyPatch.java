package origami.itembrowser.patches;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Input;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.engine.window.GameWindow;
import necesse.inventory.InventoryItem;
import net.bytebuddy.asm.Advice;
import origami.itembrowser.ItemBrowserMod;
import origami.itembrowser.lookup.CraftingGuideLookup;
import origami.itembrowser.lookup.ItemBrowserContainer;
import origami.itembrowser.network.PacketCraftingGuideLookup;
import origami.itembrowser.network.PacketOpenItemBrowser;

@ModMethodPatch(target = MainGame.class, name = "frameTick",
        arguments = {TickManager.class, GameWindow.class})
public class ItemBrowserHotkeyPatch {

    @Advice.OnMethodEnter
    static void onEnter(@Advice.This MainGame mainGame) {
        if (Input.isTyping) {
            return;
        }
        Client client = mainGame.getClient();
        if (client == null || client.getPlayer() == null) {
            return;
        }

        // ~ : toggle Item Browser
        if (ItemBrowserMod.OPEN_BROWSER != null && ItemBrowserMod.OPEN_BROWSER.isPressed()) {
            if (client.getContainer() instanceof ItemBrowserContainer) {
                client.closeContainer(true);
            } else {
                client.network.sendPacket(new PacketOpenItemBrowser());
            }
        }

        // U : open Crafting Guide with hovered item as ingredient (requires craftingguide)
        if (ItemBrowserMod.CRAFTING_GUIDE_LOOKUP != null
                && ItemBrowserMod.CRAFTING_GUIDE_LOOKUP.isPressed()) {
            InventoryItem hovered = CraftingGuideLookup.findHoveredItem(mainGame);
            if (hovered != null && hovered.item != null) {
                String id = hovered.item.getStringID();
                if (id != null && !CraftingGuideLookup.GUIDE_ITEM_ID.equals(id)) {
                    client.network.sendPacket(new PacketCraftingGuideLookup(id));
                }
            }
        }
    }
}
