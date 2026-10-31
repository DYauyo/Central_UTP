package modelo;

import java.math.BigDecimal;

public class ReporteHoraPunta {

    private int hora;
    private BigDecimal monto = BigDecimal.ZERO;

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getEtiqueta() {
        return String.format("%02d:00", hora);
    }
}
