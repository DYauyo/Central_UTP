package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InsumoRequerido {

    private int idProducto;
    private int idInsumo;
    private String nombreInsumo;
    private String unidadMedida;
    private BigDecimal cantidadPorPorcion = BigDecimal.ZERO;
    private BigDecimal cantidadRequerida = BigDecimal.ZERO;
    private BigDecimal stockActual = BigDecimal.ZERO;
    private BigDecimal stockMinimo = BigDecimal.ZERO;
    private BigDecimal diferencia = BigDecimal.ZERO;
    private String estado = "suficiente";

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(int idInsumo) {
        this.idInsumo = idInsumo;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public void setNombreInsumo(String nombreInsumo) {
        this.nombreInsumo = nombreInsumo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getCantidadPorPorcion() {
        return cantidadPorPorcion;
    }

    public void setCantidadPorPorcion(BigDecimal cantidadPorPorcion) {
        this.cantidadPorPorcion = normalizar(cantidadPorPorcion);
    }

    public BigDecimal getCantidadRequerida() {
        return cantidadRequerida;
    }

    public void setCantidadRequerida(BigDecimal cantidadRequerida) {
        this.cantidadRequerida = normalizar(cantidadRequerida);
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = normalizar(stockActual);
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = normalizar(stockMinimo);
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = normalizar(diferencia);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void calcularParaPorciones(int porciones) {
        cantidadRequerida = cantidadPorPorcion.multiply(BigDecimal.valueOf(Math.max(0, porciones)))
                .setScale(2, RoundingMode.HALF_UP);
        diferencia = stockActual.subtract(cantidadRequerida).setScale(2, RoundingMode.HALF_UP);

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            estado = "critico";
        } else if (diferencia.compareTo(stockMinimo) <= 0) {
            estado = "bajo";
        } else {
            estado = "suficiente";
        }
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
