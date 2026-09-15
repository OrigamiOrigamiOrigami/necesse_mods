package origami.autotrash.ui;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.InputEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.components.FormItemIcon;
import necesse.inventory.InventoryItem;

import java.awt.Rectangle;

/** Item icon that runs a callback on left click (used to remove from blacklist). */
public class ClickableItemIcon extends FormItemIcon {

    private final Runnable onLeftClick;

    public ClickableItemIcon(int x, int y, InventoryItem item, Runnable onLeftClick) {
        super(x, y, item, true);
        this.onLeftClick = onLeftClick;
        this.showNameAsTooltip = true;
    }

    @Override
    public void handleInputEvent(InputEvent event, TickManager tickManager, PlayerMob perspective) {
        super.handleInputEvent(event, tickManager, perspective);
        if (onLeftClick == null || event == null) {
            return;
        }
        if (!event.isMouseClickEvent() || !event.state) {
            return;
        }
        if (!isMouseOver(event)) {
            return;
        }
        onLeftClick.run();
        event.use();
    }
}
