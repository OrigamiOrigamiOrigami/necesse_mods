package origami.settleraffix;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

/**
 * UI mirror of last applied global collector storage blocks.
 * Real authority is each CollectorSettlerPersonality.allowTakeFromStorage.
 */
public class SettlerAffixSettings extends ModSettings {

    public boolean blockGemCollector = false;
    public boolean blockFlowerCollector = false;
    public boolean blockHiredHand = false;

    @Override
    public void addSaveData(SaveData data) {
        data.addBoolean("blockGemCollector", blockGemCollector);
        data.addBoolean("blockFlowerCollector", blockFlowerCollector);
        data.addBoolean("blockHiredHand", blockHiredHand);
    }

    @Override
    public void applyLoadData(LoadData data) {
        blockGemCollector = data.getBoolean("blockGemCollector", blockGemCollector);
        blockFlowerCollector = data.getBoolean("blockFlowerCollector", blockFlowerCollector);
        blockHiredHand = data.getBoolean("blockHiredHand", blockHiredHand);
    }

    public void setBlocked(byte type, boolean blocked) {
        if (type == CollectorStorageApplier.TYPE_GEM) {
            blockGemCollector = blocked;
        } else if (type == CollectorStorageApplier.TYPE_FLOWER) {
            blockFlowerCollector = blocked;
        } else if (type == CollectorStorageApplier.TYPE_HIRED) {
            blockHiredHand = blocked;
        }
    }

    public boolean isBlocked(byte type) {
        if (type == CollectorStorageApplier.TYPE_GEM) {
            return blockGemCollector;
        }
        if (type == CollectorStorageApplier.TYPE_FLOWER) {
            return blockFlowerCollector;
        }
        if (type == CollectorStorageApplier.TYPE_HIRED) {
            return blockHiredHand;
        }
        return false;
    }
}
