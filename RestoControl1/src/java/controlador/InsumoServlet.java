package controlador;

import dao.InsumoDAO;
import dao.ProductoInsumoDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Insumo;
import modelo.RecetaProducto;

@WebServlet(name = "InsumoServlet", urlPatterns = {"/InsumoServlet"})
public class InsumoServlet extends HttpServlet {

    private final InsumoDAO insumoDAO = new InsumoDAO();
    private final ProductoInsumoDAO productoInsumoDAO = new ProductoInsumoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        switch (accion) {
            case "nuevo":
                mostrarFormularioRegistro(request, response);
                break;
            case "editar":
                mostrarFormularioEdicion(request, response);
                break;
            case "eliminar":
                mostrarConfirmacionEliminar(request, response);
                break;
            case "recetas":
                cargarModuloInsumos(request, response, "recetas");
                break;
            default:
                cargarModuloInsumos(request, response, normalizarTab(request.getParameter("tab")));
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("insertar".equals(accion)) {
            registrarInsumo(request, response);
        } else if ("actualizar".equals(accion)) {
            actualizarInsumo(request, response);
        } else if ("desactivar".equals(accion)) {
            desactivarInsumo(request, response);
        } else if ("cambiarEstadoInsumo".equals(accion)) {
            cambiarEstadoInsumo(request, response);
        } else if ("guardarReceta".equals(accion)) {
            guardarReceta(request, response);
        } else if ("limpiarReceta".equals(accion)) {
            limpiarReceta(request, response);
        } else {
            response.sendRedirect("InsumoServlet?tab=inventario");
        }
    }

    private void cargarModuloInsumos(HttpServletRequest request, HttpServletResponse response, String tab)
            throws ServletException, IOException {
        List<Insumo> insumos = insumoDAO.listar();
        List<RecetaProducto> recetas = productoInsumoDAO.listarResumenRecetas();

        request.setAttribute("tabActivo", tab);
        request.setAttribute("insumos", insumos);
        request.setAttribute("insumosActivos", insumoDAO.listarActivos());
        request.setAttribute("recetas", recetas);
        request.setAttribute("totalInsumos", insumos.size());
        request.setAttribute("insumosStockBajo", contarStockBajo(insumos));
        request.setAttribute("recetasCreadas", contarRecetasCreadas(recetas));
        request.setAttribute("recetasPendientes", contarRecetasPendientes(recetas));

        reenviar(request, response, "/vistas/insumos/listar.jsp");
    }

    private void mostrarFormularioRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        reenviar(request, response, "/vistas/insumos/registrar.jsp");
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idInsumo = obtenerIdInsumo(request);
        Insumo insumo = insumoDAO.buscarPorId(idInsumo);

        if (insumo == null) {
            response.sendRedirect("InsumoServlet?tab=inventario&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("insumo", insumo);
        reenviar(request, response, "/vistas/insumos/editar.jsp");
    }

    private void mostrarConfirmacionEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idInsumo = obtenerIdInsumo(request);
        Insumo insumo = insumoDAO.buscarPorId(idInsumo);

        if (insumo == null) {
            response.sendRedirect("InsumoServlet?tab=inventario&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("insumo", insumo);
        reenviar(request, response, "/vistas/insumos/eliminar.jsp");
    }

    private void registrarInsumo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Insumo insumo;

        try {
            insumo = leerInsumoDesdeFormulario(request);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "El stock actual y el stock minimo deben tener valores numericos validos.");
            request.setAttribute("insumo", leerDatosBasicosDesdeFormulario(request));
            reenviar(request, response, "/vistas/insumos/registrar.jsp");
            return;
        }

        if (!stockValido(insumo)) {
            request.setAttribute("error", "El stock actual y el stock minimo deben ser mayores o iguales a 0.");
            request.setAttribute("insumo", insumo);
            reenviar(request, response, "/vistas/insumos/registrar.jsp");
            return;
        }

        insumoDAO.registrar(insumo);
        response.sendRedirect("InsumoServlet?tab=inventario&mensaje=registrado");
    }

    private void actualizarInsumo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Insumo insumo;

        try {
            insumo = leerInsumoDesdeFormulario(request);
        } catch (NumberFormatException e) {
            insumo = leerDatosBasicosDesdeFormulario(request);
            insumo.setIdInsumo(obtenerIdInsumo(request));
            request.setAttribute("error", "El stock actual y el stock minimo deben tener valores numericos validos.");
            request.setAttribute("insumo", insumo);
            reenviar(request, response, "/vistas/insumos/editar.jsp");
            return;
        }

        insumo.setIdInsumo(obtenerIdInsumo(request));

        if (!stockValido(insumo)) {
            request.setAttribute("error", "El stock actual y el stock minimo deben ser mayores o iguales a 0.");
            request.setAttribute("insumo", insumo);
            reenviar(request, response, "/vistas/insumos/editar.jsp");
            return;
        }

