package com.uts.adse.hellocamunda;

import java.io.Serializable;
import java.util.List;

/**
 * Isi struk pembayaran, hasil perhitungan dari proses Camunda.
 * Dikirim ke halaman lewat flash attribute (redirect), jadi harus Serializable.
 */
public record Struk(String processInstanceId, List<BarisStruk> detail, long total, long bayar,
                   long kembalian, String status, String task) implements Serializable {

    /** Satu baris rincian belanja pada struk. */
    public record BarisStruk(String nama, int qty, long harga, long subtotal) implements Serializable {
    }
}
