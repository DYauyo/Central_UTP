package controlador;

import dao.PedidoDAO;
import dao.ReservaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Pedido;
import modelo.Reserva;

@WebServlet(name = "OperacionServlet", urlPatterns = {"/OperacionServlet"})
public class OperacionServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "cuenta";
        }

        switch (accion) {
            case "cocina":
                response.sendRedirect(request.getContextPath() + "/jsf/cocina.xhtml");
                break;
            case "facturacion":
                redirigirCaja(request, response);
                break;
            default:
                redirigirPedido(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        try {
            switch (accion == null ? "" : accion) {
                case "abrirMesa":
                    abrirMesa(request, response);
                    break;
                case "abrirDelivery":
                    abrirDelivery(request, response);
                    break;
                case "abrirParaLlevar":
                    abrirParaLlevar(request, response);
                    break;
                case "registrarReserva":
                    registrarReserva(request, response);
                    break;
                case "unirMesas":
                    unirMesas(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/DashboardServlet");
                    break;
            }
        } catch (RuntimeException e) {
            getServletContext().log("Error en operacion", e);
            response.sendRedirect(request.getContextPath() + "/DashboardServlet?error=operacion");
        }
    }

    private void abrirMesa(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idMesa = obtenerEntero(request.getParameter("idMesa"));
        int idUsuario = obtenerEnteroDesdeSesion(request, "idUsuario");
        String cliente = limpiar(request.getParameter("cliente"));
        String observacion = limpiar(request.getParameter("observacion"));
        int comensales = obtenerEntero(request.getParameter("comensales"));
        String notaInicial = construirNotaMesa(cliente, observacion);
        int idPedido = pedidoDAO.abrirPedidoSalon(idMesa, idUsuario, notaInicial, comensales);
        response.sendRedirect(request.getContextPath() + "/jsf/pedidos.xhtml?idPedido=" + idPedido + "&mensaje=mesa_abierta");
    }

    private void abrirDelivery(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idUsuario = obtenerEnteroDesdeSesion(request, "idUsuario");
        String cliente = limpiar(request.getParameter("cliente"));
        String telefono = limpiar(request.getParameter("telefono"));
        String direccion = limpiar(request.getParameter("direccion"));
        String referencia = limpiar(request.getParameter("referencia"));
        String observacion = construirNotaMesa(referencia, limpiar(request.getParameter("observacion")));
        int idPedido = pedidoDAO.abrirPedidoDelivery(idUsuario, cliente, telefono, direccion, observacion);
        response.sendRedirect(request.getContextPath() + "/jsf/pedidos.xhtml?idPedido=" + idPedido + "&mensaje=delivery_abierto");
    }

    private void abrirParaLlevar(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idUsuario = obtenerEnteroDesdeSesion(request, "idUsuario");
        String cliente = limpiar(request.getParameter("cliente"));
        String telefono = limpiar(request.getParameter("telefono"));
        String horaRecojo = limpiar(request.getParameter("horaRecojo"));
        String observacion = limpiar(request.getParameter("observacion"));
        int idPedido = pedidoDAO.abrirPedidoParaLlevar(idUsuario, cliente, telefono, horaRecojo, observacion);
        response.sendRedirect(request.getContextPath() + "/jsf/pedidos.xhtml?idPedido=" + idPedido + "&mensaje=llevar_abierto");
    }

    private void registrarReserva(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Reserva reserva = new Reserva();
        reserva.setCliente(limpiar(request.getParameter("cliente")));
        reserva.setTelefono(limpiar(request.getParameter("telefono")));
        reserva.setCantidadPersonas(Math.max(1, obtenerEntero(request.getParameter("cantidadPersonas"))));
        reserva.setFechaReserva(normalizarFechaHora(request.getParameter("fechaReserva")));
        reserva.setObservacion(limpiar(request.getParameter("observacion")));
        reserva.setIdMesa(obtenerEntero(request.getParameter("idMesa")));
        reservaDAO.registrar(reserva);
        response.sendRedirect(request.getContextPath() + "/DashboardServlet?mensaje=reserva_registrada");
    }

    private void unirMesas(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idUsuario = obtenerEnteroDesdeSesion(request, "idUsuario");
        List<Integer> idsMesa = obtenerListaEnteros(request.getParameterValues("mesasUnidas"));
        String cliente = limpiar(request.getParameter("cliente"));
        int comensales = obtenerEntero(request.getParameter("comensales"));
        String observacion = limpiar(request.getParameter("observacion"));
        int idPedido = pedidoDAO.abrirPedidoMesasUnidas(idsMesa, idUsuario, cliente, comensales, observacion);
        response.sendRedirect(request.getContextPath() + "/jsf/pedidos.xhtml?idPedido=" + idPedido + "&mensaje=mesas_unidas");
    }

    private void redirigirPedido(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Pedido pedido = obtenerPedidoDesdeRequest(request);
        if (pedido != null) {
            response.sendRedirect(request.getContextPath() + "/jsf/pedidos.xhtml?idPedido=" + pedido.getIdPedido());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/DashboardServlet");
    }

    private void redirigirCaja(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Pedido pedido = obtenerPedidoDesdeRequest(request);
        if (pedido != null) {
            response.sendRedirect(request.getContextPath() + "/jsf/caja.xhtml?idPedido=" + pedido.getIdPedido());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/jsf/caja.xhtml");
    }

    private Pedido obtenerPedidoDesdeRequest(HttpServletRequest request) {
        int idPedido = obtenerEntero(request.getParameter("idPedido"));
        if (idPedido > 0) {
            return pedidoDAO.buscarPorId(idPedido);
        }

        int idMesa = obtenerEntero(request.getParameter("idMesa"));
        if (idMesa > 0) {
            return pedidoDAO.buscarAbiertoPorMesa(idMesa);
        }

        return null;
    }

    private int obtenerEnteroDesdeSesion(HttpServletRequest request, String atributo) {
        Object valor = request.getSession().getAttribute(atributo);
        if (valor instanceof Integer) {
            return (Integer) valor;
        }
        return obtenerEntero(String.valueOf(valor));
    }

    private int obtenerEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<Integer> obtenerListaEnteros(String[] valores) {
        List<Integer> numeros = new ArrayList<>();
        if (valores == null) {
            return numeros;
        }
        for (String valor : valores) {
            int numero = obtenerEntero(valor);
            if (numero > 0 && !numeros.contains(numero)) {
                numeros.add(numero);
            }
        }
        return numeros;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String normalizarFechaHora(String valor) {
        if (!tieneTexto(valor)) {
            return null;
        }
        String limpio = valor.trim().replace('T', ' ');
        if (limpio.length() == 16) {
            limpio += ":00";
        }
        return limpio;
    }

    private String construirNotaMesa(String cliente, String observacion) {
        if (tieneTexto(cliente) && tieneTexto(observacion)) {
            return cliente + " - " + observacion;
        }
        if (tieneTexto(cliente)) {
            return cliente;
        }
        return observacion;
    }

}
