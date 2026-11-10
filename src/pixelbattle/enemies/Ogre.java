package pixelbattle.enemies;

import pixelbattle.core.Enemy;
import pixelbattle.core.Fighter;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.core.Ultimate;
import pixelbattle.engine.Sprite;

/** Огр — бос. Повільний, але дуже сильний. */
public class Ogre extends Enemy {

    public Ogre() {
        super("Огр", 200, new Sprite("ogre_dark_idle_anim", 4, 4));

        addSkill(new Skill("Удар кийком", Target.ENEMY, 0, (user, target) -> {
            int dmg = random(14, 20);
            if (user.getHp() < user.getMaxHp() / 2) dmg += 8;    // лють
            return user.getName() + " б'є кийком " + target.getName() + ": "
                    + target.takeDamage(dmg) + " шкоди";
        }));

        // TODO 10: «Землетрус» — анонімний клас
        setUltimate(new Ultimate() {
            private static final int QUAKE_DAMAGE = 12;
            private static final int SELF_HEAL = 20;

            @Override
            public String getName() { return "Землетрус"; }

            @Override
            public String getDescription() {
                return QUAKE_DAMAGE + " шкоди кожному герою, огр відновлює " + SELF_HEAL + " HP";
            }

            @Override
            public String activate(Fighter user, Fighter[] allies, Fighter[] enemies) {
                int total = 0;
                // для огра «вороги» — це герої
                for (Fighter hero : enemies) {
                    if (hero.isAlive()) total += hero.takeDamage(QUAKE_DAMAGE);
                }
                int healed = user.heal(SELF_HEAL);
                return user.getName() + " тупотить — ЗЕМЛЕТРУС! Героям " + total
                        + " шкоди, огр +" + healed + " HP";
            }
        });
    }

    // ПРИКЛАД ПАСИВКИ: «Товста шкіра» — за один удар огр втрачає не більше 25 HP.
    @Override
    protected int modifyIncomingDamage(int damage) {
        return Math.min(damage, 25);
    }
}
