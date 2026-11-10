package pixelbattle.core;

/**
 * Ефект вміння: що відбувається з ціллю.
 * Повертає рядок для журналу бою.
 *
 * Інтерфейс ФУНКЦІОНАЛЬНИЙ (лише один абстрактний метод),
 * тому його зручно записувати лямбдою:
 *
 *   (user, target) -> {
 *       int dmg = target.takeDamage(10);
 *       return user.getName() + " б'є " + target.getName() + ": " + dmg;
 *   }
 */
@FunctionalInterface
public interface SkillEffect {
    String apply(Fighter user, Fighter target);
}
