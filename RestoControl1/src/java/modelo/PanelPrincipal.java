package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PanelPrincipal {

    private int pedidosDia;
    private BigDecimal ventasDia = BigDecimal.ZERO;
    private int pedidosPendientes;
    private int insumosStockBajo;
    private int productosTopVendidos;
    private int insumosPorVencer;
    private List<PanelPedidoReciente> pedidosRecientes = new ArrayList<>();
    private List<PanelAlertaInsumo> alertasInventario = new ArrayList<>();
    private List<PanelVentaSemanal> ventasSemanales = new ArrayList<>();
    private List<PanelActividad> actividadReciente = new ArrayList<>();

    public int getPedidosDia() {
        return pedidosDia;
    }

    public void setPedidosDia(int pedidosDia) {
        this.pedidosDia = pedidosDia;
    }

    public BigDecimal getVentasDia() {
        return ventasDia;
    }

    public void setVentasDia(BigDecimal ventasDia) {
        this.ventasDia = ventasDia;
    }

    public int getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(int pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public int getInsumosStockBajo() {
        return insumosStockBajo;
    }

    public void setInsumosStockBajo(int insumosStockBajo) {
        this.insumosStockBajo = insumosStockBajo;
    }

    public int getProductosTopVendidos() {
        return productosTopVendidos;
    }

    public void setProductosTopVendidos(int productosTopVendidos) {
        this.productosTopVendidos = productosTopVendidos;
    }

    public int getInsumosPorVencer() {
        return insumosPorVencer;
    }

    public void setInsumosPorVencer(int insumosPorVencer) {
        this.insumosPorVencer = insumosPorVencer;
    }

    public List<PanelPedidoReciente> getPedidosRecientes() {
        return pedidosRecientes;
    }

    public void setPedidosRecientes(List<PanelPedidoReciente> pedidosRecientes) {
        this.pedidosRecientes = pedidosRecientes;
    }

    public List<PanelAlertaInsumo> getAlertasInventario() {
        return alertasInventario;
    }

    public void setAlertasInventario(List<PanelAlertaInsumo> alertasInventario) {
        this.alertasInventario = alertasInventario;
    }

    public List<PanelVentaSemanal> getVentasSemanales() {
        return ventasSemanales;
    }

    public void setVentasSemanales(List<PanelVentaSemanal> ventasSemanales) {
        this.ventasSemanales = ventasSemanales;
    }

    public List<PanelActividad> getActividadReciente() {
        return actividadReciente;
    }

    public void setActividadReciente(List<PanelActividad> actividadReciente) {
        this.actividadReciente = actividadReciente;
    }
}
