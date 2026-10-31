package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@WebServlet(name = "IdiomaServlet", urlPatterns = {"/IdiomaServlet"})
public class IdiomaServlet extends HttpServlet {

    private static final Set<String> IDIOMAS_PERMITIDOS = Set.of("es", "en", "it", "pt");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cambiarIdioma(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        cambiarIdioma(request, response);
    }

    private void cambiarIdioma(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idioma = request.getParameter("idioma");
        if (!IDIOMAS_PERMITIDOS.contains(idioma)) {
            idioma = "es";
        }

        HttpSession session = request.getSession();
        session.setAttribute("idioma", idioma);
        session.setAttribute("idiomaEtiqueta", idioma.toUpperCase(Locale.ROOT));

        String destino = obtenerDestinoSeguro(request);
        response.sendRedirect(response.encodeRedirectURL(destino));
    }

    private String obtenerDestinoSeguro(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String volver = request.getParameter("volver");

        if (volver != null && volver.startsWith(contextPath + "/") && !volver.startsWith(contextPath + "//")) {
            return volver;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioAutenticado") != null) {
            return contextPath + "/DashboardServlet";
        }

        return contextPath + "/login.jsp";
    }
}
