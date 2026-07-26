package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void randomizedTest() {
        int n = 5000;
        for (int j = 0; j < n; j += 1) {
            StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
            ArrayDequeSolution<Integer> aas = new ArrayDequeSolution<>();

            int N = 5000;
            int[][] cache = new int[N][];
            for (int i = 0; i < N; i += 1) {
                int operationNumber = StdRandom.uniform(0, 4);
                if (operationNumber == 0) {
                    int randVal = StdRandom.uniform(0, 100);
                    sad.addLast(randVal);
                    aas.addLast(randVal);
                    cache[i] = new int[]{operationNumber, randVal};
                } else if (operationNumber == 1) {
                    if (!aas.isEmpty()) {
                        Integer lastSad = sad.removeLast();
                        Integer lastAas = aas.removeLast();
                        cache[i] = new int[]{operationNumber};
                        assertEquals(construct(cache), lastSad, lastAas);
                    }
                } else if (operationNumber == 2) {
                    int randVal = StdRandom.uniform(0, 100);
                    sad.addFirst(randVal);
                    aas.addFirst(randVal);
                    cache[i] = new int[]{operationNumber, randVal};
                } else if (operationNumber == 3) {
                    if (!aas.isEmpty()) {
                        Integer firstSad = sad.removeFirst();
                        Integer firstAas = aas.removeFirst();
                        cache[i] = new int[]{operationNumber};
                        assertEquals(construct(cache), firstSad, firstAas);
                    }
                }
            }
        }
    }

    private static String construct(int[][] cache) {
        StringBuilder returnSB = new StringBuilder();
        for (int[] ints : cache) {
            if (ints == null) {
                continue;
            }
            if (ints[0] == 0) {
                returnSB.append("addLast(");
                returnSB.append(ints[1]);
                returnSB.append(")\n");
            } else if (ints[0] == 1) {
                returnSB.append("removeLast()\n");
            } else if (ints[0] == 2) {
                returnSB.append("addFirst(");
                returnSB.append(ints[1]);
                returnSB.append(")\n");
            } else if (ints[0] == 3) {
                returnSB.append("removeFirst()\n");
            }
        }
        return returnSB.toString();
    }
}
