package com.example.individualassignment_tengkumuhammadzulhakim;

import java.util.Locale;

public class TariffCalculator {

    /** Block tariff: 1-200 @ 21.8 sen, 201-300 @ 33.4 sen, 301-600 @ 51.6 sen, 601-1000 @ 54.6 sen. */
    public static double calculateTotal(double units) {
        double total;
        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            total = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            total = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }
        return total;
    }

    /** Final cost = total - (total * rebate%). */
    public static double applyRebate(double total, double rebatePercent) {
        return total - (total * rebatePercent / 100.0);
    }

    /** Formats money as "RM 123.45". */
    public static String formatRM(double amount) {
        return String.format(Locale.US, "RM %.2f", amount);
    }

    /** Shows 467 instead of 467.0, but keeps decimals such as 467.5. */
    public static String formatUnits(double units) {
        if (units == Math.rint(units)) {
            return String.valueOf((long) units);
        }
        return String.format(Locale.US, "%.2f", units).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
