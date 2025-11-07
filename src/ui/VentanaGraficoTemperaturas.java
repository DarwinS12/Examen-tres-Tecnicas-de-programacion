package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class VentanaGraficoTemperaturas extends JFrame {

    public VentanaGraficoTemperaturas(Map<String, Double> promedios, String rangoFechas) {
        setTitle("Gráfica de Temperaturas Promedio por Ciudad");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (var entry : promedios.entrySet()) {
            dataset.addValue(entry.getValue(), "Temperatura (°C)", entry.getKey());
        }

        JFreeChart grafico = ChartFactory.createBarChart(
                "Promedio de Temperaturas por Ciudad\n(" + rangoFechas + ")",
                "Ciudad",
                "Temperatura (°C)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        grafico.setBackgroundPaint(Color.white);
        grafico.getCategoryPlot().setBackgroundPaint(new Color(240, 240, 255));
        grafico.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(0, 102, 204));

        ChartPanel panelGrafico = new ChartPanel(grafico);
        JScrollPane scroll = new JScrollPane(panelGrafico);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scroll, BorderLayout.CENTER);
    }
}
