package com.uts.adse.hellocamunda;

/**
 * Data barang yang dijual di warung.
 */
public record Barang(String kode, String nama, long harga, String emoji) {
}
