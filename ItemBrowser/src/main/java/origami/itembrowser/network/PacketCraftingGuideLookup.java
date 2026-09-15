package origami.itembrowser.network;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.ItemRegistry;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.container.Container;
import necesse.inventory.container.item.CraftingGuideContainer;
import necesse.inventory.container.slots.ContainerSlot;
import necesse.inventory.item.Item;
import origami.itembrowser.lookup.CraftingGuideLookup;

/**
 * Opens vanilla Crafting Guide and shows recipes that use the given item.
 * Uses a locked filter stack that is cleared on close (never added to the player).
 */
public class PacketCraftingGuideLookup extends Packet {

    public final String itemStringId;

    public PacketCraftingGuideLookup(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.itemStringId = reader.getNextString();
    }

    public PacketCraftingGuideLookup(String itemStringId) {
        super();
        this.itemStringId = itemStringId == null ? "" : itemStringId;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextString(this.itemStringId);
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        if (itemStringId == null || itemStringId.isEmpty()) {
            return;
        }
        Item item = ItemRegistry.getItem(itemStringId);
        if (item == null) {
            return;
        }
        PlayerInventorySlot guideSlot = CraftingGuideLookup.findCraftingGuideSlot(client);
        if (guideSlot == null) {
            return;
        }

        Container current = client.getContainer();
        if (!(current instanceof CraftingGuideContainer)) {
            PacketOpenContainer open = new PacketOpenContainer(
                    ContainerRegistry.CRAFTING_GUIDE_CONTAINER,
                    CraftingGuideContainer.getContainerContent(client, guideSlot)
            );
            ContainerRegistry.openAndSendContainer(client, open);
            current = client.getContainer();
        }

        if (!(current instanceof CraftingGuideContainer)) {
            return;
        }

        CraftingGuideContainer guide = (CraftingGuideContainer) current;
        ContainerSlot slot = guide.getSlot(guide.INGREDIENT_SLOT);
        InventoryItem existing = slot.getItem();
        // Return a real player-placed stack first; never destroy it, never refund our filter.
        if (existing != null && existing.getAmount() > 0 && !CraftingGuideLookup.isPhantomFilter(guide)) {
            client.playerMob.getInv().addItemsDropRemaining(
                    existing.copy(),
                    "addback",
                    client.playerMob,
                    false,
                    false
            );
        }

        slot.setItem(new InventoryItem(item, 1));
        guide.lockSlot(guide.INGREDIENT_SLOT);
        CraftingGuideLookup.markPhantomFilter(guide);
    }
}
