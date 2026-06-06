import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class OdemeEkraniUI extends JDialog {

    private double genelToplam;
    private List<Object[]> sepet;
    private DatabaseManager dbManager;
    private ModernPOS anaEkran;

    private JLabel lblToplamTutar, lblParaUstu;
    private JTextField txtAlinanNakit;

    public OdemeEkraniUI(ModernPOS parent, DatabaseManager dbManager, double genelToplam, List<Object[]> sepet) {
        super(parent, "Ödeme İşlemi", true);
        this.anaEkran = parent;
        this.dbManager = dbManager;
        this.genelToplam = genelToplam;
        this.sepet = sepet;

        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(236, 240, 241));

        add(solBilgiPaneliOlustur(), BorderLayout.CENTER);
        add(sagNumpadOlustur(), BorderLayout.EAST);
        add(altButonPaneliOlustur(), BorderLayout.SOUTH);

        // YENİ: Klavyeyi Dinleme Özelliğini Aktifleştir
        klavyeKisayollariniAyarla();
    }

    // YENİ: FİZİKSEL KLAVYE BAĞLANTILARI
    private void klavyeKisayollariniAyarla() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        // 0-9 Sayılarını Hem Normal Klavyeden Hem NumPad'den Dinle
        for (int i = 0; i <= 9; i++) {
            String rakam = String.valueOf(i);
            // Normal klavye rakamları
            im.put(KeyStroke.getKeyStroke(rakam), "num_" + i);
            // Numpad rakamları
            im.put(KeyStroke.getKeyStroke("NUMPAD" + i), "num_" + i);

            am.put("num_" + i, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    numpadYaz(rakam);
                }
            });
        }

        // Enter Tuşu (Satışı Tamamla)
        im.put(KeyStroke.getKeyStroke("ENTER"), "enter");
        am.put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { satisiBitir(); }
        });

        // Backspace Tuşu (Silme İşlemi - Sıfırla)
        im.put(KeyStroke.getKeyStroke("BACK_SPACE"), "backspace");
        im.put(KeyStroke.getKeyStroke("DELETE"), "backspace");
        am.put("backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { nakitAyarla(""); }
        });

        // Escape Tuşu (İptal Et - Ekranı Kapat)
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        am.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    // ... (solBilgiPaneliOlustur, panelOlustur, sagNumpadOlustur, altButonPaneliOlustur metotları tamamen aynı kalacak) ...
    private JPanel solBilgiPaneliOlustur() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10)); panel.setBorder(new EmptyBorder(20, 20, 10, 10)); panel.setOpaque(false);
        JPanel pnlToplam = panelOlustur("ÖDENECEK TUTAR", String.format("%.2f TL", genelToplam), new Color(192, 57, 43)); lblToplamTutar = (JLabel) pnlToplam.getComponent(1);
        JPanel pnlAlinan = new JPanel(new BorderLayout()); pnlAlinan.setBackground(Color.WHITE); pnlAlinan.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        JLabel lblAlinanBaslik = new JLabel(" ALINAN NAKİT:", SwingConstants.LEFT); lblAlinanBaslik.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblAlinanBaslik.setForeground(new Color(127, 140, 141));
        txtAlinanNakit = new JTextField(""); txtAlinanNakit.setFont(new Font("Segoe UI", Font.BOLD, 36)); txtAlinanNakit.setHorizontalAlignment(JTextField.RIGHT); txtAlinanNakit.setBorder(null); txtAlinanNakit.setEditable(false);
        pnlAlinan.add(lblAlinanBaslik, BorderLayout.NORTH); pnlAlinan.add(txtAlinanNakit, BorderLayout.CENTER);
        JPanel pnlParaUstu = panelOlustur("PARA ÜSTÜ", "0.00 TL", new Color(39, 174, 96)); lblParaUstu = (JLabel) pnlParaUstu.getComponent(1);
        panel.add(pnlToplam); panel.add(pnlAlinan); panel.add(pnlParaUstu);
        return panel;
    }
    private JPanel panelOlustur(String baslik, String deger, Color degerRengi) {
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setBackground(Color.WHITE); pnl.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        JLabel lblBaslik = new JLabel(" " + baslik + ":", SwingConstants.LEFT); lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblBaslik.setForeground(new Color(127, 140, 141));
        JLabel lblDeger = new JLabel(deger + "  ", SwingConstants.RIGHT); lblDeger.setFont(new Font("Segoe UI", Font.BOLD, 36)); lblDeger.setForeground(degerRengi);
        pnl.add(lblBaslik, BorderLayout.NORTH); pnl.add(lblDeger, BorderLayout.CENTER); return pnl;
    }
    private JPanel sagNumpadOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(new EmptyBorder(20, 10, 10, 20)); panel.setOpaque(false);
        JPanel pnlHizli = new JPanel(new GridLayout(1, 4, 5, 5)); pnlHizli.setOpaque(false);
        int[] hizliDegerler = {50, 100, 200};
        for (int val : hizliDegerler) { JButton btn = butonOlustur(val + " TL", new Color(52, 152, 219), Color.WHITE); btn.addActionListener(e -> nakitEkle(String.valueOf(val))); pnlHizli.add(btn); }
        JButton btnTamGirdi = butonOlustur("TAM", new Color(243, 156, 18), Color.WHITE); btnTamGirdi.addActionListener(e -> nakitAyarla(String.valueOf(genelToplam))); pnlHizli.add(btnTamGirdi);
        JPanel pnlSayilar = new JPanel(new GridLayout(4, 3, 5, 5)); pnlSayilar.setOpaque(false);
        String[] tuslar = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0", "."};
        for (String tus : tuslar) { JButton btn = butonOlustur(tus, new Color(44, 62, 80), Color.WHITE); btn.setFont(new Font("Segoe UI", Font.BOLD, 24));
            if (tus.equals("C")) { btn.setBackground(new Color(231, 76, 60)); btn.addActionListener(e -> nakitAyarla("")); } else { btn.addActionListener(e -> numpadYaz(tus)); }
            pnlSayilar.add(btn); }
        panel.add(pnlHizli, BorderLayout.NORTH); panel.add(pnlSayilar, BorderLayout.CENTER); return panel;
    }
    private JPanel altButonPaneliOlustur() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10)); panel.setOpaque(false);
        JButton btnIptal = butonOlustur("İPTAL ET (ESC)", new Color(149, 165, 166), Color.WHITE); btnIptal.setPreferredSize(new Dimension(150, 50)); btnIptal.addActionListener(e -> dispose());
        JButton btnTamamla = butonOlustur("SATIŞI TAMAMLA (ENTER)", new Color(39, 174, 96), Color.WHITE); btnTamamla.setPreferredSize(new Dimension(280, 50)); btnTamamla.addActionListener(e -> satisiBitir());
        panel.add(btnIptal); panel.add(btnTamamla); return panel;
    }
    private JButton butonOlustur(String metin, Color arkaPlan, Color yaziRengi) {
        JButton btn = new JButton(metin); btn.setBackground(arkaPlan); btn.setForeground(yaziRengi); btn.setFont(new Font("Segoe UI", Font.BOLD, 16)); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    // --- HESAPLAMA MANTIKLARI ---
    private void numpadYaz(String deger) { txtAlinanNakit.setText(txtAlinanNakit.getText() + deger); paraUstuHesapla(); }
    private void nakitEkle(String artis) { try { double mevcut = txtAlinanNakit.getText().isEmpty() ? 0 : Double.parseDouble(txtAlinanNakit.getText()); double eklenecek = Double.parseDouble(artis); txtAlinanNakit.setText(String.valueOf(mevcut + eklenecek)); paraUstuHesapla(); } catch (Exception ignored) {} }
    private void nakitAyarla(String miktar) { txtAlinanNakit.setText(miktar); paraUstuHesapla(); }

    private void paraUstuHesapla() {
        try {
            if (txtAlinanNakit.getText().isEmpty()) { lblParaUstu.setText("0.00 TL"); return; }
            double alinan = Double.parseDouble(txtAlinanNakit.getText());
            double paraUstu = alinan - genelToplam;
            if (paraUstu < 0) { lblParaUstu.setText("EKSİK TUTAR!"); lblParaUstu.setForeground(new Color(231, 76, 60)); }
            else { lblParaUstu.setText(String.format("%.2f TL", paraUstu)); lblParaUstu.setForeground(new Color(39, 174, 96)); }
        } catch (Exception e) { lblParaUstu.setText("HATA"); }
    }

    private void satisiBitir() {
        try {
            double alinan = txtAlinanNakit.getText().isEmpty() ? 0 : Double.parseDouble(txtAlinanNakit.getText());
            if (alinan < genelToplam) { JOptionPane.showMessageDialog(this, "Eksik ödeme! İşlem tamamlanamaz.", "Hata", JOptionPane.ERROR_MESSAGE); return; }
            if (dbManager.satisiTamamla(genelToplam, sepet)) {
                JOptionPane.showMessageDialog(this, "Fiş Başarıyla Kesildi!\n\nPara Üstü: " + String.format("%.2f TL", (alinan - genelToplam)));
                anaEkran.satisBasariylaTamamlandi(); dispose();
            } else { JOptionPane.showMessageDialog(this, "Fatura veritabanına kaydedilirken hata oluştu!", "Kritik Hata", JOptionPane.ERROR_MESSAGE); }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lütfen geçerli bir tutar girin."); }
    }
}