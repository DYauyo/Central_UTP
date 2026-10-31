package modelo;

import java.math.BigDecimal;

public class ReporteMetodoPago {

    private String medioPago;
    private BigDecimal monto = BigDecimal.ZERO;
    private BigDecimal porcentaje = BigDecimal.ZERO;

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getEtiqueta() {
        if ("tarjeta".equalsIgnoreCase(medioPago)) {
            return "Tarjeta";
        }
        if ("yape_plin".equalsIgnoreCase(medioPago)) {
            return "Yape / Plin";
        }
        return "Efectivo";
    }
}
