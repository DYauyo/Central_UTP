package controlador;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "SeguridadFilter", urlPatterns = {"/*"})
public class SeguridadFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();

        if (esRecursoLibre(uri, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean usuarioAutenticado = session != null
                && session.getAttribute("usuarioAutenticado") != null;

        if (usuarioAutenticado) {
            if (!tienePermiso(httpRequest, uri, contextPath, session)) {
                httpResponse.sendRedirect(contextPath + agregarMensaje(rutaInicioPorRol(obtenerIdRol(session)), "sin_permiso"));
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        httpResponse.sendRedirect(contextPath + "/login.jsp?mensaje=sesion");
    }

    private boolean esRecursoLibre(String uri, String contextPath) {
        String ruta = uri.substring(contextPath.length());

        return ruta.equals("/")
                || ruta.equals("/index.html")
                || ruta.equals("/login.jsp")
                || ruta.equals("/LoginServlet")
                || ruta.equals("/IdiomaServlet")
                || ruta.startsWith("/assets/")
                || ruta.startsWith("/jakarta.faces.resource/")
                || ruta.startsWith("/imagenes/")
                || ruta.startsWith("/images/")
                || ruta.startsWith("/img/")
                || ruta.equals("/favicon.ico");
    }

    private boolean tienePermiso(HttpServletRequest request, String uri, String contextPath, HttpSession session) {
        String ruta = uri.substring(contextPath.length());
        int idRol = obtenerIdRol(session);

        if (ruta.equals("/PanelServlet")) {
            return esAdministrador(idRol) || esGerente(idRol);
        }

        if (ruta.equals("/dashboard.jsp") || ruta.equals("/DashboardServlet")) {
            return esAdministrador(idRol) || esMesero(idRol);
        }

        if (ruta.equals("/CategoriaServlet")
                || ruta.equals("/ProductoServlet")
                || ruta.equals("/MenuServlet")
                || ruta.equals("/UsuarioServlet")
                || ruta.startsWith("/vistas/categorias/")
                || ruta.startsWith("/vistas/productos/")
                || ruta.startsWith("/vistas/menu/")
                || ruta.startsWith("/vistas/usuarios/")) {
            return esAdministrador(idRol);
        }

        if (ruta.equals("/InsumoServlet") || ruta.startsWith("/vistas/insumos/")) {
            return esAdministrador(idRol) || esAlmacen(idRol);
        }

        if (ruta.equals("/jsf/cocina.xhtml")) {
            return esAdministrador(idRol) || esCocina(idRol);
        }

        if (ruta.equals("/jsf/pedidos.xhtml")) {
            return esAdministrador(idRol) || esMesero(idRol);
        }

        if (ruta.equals("/jsf/caja.xhtml")) {
            return esAdministrador(idRol) || esCajero(idRol);
        }

        if (ruta.equals("/ComprobanteServlet")) {
            return esAdministrador(idRol) || esCajero(idRol);
        }

        if (ruta.startsWith("/vistas/panel/")) {
            return esAdministrador(idRol) || esGerente(idRol);
        }

        if (ruta.equals("/ReporteServlet") || ruta.startsWith("/vistas/reportes/")) {
            return esAdministrador(idRol) || esGerente(idRol);
        }

        if (ruta.startsWith("/vistas/operacion/")) {
            return tienePermisoVistaOperacion(ruta, idRol);
        }

        if (ruta.equals("/OperacionServlet")) {
            return tienePermisoOperacion(request, idRol);
        }

        return true;
    }

    private boolean tienePermisoOperacion(HttpServletRequest request, int idRol) {
        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "cuenta";
        }

        switch (accion) {
            case "cuenta":
            case "abrirMesa":
            case "abrirDelivery":
            case "abrirParaLlevar":
            case "registrarReserva":
            case "unirMesas":
                return esAdministrador(idRol) || esMesero(idRol);
            case "agregarProducto":
            case "cambiarCantidad":
            case "eliminarDetalle":
            case "enviarCocina":
            case "cocina":
            case "marcarListo":
            case "facturacion":
            case "registrarPago":
                return false;
            case "resumen":
                return false;
            default:
                return esAdministrador(idRol);
        }
    }

    private boolean tienePermisoVistaOperacion(String ruta, int idRol) {
        return false;
    }

    private int obtenerIdRol(HttpSession session) {
        Object idRol = session.getAttribute("idRol");
        if (idRol instanceof Integer) {
            return (Integer) idRol;
        }
        if (idRol instanceof String) {
            try {
                return Integer.parseInt((String) idRol);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private boolean esAdministrador(int idRol) {
        return idRol == 1;
    }

    private boolean esCajero(int idRol) {
        return idRol == 2;
    }

    private boolean esMesero(int idRol) {
        return idRol == 3;
    }

    private boolean esCocina(int idRol) {
        return idRol == 4;
    }

    private boolean esAlmacen(int idRol) {
        return idRol == 5;
    }

    private boolean esGerente(int idRol) {
        return idRol == 6;
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
            case 1:
            default:
                return "/PanelServlet";
        }
    }

    private String agregarMensaje(String ruta, String mensaje) {
        String separador = ruta.contains("?") ? "&" : "?";
        return ruta + separador + "mensaje=" + mensaje;
    }

    @Override
    public void destroy() {
    }
}
