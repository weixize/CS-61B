package flik;
import org.junit.*;

import static org.junit.Assert.*;

public class TestFlik {
    @Test
    public void testIsSameNumber1() {
        for (int i = 0; i < 128; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }

    @Test
    public void testIsSameNumber2() {
        for (int i = 128; i < 256; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }

    @Test
    public void testIsSameNumber3() {
        for (int i = 256; i < 500; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }

    @Test
    public void testIsSameNumber4() {
        for (int i = -1000000000; i < 1000000000; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }
}
