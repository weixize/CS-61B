package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> comparison = new AListNoResizing<>();
        BuggyAList<Integer> test = new BuggyAList<>();
        for (int i = 4; i < 7; i += 1) {
            comparison.addLast(i);
            test.addLast(i);
        }
        for (int i = 0; i < 3; i += 1) {
            assertEquals(comparison.size(), test.size());
            assertEquals(comparison.removeLast(), test.removeLast());
        }
        assertEquals(comparison.size(), test.size());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> ALNR = new AListNoResizing<>();
        BuggyAList<Integer> BAL = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                ALNR.addLast(randVal);
                BAL.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int sizeALNR = ALNR.size();
                int sizeBAL = BAL.size();
                assertEquals(sizeALNR, sizeBAL);
            } else if (operationNumber == 2) {
                if (ALNR.size() > 0) {
                    int lastALNR = ALNR.getLast();
                    int lastBAL = BAL.getLast();
                    assertEquals(lastALNR, lastBAL);
                }
            } else if (operationNumber == 3) {
                if (ALNR.size() > 0) {
                    int lastALNR = ALNR.removeLast();
                    int lastBAL = BAL.removeLast();
                    assertEquals(lastALNR, lastBAL);
                }
            }
        }
    }
}
