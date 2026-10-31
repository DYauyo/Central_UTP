package controlador;

import dao.DashboardDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PanelServlet", urlPatterns = {"/PanelServlet"})
public class PanelServlet extends HttpServlet {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("panel", dashboardDAO.cargarPanel());
        RequestDispatcher dispatcher = request.getRequestDispatcher("/vistas/panel/principal.jsp");
        dispatcher.forward(request, response);
    }
}
