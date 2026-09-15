package origami.autotrash;

import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.PacketRegistry;
import origami.autotrash.network.PacketOpenAutoTrash;
import origami.autotrash.ui.AutoTrashContainer;
import origami.autotrash.ui.AutoTrashContainerForm;

import java.awt.Color;

@ModEntry
public class AutoTrashMod {

    public static AutoTrashSettings SETTINGS;
    public static int CONTAINER;
    /** Default: N — toggle enabled */
    public static Control TOGGLE;
    /** Default: O — open blacklist manager (not B; B drinks potions) */
    public static Control OPEN_LIST;
    /** Default: Delete — add hovered inventory item to blacklist */
    public static Control ADD_HOVERED;

    public AutoTrashSettings initSettings() {
        SETTINGS = new AutoTrashSettings();
        return SETTINGS;
    }

    public void init() {
        TOGGLE = Control.addModControl(new Control(78, "autotrashtoggle"));
        OPEN_LIST = Control.addModControl(new Control(79, "autotrashopen")); // O
        ADD_HOVERED = Control.addModControl(new Control(261, "autotrashadd")); // GLFW Delete

        CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, packet) -> new AutoTrashContainerForm(
                        client, new AutoTrashContainer(client.getClient(), uniqueSeed, packet)
                ),
                (client, uniqueSeed, packet, serverObject) -> new AutoTrashContainer(client, uniqueSeed, packet)
        );
        PacketRegistry.registerPacket(PacketOpenAutoTrash.class);

        System.out.println("[AutoTrash] Loaded. Default ON. Blacklist in cfg/mods/origami.autotrash.cfg");
    }

    public static boolean isEnabled() {
        return SETTINGS != null && SETTINGS.enabled;
    }

    public static void toggleAndSave(Client client) {
        if (SETTINGS == null) {
            return;
        }
        SETTINGS.enabled = !SETTINGS.enabled;
        necesse.engine.Settings.saveClientSettings();
        String msg = SETTINGS.enabled
                ? Localization.translate("autotrash", "enabled")
                : Localization.translate("autotrash", "disabled");
        if (client != null) {
            client.setMessage(msg, SETTINGS.enabled ? new Color(120, 220, 120) : new Color(220, 140, 140), 3.0f);
        }
    }

    public static void saveSettings() {
        necesse.engine.Settings.saveClientSettings();
    }
}
