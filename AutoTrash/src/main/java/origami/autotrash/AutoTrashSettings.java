package origami.autotrash;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class AutoTrashSettings extends ModSettings {

    public boolean enabled = true;
    private final LinkedHashSet<String> blacklist = new LinkedHashSet<String>();

    @Override
    public void addSaveData(SaveData data) {
        data.addBoolean("enabled", enabled);
        data.addSafeString("blacklist", join(blacklist));
    }

    @Override
    public void applyLoadData(LoadData data) {
        enabled = data.getBoolean("enabled", enabled);
        blacklist.clear();
        String raw = data.getSafeString("blacklist", "", false);
        if (raw != null && !raw.isEmpty()) {
            String[] parts = raw.split("[,;\\s]+");
            for (int i = 0; i < parts.length; i++) {
                String id = normalize(parts[i]);
                if (!id.isEmpty()) {
                    blacklist.add(id);
                }
            }
        }
    }

    public boolean isBlacklisted(String itemStringId) {
        if (itemStringId == null) {
            return false;
        }
        return blacklist.contains(normalize(itemStringId));
    }

    public boolean add(String itemStringId) {
        String id = normalize(itemStringId);
        if (id.isEmpty()) {
            return false;
        }
        return blacklist.add(id);
    }

    public boolean remove(String itemStringId) {
        return blacklist.remove(normalize(itemStringId));
    }

    public List<String> list() {
        return Collections.unmodifiableList(new ArrayList<String>(blacklist));
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
