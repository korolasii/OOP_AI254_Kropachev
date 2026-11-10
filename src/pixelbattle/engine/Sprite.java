package pixelbattle.engine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Анімований спрайт: кілька кадрів ім'я_f0.png, ім'я_f1.png, ...
 *
 * <pre>
 *  Доступні спрайти (папка resources/sprites, по 4 кадри):
 *    герої:  knight_m_idle_anim, wizzard_m_idle_anim, elf_f_idle_anim
 *    вороги: swampy_anim, goblin_idle_anim, skelet_idle_anim, slug_anim,
 *            ogre_idle_anim (великий), big_demon_idle_anim (великий)
 *
 *  Приклад: new Sprite("goblin_idle_anim", 4, 6) — 4 кадри, збільшення ×6.
 * </pre>
 */
public class Sprite {
    /** Де шукати картинки, якщо їх немає в classpath. */
    private static final String[] SEARCH_DIRS = {
            "resources/sprites", "sprites", "../resources/sprites", "../sprites"
    };

    private final List<BufferedImage> frames = new ArrayList<>();
    private final List<BufferedImage> flashFrames = new ArrayList<>();
    private final int scale;

    public Sprite(String name, int frameCount, int scale) {
        this.scale = scale;
        List<BufferedImage> raw = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) raw.add(loadImage(name + "_f" + i + ".png"));
        Rectangle box = opaqueBounds(raw);                       // обрізаємо порожні поля
        for (BufferedImage f : raw) {
            BufferedImage cropped = f.getSubimage(box.x, box.y,
                    Math.min(box.width, f.getWidth() - box.x), Math.min(box.height, f.getHeight() - box.y));
            frames.add(cropped);
            flashFrames.add(whiteSilhouette(cropped));
        }
    }

    /** Завантажує картинку: спочатку з classpath (/sprites/...), потім із папок SEARCH_DIRS. */
    public static BufferedImage loadImage(String file) {
        try {
            URL url = Sprite.class.getResource("/sprites/" + file);
            if (url != null) return ImageIO.read(url);
            for (String dir : SEARCH_DIRS) {
                File f = new File(dir, file);
                if (f.isFile()) return ImageIO.read(f);
            }
        } catch (IOException e) {
            System.err.println("Помилка читання " + file + ": " + e.getMessage());
        }
        System.err.println("Не знайдено спрайт: " + file
                + ". Перевірте, що папка resources позначена як Resources Root. Робоча папка: "
                + new File(".").getAbsolutePath());
        return placeholder();
    }

    public int getWidth() { return frames.get(0).getWidth() * scale; }

    public int getHeight() { return frames.get(0).getHeight() * scale; }

    /** tick — лічильник кадрів гри; кадр анімації змінюється кожні 8 тиків. */
    public void draw(Graphics2D g, int x, int y, boolean mirrored, boolean flashing, int tick) {
        int i = (tick / 8) % frames.size();
        BufferedImage img = flashing ? flashFrames.get(i) : frames.get(i);
        if (mirrored) g.drawImage(img, x + getWidth(), y, -getWidth(), getHeight(), null);
        else g.drawImage(img, x, y, getWidth(), getHeight(), null);
    }

    private static BufferedImage placeholder() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++)
                img.setRGB(x, y, ((x / 4 + y / 4) % 2 == 0 ? Color.MAGENTA : Color.BLACK).getRGB());
        return img;
    }

    private static Rectangle opaqueBounds(List<BufferedImage> imgs) {
        Rectangle r = null;
        for (BufferedImage img : imgs)
            for (int y = 0; y < img.getHeight(); y++)
                for (int x = 0; x < img.getWidth(); x++)
                    if ((img.getRGB(x, y) >>> 24) > 0) {
                        if (r == null) r = new Rectangle(x, y, 1, 1);
                        else r.add(new Rectangle(x, y, 1, 1));
                    }
        return r != null ? r : new Rectangle(0, 0, imgs.get(0).getWidth(), imgs.get(0).getHeight());
    }

    private static BufferedImage whiteSilhouette(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++)
                if ((src.getRGB(x, y) >>> 24) > 0) out.setRGB(x, y, Color.WHITE.getRGB());
        return out;
    }
}
