package origami.settleraffix.network;

import necesse.engine.localization.Localization;
import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import origami.settleraffix.SettlerAffixMod;

import java.awt.Color;

public class PacketSetCollectorStorageResult extends Packet {

    public final byte type;
    public final boolean allowTakeFromStorage;
    public final int count;

    public PacketSetCollectorStorageResult(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.type = reader.getNextByte();
        this.allowTakeFromStorage = reader.getNextBoolean();
        this.count = reader.getNextInt();
    }

    public PacketSetCollectorStorageResult(byte type, boolean allowTakeFromStorage, int count) {
        this.type = type;
        this.allowTakeFromStorage = allowTakeFromStorage;
        this.count = count;
        PacketWriter writer = new PacketWriter(this);
        writer.putNextByte(type);
        writer.putNextBoolean(allowTakeFromStorage);
        writer.putNextInt(count);
    }

    @Override
    public void processClient(NetworkPacket packet, Client client) {
        if (SettlerAffixMod.SETTINGS != null) {
            SettlerAffixMod.SETTINGS.setBlocked(type, !allowTakeFromStorage);
            SettlerAffixMod.saveSettings();
        }
        if (client == null) {
            return;
        }
        String key = allowTakeFromStorage ? "appliedon" : "appliedoff";
        String msg = Localization.translate("settleraffix", key, "count", Integer.toString(count));
        client.setMessage(msg, allowTakeFromStorage ? new Color(120, 220, 120) : new Color(220, 140, 140), 3.0f);
    }
}
