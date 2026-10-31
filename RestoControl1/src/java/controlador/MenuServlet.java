package controlador;

import dao.CategoriaDAO;
import dao.DisponibilidadProductoDAO;
import dao.ProductoDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.Categoria;
import modelo.DisponibilidadProducto;
import modelo.Producto;

@WebServlet(name = "MenuServlet", urlPatterns = {"/MenuServlet"})
public class MenuServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final DisponibilidadProductoDAO disponibilidadDAO = new DisponibilidadProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cargarVista(request, response, normalizarTab(request.getParameter("tab")), obtenerFecha(request));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        String tab = normalizarTab(request.getParameter("tab"));
        LocalDate fecha = obtenerFecha(request);

        try {
            if ("guardarProducto".equals(accion)) {
                guardarProducto(request);
                redirigir(response, "productos", fecha, "guardado");
                return;
            }
            if ("desactivarProducto".equals(accion)) {
                productoDAO.desactivar(parseEntero(request.getParameter("idProducto"), 0));
                redirigir(response, "productos", fecha, "desactivado");
                return;
            }
            if ("cambiarEstadoProducto".equals(accion)) {
                cambiarEstadoProducto(request);
                redirigir(response, "productos", fecha, "guardado");
                return;
            }
            if ("guardarCategoria".equals(accion)) {
                guardarCategoria(request);
                redirigir(response, "categorias", fecha, "guardado");
                return;
            }
            if ("desactivarCategoria".equals(accion)) {
                categoriaDAO.desactivar(parseEntero(request.getParameter("idCategoria"), 0));
                redirigir(response, "categorias", fecha, "desactivado");
                return;
            }
            if ("cambiarEstadoCategoria".equals(accion)) {
                categoriaDAO.actualizarEstado(
                        parseEntero(request.getParameter("idCategoria"), 0),
                        valorPorDefecto(request.getParameter("estado"), "activo"));
                redirigir(response, "categorias", fecha, "guardado");
                return;
            }
            if ("guardarEstimacion".equals(accion)) {
                guardarEstimacion(request, fecha);
                redirigir(response, "estimacion", fecha, "estimacion_guardada");
                return;
            }

            redirigir(response, tab, fecha, "sin_accion");
        } catch (RuntimeException e) {
            request.setAttribute("error", e.getMessage());
            cargarVista(request, response, tab, fecha);
        }
    }

    private void cargarVista(HttpServletRequest request, HttpServletResponse response, String tab, LocalDate fecha)
            throws ServletException, IOException {
        request.setAttribute("tabActivo", tab);
        request.setAttribute("fechaSeleccionada", fecha);
        int categoriaFiltro = parseEntero(request.getParameter("categoria"), 0);
        request.setAttribute("categoriaFiltroId", categoriaFiltro);
        Categoria categoriaSeleccionada = categoriaFiltro > 0 ? categoriaDAO.buscarPorId(categoriaFiltro) : null;
        request.setAttribute("categoriaFiltroNombre", categoriaSeleccionada == null ? "" : categoriaSeleccionada.getNombreCategoria());
        List<Producto> productos = productoDAO.listar();
        List<Categoria> categorias = categoriaDAO.listarConCantidadProductos();
        List<Categoria> categoriasActivas = categoriaDAO.listarActivas();
        request.setAttribute("productos", productos);
        request.setAttribute("categorias", categorias);
        request.setAttribute("categoriasActivas", categoriasActivas);
        List<DisponibilidadProducto> estimaciones = disponibilidadDAO.listarPorFecha(fecha);
        request.setAttribute("estimaciones", estimaciones);
        request.setAttribute("porcionesPlanificadas", sumarPorciones(estimaciones));
        request.setAttribute("totalPlatosRegistrados", productos.size());
        request.setAttribute("totalCategoriasActivas", categoriasActivas.size());
        request.setAttribute("productosEstimadosHoy", contarPlanificados(estimaciones));
        request.setAttribute("productosDisponiblesHoy", contarListosParaVender(estimaciones));

        RequestDispatcher dispatcher = request.getRequestDispatcher("/vistas/menu/menu.jsp");
        dispatcher.forward(request, response);
    }

    private void guardarProducto(HttpServletRequest request) {
        Producto producto = new Producto();
        producto.setIdProducto(parseEntero(request.getParameter("idProducto"), 0));
        producto.setNombreProducto(requerido(request.getParameter("nombreProducto"), "El nombre del producto es obligatorio."));
        producto.setDescripcion(valor(request.getParameter("descripcion")));
        producto.setPrecio(parseDecimal(request.getParameter("precio")));
        producto.setDisponibilidad(valorPorDefecto(request.getParameter("disponibilidad"), "disponible"));
        producto.setImagen(valor(request.getParameter("imagen")));
        producto.setEstado(valorPorDefecto(request.getParameter("estado"), "activo"));
        producto.setIdCategoria(parseEntero(request.getParameter("idCategoria"), 0));

        if (producto.getIdCategoria() <= 0) {
            throw new IllegalArgumentException("Selecciona una categoria para el producto.");
        }

        Categoria categoria = categoriaDAO.buscarPorId(producto.getIdCategoria());
        if (categoria == null) {
            throw new IllegalArgumentException("Selecciona una categoria para el producto.");
        }
        if (!"activo".equalsIgnoreCase(categoria.getEstado()) && "activo".equalsIgnoreCase(producto.getEstado())) {
            throw new IllegalArgumentException("No puedes activar este plato porque su categoria esta inactiva. Primero activa la categoria " + categoria.getNombreCategoria() + ".");
        }

        if (producto.getIdProducto() > 0) {
            productoDAO.actualizar(producto);
        } else {
            productoDAO.registrar(producto);
        }
    }

    private void guardarCategoria(HttpServletRequest request) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(parseEntero(request.getParameter("idCategoria"), 0));
        categoria.setNombreCategoria(requerido(request.getParameter("nombreCategoria"), "El nombre de la categoria es obligatorio."));
        categoria.setDescripcion(valor(request.getParameter("descripcion")));
        categoria.setEstado(valorPorDefecto(request.getParameter("estado"), "activo"));

        if (categoria.getIdCategoria() > 0) {
            categoriaDAO.actualizar(categoria);
        } else {
            categoriaDAO.registrar(categoria);
        }
    }

    private void cambiarEstadoProducto(HttpServletRequest request) {
        int idProducto = parseEntero(request.getParameter("idProducto"), 0);
        String nuevoEstado = valorPorDefecto(request.getParameter("estado"), "activo");
        Producto producto = productoDAO.buscarPorId(idProducto);

        if (producto == null) {
            throw new IllegalArgumentException("No se encontro el plato seleccionado.");
        }

        if ("activo".equalsIgnoreCase(nuevoEstado) && !"activo".equalsIgnoreCase(producto.getEstadoCategoria())) {
            throw new IllegalArgumentException("No puedes activar este plato porque su categoria esta inactiva. Primero activa la categoria " + producto.getNombreCategoria() + ".");
        }

        productoDAO.actualizarEstado(idProducto, nuevoEstado);
    }

    private void guardarEstimacion(HttpServletRequest request, LocalDate fecha) {
        String[] productos = request.getParameterValues("idProducto");
        if (productos == null) {
            return;
        }

        Map<Integer, DisponibilidadProducto> disponibilidadPorProducto = new HashMap<>();
        for (DisponibilidadProducto disponibilidad : disponibilidadDAO.listarPorFecha(fecha)) {
            disponibilidadPorProducto.put(disponibilidad.getIdProducto(), disponibilidad);
        }

        for (String id : productos) {
            int idProducto = parseEntero(id, 0);
            int cantidad = parseEntero(request.getParameter("cantidad_" + idProducto), 0);
            if (idProducto > 0) {
                DisponibilidadProducto disponibilidad = disponibilidadPorProducto.get(idProducto);
                String estado = "activo";
                if (disponibilidad != null) {
                    disponibilidad.setCantidadPlanificada(cantidad);
                    estado = disponibilidad.isDisponibleParaVenta() ? "activo" : "critico";
                }
                disponibilidadDAO.guardarEstimacion(fecha, idProducto, cantidad, estado);
            }
        }
    }

    private void redirigir(HttpServletResponse response, String tab, LocalDate fecha, String mensaje)
            throws IOException {
        response.sendRedirect("MenuServlet?tab=" + tab + "&fecha=" + fecha + "&mensaje=" + mensaje);
    }

    private String normalizarTab(String tab) {
        if ("productos".equals(tab) || "estimacion".equals(tab)) {
            return tab;
        }
        return "categorias";
    }

    private int sumarPorciones(List<DisponibilidadProducto> estimaciones) {
        int total = 0;
        for (DisponibilidadProducto estimacion : estimaciones) {
            total += estimacion.getCantidadPlanificada();
        }
        return total;
    }

    private int contarPlanificados(List<DisponibilidadProducto> estimaciones) {
        int total = 0;
        for (DisponibilidadProducto estimacion : estimaciones) {
            if (estimacion.getCantidadPlanificada() > 0) {
                total++;
            }
        }
        return total;
    }

    private int contarListosParaVender(List<DisponibilidadProducto> estimaciones) {
        int total = 0;
        for (DisponibilidadProducto estimacion : estimaciones) {
            if (estimacion.getCantidadPlanificada() > 0 && estimacion.isDisponibleParaVenta()) {
                total++;
            }
        }
        return total;
    }

    private LocalDate obtenerFecha(HttpServletRequest request) {
        String fecha = request.getParameter("fecha");
        if (fecha == null || fecha.trim().isEmpty()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(fecha.trim());
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    private String requerido(String valor, String mensaje) {
        String limpio = valor(valor);
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return limpio;
    }

    private String valor(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String valorPorDefecto(String valor, String defecto) {
        String limpio = valor(valor);
        return limpio.isEmpty() ? defecto : limpio;
    }

    private int parseEntero(String valor, int defecto) {
        if (valor == null || valor.trim().isEmpty()) {
            return defecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    private BigDecimal parseDecimal(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        try {
            BigDecimal decimal = new BigDecimal(valor.trim());
            if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            return decimal;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un numero valido.");
        }
    }
}