        insumoDAO.actualizar(insumo);
        response.sendRedirect("InsumoServlet?tab=inventario&mensaje=actualizado");
    }

    private void desactivarInsumo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idInsumo = obtenerIdInsumo(request);
        insumoDAO.desactivar(idInsumo);
        response.sendRedirect("InsumoServlet?tab=inventario&mensaje=desactivado");
    }

    private void cambiarEstadoInsumo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idInsumo = obtenerIdInsumo(request);
        String estado = "activo".equalsIgnoreCase(request.getParameter("estado")) ? "activo" : "inactivo";
        insumoDAO.actualizarEstado(idInsumo, estado);
        response.sendRedirect("InsumoServlet?tab=inventario&mensaje=estado_insumo");
    }

    private void guardarReceta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int idProducto = parseEntero(request.getParameter("idProducto"), 0);
            if (idProducto <= 0) {
                throw new IllegalArgumentException("Selecciona un plato para configurar su receta.");
            }

            Map<Integer, BigDecimal> cantidades = leerCantidadesReceta(request);
            if (cantidades.isEmpty()) {
                throw new IllegalArgumentException("Agrega al menos un insumo con cantidad mayor a cero.");
            }

            productoInsumoDAO.guardarReceta(idProducto, cantidades);
            response.sendRedirect("InsumoServlet?tab=recetas&mensaje=receta_guardada");
        } catch (RuntimeException e) {
            request.setAttribute("error", e.getMessage());
            cargarModuloInsumos(request, response, "recetas");
        }
    }

    private void limpiarReceta(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idProducto = parseEntero(request.getParameter("idProducto"), 0);
        if (idProducto > 0) {
            productoInsumoDAO.eliminarReceta(idProducto);
        }
        response.sendRedirect("InsumoServlet?tab=recetas&mensaje=receta_limpiada");
    }

    private Map<Integer, BigDecimal> leerCantidadesReceta(HttpServletRequest request) {
        Map<Integer, BigDecimal> cantidades = new LinkedHashMap<>();
        String[] idsInsumo = request.getParameterValues("idInsumo");
        String[] cantidadesRequeridas = request.getParameterValues("cantidadRequerida");

        if (idsInsumo == null || cantidadesRequeridas == null) {
            return cantidades;
        }

        int filas = Math.min(idsInsumo.length, cantidadesRequeridas.length);
        for (int i = 0; i < filas; i++) {
            String idTexto = idsInsumo[i];
            String cantidadTexto = cantidadesRequeridas[i];
            if ((idTexto == null || idTexto.trim().isEmpty())
                    && (cantidadTexto == null || cantidadTexto.trim().isEmpty())) {
                continue;
            }

            int idInsumo = parseEntero(idTexto, 0);
            BigDecimal cantidad = parseDecimalPositivo(cantidadTexto);
            if (idInsumo <= 0) {
                throw new IllegalArgumentException("Selecciona un insumo valido en cada fila de la receta.");
            }

            cantidades.merge(idInsumo, cantidad, BigDecimal::add);
        }

        return cantidades;
    }

    private Insumo leerInsumoDesdeFormulario(HttpServletRequest request) {
        Insumo insumo = leerDatosBasicosDesdeFormulario(request);
        insumo.setStockActual(convertirStock(request.getParameter("stockActual")));
        insumo.setStockMinimo(convertirStock(request.getParameter("stockMinimo")));
        return insumo;
    }

    private Insumo leerDatosBasicosDesdeFormulario(HttpServletRequest request) {
        Insumo insumo = new Insumo();
        insumo.setNombreInsumo(request.getParameter("nombreInsumo"));
        insumo.setDescripcion(request.getParameter("descripcion"));
        insumo.setUnidadMedida(request.getParameter("unidadMedida"));
        insumo.setEstado(request.getParameter("estado"));
        return insumo;
    }

    private BigDecimal convertirStock(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                throw new NumberFormatException("Stock vacio");
            }
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Stock invalido");
        }
    }

    private BigDecimal parseDecimalPositivo(String valor) {
        try {
            BigDecimal numero = new BigDecimal(valor == null ? "" : valor.trim());
            if (numero.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Cantidad no positiva");
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Las cantidades de receta deben ser numeros mayores a cero.");
        }
    }

    private int parseEntero(String valor, int defecto) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                return defecto;
            }
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    private boolean stockValido(Insumo insumo) {
        return insumo.getStockActual().compareTo(BigDecimal.ZERO) >= 0
                && insumo.getStockMinimo().compareTo(BigDecimal.ZERO) >= 0;
    }

    private int obtenerIdInsumo(HttpServletRequest request) {
        return Integer.parseInt(request.getParameter("idInsumo"));
    }

    private String normalizarTab(String tab) {
        return "recetas".equals(tab) ? "recetas" : "inventario";
    }

    private int contarStockBajo(List<Insumo> insumos) {
        int total = 0;
        for (Insumo insumo : insumos) {
            if (insumo.isStockBajo()) {
                total++;
            }
        }
        return total;
    }

    private int contarRecetasCreadas(List<RecetaProducto> recetas) {
        int total = 0;
        for (RecetaProducto receta : recetas) {
            if (receta.isTieneReceta()) {
                total++;
            }
        }
        return total;
    }

    private int contarRecetasPendientes(List<RecetaProducto> recetas) {
        int total = 0;
        for (RecetaProducto receta : recetas) {
            if (!receta.isTieneReceta()) {
                total++;
            }
        }
        return total;
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(ruta);
        dispatcher.forward(request, response);
    }
}
