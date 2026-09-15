package origami.helpfulnpcs.settler;

import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.inventory.InventoryItem;

/** Exotic Merchant look; recruitable without blocking vanilla exotic visitors. */
public class ExoticSettler extends HelpfulSettler {

    public ExoticSettler() {
        // Same ticket weight as ExplorerSettler (100)
        super("hnexoticvendormob", "exotictip", 100);
    }

    @Override
    public void setDefaultArmor(HumanDrawOptions drawOptions, int skinColor, HumanLook look, boolean isFemale) {
        drawOptions.helmet(new InventoryItem("turban"));
        drawOptions.chestplate(new InventoryItem("exoticshirt"));
        drawOptions.boots(new InventoryItem("exoticshoes"));
    }
}
