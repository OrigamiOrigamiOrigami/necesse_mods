package origami.veinminer.patches;

import necesse.engine.input.Input;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.engine.window.GameWindow;
import necesse.engine.gameLoop.tickManager.TickManager;
import net.bytebuddy.asm.Advice;
import origami.veinminer.VeinMinerMod;
import origami.veinminer.network.PacketOpenVeinMinerConfig;
import origami.veinminer.ui.VeinMinerContainer;

@ModMethodPatch(target = MainGame.class, name = "frameTick",
        arguments = {TickManager.class, GameWindow.class})
public class VeinMinerHotkeyPatch {

    @Advice.OnMethodEnter
    static void onEnter(@Advice.This MainGame mainGame) {
        if (Input.isTyping) {
            return;
        }
        Client client = mainGame.getClient();
        if (client == null || client.getPlayer() == null) {
            return;
        }
        if (VeinMinerMod.TOGGLE != null && VeinMinerMod.TOGGLE.isPressed()) {
            VeinMinerMod.toggleAndSave(client);
        }
        if (VeinMinerMod.OPEN_CONFIG != null && VeinMinerMod.OPEN_CONFIG.isPressed()) {
            if (client.getContainer() instanceof VeinMinerContainer) {
                client.closeContainer(true);
            } else {
                client.network.sendPacket(new PacketOpenVeinMinerConfig());
            }
        }
    }
}
