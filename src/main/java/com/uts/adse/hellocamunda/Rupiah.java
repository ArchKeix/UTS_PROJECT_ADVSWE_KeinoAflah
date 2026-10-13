package com.uts.adse.hellocamunda;

import java.util.Locale;

/**
 * Helper format angka menjadi format rupiah Indonesia, misalnya 3500 -> "Rp 3.500".
 * Dipakai langsung dari template Thymeleaf.
 */
public final class Rupiah {

    private static final Locale LOCALE_ID = new Locale("in", "ID");

    private Rupiah() {
    }

    /** Format nilai rupiah, contoh: 15000 -> "Rp 15.000". */
    public static String format(long nilai) {
        return String.format(LOCALE_ID, "Rp %,.0f", (double) nilai);
    }

    /** Format nilai tanpa tanda "Rp", contoh: 15000 -> "15.000". */
    public static String angka(long nilai) {
        return String.format(LOCALE_ID, "%,.0f", (double) nilai);
    }
}
