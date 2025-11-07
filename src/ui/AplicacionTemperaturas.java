package ui;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import models.ProcesadorTemperaturas;
import models.RegistroTemperatura;

public class AplicacionTemperaturas extends JFrame {

    private final JButton btnCargar = new JButton("Cargar CSV");
    private final JButton btnProcesar = new JButton("Procesar");
    private final JLabel lblRango = new JLabel("Rango disponible: (sin datos)");
    private final JComboBox<String> cmbDesde = new JComboBox<>();
    private final JComboBox<String> cmbHasta = new JComboBox<>();

    private final ProcesadorTemperaturas procesador = new ProcesadorTemperaturas();
    private List<RegistroTemperatura> registros = new ArrayList<>();

    public AplicacionTemperaturas() {
        super("Aplicación de Temperaturas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 250);
        setLocationRelativeTo(null);

        setContentPane(crearContenido());
        habilitarControles(false);

        btnCargar.addActionListener(e -> cargarCSV());
        btnProcesar.addActionListener(e -> procesar());
    }

    private JPanel crearContenido() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel arriba = new JPanel(new GridLayout(3, 1, 8, 8));
        arriba.add(btnCargar);
        arriba.add(lblRango);

        JPanel rango = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rango.add(new JLabel("Desde:"));
        rango.add(cmbDesde);
        rango.add(new JLabel("Hasta:"));
        rango.add(cmbHasta);
        arriba.add(rango);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        abajo.add(btnProcesar);

        root.add(arriba, BorderLayout.CENTER);
        root.add(abajo, BorderLayout.SOUTH);
        return root;
    }

    private void cargarCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar archivo CSV");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));
        fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = fc.getSelectedFile();
        try {
            registros = procesador.cargarDesdeCSV(Path.of(archivo.getAbsolutePath()));
            if (registros.isEmpty()) {
                mostrar("El CSV no contiene datos.", "Aviso", JOptionPane.WARNING_MESSAGE);
                dejarSinDatos();
                return;
            }

            var fmin = procesador.fechaMinima(registros).orElse(null);
            var fmax = procesador.fechaMaxima(registros).orElse(null);
            lblRango.setText(String.format("Rango disponible: %s a %s",
                    ProcesadorTemperaturas.formatear(fmin),
                    ProcesadorTemperaturas.formatear(fmax)));

            Set<String> fechas = procesador.fechasUnicasOrdenadas(registros).stream()
                    .map(ProcesadorTemperaturas::formatear)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

            cmbDesde.removeAllItems();
            cmbHasta.removeAllItems();
            fechas.forEach(f -> {
                cmbDesde.addItem(f);
                cmbHasta.addItem(f);
            });

            if (cmbDesde.getItemCount() > 0) {
                cmbDesde.setSelectedIndex(0);
                cmbHasta.setSelectedIndex(cmbHasta.getItemCount() - 1);
            }

            habilitarControles(true);

            System.out.println("CSV cargado correctamente. Registros: " + registros.size());
            System.out.println("Rango disponible detectado: "
                    + ProcesadorTemperaturas.formatear(fmin) + " a "
                    + ProcesadorTemperaturas.formatear(fmax));

        } catch (IOException ex) {
            mostrar("Error leyendo el CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dejarSinDatos();
        } catch (RuntimeException ex) {
            mostrar("CSV inválido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dejarSinDatos();
        }
    }

    private void procesar() {
        if (registros.isEmpty()) {
            mostrar("Primero cargue un CSV.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String desdeTxt = (String) cmbDesde.getSelectedItem();
        String hastaTxt = (String) cmbHasta.getSelectedItem();

        LocalDate desde = ProcesadorTemperaturas.parsear(desdeTxt);
        LocalDate hasta = ProcesadorTemperaturas.parsear(hastaTxt);

        var filtrados = procesador.filtrarPorRango(registros, desde, hasta);

        if (filtrados.isEmpty()) {
            mostrar("No hay datos en el rango seleccionado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        var promedios = procesador.promedioPorCiudad(filtrados);

        var masCalurosa = procesador.ciudadMasCalurosa(promedios).orElse(null);
        var menosCalurosa = procesador.ciudadMenosCalurosa(promedios).orElse(null);

        // Mostrar la ventana del gráfico con el rango seleccionado
     String rangoTexto = ProcesadorTemperaturas.formatear(desde) + " a " + ProcesadorTemperaturas.formatear(hasta);
     SwingUtilities.invokeLater(() -> new VentanaGraficoTemperaturas(promedios, rangoTexto).setVisible(true));


        StringBuilder sb = new StringBuilder("Promedios por ciudad:\n");
        promedios.forEach((ciudad, promedio) ->
                sb.append(String.format("- %s: %.2f°C%n", ciudad, promedio)));

        if (masCalurosa != null && menosCalurosa != null) {
            sb.append("\nCiudad más calurosa: ")
                    .append(masCalurosa.getKey())
                    .append(" (")
                    .append(String.format("%.2f°C", masCalurosa.getValue()))
                    .append(")\nCiudad menos calurosa: ")
                    .append(menosCalurosa.getKey())
                    .append(" (")
                    .append(String.format("%.2f°C", menosCalurosa.getValue()))
                    .append(")");
        }

        mostrar(sb.toString(), "Resultados", JOptionPane.INFORMATION_MESSAGE);
    }

    private void habilitarControles(boolean on) {
        cmbDesde.setEnabled(on);
        cmbHasta.setEnabled(on);
        btnProcesar.setEnabled(on);
    }

    private void dejarSinDatos() {
        lblRango.setText("Rango disponible: (sin datos)");
        cmbDesde.removeAllItems();
        cmbHasta.removeAllItems();
        habilitarControles(false);
    }

    private void mostrar(String msg, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, msg, titulo, tipo);
    }

    public static void main(String[] args) {
        new AplicacionTemperaturas().setVisible(true);
    }
}