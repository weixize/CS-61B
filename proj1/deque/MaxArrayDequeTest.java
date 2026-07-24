package deque;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    private static class Dog {
        public String name;
        public int weight;

        public Dog(String n, int w) {
            name = n;
            weight = w;
        }

        @Override
        public String toString() {
            return name + " " + weight;
        }
    }

    private static class NameComparator implements Comparator<Dog> {
        @Override
        public int compare(Dog o1, Dog o2) {
            return o1.name.compareTo(o2.name);
        }
    }

    private static class WeightComparator implements Comparator<Dog> {
        @Override
        public int compare(Dog o1, Dog o2) {
            return o1.weight - o2.weight;
        }
    }

    @Test
    public void maxTest() {
        Dog d1 = new Dog("A", 10);
        Dog d2 = new Dog("A", 2);
        Dog d3 = new Dog("B", 3);
        Dog d4 = new Dog("C", 1);

        NameComparator NC = new NameComparator();
        WeightComparator WC = new WeightComparator();

        MaxArrayDeque<Dog> n = new MaxArrayDeque<>(NC);
        n.addLast(d1);
        n.addFirst(d2);
        n.addLast(d3);
        n.addLast(d4);
        n.removeLast();
        assertEquals("B", n.max().name);
        assertEquals(10, n.max(WC).weight);

        n.removeLast();
        n.removeLast();
        n.removeLast();
        assertNull(n.max());

        MaxArrayDeque<Dog> w = new MaxArrayDeque<>(WC);
        w.addLast(d1);
        w.addFirst(d2);
        w.addLast(d3);
        w.addLast(d4);
        w.removeFirst();
        assertEquals(10, w.max().weight);
        assertEquals("C", w.max(NC).name);

        w.removeFirst();
        w.removeFirst();
        w.removeFirst();
        assertNull(w.max());
    }
}
