package origami.helpfulnpcs.mobs;

import necesse.engine.registries.MusicRegistry;
import necesse.engine.sound.GameMusic;
import origami.helpfulnpcs.shop.ShopEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Sells all vanilla music vinyls (item id = musicStringId + "vinyl").
 * Prices sit near Exotic Merchant jingle-jam vinyl (250-350) and broker value (~50).
 */
public class VinylVendorMob extends HelpfulVendorMob {

    public VinylVendorMob() {
        super("hnvinylvendor", 450);
    }

    @Override
    protected Iterable<ShopEntry> shopEntries() {
        List<ShopEntry> entries = new ArrayList<ShopEntry>();
        // Portable player is the appliance that plays vinyls
        entries.add(ShopEntry.stocked("portablemusicplayer", 400, 600, 20, 2, 1));

        for (GameMusic music : MusicRegistry.getMusic()) {
            String itemId = music.getStringID() + "vinyl";
            // Holiday / rare track priced like Exotic Merchant
            if ("theeldersjinglejam".equals(music.getStringID())) {
                entries.add(ShopEntry.stocked(itemId, 250, 350, 10, 2, 1));
            } else {
                // Broker value is 50; sell at a fair collectible markup
                entries.add(ShopEntry.stocked(itemId, 150, 250, 10, 3, 1));
            }
        }
        return entries;
    }
}
