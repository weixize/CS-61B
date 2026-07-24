package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> myComparator;
    private T maxItem;

    public MaxArrayDeque(Comparator<T> c) {
        myComparator = c;
    }

    @Override
    public void addLast(T item) {
        if (size() == 0) {
            maxItem = item;
        } else if (myComparator.compare(item, maxItem) > 0) {
            maxItem = item;
        }
        super.addLast(item);
    }

    @Override
    public void addFirst(T item) {
        if (size() == 0) {
            maxItem = item;
        } else if (myComparator.compare(item, maxItem) > 0) {
            maxItem = item;
        }
        super.addFirst(item);
    }

    @Override
    public T removeLast() {
        T last = super.removeLast();
        if (maxItem == last) {
            maxItem = searchMax();
        }
        return last;
    }

    @Override
    public T removeFirst() {
        T first = super.removeFirst();
        if (maxItem == first) {
            maxItem = searchMax();
        }
        return first;
    }

    private T searchMax() {
        if (size() == 0) {
            return null;
        }
        T maxItem = get(0);
        for (T item : this) {
            if (myComparator.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }

    public T max() {
        return maxItem;
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        T maxItem = get(0);
        for(T item : this) {
            if (c.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }
}
