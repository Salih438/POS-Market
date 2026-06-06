import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Swing arayüzünü güvenli bir iş parçacığında (thread) başlat
        SwingUtilities.invokeLater(() -> {
            ModernPOS posEkrani = new ModernPOS();
            posEkrani.setVisible(true);
            posEkrani.setDefaultCloseOperation(ModernPOS.EXIT_ON_CLOSE);
        });
    }
}