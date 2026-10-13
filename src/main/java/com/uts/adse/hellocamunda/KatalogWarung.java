package com.uts.adse.hellocamunda;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Daftar barang warung (katalog sederhana, hardcoded).
 */
public final class KatalogWarung {

    private static final Map<String, Barang> KATALOG = new LinkedHashMap<>();

    static {
        daftarkan(new Barang("MIE",  "Indomie Goreng",   3500,  "\uD83C\uDF5C"));
        daftarkan(new Barang("KOPI", "Kopi Good Day",    2000,  "☕"));
        daftarkan(new Barang("TEH",  "Teh Pucuk",        4000,  "\uD83C\uDF75"));
        daftarkan(new Barang("ROTI", "Roti Tawar",       12000, "\uD83C\uDF5E"));
        daftarkan(new Barang("AIR",  "Air Mineral 600ml", 3000, "\uD83E\uDD64"));
        daftarkan(new Barang("CHIT", "Chitato Sapi",     10000, "\uD83C\uDF5F"));
        daftarkan(new Barang("SQ",   "SilverQueen",      15000, "\uD83C\uDF6B"));
        daftarkan(new Barang("BB",   "Beng-Beng",        2500,  "\uD83C\uDF6C"));
    }

    private KatalogWarung() {
    }

    private static void daftarkan(Barang barang) {
        KATALOG.put(barang.kode(), barang);
    }

    /** Semua barang sesuai urutan katalog. */
    public static List<Barang> semua() {
        return List.copyOf(KATALOG.values());
    }

    /** Cari barang berdasarkan kode, null jika tidak ada. */
    public static Barang cari(String kode) {
        return KATALOG.get(kode);
    }
}
