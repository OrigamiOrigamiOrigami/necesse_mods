package origami.itembrowser.lookup;

import necesse.engine.journal.JournalEntry;
import necesse.engine.journal.JournalEntry.MobJournalData;
import necesse.engine.localization.Localization;
import necesse.engine.registries.BiomeRegistry;
import necesse.engine.registries.ClassIDDataContainer;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.JournalRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.util.GameBlackboard;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.friendly.human.humanShop.HumanShop;
import necesse.entity.mobs.friendly.human.humanShop.SellingShopItem;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.ObtainTip;
import necesse.inventory.item.placeableItem.FireflySpawnItem;
import necesse.inventory.item.placeableItem.FlyingBugSpawnItem;
import necesse.inventory.item.placeableItem.MobSpawnItem;
import necesse.inventory.lootTable.LootList;
import necesse.inventory.lootTable.LootTable;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import necesse.level.maps.biomes.Biome;
import necesse.level.maps.biomes.FishingLootTable;
import necesse.level.maps.biomes.MobChance;
import necesse.level.maps.biomes.MobSpawnTable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reverse lookup for recipes, shops, mob drops, treasure, biome loot,
 * fishing, and wildlife/mob spawn locations.
 * Search remains items-only (ItemRegistry).
 */
public final class ItemSourceIndex {

    public static final class Sources {
        public final List<String> recipes = new ArrayList<String>();
        public final List<String> shops = new ArrayList<String>();
        public final List<String> drops = new ArrayList<String>();
        public final List<String> treasures = new ArrayList<String>();
        public final List<String> biomeLoot = new ArrayList<String>();
        public final List<String> fishing = new ArrayList<String>();
        public final List<String> spawns = new ArrayList<String>();
        public final List<String> challenges = new ArrayList<String>();
        public final List<String> obtainTips = new ArrayList<String>();

        public boolean isEmpty() {
            return recipes.isEmpty() && shops.isEmpty() && drops.isEmpty()
                    && treasures.isEmpty() && biomeLoot.isEmpty() && fishing.isEmpty()
                    && spawns.isEmpty() && challenges.isEmpty() && obtainTips.isEmpty();
        }
    }

    private static final Object INDEX_LOCK = new Object();
    private static volatile boolean indexReady = false;
    private static final Map<Integer, LinkedHashSet<String>> shopByItem = new HashMap<Integer, LinkedHashSet<String>>();
    private static final Map<Integer, LinkedHashSet<String>> dropByItem = new HashMap<Integer, LinkedHashSet<String>>();
    private static final Map<Integer, LinkedHashSet<String>> fishByItem = new HashMap<Integer, LinkedHashSet<String>>();
    /** Mob stringID -> biome / layer labels */
    private static final Map<String, LinkedHashSet<String>> spawnByMob = new HashMap<String, LinkedHashSet<String>>();

    private ItemSourceIndex() {
    }

    public static Sources lookup(Item item) {
        ensureIndex();
        Sources sources = new Sources();
        if (item == null) {
            return sources;
        }
        int itemId = item.getID();
        String stringId = item.getStringID();

        addRecipes(sources, itemId);
        addFromMap(sources.shops, shopByItem.get(itemId));
        addJournalSources(sources, itemId);
        addFromMap(sources.drops, dropByItem.get(itemId));
        addFromMap(sources.fishing, fishByItem.get(itemId));
        addSpawnSources(sources, item);
        if (item instanceof ObtainTip) {
            String tip = ((ObtainTip) item).getObtainTip().translate();
            if (tip != null && !tip.isEmpty()) {
                sources.obtainTips.add(tip);
            }
        }
        if (stringId != null && sources.isEmpty()) {
            addJournalSourcesByString(sources, stringId);
        }
        return sources;
    }

    private static void addFromMap(List<String> out, LinkedHashSet<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        out.addAll(values);
    }

