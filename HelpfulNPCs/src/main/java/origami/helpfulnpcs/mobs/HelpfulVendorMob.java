package origami.helpfulnpcs.mobs;

import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.friendly.human.humanShop.HumanShop;
import necesse.entity.mobs.friendly.human.humanShop.SellingShopItem;
import necesse.inventory.InventoryItem;
import origami.helpfulnpcs.shop.ShopEntry;

import java.util.Collections;
import java.util.List;

/** Base human shop NPC used by all Helpful NPCs vendors. */
public abstract class HelpfulVendorMob extends HumanShop {

    private final int recruitCoinCost;

    public HelpfulVendorMob(String settlerStringId, int recruitCoinCost) {
        super(500, 200, settlerStringId);
        this.recruitCoinCost = recruitCoinCost;
        registerShopItems();
    }

    protected abstract Iterable<ShopEntry> shopEntries();

    private void registerShopItems() {
        for (ShopEntry entry : shopEntries()) {
            SellingShopItem shopItem;
            if (entry.maxStock > 0) {
                shopItem = this.shop.addSellingItem(
                        entry.itemId,
                        new SellingShopItem(entry.maxStock, entry.restockAmount)
                );
            } else {
                shopItem = this.shop.addSellingItem(entry.itemId, new SellingShopItem());
            }
            shopItem.setStaticPriceBasedOnHappiness(entry.minPrice, entry.maxPrice, entry.priceStep);
            if (entry.killedBossId != null) {
                shopItem.addKilledMobRequirement(entry.killedBossId);
            }
        }
    }

    @Override
    public List<InventoryItem> getRecruitItems(ServerClient client) {
        if (isTrapped()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new InventoryItem("coin", recruitCoinCost));
    }
}
