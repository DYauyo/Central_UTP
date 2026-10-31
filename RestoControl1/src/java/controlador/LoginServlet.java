package controlador;

import dao.UsuarioDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import modelo.Usuario;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("cerrar".equals(accion)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("LoginServlet?mensaje=cerrado");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioAutenticado") != null) {
            response.sendRedirect(request.getContextPath() + rutaInicioPorRol(obtenerIdRol(session)));
            return;
        }

        reenviar(request, response, "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (!tieneTexto(username) || !tieneTexto(password)) {
            request.setAttribute("errorKey", "login.error.required");
            request.setAttribute("username", username);
            reenviar(request, response, "/login.jsp");
            return;
        }

        Usuario usuario;
        try {
            usuario = usuarioDAO.validarLogin(username, password);
        } catch (RuntimeException e) {
            getServletContext().log("Error al validar login", e);
            request.setAttribute("errorKey", "login.error.database");
            request.setAttribute("username", username);
            reenviar(request, response, "/login.jsp");
            return;
        }

        if (usuario == null) {
            request.setAttribute("errorKey", "login.error.credentials");
            request.setAttribute("username", username);
            reenviar(request, response, "/login.jsp");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("usuarioAutenticado", usuario);
        session.setAttribute("nombreUsuario", usuario.getNombres() + " " + usuario.getApellidos());
        session.setAttribute("nombreCortoUsuario", primerNombre(usuario.getNombres()));
        session.setAttribute("rolUsuario", usuario.getNombreRol());
        session.setAttribute("idRol", usuario.getIdRol());
        session.setAttribute("idUsuario", usuario.getIdUsuario());
        response.sendRedirect(request.getContextPath() + rutaInicioPorRol(usuario.getIdRol()));
    }

    private String rutaInicioPorRol(int idRol) {
        switch (idRol) {
            case 2:
                return "/jsf/caja.xhtml";
            case 3:
                return "/DashboardServlet";
            case 4:
                return "/jsf/cocina.xhtml";
            case 5:
                return "/InsumoServlet";
            case 6:
                return "/ReporteServlet";
            case 1:
            default:
                return "/DashboardServlet";
        }
    }

    private int obtenerIdRol(HttpSession session) {
        Object idRol = session.getAttribute("idRol");
        if (idRol instanceof Integer) {
            return (Integer) idRol;
        }
        try {
            return Integer.parseInt(String.valueOf(idRol));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String primerNombre(String nombres) {
        if (nombres == null || nombres.trim().isEmpty()) {
            return "Usuario";
        }
        return nombres.trim().split("\\s+")[0];
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(ruta);
        dispatcher.forward(request, response);
    }
}
