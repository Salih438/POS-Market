import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UrunYonetimiUI extends JFrame {
    private DatabaseManager dbManager;
    private JTable tablo;
    private DefaultTableModel model;
    private JTextField txtAd, txtFiyat, txtStok;
    private JComboBox<String> cbKategori, cbBirim;
    private JButton btnKaydet, btnSil, btnTemizle;
    private int secilenUrunId = -1;

    public UrunYonetimiUI(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setTitle("Stok ve Ürün Yönetim Paneli (Arka Ofis)");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(236, 240, 241));

        add(formPaneliOlustur(), BorderLayout.WEST);
        add(tabloPaneliOlustur(), BorderLayout.CENTER);

        urunleriYukle();
    }

    private JPanel formPaneliOlustur() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Ürün Ekle / Güncelle"));
        panel.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0;

        txtAd = new JTextField(15);
        cbKategori = new JComboBox<>(new String[]{"Gıda / Bakkal", "Sebze / Meyve", "Tütün Mamülleri", "Hırdavat", "Pet Shop", "Diğer"});
        cbBirim = new JComboBox<>(new String[]{"Adet", "Kg"});
        txtFiyat = new JTextField(15);
        txtStok = new JTextField(15);

        panel.add(new JLabel("Ürün Adı:"), gbc); gbc.gridy = 1; panel.add(txtAd, gbc);
        gbc.gridy = 2; panel.add(new JLabel("Kategori:"), gbc); gbc.gridy = 3; panel.add(cbKategori, gbc);
        gbc.gridy = 4; panel.add(new JLabel("Satış Birimi:"), gbc); gbc.gridy = 5; panel.add(cbBirim, gbc);
        gbc.gridy = 6; panel.add(new JLabel("Fiyat (TL):"), gbc); gbc.gridy = 7; panel.add(txtFiyat, gbc);
        gbc.gridy = 8; panel.add(new JLabel("Stok Miktarı:"), gbc); gbc.gridy = 9; panel.add(txtStok, gbc);

        btnKaydet = new JButton("Kaydet");
        btnKaydet.setBackground(new Color(39, 174, 96)); btnKaydet.setForeground(Color.WHITE);
        btnKaydet.addActionListener(e -> kaydet());

        btnSil = new JButton("Sil");
        btnSil.setBackground(new Color(231, 76, 60)); btnSil.setForeground(Color.WHITE); btnSil.setEnabled(false);
        btnSil.addActionListener(e -> sil());

        btnTemizle = new JButton("Yeni Form");
        btnTemizle.addActionListener(e -> formuTemizle());

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        btnPanel.setOpaque(false);
        btnPanel.add(btnKaydet); btnPanel.add(btnSil); btnPanel.add(btnTemizle);

        gbc.gridy = 10; panel.add(btnPanel, gbc);
        return panel;
    }

    private JPanel tabloPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 0, 10, 10));
        panel.setOpaque(false);

        model = new DefaultTableModel(new String[]{"ID", "Ürün Adı", "Kategori", "Birim", "Fiyat", "Stok"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablo = new JTable(model);
        tablo.setRowHeight(25);

        tablo.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablo.getSelectedRow() != -1) {
                int secilen = tablo.getSelectedRow();
                secilenUrunId = (int) tablo.getValueAt(secilen, 0);
                txtAd.setText(tablo.getValueAt(secilen, 1).toString());
                cbKategori.setSelectedItem(tablo.getValueAt(secilen, 2).toString());
                cbBirim.setSelectedItem(tablo.getValueAt(secilen, 3).toString());
                txtFiyat.setText(tablo.getValueAt(secilen, 4).toString());
                txtStok.setText(tablo.getValueAt(secilen, 5).toString());
                btnKaydet.setText("Güncelle"); btnSil.setEnabled(true);
            }
        });

        panel.add(new JScrollPane(tablo), BorderLayout.CENTER);
        return panel;
    }

    private void urunleriYukle() {
        model.setRowCount(0);
        for (Object[] u : dbManager.urunleriGetir("")) model.addRow(u);
    }

    private void formuTemizle() {
        secilenUrunId = -1;
        txtAd.setText(""); txtFiyat.setText(""); txtStok.setText("");
        cbKategori.setSelectedIndex(0); cbBirim.setSelectedIndex(0);
        tablo.clearSelection(); btnKaydet.setText("Kaydet"); btnSil.setEnabled(false);
    }

    private void kaydet() {
        try {
            String ad = txtAd.getText(); String kategori = cbKategori.getSelectedItem().toString();
            String birim = cbBirim.getSelectedItem().toString();
            double fiyat = Double.parseDouble(txtFiyat.getText()); double stok = Double.parseDouble(txtStok.getText());

            boolean basarili = (secilenUrunId == -1) ? dbManager.urunEkle(ad, kategori, birim, fiyat, stok) : dbManager.urunGuncelle(secilenUrunId, ad, kategori, birim, fiyat, stok);
            if (basarili) { formuTemizle(); urunleriYukle(); } else JOptionPane.showMessageDialog(this, "Hata oluştu!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Geçerli sayılar girin (Örn: 1.5)"); }
    }

    private void sil() {
        if (secilenUrunId != -1 && dbManager.urunSil(secilenUrunId)) {
            formuTemizle(); urunleriYukle();
        }
    }
}