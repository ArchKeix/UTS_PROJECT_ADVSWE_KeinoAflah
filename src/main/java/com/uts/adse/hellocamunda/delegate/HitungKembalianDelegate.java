package com.uts.adse.hellocamunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * ServiceTask Camunda: menghitung kembalian dari "jumlahBayar" - "totalBelanja".
 * Hasil disimpan di variabel proses "kembalian" dan "statusPembayaran".
 */
@Component
public class HitungKembalianDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        long total = ((Number) execution.getVariable("totalBelanja")).longValue();
        long bayar = ((Number) execution.getVariable("jumlahBayar")).longValue();
        long kembalian = bayar - total;

        execution.setVariable("kembalian", kembalian);
        execution.setVariable("statusPembayaran", kembalian >= 0 ? "CUKUP" : "KURANG");

        System.out.println(">>> [Camunda] Bayar Rp " + bayar + " - Total Rp " + total
                + " = Kembalian Rp " + kembalian + " (" + execution.getVariable("statusPembayaran") + ")");
    }
}
