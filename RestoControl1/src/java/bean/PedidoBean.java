package bean;

import dao.CategoriaDAO;
import dao.PedidoDAO;
import dao.ProductoDAO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import modelo.Categoria;
import modelo.DetallePedido;
import modelo.Pedido;
import modelo.Producto;

@Named("pedidoBean")
@ViewScoped
public class PedidoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private int idPedido;
    private Pedido pedido;
    private List<DetallePedido> detalles = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();
    private String busqueda = "";
    private int categoriaFiltro;
    private String mensaje;
    private String tipoMensaje = "success";

    @PostConstruct
    public void init() {
        productos = productoDAO.listarActivosParaPedido();
        categorias = categoriaDAO.listarActivas();
        leerParametros();
        cargarPedido();
    }

    public void buscar() {
        busqueda = busqueda == null ? "" : busqueda.trim();
    }

    public void limpiarFiltros() {
        busqueda = "";
        categoriaFiltro = 0;
    }

    public void agregarProducto(int idProducto) {
        if (!puedeEditarPedido()) {
            mostrarError("Tu rol puede consultar el pedido, pero no modificarlo.");
            return;
        }
        if (!hayPedidoAbierto()) {
            mostrarError("Primero abre una mesa para tomar el pedido.");
            return;
        }
        try {
            pedidoDAO.agregarDetalle(idPedido, idProducto, 1, "");
            mostrarExito("Producto agregado al pedido.");
        } catch (RuntimeException e) {
            mostrarError("No hay disponibilidad diaria suficiente para este plato.");
        }
        cargarPedido();
        cargarProductosDisponibles();
    }

    public void incrementar(int idDetallePedido, int cantidadActual) {
        if (!puedeEditarPedido()) {
            mostrarError("Tu rol puede consultar el pedido, pero no modificarlo.");
            return;
        }
        try {
            pedidoDAO.actualizarCantidadDetalle(idDetallePedido, cantidadActual + 1);
        } catch (RuntimeException e) {
            mostrarError("No hay disponibilidad diaria suficiente para aumentar la cantidad.");
        }
        cargarPedido();
        cargarProductosDisponibles();
    }

    public void disminuir(int idDetallePedido, int cantidadActual) {
        if (!puedeEditarPedido()) {
            mostrarError("Tu rol puede consultar el pedido, pero no modificarlo.");
            return;
        }
        pedidoDAO.actualizarCantidadDetalle(idDetallePedido, cantidadActual - 1);
        cargarPedido();
        cargarProductosDisponibles();
    }

    public void eliminarDetalle(int idDetallePedido) {
        if (!puedeEditarPedido()) {
            mostrarError("Tu rol puede consultar el pedido, pero no modificarlo.");
            return;
        }
        pedidoDAO.eliminarDetalle(idDetallePedido);
        mostrarExito("Producto retirado del pedido.");
        cargarPedido();
        cargarProductosDisponibles();
    }

    public String enviarCocina() {
        if (!puedeEditarPedido()) {
            mostrarError("Tu rol puede consultar el pedido, pero no enviarlo a cocina.");
            return null;
        }
        if (!hayPedidoAbierto() || detalles.isEmpty()) {
            mostrarError("Agrega al menos un producto antes de enviar a cocina.");
            return null;
        }
        pedidoDAO.actualizarEstadoPedido(idPedido, "recibido");
        return "/jsf/cocina.xhtml?faces-redirect=true";
    }

    public String cancelarPedido() {
        if (!puedeEditarPedido()) {
            mostrarError("Tu rol puede consultar el pedido, pero no cancelarlo.");
            return null;
        }
        if (!hayPedidoAbierto()) {
            mostrarError("No hay pedido abierto para cancelar.");
            return null;
        }
        pedidoDAO.cancelarPedido(idPedido);
        return "/DashboardServlet?mensaje=pedido_cancelado&faces-redirect=true";
    }

    public List<Producto> getProductosFiltrados() {
        List<Producto> filtrados = new ArrayList<>();
        String texto = normalizar(busqueda);
        for (Producto producto : productos) {
            boolean coincideCategoria = categoriaFiltro <= 0 || producto.getIdCategoria() == categoriaFiltro;
            boolean coincideTexto = texto.isEmpty()
                    || normalizar(producto.getNombreProducto()).contains(texto)
                    || normalizar(producto.getNombreCategoria()).contains(texto);
            if (coincideCategoria && coincideTexto) {
                filtrados.add(producto);
            }
        }
        return filtrados;
    }

    public BigDecimal getSubtotal() {
        return pedido == null || pedido.getTotal() == null ? BigDecimal.ZERO : pedido.getTotal();
    }

    public BigDecimal getIgv() {
        return getSubtotal().multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
    }

    public int getCantidadProductos() {
        int total = 0;
        for (DetallePedido detalle : detalles) {
            total += detalle.getCantidad();
        }
        return total;
    }

    public boolean isHayPedidoAbierto() {
        return hayPedidoAbierto();
    }

    public boolean isPuedeEnviarCocina() {
        return puedeEditarPedido() && hayPedidoAbierto() && !detalles.isEmpty()
                && ("pendiente".equals(pedido.getEstadoPedido()) || "recibido".equals(pedido.getEstadoPedido()));
    }

    public boolean isPuedeCobrar() {
        return hayPedidoAbierto() && !detalles.isEmpty()
                && ("listo".equals(pedido.getEstadoPedido()) || "entregado".equals(pedido.getEstadoPedido()));
    }

    public boolean isPuedeEditarPedido() {
        return puedeEditarPedido();
    }

    public String getEstadoTexto() {
        if (pedido == null || pedido.getEstadoPedido() == null) {
            return "Sin abrir";
        }
        switch (pedido.getEstadoPedido()) {
            case "pendiente":
                return "Armando pedido";
            case "recibido":
                return "En cola de cocina";
            case "en preparacion":
                return "En preparacion";
            case "listo":
                return "Listo para entregar";
            case "entregado":
                return "Entregado";
            case "pagado":
                return "Pagado";
            case "cancelado":
                return "Cancelado";
            default:
                return pedido.getEstadoPedido();
        }
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

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda;
    }

    public int getCategoriaFiltro() {
        return categoriaFiltro;
    }

    public void setCategoriaFiltro(int categoriaFiltro) {
        this.categoriaFiltro = categoriaFiltro;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    private void leerParametros() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String idPedidoParam = externalContext.getRequestParameterMap().get("idPedido");
        String mensajeParam = externalContext.getRequestParameterMap().get("mensaje");
        idPedido = convertirEntero(idPedidoParam);
        if ("mesa_abierta".equals(mensajeParam)) {
            mostrarExito("Mesa abierta correctamente. Ya puedes tomar el pedido.");
        } else if ("delivery_abierto".equals(mensajeParam)) {
            mostrarExito("Delivery abierto correctamente. Ya puedes tomar el pedido.");
        } else if ("llevar_abierto".equals(mensajeParam)) {
            mostrarExito("Pedido para llevar abierto correctamente.");
        } else if ("mesas_unidas".equals(mensajeParam)) {
            mostrarExito("Mesas unidas correctamente. Ya puedes tomar un solo pedido para el grupo.");
        }
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
        return pedido.getNumeroMesa() > 0 ? "Mesa " + pedido.getNumeroMesa() : "Sin mesa";
    }

    public String getContextoDetalle() {
        if (pedido == null) {
            return "Selecciona una mesa o inicia un pedido rapido";
        }
        if ("delivery".equals(pedido.getTipoPedido())) {
            return textoConFallback(pedido.getCliente(), "Cliente delivery");
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return textoConFallback(pedido.getCliente(), "Cliente para llevar");
        }
        return pedido.getComensales() + " comensales";
    }

    public String getIconoContexto() {
        if (pedido == null) {
            return "bi-table";
        }
        if ("delivery".equals(pedido.getTipoPedido())) {
            return "bi-scooter";
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return "bi-bag";
        }
        return "bi-table";
    }

    public String getTelefonoTexto() {
        return pedido == null || pedido.getTelefono() == null || pedido.getTelefono().trim().isEmpty()
                ? "-"
                : pedido.getTelefono();
    }

    public String getEntregaTexto() {
        if (pedido == null) {
            return "-";
        }
        if ("delivery".equals(pedido.getTipoPedido())) {
            return textoConFallback(pedido.getDireccionEntrega(), "Sin direccion");
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return textoConFallback(pedido.getHoraRecojo(), "Recojo por definir");
        }
        return pedido.getTiempoAbierto();
    }

    private void cargarPedido() {
        if (idPedido <= 0) {
            pedido = null;
            detalles = new ArrayList<>();
            return;
        }
        pedido = pedidoDAO.buscarPorId(idPedido);
        detalles = pedido == null ? new ArrayList<>() : pedidoDAO.listarDetalles(idPedido);
    }

    private void cargarProductosDisponibles() {
        productos = productoDAO.listarActivosParaPedido();
    }

    private boolean hayPedidoAbierto() {
        return pedido != null && idPedido > 0;
    }

    private boolean puedeEditarPedido() {
        int idRol = obtenerIdRolSesion();
        return idRol == 1 || idRol == 3;
    }

    private int obtenerIdRolSesion() {
        Object idRol = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("idRol");
        if (idRol instanceof Integer) {
            return (Integer) idRol;
        }
        try {
            return Integer.parseInt(String.valueOf(idRol));
        } catch (NumberFormatException e) {
            return 0;
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

    private int convertirEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    private String textoConFallback(String valor, String fallback) {
        return valor == null || valor.trim().isEmpty() ? fallback : valor;
    }
}
