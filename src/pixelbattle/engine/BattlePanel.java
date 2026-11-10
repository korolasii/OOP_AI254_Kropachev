package pixelbattle.engine;

import pixelbattle.core.Enemy;
import pixelbattle.core.Fighter;
import pixelbattle.core.Skill;
import pixelbattle.core.Ultimate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Рушій гри: черговість ходів, вибір цілей, малювання та ефекти.
 * Змінювати цей клас для виконання завдання НЕ потрібно.
 */
public class BattlePanel extends JPanel {
    private static final int W = 820, H = 470;
    private static final Color GOLD = new Color(255, 200, 40);

    private final Fighter[] heroes;
    private final Fighter[] enemies;
    private final JTextArea logArea = new JTextArea(7, 60);
    private final JPanel skillButtons = new JPanel();
    private final JLabel turnLabel = new JLabel();
    private final BufferedImage background = buildBackground();
    private final Random rnd = new Random();

    /** Стан анімації кожного бійця. */
    private static class Anim { int lunge, hit; }
    private final Map<Fighter, Anim> anims = new HashMap<>();

    private record FloatingText(String text, Color color, int x, int y, int age) { }
    private List<FloatingText> floating = new ArrayList<>();

    /** Черга дій ворогів: виконується по одній кожні 750 мс. */
    private final List<Runnable> script = new ArrayList<>();
    private final Timer scriptTimer = new Timer(750, e -> runNextScriptStep());

    private int heroIndex = 0;
    private Fighter selectedEnemy;
    private boolean playerTurn = false;
    private boolean finished = false;
    private int frame = 0;
    private int turn = 1;
    private String bannerText = "";
    private int bannerTimer = 0;

    private final Consumer<String> log = msg -> {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    };

