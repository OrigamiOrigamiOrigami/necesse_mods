package origami.settleraffix;

import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.level.maps.levelData.settlementData.LevelSettler;
import necesse.level.maps.levelData.settlementData.ServerSettlementData;
import necesse.level.maps.levelData.settlementData.settler.SettlerMob;
import necesse.level.maps.levelData.settlementData.settler.personalities.CollectorSettlerPersonality;
import necesse.level.maps.levelData.settlementData.settler.personalities.FlowerCollectorSettlerPersonality;
import necesse.level.maps.levelData.settlementData.settler.personalities.GemCollectorSettlerPersonality;
import necesse.level.maps.levelData.settlementData.settler.personalities.HiredHandCollectorSettlerPersonality;
import necesse.level.maps.levelData.settlementData.settler.personalities.SettlerPersonality;

/**
 * Applies the same flag as the collector dialogue "stop/start taking from storage".
 */
public final class CollectorStorageApplier {

    public static final byte TYPE_GEM = 0;
    public static final byte TYPE_FLOWER = 1;
    public static final byte TYPE_HIRED = 2;

    private CollectorStorageApplier() {
    }

    public static int apply(ServerSettlementData data, byte type, boolean allowTakeFromStorage) {
        if (data == null) {
            return 0;
        }
        int count = 0;
        for (LevelSettler settler : data.getSettlers()) {
            if (settler == null) {
                continue;
            }
            SettlerMob mob = settler.getMob();
            if (!(mob instanceof HumanMob)) {
                continue;
            }
            HumanMob human = (HumanMob) mob;
            for (SettlerPersonality personality : human.getPersonalities()) {
                if (matches(personality, type)) {
                    ((CollectorSettlerPersonality) personality).allowTakeFromStorage = allowTakeFromStorage;
                    count++;
                }
            }
        }
        return count;
    }

    public static boolean matches(SettlerPersonality personality, byte type) {
        if (!(personality instanceof CollectorSettlerPersonality)) {
            return false;
        }
        if (type == TYPE_GEM) {
            return personality instanceof GemCollectorSettlerPersonality;
        }
        if (type == TYPE_FLOWER) {
            return personality instanceof FlowerCollectorSettlerPersonality;
        }
        if (type == TYPE_HIRED) {
            return personality instanceof HiredHandCollectorSettlerPersonality;
        }
        return false;
    }
}
