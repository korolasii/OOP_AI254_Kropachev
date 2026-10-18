package ua.opnu.list;

import java.util.Comparator;

public class MarkSorter implements Comparator {
    private final boolean ascending;

    public MarkSorter(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Object first, Object second) {
        if (first instanceof Student && second instanceof Student) {
            Student firstStudent = (Student) first;
            Student secondStudent = (Student) second;
            double difference = firstStudent.getAvgMark() - secondStudent.getAvgMark();
            if (difference == 0) return 0;
            int result = difference < 0 ? -1 : 1;
            return ascending ? result : -result;
        }
        return 0;
    }
}
