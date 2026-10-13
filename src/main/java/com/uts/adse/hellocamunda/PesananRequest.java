package com.uts.adse.hellocamunda;

import java.util.Map;

/**
 * Request dari halaman kasir: kode barang -> jumlah, dan uang yang dibayarkan.
 */
public record PesananRequest(Map<String, Integer> items, long bayar) {
}
