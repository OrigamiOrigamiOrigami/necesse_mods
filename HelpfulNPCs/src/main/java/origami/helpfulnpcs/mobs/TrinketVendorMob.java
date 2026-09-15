package origami.helpfulnpcs.mobs;

import origami.helpfulnpcs.shop.ShopEntry;
import origami.helpfulnpcs.shop.TrinketShopCatalog;

import java.util.Arrays;

public class TrinketVendorMob extends HelpfulVendorMob {

    public TrinketVendorMob() {
        super("hntrinketvendor", 500);
    }

    @Override
    protected Iterable<ShopEntry> shopEntries() {
        return Arrays.asList(TrinketShopCatalog.entries());
    }
}
