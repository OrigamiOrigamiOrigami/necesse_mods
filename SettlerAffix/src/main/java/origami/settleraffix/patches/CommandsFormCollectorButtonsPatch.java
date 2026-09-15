package origami.settleraffix.patches;

import necesse.engine.localization.message.LocalMessage;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.client.Client;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.forms.presets.containerComponent.settlement.SettlementCommandsForm;
import necesse.gfx.ui.ButtonColor;
import net.bytebuddy.asm.Advice;
import origami.settleraffix.CollectorStorageApplier;
import origami.settleraffix.SettlerAffixMod;
import origami.settleraffix.SettlerAffixSettings;
import origami.settleraffix.network.PacketSetCollectorStorage;

/**
 * Adds three compact collector one-click buttons on the settlement Commands panel.
 */
@ModMethodPatch(target = SettlementCommandsForm.class, name = "updateNoneSelectedForm", arguments = {})
public class CommandsFormCollectorButtonsPatch {

    @Advice.OnMethodExit
    public static void onExit(@Advice.This SettlementCommandsForm<?> form,
                              @Advice.FieldValue("noneSelectedForm") Form noneSelected) {
        if (form == null || noneSelected == null) {
            return;
        }
        addButtons(form.client, noneSelected);
    }

    public static void addButtons(Client client, Form panel) {
        // Match the SIZE_32 action buttons above: same height, same side padding.
        int rowH = 36;
        int pad = 4;
        panel.setHeight(panel.getHeight() + rowH + pad);

        int y = panel.getHeight() - rowH;
        int side = 4;
        int gap = 4;
        int inner = panel.getWidth() - side * 2 - gap * 2;
        // First label is long; give it more width, short labels share the rest.
        int gemW = inner * 5 / 10;
        int shortW = (inner - gemW) / 2;

        SettlerAffixSettings settings = SettlerAffixMod.SETTINGS;
        boolean gemBlocked = settings != null && settings.blockGemCollector;
        boolean flowerBlocked = settings != null && settings.blockFlowerCollector;
        boolean hiredBlocked = settings != null && settings.blockHiredHand;

        int x = side;
        FormLocalTextButton gem = panel.addComponent(new FormLocalTextButton(
                new LocalMessage("settleraffix", "btngem"),
                x, y, gemW,
                FormInputSize.SIZE_32,
                gemBlocked ? ButtonColor.RED : ButtonColor.BASE
        ));
        gem.setLocalTooltip(new LocalMessage("settleraffix", "tipgem"));
        gem.onClicked(clickHandler(client, CollectorStorageApplier.TYPE_GEM));

        x += gemW + gap;
        FormLocalTextButton flower = panel.addComponent(new FormLocalTextButton(
                new LocalMessage("settleraffix", "btnflower"),
                x, y, shortW,
                FormInputSize.SIZE_32,
                flowerBlocked ? ButtonColor.RED : ButtonColor.BASE
        ));
        flower.setLocalTooltip(new LocalMessage("settleraffix", "tipflower"));
        flower.onClicked(clickHandler(client, CollectorStorageApplier.TYPE_FLOWER));

        x += shortW + gap;
        FormLocalTextButton hired = panel.addComponent(new FormLocalTextButton(
                new LocalMessage("settleraffix", "btnhired"),
                x, y, shortW,
                FormInputSize.SIZE_32,
                hiredBlocked ? ButtonColor.RED : ButtonColor.BASE
        ));
        hired.setLocalTooltip(new LocalMessage("settleraffix", "tiphired"));
        hired.onClicked(clickHandler(client, CollectorStorageApplier.TYPE_HIRED));

        ContainerComponent.setPosInventory(panel);
    }

    public static FormEventListener<FormInputEvent<FormButton>> clickHandler(
            final Client client, final byte type) {
        return new FormEventListener<FormInputEvent<FormButton>>() {
            @Override
            public void onEvent(FormInputEvent<FormButton> e) {
                if (client == null || client.network == null || SettlerAffixMod.SETTINGS == null) {
                    return;
                }
                boolean currentlyBlocked = SettlerAffixMod.SETTINGS.isBlocked(type);
                // Toggle: blocked -> allow; allowing -> stop
                boolean allowTakeFromStorage = currentlyBlocked;
                SettlerAffixMod.SETTINGS.setBlocked(type, !currentlyBlocked);
                client.network.sendPacket(new PacketSetCollectorStorage(type, allowTakeFromStorage));
                if (e.from instanceof FormLocalTextButton) {
                    ((FormLocalTextButton) e.from).color =
                            currentlyBlocked ? ButtonColor.BASE : ButtonColor.RED;
                }
            }
        };
    }
}
