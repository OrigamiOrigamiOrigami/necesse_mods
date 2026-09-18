package origami.veinminer;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.StonePlaceableItem;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.gameObject.FruitTreeObject;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.RockObject;
import necesse.level.gameObject.RockOreObject;
import necesse.level.gameObject.SingleOreRockSmall;
import necesse.level.gameObject.TreeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Whitelist model (inventory items, not world diggable IDs):
 * <ul>
 *   <li>{@code extraOreItems} — ore drops → rocks by {@code droppedOre}</li>
 *   <li>{@code extraStoneItems} — stone blocks → rocks by {@code droppedStone}</li>
 *   <li>{@code extraLogItems} — logs → trees by {@code logStringID}</li>
 *   <li>{@code extraObjects} — other diggable GameObject string IDs</li>
 * </ul>
 */
public class VeinMinerSettings extends ModSettings {

    /** Chebyshev radius used to connect trees in a forest patch. */
    public static final int TREE_CONNECT_RADIUS = 5;

    public boolean enabled = true;
    public int maxChain = 128;
    /** When true, every ore-like vein can chain. */
    public boolean allOres = true;
    /** When true, every tree can chain within a local area. */
    public boolean allTrees = false;
    /** Extra diggable GameObject string IDs. */
    private final LinkedHashSet<String> extraObjects = new LinkedHashSet<String>();
    /** Extra ore drop item string IDs. */
    private final LinkedHashSet<String> extraOreItems = new LinkedHashSet<String>();
    /** Extra stone block item string IDs (e.g. {@code deepsandstone}). */
    private final LinkedHashSet<String> extraStoneItems = new LinkedHashSet<String>();
    /** Extra log item string IDs (e.g. {@code oaklog}). */
    private final LinkedHashSet<String> extraLogItems = new LinkedHashSet<String>();

    @Override
    public void addSaveData(SaveData data) {
        data.addBoolean("enabled", enabled);
        data.addInt("maxChain", maxChain);
        data.addBoolean("allOres", allOres);
        data.addBoolean("allTrees", allTrees);
        data.addSafeString("extraObjects", join(extraObjects));
        data.addSafeString("extraOreItems", join(extraOreItems));
        data.addSafeString("extraStoneItems", join(extraStoneItems));
        data.addSafeString("extraLogItems", join(extraLogItems));
    }

    @Override
    public void applyLoadData(LoadData data) {
        enabled = data.getBoolean("enabled", enabled);
        maxChain = data.getInt("maxChain", maxChain);
        allOres = data.getBoolean("allOres", allOres);
        allTrees = data.getBoolean("allTrees", allTrees);
        if (maxChain < 8) {
            maxChain = 8;
        }
        if (maxChain > 512) {
            maxChain = 512;
        }
        extraObjects.clear();
        extraOreItems.clear();
        extraStoneItems.clear();
        extraLogItems.clear();
        loadIdList(data.getSafeString("extraOreItems", "", false), extraOreItems);
        loadIdList(data.getSafeString("extraStoneItems", "", false), extraStoneItems);
        loadIdList(data.getSafeString("extraLogItems", "", false), extraLogItems);
        reclassifyLists();

        String rawObjects = data.getSafeString("extraObjects", "", false);
        if (rawObjects != null && !rawObjects.isEmpty()) {
            String[] parts = rawObjects.split("[,;\\s]+");
            for (int i = 0; i < parts.length; i++) {
                String id = normalize(parts[i]);
                if (id.isEmpty()) {
                    continue;
                }
                if (objectExists(id)) {
                    GameObject obj = ObjectRegistry.getObject(id);
                    String log = logOf(obj);
                    if (log != null && itemExists(log)) {
                        extraLogItems.add(normalize(log));
                    } else {
                        extraObjects.add(id);
                    }
                } else if (isDroppedByAnyOreRock(id)) {
                    extraOreItems.add(id);
                } else if (isDroppedByAnyTree(id)) {
                    extraLogItems.add(id);
                } else if (isDroppedByAnyRock(id) || itemExists(id)) {
                    extraStoneItems.add(id);
                }
            }
        }
    }