    public BattlePanel(Fighter[] heroes, Fighter[] enemies) {
        this.heroes = heroes;
        this.enemies = enemies;
        this.selectedEnemy = enemies[0];
        for (Fighter f : heroes) anims.put(f, new Anim());
        for (Fighter f : enemies) anims.put(f, new Anim());

        setPreferredSize(new Dimension(W, H));
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        new Timer(16, e -> { frame++; tickAnimations(); repaint(); }).start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (int i = 0; i < enemies.length; i++) {
                    Fighter en = enemies[i];
                    Point p = enemyPos(i);
                    Rectangle r = new Rectangle(p.x, p.y, en.getSprite().getWidth(), en.getSprite().getHeight());
                    if (en.isAlive() && r.contains(e.getPoint())) selectedEnemy = en;
                }
            }
        });

        log.accept("Бій починається! Клацніть по ворогу, щоб вибрати ціль.");
        startHeroTurn();
    }

    /** Нижня панель: чий хід, кнопки вмінь, журнал бою. */
    public JPanel getBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        turnLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        top.add(turnLabel);
        top.add(skillButtons);
        bottom.add(top, BorderLayout.NORTH);
        bottom.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return bottom;
    }

    /* ======================= логіка ходів ======================= */

    private boolean beginTurn(Fighter f) {
        if (!f.isAlive()) return false;
        boolean canAct = f.beginTurn();
        if (!canAct && f.isAlive()) log.accept(f.getName() + " оглушений і пропускає хід");
        return canAct;
    }

    private void startHeroTurn() {
        if (finished) return;
        while (heroIndex < heroes.length && !beginTurn(heroes[heroIndex])) heroIndex++;
        if (checkEnd()) return;
        if (heroIndex >= heroes.length) { startEnemyTurn(); return; }

        Fighter hero = heroes[heroIndex];
        playerTurn = true;
        turnLabel.setText("Хід " + turn + ": " + hero.getName() + " →");

        skillButtons.removeAll();
        for (Skill s : hero.getSkills()) {
            int left = hero.getCooldownLeft(s);
            JButton b = new JButton(left > 0 ? s.getName() + " (" + left + ")" : s.getName());
            b.setEnabled(left == 0);
            b.addActionListener(e -> heroAction(() -> useSkill(hero, s)));
            skillButtons.add(b);
        }
        Ultimate u = hero.getUltimate();
        if (u != null) {
            JButton ub = new JButton("★ " + u.getName() + " (" + hero.getEnergy() + "%)");
            ub.setToolTipText(u.getDescription());
            ub.setEnabled(hero.isUltimateReady());
            if (hero.isUltimateReady()) { ub.setBackground(GOLD); ub.setOpaque(true); }
            ub.addActionListener(e -> heroAction(() -> useUltimate(hero, heroes, enemies)));
            skillButtons.add(ub);
        } else {
            JButton ub = new JButton("★ ультимейт не задано (" + hero.getEnergy() + "%)");
            ub.setToolTipText("Додайте ультимейт: setUltimate(...) у конструкторі персонажа");
            ub.setEnabled(false);
            skillButtons.add(ub);
        }
        skillButtons.revalidate();
        skillButtons.repaint();
    }

    private void heroAction(Runnable action) {
        if (!playerTurn || finished) return;
        playerTurn = false;
        skillButtons.removeAll();
        skillButtons.repaint();
        action.run();
        if (checkEnd()) return;
        heroIndex++;
        Timer t = new Timer(bannerTimer > 0 ? 1100 : 500, e -> startHeroTurn());
        t.setRepeats(false);
        t.start();
    }

    private void useSkill(Fighter user, Skill skill) {
        boolean isHero = isHero(user);
        Fighter target = switch (skill.getTarget()) {
            case SELF -> user;
            case ALLY -> weakest(isHero ? heroes : enemies, user);
            case ENEMY -> isHero ? pickEnemyTarget() : randomAlive(heroes);
        };
        anims.get(user).lunge = 20;
        log.accept(user.performSkill(skill, target));
    }

    private void useUltimate(Fighter user, Fighter[] allies, Fighter[] foes) {
        anims.get(user).lunge = 20;
        bannerText = "★ " + user.getUltimate().getName() + " ★";
        bannerTimer = 70;
        log.accept("★ УЛЬТИМЕЙТ " + user.getName() + ": " + user.performUltimate(allies, foes));
    }

    private boolean isHero(Fighter f) {
        for (Fighter h : heroes) if (h == f) return true;
        return false;
    }

    private Fighter weakest(Fighter[] team, Fighter fallback) {
        Fighter best = fallback;
        for (Fighter f : team)
            if (f.isAlive() && (double) f.getHp() / f.getMaxHp() < (double) best.getHp() / best.getMaxHp())
                best = f;
        return best;
    }

    private Fighter randomAlive(Fighter[] team) {
        List<Fighter> alive = new ArrayList<>();
        for (Fighter f : team) if (f.isAlive()) alive.add(f);
        return alive.get(rnd.nextInt(alive.size()));
    }

    private Fighter pickEnemyTarget() {
        if (selectedEnemy == null || !selectedEnemy.isAlive()) selectedEnemy = randomAlive(enemies);
        return selectedEnemy;
    }

    private void startEnemyTurn() {
        turnLabel.setText("Хід " + turn + ": ходять вороги...");
        skillButtons.removeAll();
        skillButtons.repaint();
        for (Fighter f : enemies) {
            script.add(() -> {
                if (finished || !beginTurn(f)) return;
                if (f.isUltimateReady()) {
                    useUltimate(f, enemies, heroes);
                } else if (f instanceof Enemy enemy) {
                    Skill s = enemy.chooseSkill();
                    if (s != null) useSkill(f, s);
                }
                checkEnd();
            });
        }
        script.add(() -> {
            if (finished) return;
            turn++;
            heroIndex = 0;
            startHeroTurn();
        });
        scriptTimer.start();
    }

    private void runNextScriptStep() {
        if (script.isEmpty()) { scriptTimer.stop(); return; }
        if (bannerTimer > 20) return;                 // чекаємо, поки покажеться ультимейт
        script.remove(0).run();
    }

    private static boolean allDead(Fighter[] team) {
        for (Fighter f : team) if (f.isAlive()) return false;
        return true;
    }

    private boolean checkEnd() {
        if (finished) return true;
        boolean win = allDead(enemies), lose = allDead(heroes);
        if (!win && !lose) return false;
        finished = true;
        playerTurn = false;
        script.clear();
        scriptTimer.stop();
        skillButtons.removeAll();
        skillButtons.repaint();
        turnLabel.setText(win ? "ПЕРЕМОГА!" : "ПОРАЗКА...");
        log.accept(win ? "*** ПЕРЕМОГА! Ходів: " + turn + " ***" : "*** Герої загинули... ***");
        return true;
    }

    /* ========================= графіка ========================= */

    private Point heroPos(int i) { return new Point(200 - (i % 2) * 90, 140 + i * 110); }

    private Point enemyPos(int i) {
        int[] xs = {540, 660, 540, 690, 420};      // місця для 5 ворогів
        int[] ys = {135, 230, 325, 345, 230};
        int k = Math.min(i, xs.length - 1);
        return new Point(xs[k], ys[k]);
    }

    private static BufferedImage buildBackground() {
        int t = 48;
        BufferedImage bg = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bg.createGraphics();
        Random r = new Random(7);
        BufferedImage[] floors = new BufferedImage[8];
        for (int i = 0; i < 8; i++) floors[i] = Sprite.loadImage("floor_" + (i + 1) + ".png");
        BufferedImage wallTop = Sprite.loadImage("wall_top_mid.png");
        BufferedImage wall = Sprite.loadImage("wall_mid.png");
        BufferedImage banner = Sprite.loadImage("wall_banner_red.png");
        for (int y = 0; y < H; y += t)
            for (int x = 0; x < W; x += t)
                g.drawImage(r.nextInt(9) == 0 ? floors[1 + r.nextInt(7)] : floors[0], x, y, t, t, null);
        for (int x = 0, i = 0; x < W; x += t, i++) {
            g.drawImage(wallTop, x, 0, t, t, null);
            g.drawImage(i % 5 == 2 ? banner : wall, x, t, t, t, null);
        }
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRect(0, 2 * t, W, 10);
        g.dispose();
        return bg;
    }

    private void tickAnimations() {
        if (bannerTimer > 0) bannerTimer--;
        for (int i = 0; i < heroes.length; i++) tick(heroes[i], heroPos(i));
        for (int i = 0; i < enemies.length; i++) tick(enemies[i], enemyPos(i));
        List<FloatingText> next = new ArrayList<>();
        for (FloatingText f : floating)
            if (f.age() < 45) next.add(new FloatingText(f.text(), f.color(), f.x(), f.y() - 1, f.age() + 1));
        floating = next;
    }

    private void tick(Fighter f, Point p) {
        Anim a = anims.get(f);
        if (a.lunge > 0) a.lunge--;
        if (a.hit > 0) a.hit--;
        int k = 0;
        for (Fighter.Popup pop : f.drainPopups()) {
            Color c = switch (pop.kind()) {
                case DAMAGE -> new Color(255, 80, 80);
                case HEAL -> new Color(100, 240, 100);
                case MISS -> Color.WHITE;
            };
            if (pop.kind() == Fighter.Popup.Kind.DAMAGE) a.hit = 16;
            floating.add(new FloatingText(pop.text(), c,
                    p.x + f.getSprite().getWidth() / 2 - 10 + k * 14, p.y + 30 - k * 22, 0));
            k++;
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int shake = bannerTimer > 45 ? (frame % 4 < 2 ? -5 : 5) : 0;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);
        g.translate(shake, 0);
        g.drawImage(background, 0, 0, null);

        for (int i = 0; i < heroes.length; i++) drawFighter(g, heroes[i], heroPos(i), false, i);
        for (int i = 0; i < enemies.length; i++) drawFighter(g, enemies[i], enemyPos(i), true, i);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        for (FloatingText f : floating) {
            int alpha = Math.max(0, 255 - f.age() * 5);
            Color c = f.color();
            g.setColor(new Color(0, 0, 0, alpha));
            g.drawString(f.text(), f.x() + 2, f.y() + 2);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            g.drawString(f.text(), f.x(), f.y());
        }
        g.translate(-shake, 0);

        if (bannerTimer > 0) drawBanner(g);

        if (finished) {
            boolean win = allDead(enemies);
            String s = win ? "ПЕРЕМОГА!" : "ПОРАЗКА";
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 56));
            int sx = W / 2 - g.getFontMetrics().stringWidth(s) / 2;
            g.setColor(Color.BLACK);
            g.drawString(s, sx + 4, H / 2 + 4);
            g.setColor(win ? Color.YELLOW : Color.RED);
            g.drawString(s, sx, H / 2);
        }
    }

    private void drawBanner(Graphics2D g) {
        float a = Math.min(1f, bannerTimer / 20f);
        g.setColor(new Color(1f, 0.85f, 0.3f, 0.25f * a));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(0f, 0f, 0f, 0.6f * a));
        g.fillRect(0, H / 2 - 45, W, 70);
        int size = 30 + Math.max(0, bannerTimer - 55);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
        int sx = W / 2 - g.getFontMetrics().stringWidth(bannerText) / 2;
        g.setColor(new Color(1f, 0.8f, 0.15f, a));
        g.drawString(bannerText, sx, H / 2);
    }

    private void drawFighter(Graphics2D g, Fighter f, Point base, boolean isEnemy, int index) {
        Sprite sp = f.getSprite();
        Anim a = anims.get(f);
        int x = base.x, y = base.y;
        if (a.lunge > 0) {
            int t = a.lunge > 10 ? 20 - a.lunge : a.lunge;
            x += (isEnemy ? -1 : 1) * t * 4;
        }
        if (a.hit > 0) x += (a.hit % 4 < 2 ? -4 : 4);

        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(base.x + sp.getWidth() / 8, base.y + sp.getHeight() - 8, sp.getWidth() * 3 / 4, 14);

        boolean active = !isEnemy && playerTurn && !finished && heroIndex < heroes.length && heroes[heroIndex] == f;
        if (active || (isEnemy && f == selectedEnemy && f.isAlive() && !finished)) {
            g.setColor(active ? GOLD : Color.RED);
            int mx = base.x + sp.getWidth() / 2, my = base.y - 42 + (int) (Math.sin(frame / 6.0) * 3);
            g.fillPolygon(new int[]{mx - 8, mx + 8, mx}, new int[]{my, my, my + 10}, 3);
        }

        Composite old = g.getComposite();
        if (!f.isAlive()) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        sp.draw(g, x, y, isEnemy, a.hit > 8 && a.hit % 4 < 2, f.isAlive() ? frame + index * 3 : 0);
        g.setComposite(old);

        if (f.isStunned() && f.isAlive()) {
            g.setColor(Color.YELLOW);
            for (int k = 0; k < 3; k++) {
                double ang = frame / 10.0 + k * 2.1;
                int sx = base.x + sp.getWidth() / 2 + (int) (Math.cos(ang) * 18);
                int sy = base.y - 4 + (int) (Math.sin(ang) * 5);
                g.fillOval(sx - 4, sy - 4, 8, 8);
            }
        }

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        String label = f.getName() + " " + f.getHp() + "/" + f.getMaxHp();
        int barW = Math.max(g.getFontMetrics().stringWidth(label), sp.getWidth());
        boolean hasUlt = f.getUltimate() != null;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(base.x - 3, base.y - 30, barW + 6, 29);
        g.setColor(Color.WHITE);
        g.drawString(label, base.x, base.y - 18);
        double ratio = (double) f.getHp() / f.getMaxHp();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(base.x, base.y - 14, barW, 5);
        g.setColor(ratio > 0.5 ? new Color(80, 220, 80) : ratio > 0.25 ? Color.ORANGE : Color.RED);
        g.fillRect(base.x, base.y - 14, (int) (barW * ratio), 5);
        // енергія: синя — набирається, мигає золотим — ультимейт готовий,
        // сіра — енергія є, але ультимейт ще не задано
        g.setColor(Color.DARK_GRAY);
        g.fillRect(base.x, base.y - 7, barW, 4);
        Color energyColor;
        if (!hasUlt) energyColor = new Color(130, 130, 150);
        else if (f.isUltimateReady()) energyColor = frame % 20 < 10 ? GOLD : Color.WHITE;
        else energyColor = new Color(80, 150, 255);
        g.setColor(energyColor);
        g.fillRect(base.x, base.y - 7, barW * f.getEnergy() / Fighter.MAX_ENERGY, 4);
    }
}
