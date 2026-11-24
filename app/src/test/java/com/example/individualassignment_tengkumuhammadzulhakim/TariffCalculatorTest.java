package com.example.individualassignment_tengkumuhammadzulhakim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Checks the calculator against the sample calculations in the assignment brief. */
public class TariffCalculatorTest {
    private static final double D = 0.0001;

    @Test public void sample150() { assertEquals(32.7, TariffCalculator.calculateTotal(150), D); }
    @Test public void sample250() { assertEquals(60.3, TariffCalculator.calculateTotal(250), D); }
    @Test public void sample467() { assertEquals(163.172, TariffCalculator.calculateTotal(467), D); }
    @Test public void sample551() { assertEquals(206.516, TariffCalculator.calculateTotal(551), D); }
    @Test public void sample115() { assertEquals(25.07, TariffCalculator.calculateTotal(115), D); }
    @Test public void max1000() { assertEquals(43.6 + 33.4 + 154.8 + 218.4, TariffCalculator.calculateTotal(1000), D); }

    @Test public void rebate5Percent() { assertEquals(30.40, TariffCalculator.applyRebate(32, 5), D); }

    @Test public void formatting() {
        assertEquals("RM 163.17", TariffCalculator.formatRM(163.172));
        assertEquals("467", TariffCalculator.formatUnits(467.0));
        assertEquals("467.5", TariffCalculator.formatUnits(467.5));
    }
}
