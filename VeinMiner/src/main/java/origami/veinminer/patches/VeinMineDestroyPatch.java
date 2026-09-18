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
import origami.veinminer.VeinMinerSettings;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Ore/stone: 4-adjacent BFS (vanilla cluster style).
 * Trees: Chebyshev-radius BFS so a forest patch chains even when trees are not tile-adjacent.
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

    public static final Point[] TREE_NEIGHBORS = buildTreeNeighbors(VeinMinerSettings.TREE_CONNECT_RADIUS);

    @Advice.OnMethodExit
    static void onExit(@Advice.This Level level,
                       @Advice.Argument(0) GameObject destroyed,
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
        boolean treeMode = VeinMinerSettings.isTreeLike(destroyed);
        Point[] offsets = treeMode ? TREE_NEIGHBORS : Level.adjacentGettersNotDiagonal;

        LinkedList<Point> queue = new LinkedList<Point>();
        PointHashSet visited = new PointHashSet();
        queue.add(new Point(tileX, tileY));
        visited.add(tileX, tileY);
        int broken = 0;

        CHAINING.set(Boolean.TRUE);
        try {
            while (!queue.isEmpty() && broken < max) {
                Point p = queue.removeFirst();
                for (int i = 0; i < offsets.length; i++) {
                    Point off = offsets[i];
                    int nx = p.x + off.x;
                    int ny = p.y + off.y;
                    if (visited.contains(nx, ny)) {
                        continue;
                    }
                    visited.add(nx, ny);
                    if (!isSameChainTarget(level, nx, ny, destroyed, treeMode)) {
                        continue;
                    }
                    ObjectDamageResult result = level.entityManager.destroyObjectOverride(0, nx, ny);
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
    public static boolean isSameChainTarget(Level level, int x, int y, GameObject destroyed, boolean treeMode) {
        GameObject obj = level.getObject(x, y);
        if (obj == null || destroyed == null) {
            return false;
        }
        if (VeinMinerMod.SETTINGS == null || !VeinMinerMod.SETTINGS.canChain(obj)) {
            return false;
        }
        if (!VeinMinerMod.SETTINGS.sameChainVein(destroyed, obj)) {
            return false;
        }
        if (level.isProtected(x, y)) {
            return false;
        }
        // Allow player-grown trees to chain; keep the check for ores/rocks/objects.
        if (!treeMode && level.objectLayer != null && level.objectLayer.isPlayerPlaced(x, y)) {
            return false;
        }
        return true;
    }

    public static Point[] buildTreeNeighbors(int radius) {
        if (radius < 1) {
            radius = 1;
        }
        ArrayList<Point> list = new ArrayList<Point>();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                list.add(new Point(dx, dy));
            }
        }
        return list.toArray(new Point[list.size()]);
    }
}
