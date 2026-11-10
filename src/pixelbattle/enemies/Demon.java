package pixelbattle.enemies;

import pixelbattle.core.Enemy;
import pixelbattle.core.Fighter;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.core.Ultimate;
import pixelbattle.engine.Sprite;

/**
 * БОНУС C: власний ворог — Демон.
 *  - «Пекельний кіготь»: 10–15 шкоди;
 *  - «Висмоктування життя» (перезарядка 2): 8–12 шкоди, демон лікується на завдану шкоду;
 *  - пасивка «Пекельна лють»: кожен третій отриманий удар демон ігнорує;
 *  - ультимейт «Пекельне полум'я»: 10 шкоди всім героям і оглушення найсильнішого;
 *  - «розумний» вибір вміння: при HP нижче половини перевага висмоктуванню життя.
 */
public class Demon extends Enemy {

    private final Skill claw;
    private final Skill drain;
    private int hitsTaken;

    public Demon() {
        super("Демон", 120, new Sprite("big_demon_void_idle_anim", 4, 3));

        claw = new Skill("Пекельний кіготь", Target.ENEMY, 0, (user, target) ->
                user.getName() + " роздирає " + target.getName() + " кігтями: "
                        + target.takeDamage(random(10, 15)) + " шкоди");

        drain = new Skill("Висмоктування життя", Target.ENEMY, 2, (user, target) -> {
            int dmg = target.takeDamage(random(8, 12));
            int healed = user.heal(dmg);
            return user.getName() + " висмоктує життя з " + target.getName() + ": "
                    + dmg + " шкоди, +" + healed + " HP демону";
        });

        addSkill(claw);
        addSkill(drain);

        setUltimate(new Ultimate() {
            @Override
            public String getName() { return "Пекельне полум'я"; }

            @Override
            public String getDescription() {
                return "10 шкоди всім героям, найздоровіший герой оглушений на 1 хід";
            }

            @Override
            public String activate(Fighter user, Fighter[] allies, Fighter[] enemies) {
                int total = 0;
                Fighter strongest = null;
                for (Fighter hero : enemies) {
                    if (!hero.isAlive()) continue;
                    total += hero.takeDamage(10);
                    if (hero.isAlive() && (strongest == null || hero.getHp() > strongest.getHp())) {
                        strongest = hero;
                    }
                }
                String log = user.getName() + " вивергає пекельне полум'я: " + total + " шкоди героям";
                if (strongest != null) {
                    strongest.stun(1);
                    log += ", " + strongest.getName() + " оглушений";
                }
                return log;
            }
        });
    }

    /** Розумніший ворог: поранений демон спершу намагається висмоктати життя. */
    @Override
    public Skill chooseSkill() {
        boolean wounded = getHp() < getMaxHp() / 2;
        if (wounded && getCooldownLeft(drain) == 0) return drain;
        return super.chooseSkill();
    }

    // Пасивка «Пекельна лють»: кожен третій удар не завдає шкоди.
    @Override
    protected int modifyIncomingDamage(int damage) {
        hitsTaken++;
        return hitsTaken % 3 == 0 ? 0 : damage;
    }
}
