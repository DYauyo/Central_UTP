package bean;

import dao.PedidoDAO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import modelo.CocinaPedido;
import modelo.DetallePedido;

@Named("cocinaBean")
@ViewScoped
public class CocinaBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MINUTOS_RETRASO = 25;

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private List<CocinaPedido> todos = new ArrayList<>();
    private List<CocinaPedido> recibidos = new ArrayList<>();
    private List<CocinaPedido> enPreparacion = new ArrayList<>();
    private List<CocinaPedido> listos = new ArrayList<>();
    private List<CocinaPedido> entregados = new ArrayList<>();
    private List<Integer> mesas = new ArrayList<>();
    private String busqueda = "";
    private int mesaFiltro;
    private String mensaje;
    private String tipoMensaje = "success";

    @PostConstruct
    public void init() {
        refrescar();
    }

    public void refrescar() {
        todos = pedidoDAO.listarPedidosCocina();
        cargarMesas();
        aplicarFiltros();
    }

    public void buscar() {
        aplicarFiltros();
    }

    public void limpiarFiltros() {
        busqueda = "";
        mesaFiltro = 0;
        aplicarFiltros();
    }

    public void iniciarPreparacion(int idPedido) {
        int primerPedido = pedidoDAO.obtenerPrimerPedidoRecibido();
        if (primerPedido != idPedido) {
            mostrarError("Solo se puede iniciar el primer pedido recibido en la cola.");
            refrescar();
            return;
        }
        pedidoDAO.iniciarPreparacion(idPedido);
        mostrarExito("Pedido #" + idPedido + " iniciado en cocina.");
        refrescar();
    }

    public void marcarListo(int idPedido) {
        String estado = pedidoDAO.obtenerEstadoPedido(idPedido);
        if (!"en preparacion".equals(estado)) {
            mostrarError("Solo los pedidos en preparacion pueden marcarse como listos.");
            refrescar();
            return;
        }
        pedidoDAO.marcarListoCocina(idPedido);
        mostrarExito("Pedido #" + idPedido + " marcado como listo.");
        refrescar();
    }

    public void marcarEntregado(int idPedido) {
        String estado = pedidoDAO.obtenerEstadoPedido(idPedido);
        if (!"listo".equals(estado)) {
            mostrarError("Solo los pedidos listos pueden marcarse como entregados.");
            refrescar();
            return;
        }
        pedidoDAO.marcarEntregadoCocina(idPedido);
        mostrarExito("Pedido #" + idPedido + " entregado.");
        refrescar();
    }

    public int getTotalPedidos() {
        return recibidos.size() + enPreparacion.size() + listos.size() + entregados.size();
    }

    public int getPedidosRetrasados() {
        int total = 0;
        for (CocinaPedido pedido : todos) {
            if (pedido.isRetrasado()) {
                total++;
            }
        }
        return total;
    }

    public int getTiempoPromedioPreparacion() {
        if (enPreparacion.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (CocinaPedido pedido : enPreparacion) {
            total += pedido.getMinutosEspera();
        }
        return Math.round((float) total / enPreparacion.size());
    }

    public String getTiempoPromedioTexto() {
        return formatearTiempo(getTiempoPromedioPreparacion());
    }

    public boolean isHayPedidos() {
        return getTotalPedidos() > 0;
    }

    public List<CocinaPedido> getRecibidos() {
        return recibidos;
    }

    public List<CocinaPedido> getEnPreparacion() {
        return enPreparacion;
    }

    public List<CocinaPedido> getListos() {
        return listos;
    }

    public List<CocinaPedido> getEntregados() {
        return entregados;
    }

    public List<Integer> getMesas() {
        return mesas;
    }

    public String getBusqueda() {
        return busqueda;
    }

    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda;
    }

    public int getMesaFiltro() {
        return mesaFiltro;
    }

    public void setMesaFiltro(int mesaFiltro) {
        this.mesaFiltro = mesaFiltro;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    private void aplicarFiltros() {
        recibidos = new ArrayList<>();
        enPreparacion = new ArrayList<>();
        listos = new ArrayList<>();
        entregados = new ArrayList<>();

        for (CocinaPedido pedido : todos) {
            if (!coincideFiltro(pedido)) {
                continue;
            }
            switch (pedido.getEstadoPedido()) {
                case "recibido":
                    recibidos.add(pedido);
                    break;
                case "en preparacion":
                    enPreparacion.add(pedido);
                    break;
                case "listo":
                    listos.add(pedido);
                    break;
                case "entregado":
                    entregados.add(pedido);
                    break;
                default:
                    break;
            }
        }

        marcarPrimeroVisible();
    }

    private boolean coincideFiltro(CocinaPedido pedido) {
        if (mesaFiltro > 0 && pedido.getNumeroMesa() != mesaFiltro) {
            return false;
        }

        String texto = normalizar(busqueda);
        if (texto.isEmpty()) {
            return true;
        }

        StringBuilder acumulado = new StringBuilder();
        acumulado.append(pedido.getIdPedido()).append(' ')
                .append(pedido.getUbicacionTexto()).append(' ')
                .append(pedido.getClienteTexto()).append(' ')
                .append(pedido.getEstadoPedido()).append(' ');

        for (DetallePedido detalle : pedido.getDetalles()) {
            acumulado.append(detalle.getNombreProducto()).append(' ')
                    .append(detalle.getObservacionDetalle()).append(' ');
        }

        return normalizar(acumulado.toString()).contains(texto);
    }

    private void cargarMesas() {
        mesas = new ArrayList<>();
        for (CocinaPedido pedido : todos) {
            int numeroMesa = pedido.getNumeroMesa();
            if (numeroMesa > 0 && !mesas.contains(numeroMesa)) {
                mesas.add(numeroMesa);
            }
        }
    }

    private void marcarPrimeroVisible() {
        boolean encontrado = false;
        for (CocinaPedido pedido : recibidos) {
            pedido.setPrimeroEnCola(!encontrado);
            encontrado = true;
        }
    }

    private void mostrarExito(String texto) {
        mensaje = texto;
        tipoMensaje = "success";
    }

    private void mostrarError(String texto) {
        mensaje = texto;
        tipoMensaje = "warning";
    }

    private String formatearTiempo(int minutosTotales) {
        if (minutosTotales <= 0) {
            return "0 min";
        }
        if (minutosTotales < 60) {
            return minutosTotales + " min";
        }
        int horas = minutosTotales / 60;
        int minutos = minutosTotales % 60;
        if (minutos == 0) {
            return horas + " h";
        }
        return horas + " h " + minutos + " min";
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return limpio.toLowerCase().trim();
    }
}
