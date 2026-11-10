package pixelbattle.heroes;

import pixelbattle.core.Hero;
import pixelbattle.core.Skill;
import pixelbattle.core.SkillEffect;
import pixelbattle.core.Target;
import pixelbattle.engine.Sprite;
import pixelbattle.ultimates.Meteor;

/** Маг — потужні заклинання та лікування. */
public class Mage extends Hero {

    public Mage() {
        super("Маг", 70, new Sprite("wizzard_dark_idle_anim", 4, 4));

        addSkill(new Skill("Удар посохом", Target.ENEMY, 0, (user, target) ->
                user.getName() + " б'є посохом " + target.getName() + ": "
                        + target.takeDamage(random(6, 10)) + " шкоди"));

        // TODO 2: ефекти збережені в змінні типу SkillEffect, а потім передані у Skill
        SkillEffect fireball = (user, target) -> {
            int dmg = target.takeDamage(random(22, 30));
            String result = target.isAlive() ? "" : " — " + target.getName() + " згорає дотла!";
            return user.getName() + " кидає вогняну кулю в " + target.getName()
                    + ": " + dmg + " шкоди" + result;
        };
        SkillEffect healing = (user, target) -> {
            int healed = target.heal(25);
            return healed == 0
                    ? user.getName() + " зцілює " + target.getName() + ", але той і так цілий"
                    : user.getName() + " зцілює " + target.getName() + " на " + healed + " HP";
        };

        addSkill(new Skill("Вогняна куля", Target.ENEMY, 1, fireball));
        addSkill(new Skill("Зцілення", Target.ALLY, 2, healing));

        // TODO 8: ультимейт — окремий іменований клас
        setUltimate(new Meteor());
    }

    // НОВЕ: пасивка «Медитація» — на початку ходу маг відновлює 3 HP
    @Override
    protected void onTurnStart() {
        heal(3);
    }
}
