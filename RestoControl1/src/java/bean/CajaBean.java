package bean;

import dao.PagoDAO;
import dao.PedidoDAO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import modelo.DetallePedido;
import modelo.Pago;
import modelo.Pedido;

@Named("cajaBean")
@ViewScoped
public class CajaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final PagoDAO pagoDAO = new PagoDAO();

    private int idPedido;
    private Pedido pedido;
    private List<DetallePedido> detalles = new ArrayList<>();
    private List<Pedido> pedidosPorCobrar = new ArrayList<>();
    private String mensaje;
    private String tipoMensaje = "success";

    private String tipoDocumento = "boleta";
    private String numeroDocumento;
    private String razonSocial;
    private String medioPago = "efectivo";
    private BigDecimal montoRecibido = BigDecimal.ZERO;

    private int cantidadPartes = 2;
    private List<PagoParcial> pagosParciales = new ArrayList<>();
    private String idsComprobantes;

    @PostConstruct
    public void init() {
        leerParametros();
        cargarPedido();
        cargarPendientes();
        prepararDivisionIgual();
    }

    public void seleccionarDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public void seleccionarMedio(String medioPago) {
        this.medioPago = medioPago;
    }

    public String confirmarPago() {
        if (!validarPedido()) {
            return null;
        }
        if (!validarDocumento(tipoDocumento, numeroDocumento, razonSocial)) {
            return null;
        }
        if ("efectivo".equals(medioPago) && getVuelto().compareTo(BigDecimal.ZERO) < 0) {
            mostrarError("El monto recibido no cubre el total del pedido.");
            return null;
        }

        Pago pago = crearPago(getTotal(), medioPago, tipoDocumento, numeroDocumento, razonSocial, montoRecibido,
                getVuelto(), 1, "Pago unico");
        try {
            List<Integer> idsPagos = pagoDAO.registrarPagos(idPedido, List.of(pago));
            return redirigirTrasPago(idsPagos);
        } catch (RuntimeException e) {
            mostrarError("No se pudo registrar el pago. Verifica que el pedido siga pendiente de cobro.");
            cargarPedido();
            cargarPendientes();
            return null;
        }
    }

    public void prepararDivisionIgual() {
        pagosParciales = new ArrayList<>();
        int partes = Math.max(2, cantidadPartes);
        BigDecimal total = getTotal();
        BigDecimal base = total.divide(BigDecimal.valueOf(partes), 2, RoundingMode.DOWN);
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 1; i <= partes; i++) {
            BigDecimal monto = i == partes ? total.subtract(acumulado) : base;
            acumulado = acumulado.add(monto);
            pagosParciales.add(new PagoParcial(i, "Persona " + i, monto));
        }
    }

    public void agregarPagoParcial() {
        pagosParciales.add(new PagoParcial(pagosParciales.size() + 1, "Persona " + (pagosParciales.size() + 1),
                getRestanteDivision().max(BigDecimal.ZERO)));
    }

    public void quitarPagoParcial(PagoParcial parcial) {
        pagosParciales.remove(parcial);
        renumerarParciales();
    }

    public String confirmarDivision() {
        if (!validarPedido()) {
            return null;
        }
        if (getRestanteDivision().compareTo(BigDecimal.ZERO) != 0) {
            mostrarError("La division debe completar exactamente el total del pedido.");
            return null;
        }

        List<Pago> pagos = new ArrayList<>();
        for (PagoParcial parcial : pagosParciales) {
            if (parcial.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                mostrarError("Cada pago dividido debe tener un monto mayor a cero.");
                return null;
            }
            if (!validarDocumento(parcial.getTipoDocumento(), parcial.getNumeroDocumento(), parcial.getRazonSocial())) {
                return null;
            }
            BigDecimal vueltoParcial = parcial.getVuelto();
            if ("efectivo".equals(parcial.getMedioPago()) && vueltoParcial.compareTo(BigDecimal.ZERO) < 0) {
                mostrarError(parcial.getClientePago() + " no cubre el monto asignado.");
                return null;
            }
            pagos.add(crearPago(parcial.getMonto(), parcial.getMedioPago(), parcial.getTipoDocumento(),
                    parcial.getNumeroDocumento(), parcial.getRazonSocial(), parcial.getMontoRecibido(),
                    vueltoParcial.max(BigDecimal.ZERO), parcial.getNumeroParte(), parcial.getClientePago()));
        }
        try {
            List<Integer> idsPagos = pagoDAO.registrarPagos(idPedido, pagos);
            return redirigirTrasPago(idsPagos);
        } catch (RuntimeException e) {
            mostrarError("No se pudo registrar la division de pago. Verifica los montos y el estado del pedido.");
            cargarPedido();
            cargarPendientes();
            return null;
        }
    }

    public BigDecimal getSubtotal() {
        return getTotal().divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getIgv() {
        return getTotal().subtract(getSubtotal()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return pedido == null || pedido.getTotal() == null ? BigDecimal.ZERO : pedido.getTotal();
    }

    public BigDecimal getVuelto() {
        return valor(montoRecibido).subtract(getTotal()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getVueltoMostrado() {
        BigDecimal vuelto = getVuelto();
        return vuelto.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : vuelto;
    }

    public BigDecimal getTotalDivision() {
        BigDecimal total = BigDecimal.ZERO;
        for (PagoParcial parcial : pagosParciales) {
            total = total.add(valor(parcial.getMonto()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRestanteDivision() {
        return getTotal().subtract(getTotalDivision()).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isTienePedido() {
        return pedido != null;
    }

    public boolean isFactura() {
        return "factura".equals(tipoDocumento);
    }

    public boolean isDivisionCompleta() {
        return getRestanteDivision().compareTo(BigDecimal.ZERO) == 0;
    }

    public String getContextoPrincipal() {
        if (pedido == null) {
            return "-";
        }
        if ("delivery".equals(pedido.getTipoPedido())) {
            return "Delivery";
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return "Para llevar";
        }
        return pedido.getNumeroMesa() > 0 ? "Mesa " + pedido.getNumeroMesa() : "Mesa";
    }

    public String getClienteTexto() {
        if (pedido == null) {
            return "-";
        }
        if (pedido.getCliente() != null && !pedido.getCliente().trim().isEmpty()) {
            return pedido.getCliente();
        }
        return pedido.getComensales() + " comensales";
    }

    public String getIconoContexto() {
        if (pedido == null) {
            return "bi-receipt";
        }
        if ("delivery".equals(pedido.getTipoPedido())) {
            return "bi-scooter";
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return "bi-bag";
        }
        return "bi-table";
    }

    public String etiquetaPedido(Pedido pedido) {
        if (pedido == null) {
            return "-";
        }
        if ("delivery".equals(pedido.getTipoPedido())) {
            return "Delivery";
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return "Para llevar";
        }
        return pedido.getNumeroMesa() > 0 ? "Mesa " + pedido.getNumeroMesa() : "Salon";
    }

    public String clienteResumen(Pedido pedido) {
        if (pedido == null) {
            return "-";
        }
        if (pedido.getCliente() != null && !pedido.getCliente().trim().isEmpty()) {
            return pedido.getCliente();
        }
        int comensales = Math.max(1, pedido.getComensales());
        return comensales == 1 ? "1 comensal" : comensales + " comensales";
    }

    public String estadoCobroTexto(Pedido pedido) {
        if (pedido == null || pedido.getEstadoPedido() == null) {
            return "Pendiente";
        }
        switch (pedido.getEstadoPedido()) {
            case "pendiente":
                return "En toma";
            case "recibido":
                return "En cola";
            case "en preparacion":
                return "En preparacion";
            case "listo":
                return "Listo";
            case "entregado":
                return "Entregado";
            default:
                return pedido.getEstadoPedido();
        }
    }

    public int getCantidadProductos() {
        int total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getCantidad();
        }
        return total;
    }

    public String getTextoBotonPago() {
        if ("tarjeta".equals(medioPago)) {
            return "Procesar en POS y confirmar";
        }
        if ("yape_plin".equals(medioPago)) {
            return "Confirmar Yape / Plin";
        }
        return "Confirmar pago";
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public List<Pedido> getPedidosPorCobrar() {
        return pedidosPorCobrar;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public int getCantidadPartes() {
        return cantidadPartes;
    }

    public void setCantidadPartes(int cantidadPartes) {
        this.cantidadPartes = cantidadPartes;
    }

    public List<PagoParcial> getPagosParciales() {
        return pagosParciales;
    }

    public boolean isMostrarDescargaComprobante() {
        return idsComprobantes != null && !idsComprobantes.trim().isEmpty();
    }

    public String getUrlDescargaComprobante() {
        if (!isMostrarDescargaComprobante()) {
            return "#";
        }
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String base = externalContext.getRequestContextPath();
        if (idsComprobantes.contains(",")) {
            return base + "/ComprobanteServlet?ids=" + idsComprobantes;
        }
        return base + "/ComprobanteServlet?idPago=" + idsComprobantes;
    }

    public String getTextoDescargaComprobante() {
        if (idsComprobantes != null && idsComprobantes.contains(",")) {
            return "Descargar comprobantes";
        }
        return "Descargar comprobante";
    }

    public boolean isComprobanteUnico() {
        return idsComprobantes != null && !idsComprobantes.contains(",");
    }

    public String getUrlVistaComprobante() {
        if (!isComprobanteUnico()) {
            return "";
        }
        return getUrlDescargaComprobante() + "&inline=true";
    }

    private void leerParametros() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        idPedido = convertirEntero(externalContext.getRequestParameterMap().get("idPedido"));
        idsComprobantes = limpiarTexto(externalContext.getRequestParameterMap().get("comprobantes"));
        if ("pago_ok".equals(externalContext.getRequestParameterMap().get("mensaje"))) {
            mostrarExito("Pago registrado correctamente. El pedido quedo cerrado.");
        }
    }

    private String redirigirTrasPago(List<Integer> idsPagos) {
        if (idsPagos == null || idsPagos.isEmpty()) {
            return "/jsf/caja.xhtml?faces-redirect=true&mensaje=pago_ok";
        }
        String comprobantes = idsPagos.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        return "/jsf/caja.xhtml?faces-redirect=true&mensaje=pago_ok&comprobantes=" + comprobantes;
    }

    private void cargarPedido() {
        if (idPedido <= 0) {
            pedido = null;
            detalles = new ArrayList<>();
            return;
        }
        pedido = pedidoDAO.buscarPorId(idPedido);
        detalles = pedido == null ? new ArrayList<>() : pedidoDAO.listarDetalles(idPedido);
        if (pedido != null && !esPedidoCobrable(pedido)) {
            mostrarError("Este pedido aun no esta listo para cobrar.");
            pedido = null;
            idPedido = 0;
            detalles = new ArrayList<>();
            return;
        }
        montoRecibido = getTotal();
    }

    private void cargarPendientes() {
        pedidosPorCobrar = pagoDAO.listarPedidosPorCobrar();
    }

    private boolean validarPedido() {
        if (pedido == null || idPedido <= 0) {
            mostrarError("Selecciona un pedido por cobrar.");
            return false;
        }
        if (detalles.isEmpty() || getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            mostrarError("El pedido no tiene productos para cobrar.");
            return false;
        }
        if ("pagado".equals(pedido.getEstadoPedido())) {
            mostrarError("Este pedido ya fue pagado.");
            return false;
        }
        if (!esPedidoCobrable(pedido)) {
            mostrarError("Este pedido aun no esta listo para cobrar.");
            return false;
        }
        return true;
    }

    private boolean esPedidoCobrable(Pedido pedido) {
        return pedido != null
                && ("listo".equals(pedido.getEstadoPedido()) || "entregado".equals(pedido.getEstadoPedido()));
    }

    private boolean validarDocumento(String tipo, String numero, String razon) {
        if ("factura".equals(tipo)) {
            if (numero == null || numero.trim().length() < 11) {
                mostrarError("Para factura ingresa un RUC valido.");
                return false;
            }
            if (razon == null || razon.trim().isEmpty()) {
                mostrarError("Para factura ingresa la razon social.");
                return false;
            }
        }
        return true;
    }

    private Pago crearPago(BigDecimal monto, String medio, String documento, String numero, String razon,
            BigDecimal recibido, BigDecimal vuelto, int parte, String cliente) {
        Pago pago = new Pago();
        pago.setIdPedido(idPedido);
        pago.setMontoPago(valor(monto));
        pago.setMedioPago(medio);
        pago.setTipoDocumento(documento);
        pago.setNumeroDocumento(numero);
        pago.setRazonSocial(razon);
        pago.setMontoRecibido(valor(recibido));
        pago.setVuelto(valor(vuelto).max(BigDecimal.ZERO));
        pago.setNumeroParte(parte);
        pago.setClientePago(cliente);
        return pago;
    }

    private void renumerarParciales() {
        for (int i = 0; i < pagosParciales.size(); i++) {
            pagosParciales.get(i).setNumeroParte(i + 1);
        }
    }

    private void mostrarExito(String texto) {
        mensaje = texto;
        tipoMensaje = "success";
    }

    private void mostrarError(String texto) {
        mensaje = texto;
        tipoMensaje = "danger";
    }

    private String limpiarTexto(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private int convertirEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : numero.setScale(2, RoundingMode.HALF_UP);
    }

    public static class PagoParcial implements Serializable {

        private static final long serialVersionUID = 1L;

        private int numeroParte;
        private String clientePago;
        private BigDecimal monto;
        private String medioPago = "efectivo";
        private String tipoDocumento = "boleta";
        private String numeroDocumento;
        private String razonSocial;
        private BigDecimal montoRecibido = BigDecimal.ZERO;

        public PagoParcial() {
        }

        public PagoParcial(int numeroParte, String clientePago, BigDecimal monto) {
            this.numeroParte = numeroParte;
            this.clientePago = clientePago;
            this.monto = monto;
            this.montoRecibido = monto;
        }

        public BigDecimal getVuelto() {
            BigDecimal recibido = montoRecibido == null ? BigDecimal.ZERO : montoRecibido;
            BigDecimal asignado = monto == null ? BigDecimal.ZERO : monto;
            return recibido.subtract(asignado).setScale(2, RoundingMode.HALF_UP);
        }

        public int getNumeroParte() {
            return numeroParte;
        }

        public void setNumeroParte(int numeroParte) {
            this.numeroParte = numeroParte;
        }

        public String getClientePago() {
            return clientePago;
        }

        public void setClientePago(String clientePago) {
            this.clientePago = clientePago;
        }

        public BigDecimal getMonto() {
            return monto;
        }

        public void setMonto(BigDecimal monto) {
            this.monto = monto;
        }

        public String getMedioPago() {
            return medioPago;
        }

        public void setMedioPago(String medioPago) {
            this.medioPago = medioPago;
        }

        public String getTipoDocumento() {
            return tipoDocumento;
        }

        public void setTipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
        }

        public String getNumeroDocumento() {
            return numeroDocumento;
        }

        public void setNumeroDocumento(String numeroDocumento) {
            this.numeroDocumento = numeroDocumento;
        }

        public String getRazonSocial() {
            return razonSocial;
        }

        public void setRazonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
        }

        public BigDecimal getMontoRecibido() {
            return montoRecibido;
        }

        public void setMontoRecibido(BigDecimal montoRecibido) {
            this.montoRecibido = montoRecibido;
        }
    }
}
