package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T> {
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

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int cnt = 0;

        @Override
        public boolean hasNext() {
            return cnt < size;
        }

        @Override
        public T next() {
            T returnItem = get(cnt);
            cnt += 1;
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ArrayDeque) {
            ArrayDeque AD = (ArrayDeque) o;
            if (this.size != AD.size) {
                return false;
            }
            int i = 0;
            for (T x : this) {
                if (!x.equals(AD.get(i))) {
                    return false;
                }
                i += 1;
            }
            return true;
        } else if (o instanceof LinkedListDeque) {
            LinkedListDeque LLD = (LinkedListDeque) o;
            if (this.size != LLD.size()) {
                return false;
            }
            int i = 0;
            for (Object x : LLD) {
                if (!x.equals(this.get(i))) {
                    return false;
                }
                i += 1;
            }
            return true;
        }
        return false;
    }
}
