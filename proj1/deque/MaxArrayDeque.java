package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> myComparator;

    public MaxArrayDeque(Comparator<T> c) {
        myComparator = c;
    }

    private T searchMax() {
        if (size() == 0) {
            return null;
        }
        T max = get(0);
        for (T item : this) {
            if (myComparator.compare(item, max) > 0) {
                max = item;
            }
        }
        return max;
    }

    public T max() {
        return searchMax();
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        T max = get(0);
        for (T item : this) {
            if (c.compare(item, max) > 0) {
                max = item;
            }
        }
        return max;
    }
}
