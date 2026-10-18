package ua.opnu.list;

import java.util.Comparator;

public class LastNameSorter implements Comparator {
    private final boolean ascending;

    public LastNameSorter(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Object first, Object second) {
        if (first instanceof Student && second instanceof Student) {
            Student firstStudent = (Student) first;
            Student secondStudent = (Student) second;
            return ascending
                    ? firstStudent.getLastName().compareTo(secondStudent.getLastName())
                    : secondStudent.getLastName().compareTo(firstStudent.getLastName());
        }
        return 0;
    }
}
