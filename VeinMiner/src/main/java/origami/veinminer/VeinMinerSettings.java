package origami.veinminer;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.inventory.item.Item;
import necesse.inventory.item.placeableItem.objectItem.ObjectItem;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.RockOreObject;
import necesse.level.gameObject.SingleOreRockSmall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class VeinMinerSettings extends ModSettings {

    public boolean enabled = true;
    public int maxChain = 128;
    /** When true, every ore vein can chain (vanilla-style default). */
    public boolean allOres = true;
    /** Extra GameObject string IDs that can chain (trees, custom rocks, etc.). */
    private final LinkedHashSet<String> extraObjects = new LinkedHashSet<String>();

    @Override
    public void addSaveData(SaveData data) {
        data.addBoolean("enabled", enabled);
        data.addInt("maxChain", maxChain);
        data.addBoolean("allOres", allOres);
        data.addSafeString("extraObjects", join(extraObjects));
    }

    @Override
    public void applyLoadData(LoadData data) {
        enabled = data.getBoolean("enabled", enabled);
        maxChain = data.getInt("maxChain", maxChain);
        allOres = data.getBoolean("allOres", allOres);
        if (maxChain < 8) {
            maxChain = 8;
        }
        if (maxChain > 512) {
            maxChain = 512;
        }
        extraObjects.clear();
        String raw = data.getSafeString("extraObjects", "", false);
        if (raw != null && !raw.isEmpty()) {
            String[] parts = raw.split("[,;\\s]+");
            for (int i = 0; i < parts.length; i++) {
                String id = normalize(parts[i]);
                if (!id.isEmpty()) {
                    extraObjects.add(id);
                }
            }
        }
    }

    public boolean canChain(GameObject object) {
        if (object == null) {
            return false;
        }
        if (allOres && object.isOre) {
            return true;
        }
        return extraObjects.contains(normalize(object.getStringID()));
    }

    public boolean addObjectId(String objectStringId) {
        String id = normalize(objectStringId);
        if (id.isEmpty() || ObjectRegistry.getObject(id) == null) {
            return false;
        }
        return extraObjects.add(id);
    }

    public boolean removeObjectId(String objectStringId) {
        return extraObjects.remove(normalize(objectStringId));
    }

    public List<String> listExtras() {
        return Collections.unmodifiableList(new ArrayList<String>(extraObjects));
    }

    /** Resolve an inventory item into object IDs to whitelist. */
    public int addFromItem(Item item) {
        if (item == null) {
            return 0;
        }
        int added = 0;
        if (item instanceof ObjectItem) {
            GameObject obj = ObjectRegistry.getObject(((ObjectItem) item).objectID);
            if (obj != null && addObjectId(obj.getStringID())) {
                added++;
            }
            return added;
        }
        String itemId = normalize(item.getStringID());
        GameObject sameId = ObjectRegistry.getObject(itemId);
        if (sameId != null && addObjectId(sameId.getStringID())) {
            added++;
        }
        // Ore drop item → all rock objects that drop it
        for (GameObject obj : ObjectRegistry.getObjects()) {
            if (obj == null) {
                continue;
            }
            String dropped = null;
            if (obj instanceof RockOreObject) {
                dropped = ((RockOreObject) obj).droppedOre;
            } else if (obj instanceof SingleOreRockSmall) {
                dropped = ((SingleOreRockSmall) obj).droppedOre;
            }
            if (dropped != null && normalize(dropped).equals(itemId)) {
                if (addObjectId(obj.getStringID())) {
                    added++;
                }
            }
        }
        return added;
    }

    public static String normalize(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().toLowerCase(Locale.ROOT);
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
