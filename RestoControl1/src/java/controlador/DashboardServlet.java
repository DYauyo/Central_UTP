package controlador;

import dao.MesaDAO;
import dao.PedidoDAO;
import dao.ReservaDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Mesa;
import modelo.Reserva;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/DashboardServlet"})
public class DashboardServlet extends HttpServlet {

    private final MesaDAO mesaDAO = new MesaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Mesa> mesas = completarSalon(mesaDAO.listarConPedidoAbierto(), 42);
        List<Reserva> reservas = reservaDAO.listarVigentes();
        asignarReservas(mesas, reservas);
        request.setAttribute("mesas", filtrarRango(mesas, 1, 29));
        request.setAttribute("mesasSegundoPiso", filtrarRango(mesas, 30, 42));
        request.setAttribute("totalMesas", mesas.size());
        request.setAttribute("mesasOcupadas", contarOcupadas(mesas));
        request.setAttribute("pedidosParaLlevar", pedidoDAO.contarPedidosAbiertosPorTipo("para_llevar"));
        request.setAttribute("reservasProximas", reservas);
        request.setAttribute("totalReservas", reservas.size());
        reenviar(request, response, "/dashboard.jsp");
    }

    private List<Mesa> completarSalon(List<Mesa> mesas, int totalMesas) {
        Map<Integer, Mesa> mesasPorNumero = new HashMap<>();
        for (Mesa mesa : mesas) {
            mesasPorNumero.put(mesa.getNumeroMesa(), mesa);
        }

        for (int numero = 1; numero <= totalMesas; numero++) {
            if (!mesasPorNumero.containsKey(numero)) {
                Mesa mesa = new Mesa();
                mesa.setIdMesa(numero);
                mesa.setNumeroMesa(numero);
                mesa.setCapacidad(4);
                mesa.setEstadoMesa("disponible");
                mesa.setUbicacion("Salon principal");
                mesa.setEstado("activo");
                mesas.add(mesa);
            }
        }

        mesas.sort((mesaA, mesaB) -> Integer.compare(mesaA.getNumeroMesa(), mesaB.getNumeroMesa()));
        return mesas;
    }

    private List<Mesa> filtrarRango(List<Mesa> mesas, int desde, int hasta) {
        List<Mesa> filtradas = new ArrayList<>();
        for (Mesa mesa : mesas) {
            if (mesa.getNumeroMesa() >= desde && mesa.getNumeroMesa() <= hasta) {
                filtradas.add(mesa);
            }
        }
        return filtradas;
    }

    private int contarOcupadas(List<Mesa> mesas) {
        int total = 0;
        for (Mesa mesa : mesas) {
            if (mesa.isOcupada()) {
                total++;
            }
        }
        return total;
    }

    private void asignarReservas(List<Mesa> mesas, List<Reserva> reservas) {
        Map<Integer, Mesa> mesasPorId = new HashMap<>();
        for (Mesa mesa : mesas) {
            mesasPorId.put(mesa.getIdMesa(), mesa);
        }
        for (Reserva reserva : reservas) {
            if (!reserva.isAsignadaMesa()) {
                continue;
            }
            Mesa mesa = mesasPorId.get(reserva.getIdMesa());
            if (mesa == null) {
                continue;
            }
            if (mesa.getReservaProxima() == null
                    || reserva.getMinutosParaReserva() < mesa.getReservaProxima().getMinutosParaReserva()) {
                mesa.setReservaProxima(reserva);
            }
        }
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(ruta);
        dispatcher.forward(request, response);
    }
}
