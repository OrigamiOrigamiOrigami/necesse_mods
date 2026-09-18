package origami.veinminer.ui;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormCheckBox;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormSlider;
import necesse.gfx.forms.components.containerSlot.FormContainerSlot;
import necesse.gfx.forms.presets.containerComponent.ContainerForm;
import necesse.gfx.gameFont.FontOptions;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.gameObject.GameObject;
import origami.veinminer.VeinMinerMod;
import origami.veinminer.VeinMinerSettings;

import java.awt.Rectangle;
import java.util.List;

public class VeinMinerContainerForm extends ContainerForm<VeinMinerContainer> {

    private final FormContentBox listBox;
    private final FormCheckBox enabledBox;
    private final FormCheckBox allOresBox;
    private final FormCheckBox allTreesBox;
    private final FormSlider maxChainSlider;
    private final FontOptions lineFont = new FontOptions(12);
    private String lastListKey = "";

    public VeinMinerContainerForm(Client client, VeinMinerContainer container) {
        super(client, 380, 460, container);

        this.addComponent(new FormLabel(
                Localization.translate("veinminer", "title"),
                new FontOptions(20),
                FormLabel.ALIGN_LEFT,
                12, 12, 300
        ));

        this.addComponent(new FormContainerSlot(client, container, container.ADD_SLOT, 324, 12));

        this.addComponent(new FormLabel(
                Localization.translate("veinminer", "hint1"),
                this.lineFont,
                FormLabel.ALIGN_LEFT,
                12, 52, 300
        ));
        this.addComponent(new FormLabel(
                Localization.translate("veinminer", "hint2"),
                this.lineFont,
                FormLabel.ALIGN_LEFT,
                12, 70, 300
        ));

        boolean enabled = VeinMinerMod.SETTINGS == null || VeinMinerMod.SETTINGS.enabled;
        this.enabledBox = this.addComponent(new FormCheckBox(
                Localization.translate("veinminer", "enabledoption"),
                12, 100
        ));
        this.enabledBox.checked = enabled;
        this.enabledBox.onClicked(e -> {
            container.setEnabled.runAndSend(enabledBox.checked);
        });

        boolean allOres = VeinMinerMod.SETTINGS == null || VeinMinerMod.SETTINGS.allOres;
        this.allOresBox = this.addComponent(new FormCheckBox(
                Localization.translate("veinminer", "allores"),
                12, 122
        ));
        this.allOresBox.checked = allOres;
        this.allOresBox.onClicked(e -> {
            container.setAllOres.runAndSend(allOresBox.checked);
        });

        boolean allTrees = VeinMinerMod.SETTINGS != null && VeinMinerMod.SETTINGS.allTrees;
        this.allTreesBox = this.addComponent(new FormCheckBox(
                Localization.translate("veinminer", "alltrees"),
                12, 144
        ));
        this.allTreesBox.checked = allTrees;
        this.allTreesBox.onClicked(e -> {
            container.setAllTrees.runAndSend(allTreesBox.checked);
        });

        int maxChain = VeinMinerMod.SETTINGS == null ? 128 : VeinMinerMod.SETTINGS.maxChain;
        this.maxChainSlider = this.addComponent(new FormSlider(
                Localization.translate("veinminer", "maxchain"),
                12, 170,
                maxChain, 8, 512, 340,
                this.lineFont
        ));
        this.maxChainSlider.drawValue = true;
        this.maxChainSlider.onChanged(e -> {
            container.setMaxChain.runAndSend(maxChainSlider.getValue());
        });

        this.listBox = this.addComponent(new FormContentBox(12, 218, 356, 226));
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
        if (VeinMinerMod.SETTINGS != null) {
            if (this.enabledBox.checked != VeinMinerMod.SETTINGS.enabled) {
                this.enabledBox.checked = VeinMinerMod.SETTINGS.enabled;
            }
            if (this.allOresBox.checked != VeinMinerMod.SETTINGS.allOres) {
                this.allOresBox.checked = VeinMinerMod.SETTINGS.allOres;
            }
            if (this.allTreesBox.checked != VeinMinerMod.SETTINGS.allTrees) {
                this.allTreesBox.checked = VeinMinerMod.SETTINGS.allTrees;
            }
            if (!this.maxChainSlider.isGrabbed()
                    && this.maxChainSlider.getValue() != VeinMinerMod.SETTINGS.maxChain) {
                this.maxChainSlider.setValue(VeinMinerMod.SETTINGS.maxChain);
            }
        }
        super.draw(tickManager, perspective, renderBox);
    }

