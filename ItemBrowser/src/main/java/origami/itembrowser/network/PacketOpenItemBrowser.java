package origami.itembrowser.network;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import origami.itembrowser.ItemBrowserMod;

public class PacketOpenItemBrowser extends Packet {

    public PacketOpenItemBrowser(byte[] data) {
        super(data);
    }

    public PacketOpenItemBrowser() {
        super();
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        ContainerRegistry.openAndSendContainer(
                client,
                new PacketOpenContainer(ItemBrowserMod.ITEM_BROWSER_CONTAINER)
        );
    }
}
