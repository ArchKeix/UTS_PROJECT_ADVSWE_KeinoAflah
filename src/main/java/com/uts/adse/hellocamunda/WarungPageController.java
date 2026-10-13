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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Halaman kasir warung.
 *
 * GET  /warung -> render halaman kasir (Thymeleaf)
 * POST /warung -> terima form HTML, jalankan proses Camunda "ProsesWarung",
 *                 lalu redirect kembali ke /warung dengan data struk
 *
 * Seluruh tampilan dirender di sisi server (Java), halaman depan hanya HTML + CSS.
 */
@Controller
public class WarungPageController {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public WarungPageController(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    @GetMapping({"/", "/warung"})
    public String halamanKasir(Model model) {
        model.addAttribute("katalog", KatalogWarung.semua());
        return "warung";
    }

    @PostMapping("/warung")
    public String proses(@RequestParam Map<String, String> params, RedirectAttributes redirect) {
        // baca jumlah tiap barang dari form (name="qty_<KODE>")
        Map<String, Integer> pesanan = new LinkedHashMap<>();
        Map<String, Integer> qtyLama = new LinkedHashMap<>();
        for (Barang barang : KatalogWarung.semua()) {
            int qty = bacaAngka(params.get("qty_" + barang.kode()));
            qtyLama.put(barang.kode(), qty);
            if (qty > 0) {
                pesanan.put(barang.kode(), qty);
            }
        }
        long bayar = bacaAngka(params.get("bayar"));

        // simpan isi form supaya tidak hilang setelah halaman dimuat ulang
        redirect.addFlashAttribute("qtyLama", qtyLama);
        redirect.addFlashAttribute("bayarLama", bayar);

        if (pesanan.isEmpty()) {
            redirect.addFlashAttribute("error", "Pilih minimal satu barang dulu!");
            return "redirect:/warung";
        }

        // jalankan proses Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("daftarPesanan", new HashMap<>(pesanan));
        variables.put("jumlahBayar", bayar);

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("ProsesWarung", variables);

        // ambil variabel hasil perhitungan dari proses (via history)
        Map<String, Object> vars = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(instance.getId())
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getName,
                        HistoricVariableInstance::getValue, (a, b) -> a, HashMap::new));

        String status = String.valueOf(vars.getOrDefault("statusPembayaran", "-"));

        // rincian struk
        List<Struk.BarisStruk> detail = new ArrayList<>();
        for (Map.Entry<String, Integer> e : pesanan.entrySet()) {
            Barang b = KatalogWarung.cari(e.getKey());
            detail.add(new Struk.BarisStruk(b.nama(), e.getValue(), b.harga(), b.harga() * e.getValue()));
        }

        redirect.addFlashAttribute("struk", new Struk(
                instance.getId(),
                detail,
                angka(vars.get("totalBelanja")),
                bayar,
                angka(vars.get("kembalian")),
                status,
                "CUKUP".equals(status) ? "Cetak Struk" : null));

        return "redirect:/warung";
    }

    private static int bacaAngka(String nilai) {
        if (nilai == null || nilai.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(nilai.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long angka(Object nilai) {
        return nilai instanceof Number number ? number.longValue() : 0L;
    }
}
