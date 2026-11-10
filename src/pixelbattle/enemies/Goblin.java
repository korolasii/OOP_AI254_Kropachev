package pixelbattle.enemies;

import pixelbattle.core.Enemy;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.engine.Sprite;

/** Гоблін — підступний і швидкий. */
public class Goblin extends Enemy {

    public Goblin() {
        super("Гоблін", 70, new Sprite("goblin_red_idle_anim", 4, 6));

        addSkill(new Skill("Удар кинджалом", Target.ENEMY, 0, (user, target) ->
                user.getName() + " б'є кинджалом " + target.getName() + ": "
                        + target.takeDamage(random(8, 12)) + " шкоди"));

        // БОНУС A: «Бомба» — 15..22 шкоди та оглушення на 1 хід
        addSkill(new Skill("Бомба", Target.ENEMY, 3, (user, target) -> {
            int dmg = target.takeDamage(random(15, 22));
            if (target.isAlive()) target.stun(1);
            return user.getName() + " підкидає бомбу під ноги " + target.getName()
                    + ": " + dmg + " шкоди та оглушення";
        }));
    }
}
