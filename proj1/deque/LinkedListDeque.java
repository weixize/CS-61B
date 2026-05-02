package deque;

public class LinkedListDeque<T> {
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

    public void addFirst(T item) {
        Node first = new Node(item, sentinel.next, sentinel);
        first.prev.next = first;
        first.next.prev = first;
        size += 1;
    }

    public void addLast(T item) {
        Node last = new Node(item, sentinel, sentinel.prev);
        last.prev.next = last;
        last.next.prev = last;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node pointer = sentinel.next;
        while (pointer.next.item != null) {
            System.out.print(pointer.item + " ");
            pointer = pointer.next;
        }
        System.out.println(pointer.item);
    }

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
        if (index >= size) {
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
}
