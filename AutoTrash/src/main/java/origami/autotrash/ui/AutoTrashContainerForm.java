package origami.autotrash.ui;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ItemRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.presets.containerComponent.ContainerForm;
import necesse.gfx.gameFont.FontOptions;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import origami.autotrash.AutoTrashMod;

import java.awt.Rectangle;
import java.util.List;

public class AutoTrashContainerForm extends ContainerForm<AutoTrashContainer> {

    private final FormContentBox listBox;
    private final FontOptions lineFont = new FontOptions(12);
    private String lastListKey = "";

    public AutoTrashContainerForm(Client client, AutoTrashContainer container) {
        super(client, 360, 360, container);

        this.addComponent(new FormLabel(
                Localization.translate("autotrash", "title"),
                new FontOptions(20),
                FormLabel.ALIGN_LEFT,
                8, 8, 340
        ));

        this.addComponent(new FormLabel(
                Localization.translate("autotrash", "hint"),
                this.lineFont,
                FormLabel.ALIGN_LEFT,
                8, 34, 280
        ));

        // Deposit slot: put an item here to blacklist it (stack is returned).
        this.addComponent(new FormContainerSlot(client, container, container.ADD_SLOT, 308, 28));

        this.listBox = this.addComponent(new FormContentBox(8, 72, 344, 272));
        refreshList();
    }

    @Override
    public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
        InventoryItem inSlot = this.container.getSlot(this.container.ADD_SLOT).getItem();
        if (inSlot != null && inSlot.item != null && inSlot.getAmount() > 0) {
            this.container.consumeAddSlot.runAndSend();
        }
        String key = listKey();
        if (!key.equals(this.lastListKey)) {
            refreshList();
        }
        super.draw(tickManager, perspective, renderBox);
    }

    private String listKey() {
        if (AutoTrashMod.SETTINGS == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String id : AutoTrashMod.SETTINGS.list()) {
            sb.append(id).append(',');
        }
        return sb.toString();
    }

    private void refreshList() {
        this.lastListKey = listKey();
        this.listBox.getComponentList().clearComponents();
        if (AutoTrashMod.SETTINGS == null) {
            return;
        }
        List<String> ids = AutoTrashMod.SETTINGS.list();
        if (ids.isEmpty()) {
            this.listBox.addComponent(new FormLabel(
                    Localization.translate("autotrash", "empty"),
                    this.lineFont,
                    FormLabel.ALIGN_LEFT,
                    4, 4, 320
            ));
        } else {
            int cols = 8;
            int size = 36;
            int pad = 4;
            for (int i = 0; i < ids.size(); i++) {
                final String id = ids.get(i);
                Item item = ItemRegistry.getItem(id);
                if (item == null) {
                    continue;
                }
                int col = i % cols;
                int row = i / cols;
                int x = pad + col * (size + pad);
                int y = pad + row * (size + pad);
                InventoryItem inv = new InventoryItem(item, 1);
                this.listBox.addComponent(new ClickableItemIcon(x, y, inv, () -> {
                    if (AutoTrashMod.SETTINGS.remove(id)) {
                        AutoTrashMod.saveSettings();
                        refreshList();
                    }
                }));
            }
        }
        this.listBox.fitContentBoxToComponents(0, 0, 4, 4);
    }
}
