package ua.opnu;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {

    public static void main(String[] args) {
        int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        // Завдання 1: предикат "число просте"
        System.out.println("=== Завдання 1 ===");
        Predicate<Integer> isPrime = n -> {
            if (n < 2) return false;
            for (int d = 2; d <= n / d; d++) {
                if (n % d == 0) return false;
            }
            return true;
        };
        System.out.println("Прості числа: " + Arrays.toString(filter(numbers, isPrime)));

        // Завдання 2: фільтрація студентів без заборгованостей (усі оцінки >= 60)
        System.out.println("\n=== Завдання 2 ===");
        Student[] students = {
                new Student("Іван", "Петренко", "КН-1", new int[]{90, 75, 82}),
                new Student("Марія", "Шевченко", "КН-1", new int[]{95, 58, 80}),
                new Student("Олег", "Коваль", "КН-2", new int[]{61, 72, 88}),
                new Student("Анна", "Бондар", "КН-2", new int[]{45, 50, 70})
        };
        Predicate<Student> noDebts = s -> Arrays.stream(s.getMarks()).allMatch(mark -> mark >= 60);
        Student[] debtFree = filter(students, noDebts);
        System.out.println("Студенти без заборгованостей:");
        for (Student s : debtFree) {
            System.out.println("  " + s);
        }

        // Завдання 3: фільтрація за двома умовами
        System.out.println("\n=== Завдання 3 ===");
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("Прості та парні: " + Arrays.toString(filter(numbers, isPrime, isEven)));
        Predicate<Integer> greaterThanThree = n -> n > 3;
        System.out.println("Прості та більші за 3: " + Arrays.toString(filter(numbers, isPrime, greaterThanThree)));

        // Завдання 4: Consumer, що виводить "ПРІЗВИЩЕ + ІМ'Я"
        System.out.println("\n=== Завдання 4 ===");
        Consumer<Student> printName = s -> System.out.println(s.getSurname() + " " + s.getName());
        forEach(students, printName);

        // Завдання 5: дія Consumer виконується тільки якщо виконується Predicate
        System.out.println("\n=== Завдання 5 ===");
        Predicate<Integer> greaterThanFive = n -> n > 5;
        Consumer<Integer> printSquare = n -> System.out.println(n + "^2 = " + n * n);
        conditionalForEach(numbers, greaterThanFive, printSquare);

        // Завдання 6: Function n -> 2^n
        System.out.println("\n=== Завдання 6 ===");
        Function<Integer, Integer> powerOfTwo = n -> (int) Math.pow(2, n);
        System.out.println("Степені двійки: " + Arrays.toString(map(numbers, powerOfTwo)));

        // Завдання 7: stringify() — цифра словом
        System.out.println("\n=== Завдання 7 ===");
        Function<Integer, String> digitToWord = n -> {
            String[] names = {"нуль", "один", "два", "три", "чотири",
                    "п'ять", "шість", "сім", "вісім", "дев'ять"};
            if (n < 0 || n > 9) {
                throw new IllegalArgumentException("Очікується цифра від 0 до 9, отримано: " + n);
            }
            return names[n];
        };
        System.out.println("Словами: " + Arrays.toString(stringify(numbers, digitToWord)));
    }

    // Фільтр масиву цілих чисел
    static int[] filter(int[] input, Predicate<Integer> p) {
        int[] result = new int[input.length];
        int counter = 0;
        for (int i : input) {
            if (p.test(i)) {
                result[counter++] = i;
            }
        }
        return Arrays.copyOfRange(result, 0, counter);
    }

    // Фільтр масиву студентів
    static Student[] filter(Student[] input, Predicate<Student> p) {
        Student[] result = new Student[input.length];
        int counter = 0;
        for (Student s : input) {
            if (p.test(s)) {
                result[counter++] = s;
            }
        }
        return Arrays.copyOfRange(result, 0, counter);
    }

    // Фільтр за двома умовами: елемент проходить, якщо задовольняє обом
    static int[] filter(int[] input, Predicate<Integer> p1, Predicate<Integer> p2) {
        return filter(input, p1.and(p2));
    }

    static void forEach(Student[] input, Consumer<Student> action) {
        for (Student s : input) {
            action.accept(s);
        }
    }

    static void conditionalForEach(int[] input, Predicate<Integer> condition, Consumer<Integer> action) {
        for (int i : input) {
            if (condition.test(i)) {
                action.accept(i);
            }
        }
    }

    static int[] map(int[] input, Function<Integer, Integer> f) {
        int[] result = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = f.apply(input[i]);
        }
        return result;
    }

    static String[] stringify(int[] input, Function<Integer, String> f) {
        String[] result = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = f.apply(input[i]);
        }
        return result;
    }
}

class Student {
    private String name;
    private String surname;
    private String group;
    private int[] marks;

    Student(String name, String surname, String group, int[] marks) {
        this.name = name;
        this.surname = surname;
        this.group = group;
        this.marks = marks.clone();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public int[] getMarks() { return marks.clone(); }
    public void setMarks(int[] marks) { this.marks = marks.clone(); }

    @Override
    public String toString() {
        return surname + " " + name + " (" + group + ") " + Arrays.toString(marks);
    }
}
