package origami.itembrowser.lookup;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.inventory.container.Container;

/** Hotkey-only UI container — no inventory item required. */
public class ItemBrowserContainer extends Container {

    public ItemBrowserContainer(NetworkClient client, int uniqueSeed, Packet content) {
        super(client, uniqueSeed);
    }
}
