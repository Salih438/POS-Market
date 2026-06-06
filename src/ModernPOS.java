import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ModernPOS extends JFrame {
    private JTextField aramaKutusu, miktarKutusu;
    private JTable urunTablosu, sepetTablosu;
    private DefaultTableModel urunModel, sepetModel;
    private JLabel lblGenelToplam;
    private double genelToplam = 0.0;
    private DatabaseManager dbManager;
    private int mouseX, mouseY;

    public ModernPOS() {
        dbManager = new DatabaseManager();
        setUndecorated(true);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 246, 250));
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(44, 62, 80), 2));

        add(ozelTitleBarOlustur(), BorderLayout.NORTH);

        JPanel icerikPaneli = new JPanel(new BorderLayout(15, 15));
        icerikPaneli.setBorder(new EmptyBorder(15, 15, 15, 15));
        icerikPaneli.setOpaque(false);

        icerikPaneli.add(solPanelOlustur(), BorderLayout.CENTER);
        icerikPaneli.add(sagPanelOlustur(), BorderLayout.EAST);
        add(icerikPaneli, BorderLayout.CENTER);

        urunleriEkranaYukle("");
    }

    private JPanel ozelTitleBarOlustur() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(44, 62, 80));
        titleBar.setPreferredSize(new Dimension(getWidth(), 45));
        titleBar.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }});
        titleBar.addMouseMotionListener(new MouseMotionAdapter() { public void mouseDragged(MouseEvent e) { setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY); }});

        JLabel lblBaslik = new JLabel("  PRO-POS | Hipermarket Kasa Sistemi");
        lblBaslik.setForeground(Color.WHITE); lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleBar.add(lblBaslik, BorderLayout.WEST);

        JPanel butonPaneli = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); butonPaneli.setOpaque(false);

        // YENİ: Ürün Yönetimi Butonu
        JButton btnYonetim = butonOlustur("Ürün / Stok Yönetimi", new Color(243, 156, 18), Color.WHITE, new Font("Segoe UI", Font.BOLD, 12));
        btnYonetim.addActionListener(e -> {
            new UrunYonetimiUI(dbManager).setVisible(true);
            urunleriEkranaYukle(""); // Pencere kapanınca listeyi güncelle
        });

        JButton btnGecmis = butonOlustur("Geçmiş Faturalar", new Color(46, 204, 113), Color.WHITE, new Font("Segoe UI", Font.BOLD, 12));
        btnGecmis.addActionListener(e -> new FaturaGecmisiUI(dbManager).setVisible(true));
        JButton btnMin = butonOlustur(" - ", new Color(44, 62, 80), Color.WHITE, new Font("Arial", Font.BOLD, 18));
        btnMin.addActionListener(e -> setState(JFrame.ICONIFIED));
        JButton btnClose = butonOlustur(" X ", new Color(231, 76, 60), Color.WHITE, new Font("Arial", Font.BOLD, 16));
        btnClose.addActionListener(e -> System.exit(0));

        butonPaneli.add(btnYonetim); butonPaneli.add(Box.createHorizontalStrut(10));
        butonPaneli.add(btnGecmis); butonPaneli.add(Box.createHorizontalStrut(20));
        butonPaneli.add(btnMin); butonPaneli.add(btnClose);
        titleBar.add(butonPaneli, BorderLayout.EAST);
        return titleBar;
    }

    private JButton butonOlustur(String metin, Color arkaPlan, Color yaziRengi, Font font) {
        JButton btn = new JButton(metin); btn.setBackground(arkaPlan); btn.setForeground(yaziRengi);
        btn.setFont(font); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel solPanelOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 15)); panel.setOpaque(false);

        JPanel aramaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); aramaPanel.setOpaque(false);
        aramaKutusu = new JTextField(30); aramaKutusu.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        aramaKutusu.addActionListener(e -> urunleriEkranaYukle(aramaKutusu.getText()));
        JButton btnAra = butonOlustur("Kasa Barkod / Arama", new Color(52, 152, 219), Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
        btnAra.addActionListener(e -> urunleriEkranaYukle(aramaKutusu.getText()));
        aramaPanel.add(new JLabel("Arama: ")); aramaPanel.add(aramaKutusu); aramaPanel.add(btnAra);

        // YENİ: Kategori ve Birim eklendi
        urunModel = new DefaultTableModel(new String[]{"ID", "Ürün", "Kategori", "Birim", "Fiyat", "Stok"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        urunTablosu = new JTable(urunModel); urunTablosu.setRowHeight(35); urunTablosu.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        urunTablosu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Eğer ürün KG ise miktara odaklan, Adet ise direkt 1 ekle
                    String birim = urunTablosu.getValueAt(urunTablosu.getSelectedRow(), 3).toString();
                    if(birim.equals("Kg")) {
                        miktarKutusu.setText("");
                        miktarKutusu.requestFocus();
                    } else {
                        miktarKutusu.setText("1"); sepeteEkle();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(urunTablosu);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Satıştaki Ürünler (Hızlı satış için Adet ürünlerine çift tıklayın)"));

        panel.add(aramaPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel sagPanelOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 15)); panel.setOpaque(false); panel.setPreferredSize(new Dimension(450, 0));

        sepetModel = new DefaultTableModel(new String[]{"ID", "Ürün", "Birim", "Miktar", "Ara Toplam"}, 0);
        sepetTablosu = new JTable(sepetModel); sepetTablosu.setRowHeight(30);

        JPanel islemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        miktarKutusu = new JTextField("1", 5); miktarKutusu.setHorizontalAlignment(JTextField.CENTER); miktarKutusu.setFont(new Font("Segoe UI", Font.BOLD, 16));
        miktarKutusu.addActionListener(e -> sepeteEkle());
        JButton btnSepeteEkle = butonOlustur("Sepete Ekle (Enter)", new Color(243, 156, 18), Color.WHITE, new Font("Segoe UI", Font.BOLD, 14));
        btnSepeteEkle.addActionListener(e -> sepeteEkle());

        islemPanel.add(new JLabel("Miktar (Kg/Adet): ")); islemPanel.add(miktarKutusu); islemPanel.add(Box.createHorizontalStrut(10)); islemPanel.add(btnSepeteEkle);

        JPanel odemePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        lblGenelToplam = new JLabel("TOPLAM: 0.00 TL", SwingConstants.CENTER); lblGenelToplam.setFont(new Font("Segoe UI", Font.BOLD, 32)); lblGenelToplam.setForeground(new Color(192, 57, 43));
        JButton btnFisiKes = butonOlustur("FİŞİ KES (ÖDEMEYİ AL)", new Color(41, 128, 185), Color.WHITE, new Font("Segoe UI", Font.BOLD, 18));
        btnFisiKes.addActionListener(e -> odemeAl());
        JButton btnIptal = butonOlustur("Sepeti Temizle", new Color(231, 76, 60), Color.WHITE, new Font("Segoe UI", Font.BOLD, 16));
        btnIptal.addActionListener(e -> sepetiTemizle());

        odemePanel.add(lblGenelToplam); odemePanel.add(btnFisiKes); odemePanel.add(btnIptal);

        panel.add(islemPanel, BorderLayout.NORTH); panel.add(new JScrollPane(sepetTablosu), BorderLayout.CENTER); panel.add(odemePanel, BorderLayout.SOUTH);
        return panel;
    }

    // YENİ: Listenin yüklenmesi ve Sepet hesaplamaları double (ondalıklı) tipe uygun hale getirildi.
    public void urunleriEkranaYukle(String aranan) { urunModel.setRowCount(0); for (Object[] urun : dbManager.urunleriGetir(aranan)) urunModel.addRow(urun); }

    private void sepeteEkle() {
        int secilen = urunTablosu.getSelectedRow();
        if (secilen == -1) { JOptionPane.showMessageDialog(this, "Önce listeden ürün seçin!"); return; }
        try {
            int id = (int) urunTablosu.getValueAt(secilen, 0); String ad = (String) urunTablosu.getValueAt(secilen, 1);
            String birim = (String) urunTablosu.getValueAt(secilen, 3);
            double fiyat = (double) urunTablosu.getValueAt(secilen, 4); double stok = (double) urunTablosu.getValueAt(secilen, 5);

            // YENİ: Miktar artık Double parse ediliyor (Örn: 1.5)
            double miktar = Double.parseDouble(miktarKutusu.getText().replace(",", "."));

            if (miktar <= 0 || miktar > stok) { JOptionPane.showMessageDialog(this, "Yetersiz stok veya hatalı miktar!"); return; }

            double araToplam = fiyat * miktar;
            sepetModel.addRow(new Object[]{id, ad, birim, miktar, araToplam});
            urunTablosu.setValueAt(stok - miktar, secilen, 5);
            genelToplam += araToplam;
            lblGenelToplam.setText(String.format("TOPLAM: %.2f TL", genelToplam));
            miktarKutusu.setText("1");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Geçerli bir miktar girin (Ondalık için nokta kullanın)!"); }
    }

    private void odemeAl() {
        if (sepetModel.getRowCount() == 0) return;
        List<Object[]> sepet = new ArrayList<>();
        for (int i = 0; i < sepetModel.getRowCount(); i++) {
            sepet.add(new Object[]{sepetModel.getValueAt(i, 0), sepetModel.getValueAt(i, 1), sepetModel.getValueAt(i, 2), sepetModel.getValueAt(i, 3), sepetModel.getValueAt(i, 4)});
        }
        // Ödeme ekranını çağır
        OdemeEkraniUI odemeEkrani = new OdemeEkraniUI(this, dbManager, genelToplam, sepet);
        odemeEkrani.setVisible(true);
    }

    public void satisBasariylaTamamlandi() { sepetiTemizle(); }
    private void sepetiTemizle() { sepetModel.setRowCount(0); genelToplam = 0.0; lblGenelToplam.setText("TOPLAM: 0.00 TL"); urunleriEkranaYukle(""); }
}