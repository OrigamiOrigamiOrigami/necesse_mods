package origami.veinminer.network;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketOpenContainer;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ContainerRegistry;
import origami.veinminer.VeinMinerMod;
import origami.veinminer.ui.VeinMinerContainer;

public class PacketOpenVeinMinerConfig extends Packet {

    public PacketOpenVeinMinerConfig(byte[] data) {
        super(data);
    }

    public PacketOpenVeinMinerConfig() {
        super();
    }

    @Override
    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        ContainerRegistry.openAndSendContainer(
                client,
                new PacketOpenContainer(
                        VeinMinerMod.CONTAINER,
                        VeinMinerContainer.getContainerContent(client)
                )
        );
    }
}
