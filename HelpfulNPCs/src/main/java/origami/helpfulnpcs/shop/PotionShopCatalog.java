package origami.helpfulnpcs.shop;

/**
 * Potion catalog: unlocks and prices scale by boss progression.
 */
public final class PotionShopCatalog {

    private PotionShopCatalog() {
    }

    public static ShopEntry[] entries() {
        return new ShopEntry[]{
                // --- Early (always) ---
                ShopEntry.stocked("healthpotion", 20, 50, 5, 40, 8),
                ShopEntry.stocked("manapotion", 20, 50, 5, 40, 8),
                ShopEntry.stocked("speedpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("healthregenpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("attackspeedpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("manaregenpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("battlepotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("resistancepotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("accuracypotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("rapidpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("strengthpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("rangerpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("minionpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("wisdompotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("passivepotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("knockbackpotion", 55, 120, 10, 18, 4),
                ShopEntry.stocked("miningpotion", 60, 130, 10, 16, 3),
                ShopEntry.stocked("fishingpotion", 60, 130, 10, 16, 3),
                ShopEntry.stocked("buildingpotion", 60, 130, 10, 16, 3),

                // --- Evils Protector ---
                ShopEntry.stocked("thornspotion", 90, 170, 10, 14, 3, "evilsprotector"),
                ShopEntry.stocked("webpotion", 90, 170, 10, 14, 3, "evilsprotector"),
                ShopEntry.stocked("fireresistancepotion", 90, 170, 10, 14, 3, "evilsprotector"),
                ShopEntry.stocked("spelunkerpotion", 100, 190, 10, 12, 3, "evilsprotector"),
                ShopEntry.stocked("trackerpotion", 100, 190, 10, 12, 3, "evilsprotector"),

                // --- Queen Spider ---
                ShopEntry.stocked("greaterhealthpotion", 130, 240, 10, 18, 4, "queenspider"),
                ShopEntry.stocked("greatermanapotion", 130, 240, 10, 18, 4, "queenspider"),
                ShopEntry.stocked("treasurepotion", 160, 280, 15, 10, 2, "queenspider"),
                ShopEntry.stocked("invisibilitypotion", 180, 300, 15, 8, 2, "queenspider"),

                // --- Void Wizard ---
                ShopEntry.stocked("greaterspeedpotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greaterhealthregenpotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greaterattackspeedpotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greatermanaregenpotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greaterbattlepotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greaterresistancepotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greateraccuracypotion", 220, 380, 15, 10, 2, "voidwizard"),
                ShopEntry.stocked("greaterrapidpotion", 220, 380, 15, 10, 2, "voidwizard"),

                // --- Swamp Guardian ---
                ShopEntry.stocked("superiorhealthpotion", 320, 520, 20, 10, 2, "swampguardian"),
                ShopEntry.stocked("superiormanapotion", 320, 520, 20, 10, 2, "swampguardian"),
                ShopEntry.stocked("greaterminingpotion", 280, 460, 20, 8, 2, "swampguardian"),
                ShopEntry.stocked("greaterfishingpotion", 280, 460, 20, 8, 2, "swampguardian"),
                ShopEntry.stocked("greaterbuildingpotion", 280, 460, 20, 8, 2, "swampguardian"),
        };
    }
}
