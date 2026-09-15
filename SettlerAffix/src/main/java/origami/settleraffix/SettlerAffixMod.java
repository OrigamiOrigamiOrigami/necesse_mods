package origami.settleraffix;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.PacketRegistry;
import origami.settleraffix.network.PacketSetCollectorStorage;
import origami.settleraffix.network.PacketSetCollectorStorageResult;

@ModEntry
public class SettlerAffixMod {

    public static SettlerAffixSettings SETTINGS;

    public SettlerAffixSettings initSettings() {
        SETTINGS = new SettlerAffixSettings();
        return SETTINGS;
    }

    public void init() {
        PacketRegistry.registerPacket(PacketSetCollectorStorage.class);
        PacketRegistry.registerPacket(PacketSetCollectorStorageResult.class);
        System.out.println("[SettlerAffix] Loaded. Commands panel collector storage buttons.");
    }

    public static void saveSettings() {
        necesse.engine.Settings.saveClientSettings();
    }
}
