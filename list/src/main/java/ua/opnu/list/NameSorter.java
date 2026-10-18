package ua.opnu.list;

import java.util.Comparator;

public class NameSorter implements Comparator {

    private boolean order;

    public NameSorter(boolean order) {
        this.order = order;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof Student && o2 instanceof Student) {
            Student s1 = (Student) o1;
            Student s2 = (Student) o2;

            if (order) {
                return s1.getName().compareTo(s2.getName());
            } else {
                return s2.getName().compareTo(s1.getName());
            }

        }
        return 0;
    }
}
