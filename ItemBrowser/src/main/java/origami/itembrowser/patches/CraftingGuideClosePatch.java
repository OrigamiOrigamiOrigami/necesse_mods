package origami.itembrowser.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.inventory.container.Container;
import necesse.inventory.container.item.CraftingGuideContainer;
import net.bytebuddy.asm.Advice;
import origami.itembrowser.lookup.CraftingGuideLookup;

/**
 * Clears the U-key filter stack before temp inventory dispose would refund it.
 */
@ModMethodPatch(target = Container.class, name = "onClose", arguments = {})
public class CraftingGuideClosePatch {

    @Advice.OnMethodEnter
    static void onEnter(@Advice.This Container container) {
        if (!(container instanceof CraftingGuideContainer)) {
            return;
        }
        CraftingGuideContainer guide = (CraftingGuideContainer) container;
        if (!CraftingGuideLookup.consumePhantomFilter(guide)) {
            return;
        }
        if (guide.ingredientInv != null) {
            guide.ingredientInv.clearSlot(0);
        }
    }
}
