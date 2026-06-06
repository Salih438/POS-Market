import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:salih.db";

    public DatabaseManager() {
        tabloYoksaOlustur();
    }
// --- GEÇMİŞ FATURALAR İÇİN EKSİK OLAN METOTLAR ---

    public List<Object[]> faturalariGetir() {
        List<Object[]> liste = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(DB_URL);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM invoices ORDER BY id DESC")) {
            while (rs.next()) {
                liste.add(new Object[]{rs.getInt("id"), rs.getString("date"), rs.getDouble("total_amount")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }

    public List<Object[]> faturaDetayiGetir(int invoiceId) {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ?";
        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Dikkat: quantity artık REAL (Ondalıklı) olduğu için getDouble kullanıyoruz
                liste.add(new Object[]{
                        rs.getString("product_name"),
                        rs.getDouble("quantity"),
                        rs.getDouble("price"),
                        rs.getDouble("subtotal")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }
    private void tabloYoksaOlustur() {
        try (Connection con = DriverManager.getConnection(DB_URL);
             Statement st = con.createStatement()) {
            // YENİ: category (Kategori) ve unit_type (Birim) eklendi. stock artık REAL (Ondalıklı)
            st.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, unit_type TEXT, price REAL, stock REAL)");
            st.execute("CREATE TABLE IF NOT EXISTS invoices (id INTEGER PRIMARY KEY AUTOINCREMENT, total_amount REAL, date DATETIME DEFAULT (DATETIME('now', 'localtime')))");
            // YENİ: quantity artık REAL (1.5 kg satılabilmesi için)
            st.execute("CREATE TABLE IF NOT EXISTS invoice_items (id INTEGER PRIMARY KEY AUTOINCREMENT, invoice_id INTEGER, product_name TEXT, quantity REAL, price REAL, subtotal REAL)");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Object[]> urunleriGetir(String aranan) {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR category LIKE ?";
        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + aranan + "%");
            ps.setString(2, "%" + aranan + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new Object[]{
                        rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                        rs.getString("unit_type"), rs.getDouble("price"), rs.getDouble("stock")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }

    public boolean urunEkle(String ad, String kategori, String birim, double fiyat, double stok) {
        String sql = "INSERT INTO products (name, category, unit_type, price, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ad); ps.setString(2, kategori); ps.setString(3, birim);
            ps.setDouble(4, fiyat); ps.setDouble(5, stok);
            ps.executeUpdate(); return true;
        } catch (Exception e) { return false; }
    }

    public boolean urunGuncelle(int id, String ad, String kategori, String birim, double fiyat, double stok) {
        String sql = "UPDATE products SET name = ?, category = ?, unit_type = ?, price = ?, stock = ? WHERE id = ?";
        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ad); ps.setString(2, kategori); ps.setString(3, birim);
            ps.setDouble(4, fiyat); ps.setDouble(5, stok); ps.setInt(6, id);
            ps.executeUpdate(); return true;
        } catch (Exception e) { return false; }
    }

    public boolean urunSil(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate(); return true;
        } catch (Exception e) { return false; }
    }

    public boolean satisiTamamla(double genelToplam, List<Object[]> sepetUrunleri) {
        String sqlInvoice = "INSERT INTO invoices (total_amount) VALUES (?)";
        String sqlItem = "INSERT INTO invoice_items (invoice_id, product_name, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE products SET stock = stock - ? WHERE id = ?";

        try (Connection con = DriverManager.getConnection(DB_URL)) {
            con.setAutoCommit(false);
            try (PreparedStatement psInv = con.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                psInv.setDouble(1, genelToplam); psInv.executeUpdate();
                ResultSet rs = psInv.getGeneratedKeys(); int invoiceId = rs.getInt(1);

                try (PreparedStatement psItem = con.prepareStatement(sqlItem);
                     PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                    for (Object[] urun : sepetUrunleri) {
                        int id = (int) urun[0]; String ad = (String) urun[1];
                        double miktar = (double) urun[3]; double araToplam = (double) urun[4];
                        double birimFiyat = araToplam / miktar;

                        psItem.setInt(1, invoiceId); psItem.setString(2, ad);
                        psItem.setDouble(3, miktar); psItem.setDouble(4, birimFiyat); psItem.setDouble(5, araToplam);
                        psItem.executeUpdate();

                        psStock.setDouble(1, miktar); psStock.setInt(2, id);
                        psStock.executeUpdate();
                    }
                }
                con.commit(); return true;
            } catch (Exception e) { con.rollback(); return false; }
        } catch (Exception e) { return false; }

    }
}
