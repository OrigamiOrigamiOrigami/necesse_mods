package origami.helpfulnpcs.shop;

import necesse.engine.journal.JournalEntry;
import necesse.engine.journal.JournalEntry.MobJournalData;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.JournalRegistry;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.consumableItem.ChangeTrinketSlotsItem;
import necesse.inventory.lootTable.LootList;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vanilla trinket catalog with boss unlock gates.
 * Only sells items that come from mob drops or chests (adventure journal),
 * never craftables or trinket-slot unlock consumables.
 */
public final class TrinketShopCatalog {

    private static final Object CACHE_LOCK = new Object();
    private static volatile Set<Integer> dropOrChestItemIds;

    private TrinketShopCatalog() {
    }

    public static ShopEntry[] entries() {
        ShopEntry[] raw = rawEntries();
        ArrayList<ShopEntry> filtered = new ArrayList<ShopEntry>(raw.length);
        for (ShopEntry entry : raw) {
            if (shouldSell(entry.itemId)) {
                filtered.add(entry);
            }
        }
        return filtered.toArray(new ShopEntry[0]);
    }

    /**
     * Sell only drop/chest loot: not craftable, not slot unlocks,
     * and present in adventure-journal mob drops or treasure lists.
     */
    public static boolean shouldSell(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }
        Item item = ItemRegistry.getItem(itemId);
        if (item == null) {
            return false;
        }
        if (item instanceof ChangeTrinketSlotsItem) {
            return false;
        }
        try {
            List<Recipe> recipes = Recipes.getRecipesFromResult(item.getID());
            if (recipes != null && !recipes.isEmpty()) {
                return false;
            }
        } catch (Throwable ignored) {
            return false;
        }
        return isDropOrChestLoot(item.getID());
    }

    private static boolean isDropOrChestLoot(int itemId) {
        Set<Integer> allowed = dropOrChestItemIds;
        if (allowed == null) {
            synchronized (CACHE_LOCK) {
                allowed = dropOrChestItemIds;
                if (allowed == null) {
                    allowed = buildDropOrChestIndex();
                    // Only cache a successful index so early init can retry.
                    if (!allowed.isEmpty()) {
                        dropOrChestItemIds = allowed;
                        System.out.println("[HelpfulNPCs] Trinket shop drop/chest index: "
                                + allowed.size() + " items");
                    }
                }
            }
        }
        return allowed.contains(itemId);
    }

    private static Set<Integer> buildDropOrChestIndex() {
        HashSet<Integer> ids = new HashSet<Integer>();
        try {
            for (JournalEntry entry : JournalRegistry.getJournalEntries()) {
                if (entry == null) {
                    continue;
                }
                if (entry.mobsData != null) {
                    for (MobJournalData mobData : entry.mobsData) {
                        addLootIds(ids, mobData.itemDrops);
                    }
                }
                addLootIds(ids, entry.treasuresData);
            }
        } catch (Throwable t) {
            System.err.println("[HelpfulNPCs] Failed building drop/chest index: " + t);
        }
        return ids;
    }

    private static void addLootIds(Set<Integer> ids, LootList list) {
        if (list == null) {
            return;
        }
        for (Integer id : list.getItemIDs()) {
            if (id != null) {
                ids.add(id);
            }
        }
    }

    private static ShopEntry[] rawEntries() {
        return new ShopEntry[]{
                ShopEntry.of("regenpendant", 160, 280),
                ShopEntry.of("shinebelt", 140, 240),
                ShopEntry.of("fuzzydice", 180, 320),
                ShopEntry.of("trackerboot", 160, 300),
                ShopEntry.of("fins", 180, 320),
                ShopEntry.of("spikedboots", 220, 380),
                ShopEntry.of("leatherdashers", 280, 450),
                ShopEntry.of("leatherglove", 180, 320),
                ShopEntry.of("calmingrose", 160, 300),
                ShopEntry.of("itemattractor", 220, 400),
                ShopEntry.of("constructionhammer", 180, 320),
                ShopEntry.of("toolextender", 220, 380),
                ShopEntry.of("telescopicladder", 220, 380),
                ShopEntry.of("woodshield", 160, 300),
                ShopEntry.of("forceofwind", 280, 480),
                ShopEntry.of("miningcharm", 350, 550, "evilsprotector"),
                ShopEntry.of("diggingclaw", 380, 600, "evilsprotector"),
                ShopEntry.of("minersbouquet", 420, 650, "evilsprotector"),
                ShopEntry.of("calmingminersbouquet", 520, 780, "evilsprotector"),
                ShopEntry.of("hardenedshield", 380, 600, "evilsprotector"),
                ShopEntry.of("cactusshield", 420, 650, "evilsprotector"),
                ShopEntry.of("demonclaw", 350, 580, "evilsprotector"),
                ShopEntry.of("firestone", 350, 580, "evilsprotector"),
                ShopEntry.of("froststone", 350, 580, "evilsprotector"),
                ShopEntry.of("meleefoci", 480, 720, "evilsprotector"),
                ShopEntry.of("rangefoci", 480, 720, "evilsprotector"),
                ShopEntry.of("magicfoci", 480, 720, "evilsprotector"),
                ShopEntry.of("summonfoci", 480, 720, "evilsprotector"),
                ShopEntry.of("balancedfoci", 650, 950, "evilsprotector"),
                ShopEntry.of("mesmertablet", 420, 650, "evilsprotector"),
                ShopEntry.of("inducingamulet", 520, 780, "evilsprotector"),
                ShopEntry.of("spidercharm", 550, 850, "queenspider"),
                ShopEntry.of("shellofretribution", 700, 1050, "queenspider"),
                ShopEntry.of("spikedbatboots", 600, 920, "queenspider"),
                ShopEntry.of("zephyrcharm", 550, 850, "queenspider"),
                ShopEntry.of("zephyrboots", 700, 1050, "queenspider"),
                ShopEntry.of("windboots", 650, 980, "queenspider"),
                ShopEntry.of("explorercloak", 700, 1050, "queenspider"),
                ShopEntry.of("explorersatchel", 700, 1050, "queenspider"),
                ShopEntry.of("travelercloak", 600, 920, "queenspider"),
                ShopEntry.of("mobilitycloak", 800, 1200, "queenspider"),
                ShopEntry.of("toolbox", 850, 1300, "queenspider"),
                ShopEntry.of("minersprosthetic", 800, 1200, "queenspider"),
                ShopEntry.of("dreamcatcher", 850, 1300, "voidwizard"),
                ShopEntry.of("magicmanual", 950, 1450, "voidwizard"),
                ShopEntry.of("spellstone", 1050, 1600, "voidwizard"),
                ShopEntry.of("sparegemstones", 950, 1450, "voidwizard"),
                ShopEntry.of("prophecyslab", 950, 1450, "voidwizard"),
                ShopEntry.of("scryingmirror", 1050, 1600, "voidwizard"),
                ShopEntry.of("hysteriatablet", 1150, 1750, "voidwizard"),
                ShopEntry.of("blinkscepter", 1250, 1900, "voidwizard"),
                ShopEntry.of("voidphasingstaff", 1450, 2200, "voidwizard"),
                ShopEntry.of("tungstenshield", 1050, 1600, "voidwizard"),
                ShopEntry.of("claygauntlet", 850, 1300, "voidwizard"),
                ShopEntry.of("vambrace", 850, 1300, "voidwizard"),
                ShopEntry.of("chainshirt", 950, 1450, "voidwizard"),
                ShopEntry.of("manica", 1150, 1750, "voidwizard"),
                ShopEntry.of("lifependant", 1200, 1800, "swampguardian"),
                ShopEntry.of("frozensoul", 1600, 2400, "swampguardian"),
                ShopEntry.of("frozenwave", 1300, 2000, "swampguardian"),
                ShopEntry.of("frozenheart", 1400, 2200, "swampguardian"),
                ShopEntry.of("lifeline", 1600, 2400, "swampguardian"),
                ShopEntry.of("polarclaw", 1100, 1700, "swampguardian"),
                ShopEntry.of("frostflame", 1300, 2000, "swampguardian"),
                ShopEntry.of("balancedfrostfirefoci", 1600, 2400, "swampguardian"),
                ShopEntry.of("parrybuckler", 1200, 1800, "swampguardian"),
                ShopEntry.of("siphonshield", 1300, 2000, "swampguardian"),
                ShopEntry.of("ancientfeather", 1500, 2300, "ancientvulture"),
                ShopEntry.of("ancientrelics", 1900, 2900, "ancientvulture"),
                ShopEntry.of("airvessel", 1700, 2600, "ancientvulture"),
                ShopEntry.of("templependant", 1600, 2500, "ancientvulture"),
                ShopEntry.of("secondwindcharm", 1700, 2600, "ancientvulture"),
                ShopEntry.of("ghostboots", 1800, 2800, "ancientvulture"),
                ShopEntry.of("hoverboots", 1900, 2900, "ancientvulture"),
                ShopEntry.of("kineticboots", 2200, 3300, "ancientvulture"),
                ShopEntry.of("guardianshell", 2000, 3100, "ancientvulture"),
                ShopEntry.of("guardianbracelet", 1900, 2900, "ancientvulture"),
                ShopEntry.of("challengerspauldron", 2200, 3300, "ancientvulture"),
                ShopEntry.of("piratesheath", 2400, 3600, "piratecaptain"),
                ShopEntry.of("ninjasmark", 2200, 3400, "piratecaptain"),
                ShopEntry.of("foolsgambit", 2800, 4200, "piratecaptain"),
                ShopEntry.of("assassinscowl", 2000, 3100, "piratecaptain"),
                ShopEntry.of("ammobox", 1800, 2800, "piratecaptain"),
                ShopEntry.of("magicalquiver", 1800, 2800, "piratecaptain"),
                ShopEntry.of("luckycape", 1900, 2900, "piratecaptain"),
                ShopEntry.of("noblehorseshoe", 1700, 2600, "piratecaptain"),
                ShopEntry.of("bonehilt", 1800, 2800, "piratecaptain"),
                ShopEntry.of("piratetelescope", 1600, 2500, "piratecaptain"),
                ShopEntry.of("nightmaretalisman", 2200, 3400, "piratecaptain"),
                ShopEntry.of("scryingcards", 2200, 3400, "piratecaptain"),
                ShopEntry.of("forbiddenspellbook", 2400, 3600, "piratecaptain"),
                ShopEntry.of("crystalshield", 2100, 3200, "piratecaptain"),
                ShopEntry.of("frenzyorb", 2600, 3900, "reaper"),
                ShopEntry.of("clockworkheart", 2800, 4200, "reaper"),
                ShopEntry.of("vampiresgift", 2400, 3600, "reaper"),
                ShopEntry.of("bloodstonering", 2600, 3900, "reaper"),
                ShopEntry.of("necroticsoulskull", 2800, 4300, "reaper"),
                ShopEntry.of("agedchampionshield", 3000, 4500, "reaper"),
                ShopEntry.of("agedchampionscabbard", 2800, 4300, "reaper"),
                ShopEntry.of("carapaceshield", 2600, 3900, "reaper"),
                ShopEntry.of("companionlocket", 2600, 3900, "reaper"),
                ShopEntry.of("summonersbestiary", 2800, 4300, "reaper"),
                ShopEntry.of("moonshield", 3200, 4800, "cryoqueen"),
                ShopEntry.of("willowisplantern", 2800, 4200, "cryoqueen"),
                ShopEntry.of("spiritgreaves", 3000, 4600, "cryoqueen"),
                ShopEntry.of("spiritboard", 3200, 4800, "cryoqueen"),
                ShopEntry.of("gelatincasings", 2600, 4000, "cryoqueen"),
                ShopEntry.of("cavelingsfoot", 3400, 5200, "pestwarden"),
                ShopEntry.of("cavelingscollection", 3600, 5500, "pestwarden"),
                ShopEntry.of("essenceofperspective", 3200, 4900, "pestwarden"),
                ShopEntry.of("essenceofprolonging", 3200, 4900, "pestwarden"),
                ShopEntry.of("essenceofrebirth", 4000, 6000, "pestwarden"),
                ShopEntry.of("jonasgambit", 4800, 7200, "sageandgrit"),
                ShopEntry.of("wormholelocket", 5500, 8200, "fallenwizard"),
        };
    }
}