    private String listKey() {
        if (VeinMinerMod.SETTINGS == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(VeinMinerMod.SETTINGS.enabled).append('|');
        sb.append(VeinMinerMod.SETTINGS.allOres).append('|');
        sb.append(VeinMinerMod.SETTINGS.allTrees).append('|');
        for (String id : VeinMinerMod.SETTINGS.listExtraOreItems()) {
            sb.append('i').append(id).append(',');
        }
        for (String id : VeinMinerMod.SETTINGS.listExtraStoneItems()) {
            sb.append('s').append(id).append(',');
        }
        for (String id : VeinMinerMod.SETTINGS.listExtraLogItems()) {
            sb.append('l').append(id).append(',');
        }
        for (String id : VeinMinerMod.SETTINGS.listExtras()) {
            sb.append('o').append(id).append(',');
        }
        return sb.toString();
    }

    private void refreshList() {
        this.lastListKey = listKey();
        this.listBox.getComponentList().clearComponents();
        if (VeinMinerMod.SETTINGS == null) {
            return;
        }
        List<String> oreItems = VeinMinerMod.SETTINGS.listExtraOreItems();
        List<String> stoneItems = VeinMinerMod.SETTINGS.listExtraStoneItems();
        List<String> logItems = VeinMinerMod.SETTINGS.listExtraLogItems();
        List<String> objects = VeinMinerMod.SETTINGS.listExtras();
        if (oreItems.isEmpty() && stoneItems.isEmpty() && logItems.isEmpty() && objects.isEmpty()) {
            this.listBox.addComponent(new FormLabel(
                    Localization.translate("veinminer", "empty"),
                    this.lineFont,
                    FormLabel.ALIGN_LEFT,
                    8, 8, 330
            ));
            this.listBox.fitContentBoxToComponents(0, 0, 8, 8);
            return;
        }

        int cols = 8;
        int size = 36;
        int pad = 6;
        int shown = 0;

        for (int i = 0; i < oreItems.size(); i++) {
            final String id = oreItems.get(i);
            InventoryItem inv = inventoryItemForItemId(id);
            if (inv == null) {
                continue;
            }
            shown = addIcon(shown, cols, size, pad, inv, () -> {
                if (VeinMinerMod.SETTINGS.removeOreItemId(id)) {
                    VeinMinerMod.saveSettings();
                    refreshList();
                }
            });
        }

        for (int i = 0; i < stoneItems.size(); i++) {
            final String id = stoneItems.get(i);
            InventoryItem inv = inventoryItemForItemId(id);
            if (inv == null) {
                continue;
            }
            shown = addIcon(shown, cols, size, pad, inv, () -> {
                if (VeinMinerMod.SETTINGS.removeStoneItemId(id)) {
                    VeinMinerMod.saveSettings();
                    refreshList();
                }
            });
        }

        for (int i = 0; i < logItems.size(); i++) {
            final String id = logItems.get(i);
            InventoryItem inv = inventoryItemForItemId(id);
            if (inv == null) {
                continue;
            }
            shown = addIcon(shown, cols, size, pad, inv, () -> {
                if (VeinMinerMod.SETTINGS.removeLogItemId(id)) {
                    VeinMinerMod.saveSettings();
                    refreshList();
                }
            });
        }

        for (int i = 0; i < objects.size(); i++) {
            final String id = objects.get(i);
            InventoryItem inv = inventoryItemForObjectId(id);
            if (inv == null) {
                continue;
            }
            shown = addIcon(shown, cols, size, pad, inv, () -> {
                if (VeinMinerMod.SETTINGS.removeObjectId(id)) {
                    VeinMinerMod.saveSettings();
                    refreshList();
                }
            });
        }

        this.listBox.fitContentBoxToComponents(0, 0, 8, 8);
    }

    private int addIcon(int shown, int cols, int size, int pad, InventoryItem inv, Runnable onClick) {
        int col = shown % cols;
        int row = shown / cols;
        int x = pad + col * (size + pad);
        int y = pad + row * (size + pad);
        this.listBox.addComponent(new ClickableItemIcon(x, y, inv, onClick));
        return shown + 1;
    }

    private static InventoryItem inventoryItemForItemId(String itemId) {
        if (!VeinMinerSettings.itemExists(itemId)) {
            return null;
        }
        Item item = ItemRegistry.getItem(itemId);
        if (item == null) {
            return null;
        }
        return new InventoryItem(item, 1);
    }

    private static InventoryItem inventoryItemForObjectId(String objectId) {
        if (!VeinMinerSettings.objectExists(objectId)) {
            return null;
        }
        GameObject obj = ObjectRegistry.getObject(objectId);
        if (obj == null) {
            return null;
        }
        ObjectItem objItem = obj.getObjectItem();
        if (objItem == null) {
            return null;
        }
        return new InventoryItem(objItem, 1);
    }
}
