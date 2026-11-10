package pixelbattle.core;

import pixelbattle.engine.Sprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Базовий клас будь-якого бійця.
 *
 * <pre>
 *  Що вміє будь-який боєць (цим можна користуватися у своєму коді):
 *    getName(), getHp(), getMaxHp(), isAlive()
 *    takeDamage(n)     — завдати n шкоди; повертає, скільки реально знято
 *    heal(n)           — вилікувати на n (не більше максимуму); повертає, скільки відновлено
 *    setGuarding(true) — стати в захист: до свого наступного ходу шкода вдвічі менша
 *    stun(n)           — оглушити на n ходів (оглушений пропускає хід)
 *    random(min, max)  — випадкове ціле число від min до max включно
 *    chance(percent)   — true з імовірністю percent відсотків
 *
 *  Що можна викликати в конструкторі свого персонажа:
 *    addSkill(skill)       — додати вміння (з'явиться нова кнопка)
 *    setUltimate(ultimate) — задати ультимейт
 *
 *  Пасивні вміння — методи, які можна ПЕРЕВИЗНАЧИТИ (@Override) у підкласі:
 *    onTurnStart()                    — викликається на початку кожного свого ходу
 *    modifyIncomingDamage(int damage) — змінює шкоду, що надходить; повертає нову шкоду
 * </pre>
 */
public abstract class Fighter {
    public static final int MAX_ENERGY = 100;
    private static final int ENERGY_PER_ACTION = 25;
    private static final int ENERGY_PER_HIT = 10;
    private static final Random RND = new Random();

    private final String name;
    private final int maxHp;
    private int hp;
    private int energy;
    private int stunTurns;
    private boolean guarding;
    private final Sprite sprite;
    private final List<Skill> skills = new ArrayList<>();
    private final Map<Skill, Integer> cooldowns = new HashMap<>();
    private Ultimate ultimate;
    private final List<Popup> popups = new ArrayList<>();

    protected Fighter(String name, int maxHp, Sprite sprite) {
        if (maxHp <= 0) throw new IllegalArgumentException("HP має бути більше 0");
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.sprite = sprite;
    }

    /* =================== для вашого коду =================== */

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public boolean isAlive() { return hp > 0; }
    public boolean isStunned() { return stunTurns > 0; }
    public void setGuarding(boolean guarding) { this.guarding = guarding; }

    /** Завдає шкоди з урахуванням пасивок і захисту. Повертає реально знятий HP. */
    public int takeDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Шкода не може бути від'ємною");
        if (!isAlive()) return 0;
        int dmg = modifyIncomingDamage(amount);      // пасивки — тут працює поліморфізм
        if (guarding) dmg /= 2;
        dmg = Math.max(0, Math.min(hp, dmg));
        hp -= dmg;
        if (dmg > 0) {
            addEnergy(ENERGY_PER_HIT);
            popups.add(new Popup("-" + dmg, Popup.Kind.DAMAGE));
        } else {
            popups.add(new Popup("МИМО", Popup.Kind.MISS));
        }
        return dmg;
    }

    /** Лікує, але не вище максимуму. Повертає, скільки реально відновлено. */
    public int heal(int amount) {
        if (!isAlive() || amount <= 0) return 0;
        int healed = Math.min(maxHp - hp, amount);
        hp += healed;
        if (healed > 0) popups.add(new Popup("+" + healed, Popup.Kind.HEAL));
        return healed;
    }

    /** Оглушує на вказану кількість ходів. */
    public void stun(int turns) {
        if (isAlive()) stunTurns = Math.max(stunTurns, turns);
    }

    /** Випадкове ціле число від min до max включно. */
    public static int random(int min, int max) { return min + RND.nextInt(max - min + 1); }

    /** true з імовірністю percent відсотків. */
    public static boolean chance(int percent) { return RND.nextInt(100) < percent; }

    /** Додає вміння (викликайте в конструкторі). */
    protected void addSkill(Skill skill) { skills.add(skill); }

    /** Задає ультимейт (викликайте в конструкторі). */
    protected void setUltimate(Ultimate ultimate) { this.ultimate = ultimate; }

    /* ====== пасивки: перевизначайте в підкласах (@Override) ====== */

    /** Викликається на початку кожного власного ходу. За замовчуванням нічого не робить. */
    protected void onTurnStart() { }

    /** Змінює шкоду, що надходить. За замовчуванням шкода не змінюється. */
    protected int modifyIncomingDamage(int damage) { return damage; }

    /* ==================================================================
     *  СЛУЖБОВІ МЕТОДИ — їх викликає рушій гри. У своєму коді не потрібні.
     * ================================================================== */

    /** Спливаючий напис над бійцем (шкода, лікування, промах). */
    public record Popup(String text, Kind kind) {
        public enum Kind { DAMAGE, HEAL, MISS }
    }

    public Sprite getSprite() { return sprite; }
    public List<Skill> getSkills() { return Collections.unmodifiableList(skills); }
    public Ultimate getUltimate() { return ultimate; }
    public int getEnergy() { return energy; }
    public boolean isUltimateReady() { return ultimate != null && energy >= MAX_ENERGY && isAlive(); }
    public int getCooldownLeft(Skill s) { return cooldowns.getOrDefault(s, 0); }

    /** Забирає нові написи для показу на екрані. */
    public List<Popup> drainPopups() {
        List<Popup> out = new ArrayList<>(popups);
        popups.clear();
        return out;
    }

    /**
     * Початок власного ходу: перезарядки, зняття захисту, пасивки, оглушення.
     * Повертає true, якщо боєць може діяти.
     */
    public boolean beginTurn() {
        if (!isAlive()) return false;
        cooldowns.replaceAll((s, left) -> Math.max(0, left - 1));
        guarding = false;
        onTurnStart();
        if (!isAlive()) return false;
        if (stunTurns > 0) {
            stunTurns--;
            return false;
        }
        return true;
    }

    /** Використовує вміння. Повертає рядок для журналу. */
    public String performSkill(Skill skill, Fighter target) {
        String msg = skill.getEffect().apply(this, target);
        addEnergy(ENERGY_PER_ACTION);
        if (skill.getCooldown() > 0) cooldowns.put(skill, skill.getCooldown() + 1);
        return msg;
    }

    /** Використовує ультимейт. Повертає рядок для журналу. */
    public String performUltimate(Fighter[] allies, Fighter[] enemies) {
        String msg = ultimate.activate(this, allies, enemies);
        energy = 0;
        return msg;
    }

    private void addEnergy(int amount) { energy = Math.min(MAX_ENERGY, energy + amount); }

    @Override
    public String toString() { return name + " [" + hp + "/" + maxHp + "]"; }
}
