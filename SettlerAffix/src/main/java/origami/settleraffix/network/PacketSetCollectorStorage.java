package origami.settleraffix.network;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.inventory.container.Container;
import necesse.inventory.container.settlement.SettlementContainer;
import necesse.level.maps.levelData.settlementData.ServerSettlementData;
import origami.settleraffix.CollectorStorageApplier;
import origami.settleraffix.SettlerAffixMod;

/**
 * Server: set allowTakeFromStorage on all matching collector personalities in the open settlement.
 */
public class PacketSetCollectorStorage extends Packet {

    public final byte type;
    public final boolean allowTakeFromStorage;

    public PacketSetCollectorStorage(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.type = reader.getNextByte();
        this.allowTakeFromStorage = reader.getNextBoolean();
    }

    public PacketSetCollectorStorage(byte type, boolean allowTakeFromStorage) {
        this.type = type;
        this.allowTakeFromStorage = allowTakeFromStorage;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextByte(type);
        writer.putNextBoolean(allowTakeFromStorage);
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        if (client == null) {
            return;
        }
        Container container = client.getContainer();
        if (!(container instanceof SettlementContainer)) {
            return;
        }
        if (!((SettlementContainer) container).hasSettlementAccess(client)) {
            return;
        }
        ServerSettlementData data = ((SettlementContainer) container).getServerData();
        int count = CollectorStorageApplier.apply(data, type, allowTakeFromStorage);
        if (SettlerAffixMod.SETTINGS != null) {
            SettlerAffixMod.SETTINGS.setBlocked(type, !allowTakeFromStorage);
        }
        client.sendPacket(new PacketSetCollectorStorageResult(type, allowTakeFromStorage, count));
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
    }
}