    private static void addSpawnSources(Sources sources, Item item) {
        // Fireflies use a special spawn system (not MobSpawnItem / normal tables).
        if (item instanceof FireflySpawnItem || isFireflyItemId(item.getStringID())) {
            addFireflySources(sources, item.getStringID());
            return;
        }
        String mobId = null;
        if (item instanceof MobSpawnItem) {
            mobId = ((MobSpawnItem) item).mobStringID;
        } else if (item instanceof FlyingBugSpawnItem) {
            mobId = ((FlyingBugSpawnItem) item).mobStringID;
        }
        if (mobId == null || mobId.isEmpty()) {
            return;
        }
        LinkedHashSet<String> places = spawnByMob.get(mobId);
        if (places != null && !places.isEmpty()) {
            sources.spawns.addAll(places);
        }
        try {
            Mob mob = MobRegistry.getMob(mobId);
            if (mob != null) {
                if (mob.isCritter) {
                    sources.obtainTips.add(Localization.translate("itembrowser", "browsercritter"));
                }
                LootList list = new LootList();
                collectLootItems(mob.getLootTable(), list);
                collectLootItems(mob.getPrivateLootTable(), list);
                LinkedHashSet<String> dropNames = new LinkedHashSet<String>();
                for (Item drop : list.getItems()) {
                    if (drop != null) {
                        dropNames.add(drop.getDisplayName(new InventoryItem(drop)));
                    }
                }
                if (!dropNames.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Localization.translate("itembrowser", "browsermobdrops"));
                    sb.append(" ");
                    boolean first = true;
                    for (String name : dropNames) {
                        if (!first) {
                            sb.append(", ");
                        }
                        first = false;
                        sb.append(name);
                    }
                    sources.obtainTips.add(sb.toString());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean isFireflyItemId(String stringId) {
        return stringId != null && stringId.startsWith("firefly")
                && !stringId.contains("jar");
    }

    private static void addFireflySources(Sources sources, String stringId) {
        sources.obtainTips.add(Localization.translate("itembrowser", "browsercritter"));
        sources.obtainTips.add(Localization.translate("itembrowser", "browserfireflycatch"));
        if ("fireflyblue".equals(stringId)) {
            sources.spawns.add(Localization.translate("itembrowser", "browserfireflyblue"));
        } else if ("fireflygreen".equals(stringId)) {
            sources.spawns.add(Localization.translate("itembrowser", "browserfireflygreen"));
        } else if ("fireflyyellow".equals(stringId)) {
            sources.spawns.add(Localization.translate("itembrowser", "browserfireflyyellow"));
        } else {
            sources.spawns.add(Localization.translate("itembrowser", "browserfireflyany"));
        }
    }

    private static void ensureIndex() {
        if (indexReady) {
            return;
        }
        synchronized (INDEX_LOCK) {
            if (indexReady) {
                return;
            }
            try {
                buildShopIndex();
            } catch (Throwable t) {
                System.err.println("[ItemBrowser] Shop index failed: " + t);
            }
            try {
                buildMobLootIndex();
            } catch (Throwable t) {
                System.err.println("[ItemBrowser] Mob loot index failed: " + t);
            }
            try {
                buildSpawnIndex();
            } catch (Throwable t) {
                System.err.println("[ItemBrowser] Spawn index failed: " + t);
            }
            try {
                buildFishingIndex();
            } catch (Throwable t) {
                System.err.println("[ItemBrowser] Fishing index failed: " + t);
            }
            indexReady = true;
            System.out.println("[ItemBrowser] Source index ready"
                    + " shops=" + shopByItem.size()
                    + " drops=" + dropByItem.size()
                    + " spawns=" + spawnByMob.size()
                    + " fish=" + fishByItem.size());
        }
    }

    private static void buildShopIndex() {
        GameRandom random = new GameRandom(1L);
        GameBlackboard blackboard = new GameBlackboard();
        for (ClassIDDataContainer<Mob> entry : MobRegistry.getMobs()) {
            if (entry == null) {
                continue;
            }
            Mob mob;
            try {
                mob = MobRegistry.getMob(entry.getIDData().getID());
            } catch (Throwable t) {
                continue;
            }
            if (!(mob instanceof HumanShop)) {
                continue;
            }
            HumanShop shopMob = (HumanShop) mob;
            String vendor = safeMobName(shopMob);
            try {
                for (SellingShopItem sale : shopMob.shop.sellingShop.getItems()) {
                    if (sale == null) {
                        continue;
                    }
                    LinkedHashSet<Integer> itemIds = new LinkedHashSet<Integer>();
                    tryAddItemId(itemIds, sale.getStringID());
                    for (int i = 0; i < 40; i++) {
                        try {
                            InventoryItem generated = sale.generateItem(
                                    random.nextSeeded(i + 1), null, shopMob, blackboard);
                            if (generated != null && generated.item != null) {
                                itemIds.add(generated.item.getID());
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    for (Integer itemId : itemIds) {
                        if (itemId == null) {
                            continue;
                        }
                        put(shopByItem, itemId, vendor);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void tryAddItemId(Set<Integer> out, String stringId) {
        if (stringId == null || stringId.isEmpty()) {
            return;
        }
        Item item = ItemRegistry.getItem(stringId);
        if (item != null) {
            out.add(item.getID());
        }
    }

    private static void buildMobLootIndex() {
        for (ClassIDDataContainer<Mob> entry : MobRegistry.getMobs()) {
            if (entry == null) {
                continue;
            }
            Mob mob;
            try {
                mob = MobRegistry.getMob(entry.getIDData().getID());
            } catch (Throwable t) {
                continue;
            }
            if (mob == null || !mob.dropsLoot) {
                continue;
            }
            String mobName = safeMobName(mob);
            LootList list = new LootList();
            collectLootItems(mob.getLootTable(), list);
            collectLootItems(mob.getPrivateLootTable(), list);
            collectLootItems(mob.showAdditionalLootTableInJournal(), list);
            for (Integer itemId : list.getItemIDs()) {
                if (itemId != null) {
                    put(dropByItem, itemId, mobName);
                }
            }
            for (InventoryItem custom : list.getCustomItems()) {
                if (custom != null && custom.item != null) {
                    put(dropByItem, custom.item.getID(), mobName);
                }
            }
        }
    }

    private static void collectLootItems(LootTable table, LootList list) {
        if (table == null || list == null) {
            return;
        }
        try {
            table.addPossibleLoot(list);
        } catch (Throwable ignored) {
        }
    }

    private static void buildSpawnIndex() {
        // Default shared tables on Biome
        indexSpawnTable(Biome.defaultSurfaceCritters, "Default surface critters");
        indexSpawnTable(Biome.defaultCaveCritters, "Default cave critters");
        indexSpawnTable(Biome.defaultSurfaceMobs, "Default surface mobs");
        indexSpawnTable(Biome.defaultCaveMobs, "Default cave mobs");
        indexSpawnTable(Biome.forestCaveMobs, "Forest cave mobs");
        indexSpawnTable(Biome.defaultDeepCaveMobs, "Default deep cave mobs");

        for (Biome biome : BiomeRegistry.getBiomes()) {
            if (biome == null) {
                continue;
            }
            String biomeName = biome.getDisplayName();
            if (biomeName == null || biomeName.isEmpty()) {
                biomeName = biome.getStringID();
            }
            indexSpawnTablesOn(biome.getClass(), biome, biomeName);
            // Parent Biome statics already covered; still scan instance class hierarchy fields
            Class<?> c = biome.getClass().getSuperclass();
            while (c != null && Biome.class.isAssignableFrom(c) && c != Object.class) {
                indexSpawnTablesOn(c, biome, biomeName);
                c = c.getSuperclass();
            }
        }
    }

    private static void indexSpawnTablesOn(Class<?> type, Object instance, String biomeName) {
        if (type == null) {
            return;
        }
        for (Field field : type.getDeclaredFields()) {
            if (!MobSpawnTable.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object owner = Modifier.isStatic(field.getModifiers()) ? null : instance;
                MobSpawnTable table = (MobSpawnTable) field.get(owner);
                if (table == null) {
                    continue;
                }
                String layer = field.getName();
                indexSpawnTable(table, biomeName + " [" + layer + "]");
            } catch (Throwable ignored) {
            }
        }
        for (Field field : type.getDeclaredFields()) {
            if (!FishingLootTable.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object owner = Modifier.isStatic(field.getModifiers()) ? null : instance;
                FishingLootTable table = (FishingLootTable) field.get(owner);
                if (table != null) {
                    indexFishingTable(table, biomeName + " [" + field.getName() + "]");
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void indexSpawnTable(MobSpawnTable table, String label) {
        if (table == null || label == null) {
            return;
        }
        Set<MobSpawnTable> visited = Collections.newSetFromMap(new IdentityHashMap<MobSpawnTable, Boolean>());
        LinkedHashSet<String> mobIds = new LinkedHashSet<String>();
        collectMobIdsFromSpawnTable(table, mobIds, visited);
        for (String mobId : mobIds) {
            LinkedHashSet<String> places = spawnByMob.get(mobId);
            if (places == null) {
                places = new LinkedHashSet<String>();
                spawnByMob.put(mobId, places);
            }
            places.add(label);
        }
    }

    @SuppressWarnings("unchecked")
    private static void collectMobIdsFromSpawnTable(MobSpawnTable table, Set<String> out,
                                                    Set<MobSpawnTable> visited) {
        if (table == null || !visited.add(table)) {
            return;
        }
        try {
            Field includesField = MobSpawnTable.class.getDeclaredField("includes");
            includesField.setAccessible(true);
            LinkedListLike includes = new LinkedListLike(includesField.get(table));
            for (Object include : includes) {
                if (include instanceof MobSpawnTable) {
                    collectMobIdsFromSpawnTable((MobSpawnTable) include, out, visited);
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            Field tableField = MobSpawnTable.class.getDeclaredField("table");
            tableField.setAccessible(true);
            LinkedListLike chances = new LinkedListLike(tableField.get(table));
            for (Object chance : chances) {
                if (chance instanceof MobChance) {
                    collectRegistryStrings(chance, out, true, false, 0);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void buildFishingIndex() {
        indexFishingTable(Biome.commonFish, "Common fish");
        indexFishingTable(Biome.defaultSurfaceFish, "Default surface fish");
        indexFishingTable(Biome.defaultCaveFish, "Default cave fish");
        for (Biome biome : BiomeRegistry.getBiomes()) {
            if (biome == null) {
                continue;
            }
            String biomeName = biome.getDisplayName();
            if (biomeName == null || biomeName.isEmpty()) {
                biomeName = biome.getStringID();
            }
            indexSpawnTablesOn(biome.getClass(), biome, biomeName);
        }
    }

    @SuppressWarnings("unchecked")
    private static void indexFishingTable(FishingLootTable table, String label) {
        if (table == null || label == null) {
            return;
        }
        Set<FishingLootTable> visited = Collections.newSetFromMap(
                new IdentityHashMap<FishingLootTable, Boolean>());
        LinkedHashSet<String> itemIds = new LinkedHashSet<String>();
        collectItemIdsFromFishingTable(table, itemIds, visited);
        for (String stringId : itemIds) {
            Item item = ItemRegistry.getItem(stringId);
            if (item != null) {
                put(fishByItem, item.getID(), label);
            }
        }
    }

    private static void collectItemIdsFromFishingTable(FishingLootTable table, Set<String> out,
                                                       Set<FishingLootTable> visited) {
        if (table == null || !visited.add(table)) {
            return;
        }
        try {
            Field includesField = FishingLootTable.class.getDeclaredField("includes");
            includesField.setAccessible(true);
            LinkedListLike includes = new LinkedListLike(includesField.get(table));
            for (Object include : includes) {
                if (include instanceof FishingLootTable) {
                    collectItemIdsFromFishingTable((FishingLootTable) include, out, visited);
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            Field tableField = FishingLootTable.class.getDeclaredField("table");
            tableField.setAccessible(true);
            LinkedListLike rows = new LinkedListLike(tableField.get(table));
            for (Object row : rows) {
                collectRegistryStrings(row, out, false, true, 0);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Walk object fields (shallow recursion) for String IDs present in mob/item registries.
     */
    private static void collectRegistryStrings(Object obj, Set<String> out,
                                               boolean mobs, boolean items, int depth) {
        if (obj == null || depth > 4) {
            return;
        }
        Class<?> type = obj.getClass();
        // Avoid scanning huge JDK structures
        if (type.getName().startsWith("java.") && !(obj instanceof String)) {
            return;
        }
        if (obj instanceof String) {
            considerString((String) obj, out, mobs, items);
            return;
        }
        for (Field field : getAllFields(type)) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) {
                    continue;
                }
                if (value instanceof String) {
                    considerString((String) value, out, mobs, items);
                } else if (value instanceof InventoryItem) {
                    InventoryItem inv = (InventoryItem) value;
                    if (inv.item != null && items) {
                        out.add(inv.item.getStringID());
                    }
                } else if (value.getClass().isArray()) {
                    int len = java.lang.reflect.Array.getLength(value);
                    for (int i = 0; i < len && i < 64; i++) {
                        collectRegistryStrings(java.lang.reflect.Array.get(value, i), out, mobs, items, depth + 1);
                    }
                } else if (!value.getClass().isPrimitive()
                        && (value.getClass().isSynthetic()
                        || value.getClass().getName().contains("$$")
                        || field.getName().contains("arg")
                        || field.getType().isInterface())) {
                    collectRegistryStrings(value, out, mobs, items, depth + 1);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void considerString(String value, Set<String> out, boolean mobs, boolean items) {
        if (value == null || value.isEmpty() || value.length() > 64) {
            return;
        }
        if (mobs) {
            try {
                if (MobRegistry.getMob(value) != null) {
                    out.add(value);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }
        if (items) {
            Item item = ItemRegistry.getItem(value);
            if (item != null) {
                out.add(value);
            }
        }
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> c = type;
        int guard = 0;
        while (c != null && c != Object.class && guard++ < 8) {
            Field[] declared = c.getDeclaredFields();
            for (Field field : declared) {
                fields.add(field);
            }
            c = c.getSuperclass();
        }
        return fields;
    }

    private static void put(Map<Integer, LinkedHashSet<String>> map, int itemId, String label) {
        if (label == null || label.isEmpty()) {
            return;
        }
        LinkedHashSet<String> set = map.get(itemId);
        if (set == null) {
            set = new LinkedHashSet<String>();
            map.put(itemId, set);
        }
        set.add(label);
    }

    private static String safeMobName(Mob mob) {
        try {
            String name = MobRegistry.getDisplayName(mob.getID());
            if (name != null && !name.isEmpty()) {
                return name;
            }
        } catch (Throwable ignored) {
        }
        try {
            return mob.getStringID();
        } catch (Throwable ignored) {
            return "?";
        }
    }

    /** Minimal iterable over reflective LinkedList without depending on raw type casts. */
    private static final class LinkedListLike implements Iterable<Object> {
        private final Iterable<?> source;

        @SuppressWarnings("unchecked")
        LinkedListLike(Object maybeList) {
            if (maybeList instanceof Iterable) {
                this.source = (Iterable<?>) maybeList;
            } else {
                this.source = Collections.emptyList();
            }
        }

        @Override
        public java.util.Iterator<Object> iterator() {
            final java.util.Iterator<?> it = source.iterator();
            return new java.util.Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Object next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static void addRecipes(Sources sources, int itemId) {
        List<Recipe> recipes = Recipes.getRecipesFromResult(itemId);
        if (recipes == null) {
            return;
        }
        LinkedHashSet<String> unique = new LinkedHashSet<String>();
        for (Recipe recipe : recipes) {
            unique.add(formatRecipe(recipe));
        }
        sources.recipes.addAll(unique);
    }

    private static String formatRecipe(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        if (recipe.tech != null && recipe.tech.displayName != null) {
            sb.append(recipe.tech.displayName.translate());
        } else {
            sb.append("?");
        }
        sb.append(": ");
        if (recipe.ingredients != null) {
            for (int i = 0; i < recipe.ingredients.length; i++) {
                Ingredient ingredient = recipe.ingredients[i];
                if (i > 0) {
                    sb.append(" + ");
                }
                sb.append(ingredient.getIngredientAmount());
                sb.append(" ");
                sb.append(ingredient.getDisplayName());
            }
        }
        if (recipe.resultAmount > 1) {
            sb.append(" -> x");
            sb.append(recipe.resultAmount);
        }
        return sb.toString();
    }

    private static void addJournalSources(Sources sources, int itemId) {
        LinkedHashSet<String> drops = new LinkedHashSet<String>();
        LinkedHashSet<String> treasures = new LinkedHashSet<String>();
        LinkedHashSet<String> biomeLoot = new LinkedHashSet<String>();

        for (JournalEntry entry : JournalRegistry.getJournalEntries()) {
            String entryName = entry.getLocalization().translate();
            if (entry.mobsData != null) {
                for (MobJournalData mobData : entry.mobsData) {
                    if (lootContains(mobData.itemDrops, itemId)) {
                        String mobName = mobData.mob != null
                                ? mobData.mob.getLocalization().translate()
                                : "?";
                        drops.add(mobName + " (" + entryName + ")");
                    }
                }
            }
            if (lootContains(entry.treasuresData, itemId)) {
                treasures.add(entryName);
            }
            if (lootContains(entry.biomeLoot, itemId)) {
                biomeLoot.add(entryName);
            }
        }

        // Merge journal drops first, then any extra loot-table hits already buffered
        LinkedHashSet<String> mergedDrops = new LinkedHashSet<String>();
        mergedDrops.addAll(drops);
        mergedDrops.addAll(sources.drops);
        sources.drops.clear();
        sources.drops.addAll(mergedDrops);
        sources.treasures.addAll(treasures);
        sources.biomeLoot.addAll(biomeLoot);
    }

    private static void addJournalSourcesByString(Sources sources, String stringId) {
        // reserved fallback
    }

    private static boolean lootContains(LootList list, int itemId) {
        if (list == null) {
            return false;
        }
        for (Integer id : list.getItemIDs()) {
            if (id != null && id == itemId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalize search text: full-width→half-width, unify punctuation,
     * collapse whitespace, trim, lower-case.
     */
    public static String normalizeForSearch(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length());
        boolean lastSpace = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 0xFF01 && c <= 0xFF5E) {
                c = (char) (c - 0xFEE0);
            } else if (c == 0x3000) {
                c = ' ';
            }
            c = unifyPunctuation(c);
            c = Character.toLowerCase(c);
            if (Character.isWhitespace(c)) {
                if (!lastSpace && sb.length() > 0) {
                    sb.append(' ');
                    lastSpace = true;
                }
                continue;
            }
            lastSpace = false;
            sb.append(c);
        }
        int end = sb.length();
        while (end > 0 && sb.charAt(end - 1) == ' ') {
            end--;
        }
        return sb.substring(0, end);
    }

    private static char unifyPunctuation(char c) {
        switch (c) {
            case '\u2018':
            case '\u2019':
            case '\u201A':
            case '\uFF07':
                return '\'';
            case '\u201C':
            case '\u201D':
            case '\u201E':
            case '\uFF02':
                return '"';
            case '\u3001':
            case '\uFF0C':
                return ',';
            case '\u3002':
            case '\uFF0E':
                return '.';
            case '\uFF1A':
                return ':';
            case '\uFF1B':
                return ';';
            case '\uFF01':
                return '!';
            case '\uFF1F':
                return '?';
            case '\u2013':
            case '\u2014':
            case '\uFF0D':
                return '-';
            case '\uFF08':
                return '(';
            case '\uFF09':
                return ')';
            case '\u3010':
                return '[';
            case '\u3011':
                return ']';
            case '\u300A':
                return '<';
            case '\u300B':
                return '>';
            default:
                return c;
        }
    }

    private static String stripSpaces(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean matchesQuery(String haystackNormalized, String haystackCompact,
                                        String query, String queryCompact) {
        if (haystackNormalized == null || haystackNormalized.isEmpty()) {
            return false;
        }
        if (haystackNormalized.contains(query)) {
            return true;
        }
        return !queryCompact.isEmpty()
                && haystackCompact != null
                && haystackCompact.contains(queryCompact);
    }

    public static List<Item> searchItems(String query, int limit) {
        String q = normalizeForSearch(query);
        if (q.isEmpty()) {
            return Collections.emptyList();
        }
        String qCompact = stripSpaces(q);
        List<Item> matches = new ArrayList<Item>();
        for (Item item : ItemRegistry.getItems()) {
            if (item == null) {
                continue;
            }
            String id = item.getStringID();
            String name = item.getDisplayName(new InventoryItem(item));
            String idNorm = normalizeForSearch(id);
            String nameNorm = normalizeForSearch(name);
            String idCompact = stripSpaces(idNorm);
            String nameCompact = stripSpaces(nameNorm);
            if (matchesQuery(idNorm, idCompact, q, qCompact)
                    || matchesQuery(nameNorm, nameCompact, q, qCompact)) {
                matches.add(item);
                if (matches.size() >= limit) {
                    break;
                }
            }
        }
        return matches;
    }
}
