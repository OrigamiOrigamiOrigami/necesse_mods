package origami.veinminer;

import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.PacketRegistry;
import origami.veinminer.network.PacketOpenVeinMinerConfig;
import origami.veinminer.ui.VeinMinerContainer;
import origami.veinminer.ui.VeinMinerContainerForm;

import java.awt.Color;

@ModEntry
public class VeinMinerMod {

    public static VeinMinerSettings SETTINGS;
    public static int CONTAINER;
    /** Default: V — toggle on/off */
    public static Control TOGGLE;
    /** Default: K — open chain config */
    public static Control OPEN_CONFIG;

    public VeinMinerSettings initSettings() {
        SETTINGS = new VeinMinerSettings();
        return SETTINGS;
    }

    public void init() {
        TOGGLE = Control.addModControl(new Control(86, "veinminertoggle"));
        OPEN_CONFIG = Control.addModControl(new Control(75, "veinminerconfig"));

        CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, packet) -> new VeinMinerContainerForm(
                        client, new VeinMinerContainer(client.getClient(), uniqueSeed, packet)
                ),
                (client, uniqueSeed, packet, serverObject) -> new VeinMinerContainer(client, uniqueSeed, packet)
        );
        PacketRegistry.registerPacket(PacketOpenVeinMinerConfig.class);

        System.out.println("[VeinMiner] Loaded. Default ON. V toggles, K opens config.");
    }

    public static boolean isEnabled() {
        return SETTINGS != null && SETTINGS.enabled;
    }

    public static void toggleAndSave(Client client) {
        if (SETTINGS == null) {
            return;
        }
        SETTINGS.enabled = !SETTINGS.enabled;
        saveSettings();
        String msg = SETTINGS.enabled
                ? Localization.translate("veinminer", "enabled")
                : Localization.translate("veinminer", "disabled");
        if (client != null) {
            client.setMessage(msg, SETTINGS.enabled ? new Color(120, 220, 120) : new Color(220, 140, 140), 3.0f);
        }
    }

    public static void saveSettings() {
        necesse.engine.Settings.saveClientSettings();
    }
}
