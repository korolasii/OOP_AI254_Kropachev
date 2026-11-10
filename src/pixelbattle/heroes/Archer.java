package pixelbattle.heroes;

import pixelbattle.core.Fighter;
import pixelbattle.core.Hero;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.core.Ultimate;
import pixelbattle.engine.Sprite;

/** Лучниця — швидка й спритна. */
public class Archer extends Hero {

    private static final int DODGE_CHANCE = 25;

    public Archer() {
        super("Лучниця", 80, new Sprite("elf_crimson_idle_anim", 4, 4));

        addSkill(new Skill("Постріл", Target.ENEMY, 0, (user, target) ->
                user.getName() + " стріляє в " + target.getName() + ": "
                        + target.takeDamage(random(10, 16)) + " шкоди"));

        // TODO 3: «Подвійний постріл» — постріли в циклі, у журнал — сума
        addSkill(new Skill("Подвійний постріл", Target.ENEMY, 1, (user, target) -> {
            int sum = 0;
            for (int shot = 0; shot < 2; shot++) {
                sum += target.takeDamage(random(7, 11));
            }
            return user.getName() + " двічі влучає в " + target.getName() + ": разом " + sum + " шкоди";
        }));

        // НОВЕ: «Прицільний постріл» — 30% шанс критичного влучання (подвійна шкода)
        addSkill(new Skill("Прицільний постріл", Target.ENEMY, 2, (user, target) -> {
            int base = random(12, 16);
            boolean crit = chance(30);
            int dmg = target.takeDamage(crit ? base * 2 : base);
            return user.getName() + " цілиться в " + target.getName() + ": " + dmg + " шкоди"
                    + (crit ? " (КРИТ!)" : "");
        }));

        // TODO 9: «Злива стріл» — анонімний клас з полем-лічильником
        setUltimate(new Ultimate() {
            private int uses = 0;   // скільки разів ультимейт уже використано

            private int arrowsPerEnemy() { return 3 + uses; }

            @Override
            public String getName() { return "Злива стріл"; }

            @Override
            public String getDescription() {
                return arrowsPerEnemy() + " стріл по 4–8 шкоди кожному ворогу (з кожним разом більше)";
            }

            @Override
            public String activate(Fighter user, Fighter[] allies, Fighter[] enemies) {
                int arrows = arrowsPerEnemy();
                int total = 0;
                int hit = 0;
                for (Fighter enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    hit++;
                    for (int i = 0; i < arrows; i++) {
                        total += enemy.takeDamage(Fighter.random(4, 8));
                    }
                }
                uses++;
                return user.getName() + ": злива з " + arrows + " стріл на кожного ("
                        + hit + " ворогів) — " + total + " шкоди";
            }
        });
    }

    // TODO 5: «Ухилення» — 25% шанс уникнути шкоди
    @Override
    protected int modifyIncomingDamage(int damage) {
        if (chance(DODGE_CHANCE)) return 0;
        return damage;
    }
}
