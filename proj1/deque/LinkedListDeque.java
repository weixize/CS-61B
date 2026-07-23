package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        public T item;
        public Node next;
        public Node prev;

        public Node(T i, Node n, Node p) {
            item = i;
            next = n;
            prev = p;
        }
    }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        Node first = new Node(item, sentinel.next, sentinel);
        first.prev.next = first;
        first.next.prev = first;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        Node last = new Node(item, sentinel, sentinel.prev);
        last.prev.next = last;
        last.next.prev = last;
        size += 1;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node pointer = sentinel.next;
        while (pointer.next.item != null) {
            System.out.print(pointer.item + " ");
            pointer = pointer.next;
        }
        if (!isEmpty()){
            System.out.println(pointer.item);
        } else {
            System.out.println();
        }
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            T result = sentinel.next.item;
            sentinel.next = sentinel.next.next;
            sentinel.next.prev = sentinel;
            size -= 1;
            return result;
        }
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            T result = sentinel.prev.item;
            sentinel.prev = sentinel.prev.prev;
            sentinel.prev.next = sentinel;
            size -= 1;
            return result;
        }
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        } else {
            Node pointer = sentinel.next;
            for (int i = 0; i < index; i += 1) {
                pointer = pointer.next;
            }
            return pointer.item;
        }
    }

    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        } else {
            return getRecursiveHelper(index, sentinel.next);
        }
    }

    /**
     * return the index Node's item behind pointer if there is index Nodes behind pointer
     * @param index an integer
     * @param pointer current Node
     * @return a Node's item
     */
    private T getRecursiveHelper(int index, Node pointer) {
        if (index == 0) {
            return pointer.item;
        } else {
            return getRecursiveHelper(index - 1, pointer.next);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node pointer;
        public LinkedListDequeIterator() {
            pointer = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return pointer != sentinel;
        }

        @Override
        public T next() {
            T returnItem = pointer.item;
            pointer = pointer.next;
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
            if (this.size != AD.size()) {
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
            if (this.size != LLD.size) {
                return false;
            }
            Node p = LLD.sentinel.next;
            for (T x : this) {
                if (!x.equals(p.item)) {
                    return false;
                }
                p = p.next;
            }
            return true;
        }
        return false;
    }
}
