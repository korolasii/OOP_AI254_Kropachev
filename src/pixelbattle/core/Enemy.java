package pixelbattle.core;

import pixelbattle.engine.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * Ворог — ним керує комп'ютер.
 * Ультимейт ворог використовує сам, щойно той готовий.
 * Інакше — випадкове вміння, яке не на перезарядці.
 */
public abstract class Enemy extends Fighter {
    protected Enemy(String name, int maxHp, Sprite sprite) {
        super(name, maxHp, sprite);
    }

    /** Яке вміння використати цього ходу (можна перевизначити — «розумніший» ворог). */
    public Skill chooseSkill() {
        List<Skill> ready = new ArrayList<>();
        for (Skill s : getSkills()) if (getCooldownLeft(s) == 0) ready.add(s);
        return ready.isEmpty() ? null : ready.get(random(0, ready.size() - 1));
    }
}
