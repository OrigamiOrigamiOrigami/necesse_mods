package origami.helpfulnpcs;

import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.registries.SettlerRegistry;
import origami.helpfulnpcs.mobs.ExoticVendorMob;
import origami.helpfulnpcs.mobs.PotionVendorMob;
import origami.helpfulnpcs.mobs.TrinketVendorMob;
import origami.helpfulnpcs.mobs.VinylVendorMob;
import origami.helpfulnpcs.settler.ExoticSettler;
import origami.helpfulnpcs.settler.HelpfulSettler;

@ModEntry
public class HelpfulNPCsMod {

    public void init() {
        System.out.println("[HelpfulNPCs] Loading...");

        // Same pattern as ExplorerSettler: pirate gate + 100 recruit tickets, no second copy
        SettlerRegistry.registerSettler("hntrinketvendor",
                new HelpfulSettler("hntrinketvendormob", "trinkettip", 100, "defeatpiratecaptain"));
        MobRegistry.registerMob("hntrinketvendormob", TrinketVendorMob.class, true);

        SettlerRegistry.registerSettler("hnpotionvendor",
                new HelpfulSettler("hnpotionvendormob", "potiontip", 100, "defeatpiratecaptain"));
        MobRegistry.registerMob("hnpotionvendormob", PotionVendorMob.class, true);

        // No story gate; modest tickets (between generic 50 and explorer 100)
        SettlerRegistry.registerSettler("hnvinylvendor",
                new HelpfulSettler("hnvinylvendormob", "vinyltip", 75));
        MobRegistry.registerMob("hnvinylvendormob", VinylVendorMob.class, true);

        // Separate settler id so vanilla exoticmerchant visitors stay untouched
        SettlerRegistry.registerSettler("hnexoticvendor", new ExoticSettler());
        MobRegistry.registerMob("hnexoticvendormob", ExoticVendorMob.class, true);

        System.out.println("[HelpfulNPCs] Registered vendors (recruit pool only, Explorer-like).");
    }
}
