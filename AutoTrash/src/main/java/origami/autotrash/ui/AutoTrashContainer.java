package origami.autotrash.ui;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerTempInventory;
import necesse.inventory.container.Container;
import necesse.inventory.container.customAction.EmptyCustomAction;
import necesse.inventory.container.slots.ContainerSlot;
import origami.autotrash.AutoTrashMod;

public class AutoTrashContainer extends Container {

    public final int ADD_SLOT;
    public final PlayerTempInventory addInv;
    public final EmptyCustomAction consumeAddSlot;

    public AutoTrashContainer(NetworkClient client, int uniqueSeed, Packet content) {
        super(client, uniqueSeed);
        PacketReader reader = new PacketReader(content);
        Packet tempPacket = reader.getNextContentPacket();
        this.addInv = client.playerMob.getInv().applyTempInventoryPacket(tempPacket, m -> isClosed());
        this.ADD_SLOT = addSlot(new ContainerSlot(this.addInv, 0));
        addInventoryQuickTransfer(ADD_SLOT, ADD_SLOT);

        this.consumeAddSlot = registerAction(new EmptyCustomAction() {
            @Override
            protected void run() {
                ContainerSlot slot = getSlot(ADD_SLOT);
                InventoryItem item = slot.getItem();
                if (item == null || item.item == null || item.getAmount() <= 0) {
                    return;
                }
                String id = item.item.getStringID();
                if (AutoTrashMod.SETTINGS != null && id != null) {
                    AutoTrashMod.SETTINGS.add(id);
                    AutoTrashMod.saveSettings();
                }
                // Put the stack back into the player inventory (do not destroy it).
                transferToSlots(slot, CLIENT_HOTBAR_START, CLIENT_INVENTORY_END);
            }
        });
    }

    public static Packet getContainerContent(ServerClient client) {
        Packet packet = new Packet();
        PacketWriter writer = new PacketWriter(packet);
        writer.putNextContentPacket(client.playerMob.getInv().getTempInventoryPacket(1));
        return packet;
    }
}
