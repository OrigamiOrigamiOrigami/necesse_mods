package origami.helpfulnpcs.settler;

import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.util.TicketSystemList;
import necesse.entity.mobs.friendly.human.HumanMob;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.levelData.settlementData.ServerSettlementData;
import necesse.level.maps.levelData.settlementData.settler.Settler;

import java.util.function.Supplier;

/**
 * Same recruit-visitor pattern as vanilla ExplorerSettler:
 * optional story gate, skip second copy on forced recruit, fixed ticket weight.
 */
public class HelpfulSettler extends Settler {

    private final String localeKey;
    private final int recruitTickets;
    /** Story objective id, or null for no gate. */
    private final String requiredStoryObjective;

    public HelpfulSettler(String mobStringId, String localeKey, int recruitTickets) {
        this(mobStringId, localeKey, recruitTickets, null);
    }

    public HelpfulSettler(String mobStringId, String localeKey, int recruitTickets,
                          String requiredStoryObjective) {
        super(mobStringId);
        this.localeKey = localeKey;
        this.recruitTickets = recruitTickets;
        this.requiredStoryObjective = requiredStoryObjective;
    }

    @Override
    public void loadTextures() {
        this.texture = GameTexture.fromFile("mobs/icons/human");
    }

    @Override
    public GameMessage getAcquireTip() {
        return new LocalMessage("helpfulnpcs", localeKey);
    }

    @Override
    public void addNewRecruitSettler(ServerSettlementData data, boolean isRandomEvent,
                                     TicketSystemList<Supplier<HumanMob>> ticketSystem) {
        // Mirror ExplorerSettler: forced recruit skips if already hired
        if (!isRandomEvent && doesSettlementHaveThisSettler(data)) {
            return;
        }
        if (requiredStoryObjective != null
                && !data.storyObjectives.hasCompletedStoryObjectiveOrHigher(requiredStoryObjective)) {
            return;
        }
        if (recruitTickets <= 0) {
            return;
        }
        ticketSystem.addObject(recruitTickets, getNewRecruitMob(data));
    }
}
