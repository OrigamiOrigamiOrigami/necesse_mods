package origami.helpfulnpcs.mobs;

import origami.helpfulnpcs.shop.PotionShopCatalog;
import origami.helpfulnpcs.shop.ShopEntry;

import java.util.Arrays;

public class PotionVendorMob extends HelpfulVendorMob {

    public PotionVendorMob() {
        super("hnpotionvendor", 400);
    }

    @Override
    protected Iterable<ShopEntry> shopEntries() {
        return Arrays.asList(PotionShopCatalog.entries());
    }
}
