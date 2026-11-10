package pixelbattle.enemies;

import pixelbattle.core.Enemy;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.engine.Sprite;

/** БОНУС B: Скелет — один раз відмовляється помирати. */
public class Skeleton extends Enemy {

    private boolean usedResurrection;

    public Skeleton() {
        super("Скелет", 50, new Sprite("skelet_ice_idle_anim", 4, 6));

        addSkill(new Skill("Кістяний удар", Target.ENEMY, 0, (user, target) ->
                user.getName() + " гатить кісткою по " + target.getName() + ": "
                        + target.takeDamage(random(8, 12)) + " шкоди"));
    }

    // Пасивка «Воскресіння»: перший смертельний удар залишає 1 HP.
    @Override
    protected int modifyIncomingDamage(int damage) {
        boolean lethal = damage >= getHp();
        if (lethal && !usedResurrection) {
            usedResurrection = true;
            return getHp() - 1;          // після удару лишиться рівно 1 HP
        }
        return damage;
    }
}
