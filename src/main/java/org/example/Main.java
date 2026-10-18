package org.example;

public class Main {
    public static void main(String[] args) {
        Animal dog = new Dog("Рекс");
        Animal cat = new Cat("Мурка");
        System.out.println(dog.getName() + ": " + dog.makeSound());
        System.out.println(cat.getName() + ": " + cat.makeSound());

        Point point = new Point(1.5, 2.5);
        point.moveTo(4.0, -3.0);
        System.out.println("Точка: " + point.getX() + ", " + point.getY());

        Smartphone phone = new Smartphone(48.45, 35.04);
        double[] coordinates = phone.getCoordinates();
        System.out.println("Координаты смартфона: " + coordinates[0] + ", " + coordinates[1]);
        phone.makeCall();
        phone.receiveCall();

        Car first = new Car(20000, 2020, 150);
        Car second = new Car(20000, 2018, 180);
        Car third = new Car(18000, 2022, 130);
        System.out.println("Сравнение автомобилей: " + first.compareTo(second) + ", " + third.compareTo(first));
    }
}

abstract class Animal {
    private String name;

    Animal(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    abstract String makeSound();
}

class Dog extends Animal {
    Dog(String name) { super(name); }
    @Override String makeSound() { return "Гав!"; }
}

class Cat extends Animal {
    Cat(String name) { super(name); }
    @Override String makeSound() { return "Мяу!"; }
}

interface Movable {
    void setX(double x);
    void setY(double y);
    double getX();
    double getY();
    void moveTo(double x, double y);
}

class Point implements Movable {
    private double x;
    private double y;

    Point(double x, double y) { moveTo(x, y); }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void moveTo(double x, double y) { this.x = x; this.y = y; }
}

interface GPS {
    double[] getCoordinates();
}

interface Cellular {
    void makeCall();
    void receiveCall();
}

class Smartphone implements GPS, Cellular {
    private final double latitude;
    private final double longitude;

    Smartphone(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double[] getCoordinates() { return new double[]{latitude, longitude}; }
    public void makeCall() { System.out.println("Исходящий звонок."); }
    public void receiveCall() { System.out.println("Входящий звонок."); }
}

class Car implements Comparable {
    private int price;
    private int year;
    private int horsePower;

    Car(int price, int year, int horsePower) {
        this.price = price;
        this.year = year;
        this.horsePower = horsePower;
    }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getHorsePower() { return horsePower; }
    public void setHorsePower(int horsePower) { this.horsePower = horsePower; }

    @Override
    public int compareTo(Object object) {
        if (!(object instanceof Car)) return 0;
        Car other = (Car) object;
        if (price < other.price) return 1;
        if (price > other.price) return -1;
        if (year < other.year) return 1;
        if (year > other.year) return -1;
        if (horsePower > other.horsePower) return 1;
        if (horsePower < other.horsePower) return -1;
        return 0;
    }
}
