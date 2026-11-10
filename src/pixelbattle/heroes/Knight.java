package pixelbattle.heroes;

import pixelbattle.core.Fighter;
import pixelbattle.core.Hero;
import pixelbattle.core.Skill;
import pixelbattle.core.Target;
import pixelbattle.core.Ultimate;
import pixelbattle.engine.Sprite;

/** Лицар — міцний воїн ближнього бою. */
public class Knight extends Hero {

    private static final int ARMOR = 4;          // «Важка броня»
    private static final int HAMMER_DAMAGE = 15; // «Небесний молот»

    public Knight() {
        super("Лицар", 110, new Sprite("knight_gold_idle_anim", 4, 4));

        // ПРИКЛАД: звичайна атака, ефект записаний ЛЯМБДОЮ
        addSkill(new Skill("Удар мечем", Target.ENEMY, 0, (user, target) -> {
            int dmg = target.takeDamage(random(12, 18));
            return user.getName() + " рубає " + target.getName() + ": " + dmg + " шкоди";
        }));

        // TODO 1: «Щит» — захист до наступного ходу
        addSkill(new Skill("Щит", Target.SELF, 2, (user, target) -> {
            user.setGuarding(true);
            return user.getName() + " закривається щитом — шкода вдвічі менша до наступного ходу";
        }));

        // НОВЕ: «Удар щитом» — невелика шкода, але 35% шанс оглушити
        addSkill(new Skill("Удар щитом", Target.ENEMY, 3, (user, target) -> {
            int dmg = target.takeDamage(random(6, 9));
            boolean stunned = chance(35);
            if (stunned) target.stun(1);
            return user.getName() + " б'є щитом " + target.getName() + ": " + dmg + " шкоди"
                    + (stunned ? ", ціль приголомшена!" : "");
        }));

        // TODO 7: «Небесний молот» — анонімний клас
        setUltimate(new Ultimate() {
            @Override
            public String getName() { return "Небесний молот"; }

            @Override
            public String getDescription() {
                return "Кожному живому ворогу " + HAMMER_DAMAGE + " шкоди та оглушення на 1 хід";
            }

            @Override
            public String activate(Fighter user, Fighter[] allies, Fighter[] enemies) {
                StringBuilder log = new StringBuilder(user.getName() + " кличе Небесний молот!");
                for (Fighter enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    int dmg = enemy.takeDamage(HAMMER_DAMAGE);
                    enemy.stun(1);
                    log.append(' ').append(enemy.getName()).append(" -").append(dmg).append(';');
                }
                return log.toString();
            }
        });
    }

    // TODO 4: «Важка броня» — на 4 шкоди менше, але не менше 0
    @Override
    protected int modifyIncomingDamage(int damage) {
        int reduced = damage - ARMOR;
        return reduced < 0 ? 0 : reduced;
    }
}
