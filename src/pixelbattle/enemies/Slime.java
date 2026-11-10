package pixelbattle.enemies;

import pixelbattle.core.Enemy;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.engine.Sprite;

/** Слиз — слабкий, але живучий. */
public class Slime extends Enemy {

    private static final int REGEN = 6;

    public Slime() {
        super("Слиз", 60, new Sprite("swampy_ice_anim", 4, 6));

        addSkill(new Skill("Стрибок", Target.ENEMY, 0, (user, target) ->
                user.getName() + " стрибає на " + target.getName() + ": "
                        + target.takeDamage(random(5, 9)) + " шкоди"));

        // НОВЕ: «Липкий плювок» — мало шкоди, але ціль пропускає хід
        addSkill(new Skill("Липкий плювок", Target.ENEMY, 4, (user, target) -> {
            int dmg = target.takeDamage(random(2, 4));
            target.stun(1);
            return user.getName() + " заліплює " + target.getName() + " слизом: " + dmg + " шкоди, ціль застрягла";
        }));
    }

    // TODO 6: «Регенерація» — +6 HP на початку свого ходу
    @Override
    protected void onTurnStart() {
        if (getHp() < getMaxHp()) heal(REGEN);
    }
}
