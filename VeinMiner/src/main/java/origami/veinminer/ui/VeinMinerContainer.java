package origami.veinminer.ui;

import necesse.engine.network.NetworkClient;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.server.ServerClient;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerTempInventory;
import necesse.inventory.container.Container;
import necesse.inventory.container.customAction.BooleanCustomAction;
import necesse.inventory.container.customAction.EmptyCustomAction;
import necesse.inventory.container.customAction.IntCustomAction;
import necesse.inventory.container.slots.ContainerSlot;
import origami.veinminer.VeinMinerMod;

public class VeinMinerContainer extends Container {

    public final int ADD_SLOT;
    public final PlayerTempInventory addInv;
    public final EmptyCustomAction consumeAddSlot;
    public final BooleanCustomAction setEnabled;
    public final BooleanCustomAction setAllOres;
    public final BooleanCustomAction setAllTrees;
    public final IntCustomAction setMaxChain;

    public VeinMinerContainer(NetworkClient client, int uniqueSeed, Packet content) {
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
                if (VeinMinerMod.SETTINGS != null) {
                    VeinMinerMod.SETTINGS.addFromItem(item.item);
                    VeinMinerMod.saveSettings();
                }
                transferToSlots(slot, CLIENT_HOTBAR_START, CLIENT_INVENTORY_END);
            }
        });

        this.setEnabled = registerAction(new BooleanCustomAction() {
            @Override
            protected void run(boolean value) {
                if (VeinMinerMod.SETTINGS != null) {
                    VeinMinerMod.SETTINGS.enabled = value;
                    VeinMinerMod.saveSettings();
                }
            }
        });

        this.setAllOres = registerAction(new BooleanCustomAction() {
            @Override
            protected void run(boolean value) {
                if (VeinMinerMod.SETTINGS != null) {
                    VeinMinerMod.SETTINGS.allOres = value;
                    VeinMinerMod.saveSettings();
                }
            }
        });

        this.setAllTrees = registerAction(new BooleanCustomAction() {
            @Override
            protected void run(boolean value) {
                if (VeinMinerMod.SETTINGS != null) {
                    VeinMinerMod.SETTINGS.allTrees = value;
                    VeinMinerMod.saveSettings();
                }
            }
        });

        this.setMaxChain = registerAction(new IntCustomAction() {
            @Override
            protected void run(int value) {
                if (VeinMinerMod.SETTINGS == null) {
                    return;
                }
                if (value < 8) {
                    value = 8;
                }
                if (value > 512) {
                    value = 512;
                }
                VeinMinerMod.SETTINGS.maxChain = value;
                VeinMinerMod.saveSettings();
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
