package runes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Turn engine distilled from docs 10-data + 11-battle-system. */
public final class BattleEngine {
    public static final class Battler {
        public final String name;
        public int hp;
        public final int maxHp;
        public final int atk;
        public final int def;
        public final int spd;
        public final int tier;

        public Battler(String name, int hp, int atk, int def, int spd, int tier) {
            this.name = name;
            this.hp = hp;
            this.maxHp = hp;
            this.atk = atk;
            this.def = def;
            this.spd = spd;
            this.tier = tier;
        }

        public boolean alive() {
            return hp > 0;
        }
    }

    /** Tier scale from b.a(): 1<<tier below 6, 256<<(tier-6) at/above 6. */
    public static int tierScale(int tier) {
        if (tier >= 6) {
            return 256 << (tier - 6);
        }
        return 1 << tier;
    }

    public static int damage(int atk, int tier, int def) {
        int scaled = atk * Math.max(1, tierScale(tier) / 256);
        return Math.max(1, scaled + 1 - def);
    }

    public static List<Battler> turnOrder(List<Battler> allies, List<Battler> enemies) {
        List<Battler> all = new ArrayList<>();
        all.addAll(allies);
        all.addAll(enemies);
        all.sort(Comparator.comparingInt((Battler b) -> b.spd).reversed());
        return all;
    }
}
