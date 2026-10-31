package controlador;

import dao.CategoriaDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import modelo.Categoria;

@WebServlet(name = "CategoriaServlet", urlPatterns = {"/CategoriaServlet"})
public class CategoriaServlet extends HttpServlet {

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
                listarCategorias(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("insertar".equals(accion)) {
            registrarCategoria(request, response);
        } else if ("actualizar".equals(accion)) {
            actualizarCategoria(request, response);
        } else if ("desactivar".equals(accion)) {
            desactivarCategoria(request, response);
        } else {
            response.sendRedirect("CategoriaServlet?accion=listar");
        }
    }

    private void listarCategorias(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categorias", categoriaDAO.listar());
        reenviar(request, response, "/vistas/categorias/listar.jsp");
    }

    private void mostrarFormularioRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        reenviar(request, response, "/vistas/categorias/registrar.jsp");
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idCategoria = obtenerIdCategoria(request);
        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (categoria == null) {
            response.sendRedirect("CategoriaServlet?accion=listar&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("categoria", categoria);
        reenviar(request, response, "/vistas/categorias/editar.jsp");
    }

    private void mostrarConfirmacionEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idCategoria = obtenerIdCategoria(request);
        Categoria categoria = categoriaDAO.buscarPorId(idCategoria);

        if (categoria == null) {
            response.sendRedirect("CategoriaServlet?accion=listar&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("categoria", categoria);
        reenviar(request, response, "/vistas/categorias/eliminar.jsp");
    }

    private void registrarCategoria(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Categoria categoria = leerCategoriaDesdeFormulario(request);
        categoriaDAO.registrar(categoria);
        response.sendRedirect("CategoriaServlet?accion=listar&mensaje=registrado");
    }

    private void actualizarCategoria(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Categoria categoria = leerCategoriaDesdeFormulario(request);
        categoria.setIdCategoria(obtenerIdCategoria(request));
        categoriaDAO.actualizar(categoria);
        response.sendRedirect("CategoriaServlet?accion=listar&mensaje=actualizado");
    }

    private void desactivarCategoria(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idCategoria = obtenerIdCategoria(request);
        categoriaDAO.desactivar(idCategoria);
        response.sendRedirect("CategoriaServlet?accion=listar&mensaje=desactivado");
    }

    private Categoria leerCategoriaDesdeFormulario(HttpServletRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(request.getParameter("nombreCategoria"));
        categoria.setDescripcion(request.getParameter("descripcion"));
        categoria.setEstado(request.getParameter("estado"));
        return categoria;
    }

    private int obtenerIdCategoria(HttpServletRequest request) {
        String idCategoria = request.getParameter("idCategoria");
        return Integer.parseInt(idCategoria);
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(ruta);
        dispatcher.forward(request, response);
    }
}
