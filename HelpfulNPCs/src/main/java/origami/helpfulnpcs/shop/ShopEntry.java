package origami.helpfulnpcs.shop;

/** Shared shop listing for Helpful NPCs vendors. */
public final class ShopEntry {
    public final String itemId;
    public final int minPrice;
    public final int maxPrice;
    public final int priceStep;
    /** Boss mob stringID, or null if always available. */
    public final String killedBossId;
    public final int maxStock;
    public final int restockAmount;

    public ShopEntry(String itemId, int minPrice, int maxPrice, int priceStep,
                     String killedBossId, int maxStock, int restockAmount) {
        this.itemId = itemId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceStep = priceStep;
        this.killedBossId = killedBossId;
        this.maxStock = maxStock;
        this.restockAmount = restockAmount;
    }

    public static ShopEntry of(String itemId, int minPrice, int maxPrice) {
        return new ShopEntry(itemId, minPrice, maxPrice, 0, null, 0, 0);
    }

    public static ShopEntry of(String itemId, int minPrice, int maxPrice, String boss) {
        return new ShopEntry(itemId, minPrice, maxPrice, 0, boss, 0, 0);
    }

    public static ShopEntry stocked(String itemId, int minPrice, int maxPrice, int priceStep,
                                    int maxStock, int restockAmount) {
        return stocked(itemId, minPrice, maxPrice, priceStep, maxStock, restockAmount, null);
    }

    public static ShopEntry stocked(String itemId, int minPrice, int maxPrice, int priceStep,
                                    int maxStock, int restockAmount, String boss) {
        return new ShopEntry(itemId, minPrice, maxPrice, priceStep, boss, maxStock, restockAmount);
    }
}
