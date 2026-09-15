package origami.veinminer.ui;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ObjectRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormCheckBox;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.presets.containerComponent.ContainerForm;
import necesse.gfx.gameFont.FontOptions;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.gameObject.GameObject;
import origami.veinminer.VeinMinerMod;

import java.awt.Rectangle;
import java.util.List;

public class VeinMinerContainerForm extends ContainerForm<VeinMinerContainer> {

    private final FormContentBox listBox;
    private final FormCheckBox allOresBox;
    private final FontOptions lineFont = new FontOptions(12);
    private String lastListKey = "";

    public VeinMinerContainerForm(Client client, VeinMinerContainer container) {
        super(client, 360, 360, container);

        this.addComponent(new FormLabel(
                Localization.translate("veinminer", "title"),
                new FontOptions(20),
                FormLabel.ALIGN_LEFT,
                8, 8, 340
        ));

        this.addComponent(new FormLabel(
                Localization.translate("veinminer", "hint"),
                this.lineFont,
                FormLabel.ALIGN_LEFT,
                8, 34, 280
        ));

        this.addComponent(new FormContainerSlot(client, container, container.ADD_SLOT, 308, 28));

        boolean allOres = VeinMinerMod.SETTINGS == null || VeinMinerMod.SETTINGS.allOres;
        this.allOresBox = this.addComponent(new FormCheckBox(
                Localization.translate("veinminer", "allores"),
                8, 58
        ));
        this.allOresBox.checked = allOres;
        this.allOresBox.onClicked(e -> {
            container.setAllOres.runAndSend(allOresBox.checked);
        });

        this.listBox = this.addComponent(new FormContentBox(8, 88, 344, 256));
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
        if (VeinMinerMod.SETTINGS != null && this.allOresBox.checked != VeinMinerMod.SETTINGS.allOres) {
            this.allOresBox.checked = VeinMinerMod.SETTINGS.allOres;
        }
        super.draw(tickManager, perspective, renderBox);
    }

    private String listKey() {
        if (VeinMinerMod.SETTINGS == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(VeinMinerMod.SETTINGS.allOres).append('|');
        for (String id : VeinMinerMod.SETTINGS.listExtras()) {
            sb.append(id).append(',');
        }
        return sb.toString();
    }

    private void refreshList() {
        this.lastListKey = listKey();
        this.listBox.getComponentList().clearComponents();
        if (VeinMinerMod.SETTINGS == null) {
            return;
        }
        List<String> ids = VeinMinerMod.SETTINGS.listExtras();
        if (ids.isEmpty()) {
            this.listBox.addComponent(new FormLabel(
                    Localization.translate("veinminer", "empty"),
                    this.lineFont,
                    FormLabel.ALIGN_LEFT,
                    4, 4, 320
            ));
        } else {
            int cols = 8;
            int size = 36;
            int pad = 4;
            int shown = 0;
            for (int i = 0; i < ids.size(); i++) {
                final String id = ids.get(i);
                GameObject obj = ObjectRegistry.getObject(id);
                if (obj == null) {
                    continue;
                }
                ObjectItem objItem = obj.getObjectItem();
                if (objItem == null) {
                    continue;
                }
                int col = shown % cols;
                int row = shown / cols;
                int x = pad + col * (size + pad);
                int y = pad + row * (size + pad);
                InventoryItem inv = new InventoryItem(objItem, 1);
                this.listBox.addComponent(new ClickableItemIcon(x, y, inv, () -> {
                    if (VeinMinerMod.SETTINGS.removeObjectId(id)) {
                        VeinMinerMod.saveSettings();
                        refreshList();
                    }
                }));
                shown++;
            }
        }
        this.listBox.fitContentBoxToComponents(0, 0, 4, 4);
    }
}
