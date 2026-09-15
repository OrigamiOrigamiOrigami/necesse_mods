package origami.itembrowser;

import necesse.engine.input.Control;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.PacketRegistry;
import origami.itembrowser.lookup.ItemBrowserContainer;
import origami.itembrowser.lookup.ItemBrowserContainerForm;
import origami.itembrowser.network.PacketCraftingGuideLookup;
import origami.itembrowser.network.PacketOpenItemBrowser;

@ModEntry
public class ItemBrowserMod {

    public static int ITEM_BROWSER_CONTAINER;
    /** Default key: grave accent / ~ (GLFW 96) */
    public static Control OPEN_BROWSER;
    /** Default key: U (GLFW 85) — Crafting Guide usage lookup */
    public static Control CRAFTING_GUIDE_LOOKUP;

    public void init() {
        System.out.println("[ItemBrowser] Loading...");

        OPEN_BROWSER = Control.addModControl(new Control(96, "openitembrowser"));
        CRAFTING_GUIDE_LOOKUP = Control.addModControl(new Control(85, "craftingguidelookup"));

        ITEM_BROWSER_CONTAINER = ContainerRegistry.registerContainer(
                (client, uniqueSeed, packet) -> new ItemBrowserContainerForm(
                        client, new ItemBrowserContainer(client.getClient(), uniqueSeed, packet)
                ),
                (client, uniqueSeed, packet, serverObject) -> new ItemBrowserContainer(client, uniqueSeed, packet)
        );

        PacketRegistry.registerPacket(PacketOpenItemBrowser.class);
        PacketRegistry.registerPacket(PacketCraftingGuideLookup.class);

        System.out.println("[ItemBrowser] Hotkeys: ~ browser, U crafting-guide lookup.");
    }
}
