package controlador;

import dao.CategoriaDAO;
import dao.ProductoDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import modelo.Producto;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

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
            default:
                listarProductos(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("insertar".equals(accion)) {
            registrarProducto(request, response);
        } else if ("actualizar".equals(accion)) {
            actualizarProducto(request, response);
        } else if ("desactivar".equals(accion)) {
            desactivarProducto(request, response);
        } else {
            response.sendRedirect("ProductoServlet?accion=listar");
        }
    }

    private void listarProductos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("productos", productoDAO.listar());
        reenviar(request, response, "/vistas/productos/listar.jsp");
    }

    private void mostrarFormularioRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categorias", categoriaDAO.listarActivas());
        reenviar(request, response, "/vistas/productos/registrar.jsp");
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idProducto = obtenerIdProducto(request);
        Producto producto = productoDAO.buscarPorId(idProducto);

        if (producto == null) {
            response.sendRedirect("ProductoServlet?accion=listar&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("producto", producto);
        request.setAttribute("categorias", categoriaDAO.listarActivas());
        reenviar(request, response, "/vistas/productos/editar.jsp");
    }

    private void mostrarConfirmacionEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idProducto = obtenerIdProducto(request);
        Producto producto = productoDAO.buscarPorId(idProducto);

        if (producto == null) {
            response.sendRedirect("ProductoServlet?accion=listar&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("producto", producto);
        reenviar(request, response, "/vistas/productos/eliminar.jsp");
    }

    private void registrarProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Producto producto;

        try {
            producto = leerProductoDesdeFormulario(request);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "El precio debe ser un número válido y la categoría es obligatoria.");
            request.setAttribute("producto", leerDatosBasicosDesdeFormulario(request));
            request.setAttribute("categorias", categoriaDAO.listarActivas());
            reenviar(request, response, "/vistas/productos/registrar.jsp");
            return;
        }

        if (!productoValido(producto)) {
            request.setAttribute("error", "Completa el nombre, selecciona una categoría y usa un precio mayor o igual a 0.");
            request.setAttribute("producto", producto);
            request.setAttribute("categorias", categoriaDAO.listarActivas());
            reenviar(request, response, "/vistas/productos/registrar.jsp");
            return;
        }

        productoDAO.registrar(producto);
        response.sendRedirect("ProductoServlet?accion=listar&mensaje=registrado");
    }

    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Producto producto;

        try {
            producto = leerProductoDesdeFormulario(request);
        } catch (NumberFormatException e) {
            producto = leerDatosBasicosDesdeFormulario(request);
            producto.setIdProducto(obtenerIdProducto(request));
            request.setAttribute("error", "El precio debe ser un número válido y la categoría es obligatoria.");
            request.setAttribute("producto", producto);
            request.setAttribute("categorias", categoriaDAO.listarActivas());
            reenviar(request, response, "/vistas/productos/editar.jsp");
            return;
        }

        producto.setIdProducto(obtenerIdProducto(request));

        if (!productoValido(producto)) {
            request.setAttribute("error", "Completa el nombre, selecciona una categoría y usa un precio mayor o igual a 0.");
            request.setAttribute("producto", producto);
            request.setAttribute("categorias", categoriaDAO.listarActivas());
            reenviar(request, response, "/vistas/productos/editar.jsp");
            return;
        }

        productoDAO.actualizar(producto);
        response.sendRedirect("ProductoServlet?accion=listar&mensaje=actualizado");
    }

    private void desactivarProducto(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idProducto = obtenerIdProducto(request);
        productoDAO.desactivar(idProducto);
        response.sendRedirect("ProductoServlet?accion=listar&mensaje=desactivado");
    }

    private Producto leerProductoDesdeFormulario(HttpServletRequest request) {
        Producto producto = leerDatosBasicosDesdeFormulario(request);
        producto.setPrecio(convertirPrecio(request.getParameter("precio")));
        producto.setIdCategoria(obtenerIdCategoria(request));
        return producto;
    }

    private Producto leerDatosBasicosDesdeFormulario(HttpServletRequest request) {
        Producto producto = new Producto();
        producto.setNombreProducto(request.getParameter("nombreProducto"));
        producto.setDescripcion(request.getParameter("descripcion"));
        producto.setDisponibilidad(request.getParameter("disponibilidad"));
        producto.setImagen(request.getParameter("imagen"));
        producto.setEstado(request.getParameter("estado"));
        return producto;
    }

    private BigDecimal convertirPrecio(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new NumberFormatException("Precio vacío");
        }
        return new BigDecimal(valor.trim());
    }

    private boolean productoValido(Producto producto) {
        return tieneTexto(producto.getNombreProducto())
                && producto.getIdCategoria() > 0
                && producto.getPrecio() != null
                && producto.getPrecio().compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private int obtenerIdProducto(HttpServletRequest request) {
        String idProducto = request.getParameter("idProducto");
        return Integer.parseInt(idProducto);
    }

    private int obtenerIdCategoria(HttpServletRequest request) {
        return Integer.parseInt(request.getParameter("idCategoria"));
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(ruta);
        dispatcher.forward(request, response);
    }
}
