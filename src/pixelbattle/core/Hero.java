package pixelbattle.core;

import pixelbattle.engine.Sprite;

/** Герой — ним керує гравець. */
public abstract class Hero extends Fighter {
    protected Hero(String name, int maxHp, Sprite sprite) {
        super(name, maxHp, sprite);
    }
}
