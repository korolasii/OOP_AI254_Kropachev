package org.example;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.List;

public class Draw extends JPanel {
    private final List<Shape> shapes = Arrays.asList(
            new Circle(90, 100, 60, Color.BLUE),
            new RectangleShape(210, 55, 150, 90, Color.RED));

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        for (Shape shape : shapes) {
            shape.draw(graphics);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Draw — абстрактні класи");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Draw());
        frame.setSize(420, 240);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

abstract class Shape {
    private final Color color;

    Shape(Color color) {
        this.color = color;
    }

    protected Color getColor() {
        return color;
    }

    abstract void draw(Graphics graphics);
}

class Circle extends Shape {
    private final int x;
    private final int y;
    private final int diameter;

    Circle(int x, int y, int diameter, Color color) {
        super(color);
        this.x = x;
        this.y = y;
        this.diameter = diameter;
    }

    @Override
    void draw(Graphics graphics) {
        graphics.setColor(getColor());
        graphics.fillOval(x, y, diameter, diameter);
    }
}

class RectangleShape extends Shape {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    RectangleShape(int x, int y, int width, int height, Color color) {
        super(color);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    void draw(Graphics graphics) {
        graphics.setColor(getColor());
        graphics.fillRect(x, y, width, height);
    }
}
