package models;

import java.time.LocalDate;

public class RegistroTemperatura {

    private final String ciudad;
    private final LocalDate fecha;
    private final double temperatura;

    public RegistroTemperatura(String ciudad, LocalDate fecha, double temperatura) {
        this.ciudad = ciudad;
        this.fecha = fecha;
        this.temperatura = temperatura;
    }

    public String getCiudad() {
        return ciudad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getTemperatura() {
        return temperatura;
    }

    @Override
    public String toString() {
        return "RegistroTemperatura{"
                + "ciudad='" + ciudad + '\''
                + ", fecha=" + fecha
                + ", temperatura=" + temperatura
                + '}';
    }
}