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
            for (int i = 0; i < N; i += 1) {
                int operationNumber = StdRandom.uniform(0, 4);
                if (operationNumber == 0) {
                    int randVal = StdRandom.uniform(0, 100);
                    sad.addLast(randVal);
                    aas.addLast(randVal);
                } else if (operationNumber == 1) {
                    if (!aas.isEmpty()) {
                        Integer lastSad = sad.removeLast();
                        Integer lastAas = aas.removeLast();
                        assertEquals(lastSad, lastAas);
                    }
                } else if (operationNumber == 2) {
                    int randVal = StdRandom.uniform(0, 100);
                    sad.addFirst(randVal);
                    aas.addFirst(randVal);
                } else if (operationNumber == 3) {
                    if (!aas.isEmpty()) {
                        Integer firstSad = sad.removeFirst();
                        Integer firstAas = aas.removeFirst();
                        assertEquals(firstSad, firstAas);
                    }
                }
            }
        }
    }
}
