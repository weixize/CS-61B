package flik;
import org.junit.*;

import static org.junit.Assert.*;

public class TestFlik {
    @Test
    public void TestIsSameNumber1() {
        for (int i = 0; i < 128; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }

    @Test
    public void TestIsSameNumber2() {
        for (int i = 128; i < 256; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }

    @Test
    public void TestIsSameNumber3() {
        for (int i = 256; i < 500; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }

    @Test
    public void TestIsSameNumber4() {
        for (int i = -10000; i < 10000; i += 1) {
            int j = i;
            assertTrue("testing " + i, Flik.isSameNumber(i, j));
        }
    }
}
