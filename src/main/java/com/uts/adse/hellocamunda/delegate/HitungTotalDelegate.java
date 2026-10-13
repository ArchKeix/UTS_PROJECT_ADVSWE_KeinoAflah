package com.uts.adse.hellocamunda.delegate;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.uts.adse.hellocamunda.KatalogWarung;

/**
 * ServiceTask Camunda: menghitung total belanja dari variabel "daftarPesanan".
 * Hasil disimpan di variabel proses "totalBelanja".
 */
@Component
public class HitungTotalDelegate implements JavaDelegate {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        Map<String, Object> pesanan = (Map<String, Object>) execution.getVariable("daftarPesanan");

        long total = 0;
        if (pesanan != null) {
            for (Map.Entry<String, Object> entry : pesanan.entrySet()) {
                var barang = KatalogWarung.cari(entry.getKey());
                int qty = ((Number) entry.getValue()).intValue();
                if (barang != null && qty > 0) {
                    total += barang.harga() * qty;
                }
            }
        }

        execution.setVariable("totalBelanja", total);
        System.out.println(">>> [Camunda] Hitung Total Belanja: Rp " + total);
    }
}
