import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FaturaGecmisiUI extends JFrame {
    private JTable faturaTablosu, detayTablosu;
    private DefaultTableModel faturaModel, detayModel;
    private DatabaseManager dbManager;

    public FaturaGecmisiUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setTitle("Geçmiş Faturalar ve Satış Analizi");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // Sol Panel - Faturalar Listesi
        faturaModel = new DefaultTableModel(new String[]{"Fatura No", "Tarih / Saat", "Toplam (TL)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        faturaTablosu = new JTable(faturaModel);
        faturaTablosu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        faturaTablosu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && faturaTablosu.getSelectedRow() != -1) {
                int secilenFaturaId = (int) faturaTablosu.getValueAt(faturaTablosu.getSelectedRow(), 0);
                detaylariYukle(secilenFaturaId);
            }
        });

        JScrollPane leftScroll = new JScrollPane(faturaTablosu);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Tüm Satışlar (Faturalar)"));
        leftScroll.setPreferredSize(new Dimension(350, 0));

        // Sağ Panel - Fatura İçeriği (Satılan Ürünler)
        detayModel = new DefaultTableModel(new String[]{"Ürün Adı", "Adet", "Birim Fiyat", "Ara Toplam"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        detayTablosu = new JTable(detayModel);
        JScrollPane rightScroll = new JScrollPane(detayTablosu);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Seçilen Faturanın İçeriği"));

        add(leftScroll, BorderLayout.WEST);
        add(rightScroll, BorderLayout.CENTER);

        faturalariYukle();
    }

    private void faturalariYukle() {
        faturaModel.setRowCount(0);
        List<Object[]> faturalar = dbManager.faturalariGetir();
        for (Object[] f : faturalar) faturaModel.addRow(f);
    }

    private void detaylariYukle(int invoiceId) {
        detayModel.setRowCount(0);
        List<Object[]> detaylar = dbManager.faturaDetayiGetir(invoiceId);
        for (Object[] d : detaylar) detayModel.addRow(d);
    }
}