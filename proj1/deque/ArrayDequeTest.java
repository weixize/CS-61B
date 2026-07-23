package deque;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void iterableTest() {
        ArrayDeque<Integer> AD = new ArrayDeque<>();
        int N = 8;
        for (int i = 0; i < N; i += 1) {
            AD.addLast(i);
        }

        int expected = 0;
        for (int i : AD) {
            assertEquals(expected, i);
            expected += 1;
        }
    }

    @Test
    public void equalsTest() {
        LinkedListDeque<Integer> LLD = new LinkedListDeque<>();
        ArrayDeque<Integer> AD = new ArrayDeque<>();
        ArrayDeque<Integer> oAD = new ArrayDeque<>();
        int N = 8;
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
}
