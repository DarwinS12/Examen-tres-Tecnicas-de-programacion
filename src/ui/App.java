package ui;

public class App {
    public static void main(String[] args) {
        // Iniciar la interfaz principal en el hilo de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            new AplicacionTemperaturas().setVisible(true);
        });
    }
}
