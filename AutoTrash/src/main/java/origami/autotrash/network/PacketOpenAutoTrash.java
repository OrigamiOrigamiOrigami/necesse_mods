package origami.autotrash.network;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import origami.autotrash.AutoTrashMod;
import origami.autotrash.ui.AutoTrashContainer;

public class PacketOpenAutoTrash extends Packet {

    public PacketOpenAutoTrash(byte[] data) {
        super(data);
    }

    public PacketOpenAutoTrash() {
        super();
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        ContainerRegistry.openAndSendContainer(
                client,
                new PacketOpenContainer(
                        AutoTrashMod.CONTAINER,
                        AutoTrashContainer.getContainerContent(client)
                )
        );
    }
}
