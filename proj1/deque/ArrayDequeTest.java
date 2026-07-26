package deque;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ArrayDequeTest {
    @Test
    public void iterableTest() {
        ArrayDeque<Integer> AD = new ArrayDeque<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            AD.addLast(i);
        }

        int expected = 0;
        for (int i : AD) {
            assertEquals(expected, i);
            expected += 1;
        }

        assertEquals(N, expected);
    }

    @Test
    public void equalsTest() {
        LinkedListDeque<Integer> LLD = new LinkedListDeque<>();
        ArrayDeque<Integer> AD = new ArrayDeque<>();
        ArrayDeque<Integer> oAD = new ArrayDeque<>();
        int N = 5000;
        LLD.addFirst(null);
        oAD.addFirst(null);
        AD.addFirst(null);
        for (int i = 0; i < N; i += 1) {
            LLD.addLast(i);
            oAD.addLast(i);
            AD.addLast(i);
        }
        assertTrue("LinkedListDeque as input failed! ", AD.equals(LLD));
        assertTrue("ArrayDeque as input failed! ", AD.equals(oAD));
        assertTrue("ArrayDeque as input failed! ", AD.equals(AD));

        LinkedListDeque<Integer> fLLD = new LinkedListDeque<>();
        ArrayDeque<Integer> fAD = new ArrayDeque<>();
        ArrayDeque<Integer> foAD = new ArrayDeque<>();
        int M = 8;
        for (int i = 0; i < M; i += 1) {
            fLLD.addLast(i);
            foAD.addLast(i);
            fAD.addFirst(i);
        }
        assertTrue("LinkedListDeque as input failed! ", !fAD.equals(fLLD));
        assertTrue("ArrayDeque as input failed! ", !fAD.equals(foAD));
    }

    @Test
    public void resizeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ArrayDeque<Integer> AD = new ArrayDeque<>();
        int N = 10000;
        for (int i = 0; i < N; i += 1) {
            AD.addLast(i);
        }
        int M = 4999;
        for (int i = 0; i < M; i += 1) {
            AD.removeFirst();
        }
        for (int i = 0; i < N - M - 1; i += 1) {
            AD.removeLast();
        }

        Method method = ArrayDeque.class.getDeclaredMethod("itemsLength");
        method.setAccessible(true);
        assertTrue(((int) method.invoke(AD) <= 8) || (AD.size() >= 0.25 * (int) method.invoke(AD)));
    }

    @Test
    public void randomizedTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int n = 5000;
        for (int j = 0; j < n; j += 1) {
            ArrayDeque<Integer> AD = new ArrayDeque<>();
            LinkedListDeque<Integer> LLD = new LinkedListDeque<>();

            int N = 5000;
            for (int i = 0; i < N; i += 1) {
                int operationNumber = StdRandom.uniform(0, 6);
                if (operationNumber == 0) {
                    // addLast
                    int randVal = StdRandom.uniform(0, 100);
                    AD.addLast(randVal);
                    LLD.addLast(randVal);
                } else if (operationNumber == 1) {
                    // size
                    int sizeALNR = AD.size();
                    int sizeBAL = LLD.size();
                    assertEquals(sizeALNR, sizeBAL);
                } else if (operationNumber == 2) {
                    if (!AD.isEmpty()) {
                        int randomIndex = StdRandom.uniform(0, AD.size());
                        Integer randomALNR = AD.get(randomIndex);
                        Integer randomBAL = LLD.get(randomIndex);
                        assertEquals(randomALNR, randomBAL);
                    }
                } else if (operationNumber == 3) {
                    if (!AD.isEmpty()) {
                        int lastALNR = AD.removeLast();
                        int lastBAL = LLD.removeLast();
                        assertEquals(lastALNR, lastBAL);
                    }
                } else if (operationNumber == 4) {
                    int randVal = StdRandom.uniform(0, 100);
                    AD.addFirst(randVal);
                    LLD.addFirst(randVal);
                } else if (operationNumber == 5) {
                    if (!AD.isEmpty()) {
                        int firstALNR = AD.removeFirst();
                        int firstBAL = LLD.removeFirst();
                        assertEquals(firstALNR, firstBAL);
                    }
                }
                assertEquals(AD, LLD);
                assertEquals(LLD, AD);

                Method method = ArrayDeque.class.getDeclaredMethod("itemsLength");
                method.setAccessible(true);
                assertTrue(((int) method.invoke(AD) <= 8) || (AD.size() >= 0.25 * (int) method.invoke(AD)));
            }
        }
    }
}
