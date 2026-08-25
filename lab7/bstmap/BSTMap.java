package bstmap;

import java.io.Serializable;
import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        public K key;
        public V value;
        public BSTNode left = null;
        public BSTNode right = null;
        public BSTNode rightParent = null;
        public BSTNode leftParent = null;

        public BSTNode(K k, V v, BSTNode rP, BSTNode lP) {
            key = k;
            value = v;
            rightParent = rP;
            leftParent = lP;
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
            root = new BSTNode(key, value, null, null);
            size += 1;
            return;
        }

        put(key, value, root);
        size += 1;
    }

    private void put(K key, V value, BSTNode currNode) {
        if (key.compareTo(currNode.key) < 0) {
            if (currNode.left == null) {
                currNode.left = new BSTNode(key, value, currNode, null);
            } else {
                put(key, value, currNode.left);
            }
        } else if (key.compareTo(currNode.key) > 0) {
            if (currNode.right == null) {
                currNode.right = new BSTNode(key, value, null, currNode);
            } else {
                put(key, value, currNode.right);
            }
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> returnSet = new HashSet<>();
        for (K key : this) {
            returnSet.add(key);
        }
        return returnSet;
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
        return new BSTMapIterator();
    }

    private class BSTMapIterator implements Iterator<K> {
        private BSTNode currNode;

        private void moveToLeftBottom() {
            while (currNode.left != null) {
                currNode = currNode.left;
            }
        }

        public BSTMapIterator() {
            currNode = root;
            if (currNode != null) {
                moveToLeftBottom();
            }
        }

        @Override
        public boolean hasNext() {
            return currNode != null;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            K returnK = currNode.key;

            if (currNode.right != null) {
                currNode = currNode.right;
                moveToLeftBottom();
            } else if (currNode.rightParent != null) {
                currNode = currNode.rightParent;
            } else if (currNode.leftParent != null) {
                backFromRightBottom();
            } else {
                currNode = null;
            }

            return returnK;
        }

        private void backFromRightBottom() {
            while (currNode.leftParent != null) {
                currNode = currNode.leftParent;
            }
            currNode = currNode.rightParent;
        }
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
