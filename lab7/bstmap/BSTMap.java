package bstmap;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        public K key;
        public V value;
        public BSTNode left = null;
        public BSTNode right = null;

        public BSTNode(K k, V v) {
            key = k;
            value = v;
        }
    }

    private int size = 0;
    private BSTNode root = null;

    private BSTNode findNode(K key) {
        return findNode(key, root);
    }

    private BSTNode findNode(K key, BSTNode currNode) {
        if (currNode == null) {
            return null;
        }

        if (Objects.equals(currNode.key, key)) {
            return currNode;
        } else if (key.compareTo(currNode.key) < 0) {
            return findNode(key, currNode.left);
        } else {
            return findNode(key, currNode.right);
        }
    }

    @Override
    public void clear() {
        if (size != 0) {
            root = null;
            size = 0;
        }
    }

    @Override
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    @Override
    public V get(K key) {
        BSTNode foundNode = findNode(key);
        if (foundNode != null) {
            return foundNode.value;
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        BSTNode foundNode = findNode(key);

        if (foundNode != null) {
            foundNode.value = value;
            return;
        }

        if (size == 0) {
            root = new BSTNode(key, value);
            size += 1;
            return;
        }

        put(key, value, root);
        size += 1;
    }

    private void put(K key, V value, BSTNode currNode) {
        if (key.compareTo(currNode.key) < 0) {
            if (currNode.left == null) {
                currNode.left = new BSTNode(key, value);
            } else {
                put(key, value, currNode.left);
            }
        } else if (key.compareTo(currNode.key) > 0) {
            if (currNode.right == null) {
                currNode.right = new BSTNode(key, value);
            } else {
                put(key, value, currNode.right);
            }
        }
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    public void printInOrder() {
        if (size > 0) {
            printInOrder(root);
        }
    }

    private void printInOrder(BSTNode currNode) {
        if (currNode.left != null) {
            printInOrder(currNode.left);
        }

        System.out.println(currNode.key + ": " + currNode.value);

        if (currNode.right != null) {
            printInOrder(currNode.right);
        }
    }
}
