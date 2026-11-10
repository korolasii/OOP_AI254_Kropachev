package pixelbattle.ultimates;

import pixelbattle.core.Fighter;
import pixelbattle.core.Ultimate;

/** TODO 8: ультимейт «Метеорит» — звичайний (іменований) клас. */
public class Meteor implements Ultimate {

    private static final int MIN_DAMAGE = 20;
    private static final int MAX_DAMAGE = 30;

    @Override
    public String getName() { return "Метеорит"; }

    @Override
    public String getDescription() {
        return "З неба падає метеорит: " + MIN_DAMAGE + "–" + MAX_DAMAGE + " шкоди кожному ворогу";
    }

    @Override
    public String activate(Fighter user, Fighter[] allies, Fighter[] enemies) {
        int total = 0;
        int killed = 0;
        for (Fighter enemy : enemies) {
            if (!enemy.isAlive()) continue;
            total += enemy.takeDamage(Fighter.random(MIN_DAMAGE, MAX_DAMAGE));
            if (!enemy.isAlive()) killed++;
        }
        String log = user.getName() + " обрушує Метеорит: " + total + " шкоди";
        return killed > 0 ? log + ", знищено ворогів: " + killed : log;
    }
}
