package models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProcesadorTemperaturas {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<RegistroTemperatura> cargarDesdeCSV(Path rutaArchivo) throws IOException {
        List<String> lineas = Files.readAllLines(rutaArchivo);
        List<RegistroTemperatura> registros = new ArrayList<>();

        for (int i = 1; i < lineas.size(); i++) { // saltar cabecera
            String[] partes = lineas.get(i).split(",");
            if (partes.length == 3) {
                String ciudad = partes[0].trim();
                LocalDate fecha = LocalDate.parse(partes[1].trim(), FORMATO);
                double temperatura = Double.parseDouble(partes[2].trim());
                registros.add(new RegistroTemperatura(ciudad, fecha, temperatura));
            }
        }
        return registros;
    }

    public List<LocalDate> fechasUnicasOrdenadas(List<RegistroTemperatura> registros) {
        return registros.stream()
                .map(RegistroTemperatura::getFecha)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Optional<LocalDate> fechaMinima(List<RegistroTemperatura> registros) {
        return registros.stream().map(RegistroTemperatura::getFecha).min(LocalDate::compareTo);
    }

    public Optional<LocalDate> fechaMaxima(List<RegistroTemperatura> registros) {
        return registros.stream().map(RegistroTemperatura::getFecha).max(LocalDate::compareTo);
    }

    public List<RegistroTemperatura> filtrarPorRango(List<RegistroTemperatura> registros, LocalDate desde, LocalDate hasta) {
        return registros.stream()
                .filter(r -> !r.getFecha().isBefore(desde) && !r.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
    }

    public Map<String, Double> promedioPorCiudad(List<RegistroTemperatura> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(
                        RegistroTemperatura::getCiudad,
                        Collectors.averagingDouble(RegistroTemperatura::getTemperatura)
                ));
    }

    public Optional<Map.Entry<String, Double>> ciudadMasCalurosa(Map<String, Double> promedios) {
        return promedios.entrySet().stream().max(Map.Entry.comparingByValue());
    }

    public Optional<Map.Entry<String, Double>> ciudadMenosCalurosa(Map<String, Double> promedios) {
        return promedios.entrySet().stream().min(Map.Entry.comparingByValue());
    }

    public static String formatear(LocalDate fecha) {
        return (fecha == null) ? "(sin fecha)" : fecha.format(FORMATO);
    }

    public static LocalDate parsear(String texto) {
        return LocalDate.parse(texto, FORMATO);
    }
}