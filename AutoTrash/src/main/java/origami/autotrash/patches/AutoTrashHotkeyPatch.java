package origami.autotrash.patches;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.Input;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.engine.window.GameWindow;
import necesse.gfx.forms.ComponentListContainer;
import necesse.gfx.forms.FormManager;
import necesse.gfx.forms.components.FormComponent;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.inventory.InventoryItem;
import net.bytebuddy.asm.Advice;
import origami.autotrash.AutoTrashMod;
import origami.autotrash.network.PacketOpenAutoTrash;
import origami.autotrash.ui.AutoTrashContainer;

import java.awt.Color;

@ModMethodPatch(target = MainGame.class, name = "frameTick",
        arguments = {TickManager.class, GameWindow.class})
public class AutoTrashHotkeyPatch {

    @Advice.OnMethodEnter
    static void onEnter(@Advice.This MainGame mainGame) {
        if (Input.isTyping) {
            return;
        }
        Client client = mainGame.getClient();
        if (client == null || client.getPlayer() == null) {
            return;
        }

        if (AutoTrashMod.TOGGLE != null && AutoTrashMod.TOGGLE.isPressed()) {
            AutoTrashMod.toggleAndSave(client);
        }

        if (AutoTrashMod.OPEN_LIST != null && AutoTrashMod.OPEN_LIST.isPressed()) {
            if (client.getContainer() instanceof AutoTrashContainer) {
                client.closeContainer(true);
            } else {
                client.network.sendPacket(new PacketOpenAutoTrash());
            }
        }

        if (AutoTrashMod.ADD_HOVERED != null && AutoTrashMod.ADD_HOVERED.isPressed()
                && AutoTrashMod.SETTINGS != null) {
            InventoryItem hovered = findHovered(mainGame.formManager);
            if (hovered != null && hovered.item != null) {
                String id = hovered.item.getStringID();
                if (id != null && AutoTrashMod.SETTINGS.add(id)) {
                    AutoTrashMod.saveSettings();
                    client.setMessage(
                            Localization.translate("autotrash", "added") + " " + id,
                            new Color(120, 220, 120),
                            2.5f
                    );
                }
            }
        }
    }

    // Must be public: Advice is inlined into MainGame and cannot call private helpers.
    public static InventoryItem findHovered(Object node) {
        if (node instanceof FormContainerSlot) {
            FormContainerSlot slot = (FormContainerSlot) node;
            if (slot.isHovering() && slot.isActive() && slot.getContainerSlot() != null) {
                return slot.getContainerSlot().getItem();
            }
        }
        if (node instanceof ComponentListContainer) {
            @SuppressWarnings("unchecked")
            ComponentListContainer<? extends FormComponent> list =
                    (ComponentListContainer<? extends FormComponent>) node;
            for (FormComponent child : list.getComponents()) {
                InventoryItem found = findHovered(child);
                if (found != null) {
                    return found;
                }
            }
        }
        if (node instanceof FormManager) {
            for (FormComponent child : ((FormManager) node).getComponents()) {
                InventoryItem found = findHovered(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
