package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextLast;
    private int nextFirst;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextLast = 5;
        nextFirst = 4;
    }

    public void addLast(T item) {
        items[nextLast] = item;
        nextLast = indexNormalization(nextLast + 1);
        size += 1;
    }

    public void addFirst(T item) {
        items[nextFirst] = item;
        nextFirst = indexNormalization(nextFirst - 1);
        size += 1;
    }

    /**
     * take an index, normalize it based on the length of items
     * @param index the given index
     * @return valid index
     */
    private int indexNormalization(int index) {
        if (index >= items.length) {
            index %= items.length;
        } else {
            while (index < 0) {
                index += items.length;
            }
        }
        return index;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size - 1; i += 1) {
            System.out.print(get(i) + " ");
        }
        if (isEmpty()) {
            System.out.println();
        } else {
            System.out.println(get(size - 1));
        }
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = indexNormalization(nextFirst + 1);
        T item = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        return item;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = indexNormalization(nextLast - 1);
        T item = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        return item;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        int first = indexNormalization(nextFirst + 1);
        return items[indexNormalization(index + first)];
    }
}
