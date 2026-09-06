package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private HashSet<K> keys = new HashSet<>();
    private int bucketSize = 0;
    private double loadFactor = 0.75;
    private int size = 0;
    private int initialSize = 16;

    /** Constructors */
    public MyHashMap() {
        buckets = createTable(initialSize);
        bucketSize = initialSize;
    }

    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        buckets = createTable(this.initialSize);
        bucketSize = this.initialSize;
    }

    /**
     * MyHashMap constructor that creates a backing array of bucketSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(this.initialSize);
        bucketSize = this.initialSize;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] returnTable =  new Collection[tableSize];
        for (int i = 0; i < tableSize; i += 1) {
            returnTable[i] = createBucket();
        }
        return returnTable;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    @Override
    public void clear() {
        buckets = createTable(initialSize);
        keys = new HashSet<>();
        size = 0;
        bucketSize = initialSize;
    }

    private Node searchNode(K key) {
        for (Node node : buckets[Math.floorMod(key.hashCode(), bucketSize)]) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return searchNode(key) != null;
    }

    @Override
    public V get(K key) {
        Node targetNode = searchNode(key);
        if (targetNode == null) {
            return null;
        }
        return targetNode.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        Node targetNode = searchNode(key);
        if (targetNode != null) {
            targetNode.value = value;
        } else {
            buckets[Math.floorMod(key.hashCode(), bucketSize)].add(createNode(key, value));
            size += 1;
            keys.add(key);
            if ((double) size / bucketSize > loadFactor) {
                resize(2 * bucketSize);
            }
        }
    }

    private void resize(int s) {
        Collection<Node>[] bucketsAfterResize = createTable(s);
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                bucketsAfterResize[Math.floorMod(node.key.hashCode(), s)].add(node);
            }
        }
        buckets = bucketsAfterResize;
        bucketSize = s;
    }

    @Override
    public Set<K> keySet() {
        return keys;
    }

    @Override
    public V remove(K key) {
        Node targetNode = searchNode(key);
        if (targetNode == null) {
            return null;
        }
        return remove(targetNode);
    }

    @Override
    public V remove(K key, V value) {
        Node targetNode = searchNode(key);
        if (targetNode == null || !Objects.equals(targetNode.value, value)) {
            return null;
        }
        return remove(targetNode);
    }

    private V remove(Node targetNode) {
        buckets[Math.floorMod(targetNode.key.hashCode(), bucketSize)].remove(targetNode);
        size -= 1;
        keys.remove(targetNode.key);
        return targetNode.value;
    }

    @Override
    public Iterator<K> iterator() {
        return keys.iterator();
    }
}
