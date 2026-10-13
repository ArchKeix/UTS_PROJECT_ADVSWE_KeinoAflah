package com.uts.adse.hellocamunda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API kasir warung.
 *
 * GET  /api/warung/katalog -> daftar barang
 * POST /api/warung/hitung  -> jalankan proses Camunda "ProsesWarung",
 *                             kembalikan struk (total, bayar, kembalian, status)
 */
@RestController
@RequestMapping("/api/warung")
public class WarungController {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public WarungController(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    @GetMapping("/katalog")
    public List<Barang> katalog() {
        return KatalogWarung.semua();
    }

    @PostMapping("/hitung")
    public ResponseEntity<Map<String, Object>> hitung(@RequestBody PesananRequest request) {
        // saring hanya barang valid dengan qty > 0
        Map<String, Integer> items = new LinkedHashMap<>();
        if (request.items() != null) {
            request.items().forEach((kode, qty) -> {
                if (kode != null && qty != null && qty > 0 && KatalogWarung.cari(kode) != null) {
                    items.put(kode, qty);
                }
            });
        }
        if (items.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Pilih minimal satu barang dulu!"));
        }

        // jalankan proses Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("daftarPesanan", new HashMap<>(items));
        variables.put("jumlahBayar", request.bayar());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("ProsesWarung", variables);

        // ambil variabel hasil perhitungan dari proses (via history, aman untuk proses yang sudah/jeda)
        Map<String, Object> vars = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(instance.getId())
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getName,
                        HistoricVariableInstance::getValue, (a, b) -> a, HashMap::new));

        String status = String.valueOf(vars.getOrDefault("statusPembayaran", "-"));

        // detail struk untuk ditampilkan
        List<Map<String, Object>> detail = new ArrayList<>();
        for (Map.Entry<String, Integer> e : items.entrySet()) {
            Barang b = KatalogWarung.cari(e.getKey());
            detail.add(Map.of(
                    "nama", b.nama(),
                    "qty", e.getValue(),
                    "harga", b.harga(),
                    "subtotal", b.harga() * e.getValue()));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("processInstanceId", instance.getId());
        response.put("detail", detail);
        response.put("total", vars.getOrDefault("totalBelanja", 0L));
        response.put("bayar", request.bayar());
        response.put("kembalian", vars.getOrDefault("kembalian", 0L));
        response.put("status", status);
        // kalau pembayaran cukup, proses berhenti di userTask "Cetak Struk"
        response.put("task", "CUKUP".equals(status) ? "Cetak Struk" : null);
        return ResponseEntity.ok(response);
    }
}