    public boolean canChain(GameObject object) {
        if (object == null) {
            return false;
        }
        if (allOres && isOreLike(object)) {
            return true;
        }
        if (allTrees && isTreeLike(object)) {
            return true;
        }
        if (extraObjects.contains(normalize(object.getStringID()))) {
            return true;
        }
        String droppedOre = droppedOreOf(object);
        if (droppedOre != null && extraOreItems.contains(normalize(droppedOre))) {
            return true;
        }
        String log = logOf(object);
        if (log != null && extraLogItems.contains(normalize(log))) {
            return true;
        }
        // Stone whitelist only applies to plain rocks, not ore rocks.
        if (!isOreLike(object)) {
            String droppedStone = droppedStoneOf(object);
            if (droppedStone != null && extraStoneItems.contains(normalize(droppedStone))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Same vein: ores by dropped ore, trees by log, otherwise exact object ID.
     */
    public boolean sameChainVein(GameObject a, GameObject b) {
        if (a == null || b == null) {
            return false;
        }
        String droppedA = droppedOreOf(a);
        String droppedB = droppedOreOf(b);
        if (droppedA != null && droppedB != null) {
            return normalize(droppedA).equals(normalize(droppedB));
        }
        String logA = logOf(a);
        String logB = logOf(b);
        if (logA != null && logB != null) {
            return normalize(logA).equals(normalize(logB));
        }
        return a.getID() == b.getID();
    }

    public boolean addObjectId(String objectStringId) {
        String id = normalize(objectStringId);
        if (id.isEmpty() || !objectExists(id)) {
            return false;
        }
        return extraObjects.add(id);
    }

    public boolean addOreItemId(String itemStringId) {
        String id = normalize(itemStringId);
        if (id.isEmpty() || !itemExists(id)) {
            return false;
        }
        return extraOreItems.add(id);
    }

    public boolean addStoneItemId(String itemStringId) {
        String id = normalize(itemStringId);
        if (id.isEmpty() || !itemExists(id)) {
            return false;
        }
        return extraStoneItems.add(id);
    }

    public boolean addLogItemId(String itemStringId) {
        String id = normalize(itemStringId);
        if (id.isEmpty() || !itemExists(id)) {
            return false;
        }
        return extraLogItems.add(id);
    }

    public boolean removeObjectId(String objectStringId) {
        return extraObjects.remove(normalize(objectStringId));
    }

    public boolean removeOreItemId(String itemStringId) {
        return extraOreItems.remove(normalize(itemStringId));
    }

    public boolean removeStoneItemId(String itemStringId) {
        return extraStoneItems.remove(normalize(itemStringId));
    }

    public boolean removeLogItemId(String itemStringId) {
        return extraLogItems.remove(normalize(itemStringId));
    }

    public List<String> listExtras() {
        return Collections.unmodifiableList(new ArrayList<String>(extraObjects));
    }

    public List<String> listExtraOreItems() {
        return Collections.unmodifiableList(new ArrayList<String>(extraOreItems));
    }

    public List<String> listExtraStoneItems() {
        return Collections.unmodifiableList(new ArrayList<String>(extraStoneItems));
    }

    public List<String> listExtraLogItems() {
        return Collections.unmodifiableList(new ArrayList<String>(extraLogItems));
    }

    /**
     * Resolve an inventory item into whitelist entries.
     */
    public int addFromItem(Item item) {
        if (item == null) {
            return 0;
        }
        if (item instanceof ObjectItem) {
            GameObject obj = ObjectRegistry.getObject(((ObjectItem) item).objectID);
            if (obj == null) {
                return 0;
            }
            String droppedOre = droppedOreOf(obj);
            if (droppedOre != null && itemExists(droppedOre)) {
                return addOreItemId(droppedOre) ? 1 : 0;
            }
            String log = logOf(obj);
            if (log != null && itemExists(log)) {
                return addLogItemId(log) ? 1 : 0;
            }
            String droppedStone = droppedStoneOf(obj);
            if (droppedStone != null && itemExists(droppedStone)) {
                return addStoneItemId(droppedStone) ? 1 : 0;
            }
            return addObjectId(obj.getStringID()) ? 1 : 0;
        }

        String itemId = normalize(item.getStringID());
        if (itemId.isEmpty()) {
            return 0;
        }
        if (isDroppedByAnyOreRock(itemId)) {
            return addOreItemId(itemId) ? 1 : 0;
        }
        if (isDroppedByAnyTree(itemId)) {
            return addLogItemId(itemId) ? 1 : 0;
        }
        if (item instanceof StonePlaceableItem || isDroppedByAnyRock(itemId)) {
            return addStoneItemId(itemId) ? 1 : 0;
        }
        if (objectExists(itemId)) {
            return addObjectId(itemId) ? 1 : 0;
        }
        return 0;
    }

    public static boolean isOreLike(GameObject object) {
        if (object == null) {
            return false;
        }
        if (object.isOre) {
            return true;
        }
        return object instanceof RockOreObject || object instanceof SingleOreRockSmall;
    }

    public static boolean isTreeLike(GameObject object) {
        if (object == null) {
            return false;
        }
        if (object.isTree) {
            return true;
        }
        return object instanceof TreeObject || object instanceof FruitTreeObject;
    }

    public static String droppedOreOf(GameObject object) {
        if (object instanceof RockOreObject) {
            return ((RockOreObject) object).droppedOre;
        }
        if (object instanceof SingleOreRockSmall) {
            return ((SingleOreRockSmall) object).droppedOre;
        }
        return null;
    }

    public static String logOf(GameObject object) {
        if (object instanceof TreeObject) {
            return ((TreeObject) object).logStringID;
        }
        if (object instanceof FruitTreeObject) {
            return ((FruitTreeObject) object).logStringID;
        }
        return null;
    }

    public static String droppedStoneOf(GameObject object) {
        if (object instanceof SingleOreRockSmall) {
            return ((SingleOreRockSmall) object).droppedStone;
        }
        if (object instanceof RockObject) {
            return readRockDroppedStone((RockObject) object);
        }
        return null;
    }

    /** RockObject.droppedStone is package-private; read via reflection. */
    private static String readRockDroppedStone(RockObject rock) {
        try {
            java.lang.reflect.Field field = RockObject.class.getDeclaredField("droppedStone");
            field.setAccessible(true);
            Object value = field.get(rock);
            return value instanceof String ? (String) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean objectExists(String stringId) {
        String id = normalize(stringId);
        return !id.isEmpty() && ObjectRegistry.getObjectID(id) != -1;
    }

    public static boolean itemExists(String stringId) {
        String id = normalize(stringId);
        return !id.isEmpty() && ItemRegistry.getItemID(id) != -1;
    }

    public static String normalize(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isDroppedByAnyOreRock(String itemId) {
        for (GameObject obj : ObjectRegistry.getObjects()) {
            if (obj == null) {
                continue;
            }
            String dropped = droppedOreOf(obj);
            if (dropped != null && normalize(dropped).equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDroppedByAnyRock(String itemId) {
        for (GameObject obj : ObjectRegistry.getObjects()) {
            if (obj == null) {
                continue;
            }
            String dropped = droppedStoneOf(obj);
            if (dropped != null && normalize(dropped).equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDroppedByAnyTree(String itemId) {
        for (GameObject obj : ObjectRegistry.getObjects()) {
            if (obj == null) {
                continue;
            }
            String log = logOf(obj);
            if (log != null && normalize(log).equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    private void reclassifyLists() {
        ArrayList<String> oreToStone = new ArrayList<String>();
        ArrayList<String> oreToLog = new ArrayList<String>();
        for (String id : extraOreItems) {
            if (isDroppedByAnyOreRock(id)) {
                continue;
            }
            if (isDroppedByAnyTree(id)) {
                oreToLog.add(id);
            } else if (isDroppedByAnyRock(id)) {
                oreToStone.add(id);
            }
        }
        for (int i = 0; i < oreToStone.size(); i++) {
            String id = oreToStone.get(i);
            extraOreItems.remove(id);
            extraStoneItems.add(id);
        }
        for (int i = 0; i < oreToLog.size(); i++) {
            String id = oreToLog.get(i);
            extraOreItems.remove(id);
            extraLogItems.add(id);
        }
    }

    private static void loadIdList(String raw, LinkedHashSet<String> into) {
        if (raw == null || raw.isEmpty()) {
            return;
        }
        String[] parts = raw.split("[,;\\s]+");
        for (int i = 0; i < parts.length; i++) {
            String id = normalize(parts[i]);
            if (!id.isEmpty()) {
                into.add(id);
            }
        }
    }

    private static String join(LinkedHashSet<String> set) {
        if (set.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : set) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append(s);
        }
        return sb.toString();
    }
}
