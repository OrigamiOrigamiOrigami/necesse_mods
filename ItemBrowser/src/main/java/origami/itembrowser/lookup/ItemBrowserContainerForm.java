package origami.itembrowser.lookup;

import necesse.engine.localization.Localization;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormTextInput;
import necesse.gfx.forms.presets.containerComponent.ContainerForm;
import necesse.gfx.gameFont.FontOptions;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;

import java.awt.Color;
import java.util.List;

public class ItemBrowserContainerForm extends ContainerForm<ItemBrowserContainer> {

    private final FormTextInput search;
    private final FormContentBox results;
    private final FontOptions titleFont = new FontOptions(16);
    private final FontOptions lineFont = new FontOptions(12);

    public ItemBrowserContainerForm(Client client, ItemBrowserContainer container) {
        super(client, 420, 420, container);

        this.addComponent(new FormLabel(
                Localization.translate("itembrowser", "browsertitle"),
                new FontOptions(20),
                FormLabel.ALIGN_LEFT,
                8, 8, 400
        ));

        this.search = this.addComponent(new FormTextInput(8, 36, FormInputSize.SIZE_32_TO_40, 404, 80));
        this.search.placeHolder = new LocalMessage("ui", "searchtip");
        this.search.rightClickToClear = true;
        this.search.onChange(event -> refresh());

        this.results = this.addComponent(new FormContentBox(8, 80, 404, 328));
        refresh();
    }

    private void refresh() {
        this.results.getComponentList().clearComponents();
        String query = this.search.getText();
        List<Item> matches = ItemSourceIndex.searchItems(query, 12);
        int y = 4;
        if (query == null || ItemSourceIndex.normalizeForSearch(query).isEmpty()) {
            y = addLine(y, Localization.translate("itembrowser", "browserhint"), false);
        } else if (matches.isEmpty()) {
            y = addLine(y, Localization.translate("itembrowser", "browserempty"), false);
        } else {
            for (Item item : matches) {
                y = addItemBlock(y, item);
                y += 8;
            }
        }
        this.results.fitContentBoxToComponents(0, 0, 4, 4);
    }

    private int addItemBlock(int y, Item item) {
        InventoryItem inv = new InventoryItem(item);
        String title = item.getDisplayName(inv) + "  [" + item.getStringID() + "]";
        y = addLine(y, title, true);

        ItemSourceIndex.Sources sources = ItemSourceIndex.lookup(item);
        y = addSection(y, Localization.translate("itembrowser", "browserrecipes"), sources.recipes);
        y = addSection(y, Localization.translate("itembrowser", "browsershops"), sources.shops);
        y = addSection(y, Localization.translate("itembrowser", "browserdrops"), sources.drops);
        y = addSection(y, Localization.translate("itembrowser", "browsertreasure"), sources.treasures);
        y = addSection(y, Localization.translate("itembrowser", "browserbiome"), sources.biomeLoot);
        y = addSection(y, Localization.translate("itembrowser", "browserfishing"), sources.fishing);
        y = addSection(y, Localization.translate("itembrowser", "browserspawns"), sources.spawns);
        y = addSection(y, Localization.translate("itembrowser", "browsertips"), sources.obtainTips);
        if (sources.isEmpty()) {
            y = addLine(y, "  " + Localization.translate("itembrowser", "browsunknown"), false);
        }
        return y;
    }

    private int addSection(int y, String header, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return y;
        }
        y = addLine(y, header, true);
        for (String line : lines) {
            y = addLine(y, "  - " + line, false);
        }
        return y;
    }

    private int addLine(int y, String text, boolean title) {
        FormLabel label = this.results.addComponent(new FormLabel(
                text,
                title ? this.titleFont : this.lineFont,
                FormLabel.ALIGN_LEFT,
                4, y, 380
        ));
        if (!title) {
            label.setColor(new Color(220, 220, 220));
        }
        return y + label.getHeight() + 2;
    }
}
