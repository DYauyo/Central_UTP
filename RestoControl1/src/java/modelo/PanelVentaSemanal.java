package modelo;

import java.math.BigDecimal;

public class PanelVentaSemanal {

    private String dia;
    private BigDecimal monto = BigDecimal.ZERO;

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
