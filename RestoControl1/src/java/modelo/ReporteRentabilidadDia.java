package modelo;

import java.math.BigDecimal;

public class ReporteRentabilidadDia {

    private String fecha;
    private BigDecimal ventasNetas = BigDecimal.ZERO;
    private BigDecimal costoVentas = BigDecimal.ZERO;
    private BigDecimal gastosOperativos = BigDecimal.ZERO;
    private BigDecimal gastosPersonal = BigDecimal.ZERO;
    private BigDecimal otrosGastos = BigDecimal.ZERO;
    private BigDecimal utilidadNeta = BigDecimal.ZERO;
    private BigDecimal margenNeto = BigDecimal.ZERO;

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getVentasNetas() {
        return ventasNetas;
    }

    public void setVentasNetas(BigDecimal ventasNetas) {
        this.ventasNetas = ventasNetas;
    }

    public BigDecimal getCostoVentas() {
        return costoVentas;
    }

    public void setCostoVentas(BigDecimal costoVentas) {
        this.costoVentas = costoVentas;
    }

    public BigDecimal getGastosOperativos() {
        return gastosOperativos;
    }

    public void setGastosOperativos(BigDecimal gastosOperativos) {
        this.gastosOperativos = gastosOperativos;
    }

    public BigDecimal getGastosPersonal() {
        return gastosPersonal;
    }

    public void setGastosPersonal(BigDecimal gastosPersonal) {
        this.gastosPersonal = gastosPersonal;
    }

    public BigDecimal getOtrosGastos() {
        return otrosGastos;
    }

    public void setOtrosGastos(BigDecimal otrosGastos) {
        this.otrosGastos = otrosGastos;
    }

    public BigDecimal getUtilidadNeta() {
        return utilidadNeta;
    }

    public void setUtilidadNeta(BigDecimal utilidadNeta) {
        this.utilidadNeta = utilidadNeta;
    }

    public BigDecimal getMargenNeto() {
        return margenNeto;
    }

    public void setMargenNeto(BigDecimal margenNeto) {
        this.margenNeto = margenNeto;
    }
}
