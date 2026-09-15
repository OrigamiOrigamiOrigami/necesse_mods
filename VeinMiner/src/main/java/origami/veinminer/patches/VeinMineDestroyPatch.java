package origami.veinminer.patches;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.network.server.ServerClient;
import necesse.engine.util.PointHashSet;
import necesse.entity.ObjectDamageResult;
import necesse.entity.mobs.Attacker;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;
import origami.veinminer.VeinMinerMod;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Mirrors ChanceToMineFullClusterLevelEvent: BFS on 4-adjacent same-type objects.
 */
@ModMethodPatch(target = Level.class, name = "onObjectDestroyed", arguments = {
        GameObject.class, int.class, int.class, int.class,
        Attacker.class, ServerClient.class, ArrayList.class
})
public class VeinMineDestroyPatch {

    public static final ThreadLocal<Boolean> CHAINING = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    @Advice.OnMethodExit
    static void onExit(@Advice.This Level level,
                       @Advice.Argument(0) GameObject destroyed,
                       @Advice.Argument(1) int layer,
                       @Advice.Argument(2) int tileX,
                       @Advice.Argument(3) int tileY,
                       @Advice.Argument(5) ServerClient client) {
        if (CHAINING.get()) {
            return;
        }
        if (!VeinMinerMod.isEnabled() || VeinMinerMod.SETTINGS == null) {
            return;
        }
        if (client == null || level == null || destroyed == null) {
            return;
        }
        if (!level.isServer()) {
            return;
        }
        if (!VeinMinerMod.SETTINGS.canChain(destroyed)) {
            return;
        }
        if (level.isProtected(tileX, tileY)) {
            return;
        }

        int max = VeinMinerMod.SETTINGS.maxChain;
        int targetId = destroyed.getID();

        LinkedList<Point> queue = new LinkedList<Point>();
        PointHashSet visited = new PointHashSet();
        queue.add(new Point(tileX, tileY));
        visited.add(tileX, tileY);
        int broken = 0;

        CHAINING.set(Boolean.TRUE);
        try {
            while (!queue.isEmpty() && broken < max) {
                Point p = queue.removeFirst();
                for (Point off : Level.adjacentGettersNotDiagonal) {
                    int nx = p.x + off.x;
                    int ny = p.y + off.y;
                    if (visited.contains(nx, ny)) {
                        continue;
                    }
                    visited.add(nx, ny);
                    if (!isSameChainTarget(level, layer, nx, ny, targetId)) {
                        continue;
                    }
                    ObjectDamageResult result = level.entityManager.destroyObjectOverride(layer, nx, ny);
                    broken++;
                    if (result != null && result.destroyed) {
                        queue.add(new Point(nx, ny));
                    }
                    if (broken >= max) {
                        break;
                    }
                }
            }
        } finally {
            CHAINING.set(Boolean.FALSE);
        }
    }

    // Must be public: Advice is inlined into Level.
    public static boolean isSameChainTarget(Level level, int layer, int x, int y, int targetId) {
        GameObject obj = level.getObject(layer, x, y);
        if (obj == null || obj.getID() != targetId) {
            return false;
        }
        if (VeinMinerMod.SETTINGS == null || !VeinMinerMod.SETTINGS.canChain(obj)) {
            return false;
        }
        if (level.isProtected(x, y)) {
            return false;
        }
        if (level.objectLayer != null && level.objectLayer.isPlayerPlaced(x, y)) {
            return false;
        }
        return true;
    }
}
