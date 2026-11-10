package pixelbattle.core;

/**
 * Вміння = назва + на кого діє + перезарядка + ефект.
 *
 * Приклад:
 *   new Skill("Удар", Target.ENEMY, 0, (user, target) -> ...)
 */
public class Skill {
    private final String name;
    private final Target target;
    private final int cooldown;
    private final SkillEffect effect;

    /**
     * @param name     назва (на кнопці)
     * @param target   на кого діє
     * @param cooldown скільки своїх ходів чекати після використання (0 — без перезарядки)
     * @param effect   що робить вміння (зазвичай лямбда)
     */
    public Skill(String name, Target target, int cooldown, SkillEffect effect) {
        if (cooldown < 0) throw new IllegalArgumentException("Перезарядка не може бути від'ємною");
        this.name = name;
        this.target = target;
        this.cooldown = cooldown;
        this.effect = effect;
    }

    public String getName() { return name; }
    public Target getTarget() { return target; }
    public int getCooldown() { return cooldown; }
    public SkillEffect getEffect() { return effect; }
}
