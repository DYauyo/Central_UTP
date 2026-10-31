package modelo;

import java.math.BigDecimal;

public class ReporteCostoDistribucion {

    private String concepto;
    private BigDecimal monto = BigDecimal.ZERO;
    private BigDecimal porcentaje = BigDecimal.ZERO;

    public ReporteCostoDistribucion() {
    }

    public ReporteCostoDistribucion(String concepto, BigDecimal monto, BigDecimal porcentaje) {
        this.concepto = concepto;
        this.monto = monto;
        this.porcentaje = porcentaje;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
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
}
