package controlador;

import dao.InsumoDAO;
import dao.ProveedorDAO;
import dao.ProveedorDAO.Proveedor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/ProveedorServlet")
public class ProveedorServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ProveedorServlet.class.getName());
    private final ProveedorDAO proveedores = new ProveedorDAO();
    private final InsumoDAO insumos = new InsumoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        mostrar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            String accion = request.getParameter("accion");
            if ("guardar".equals(accion)) {
                proveedores.guardar(leerProveedor(request));
            } else if ("vincular".equals(accion) || "desvincular".equals(accion)) {
                proveedores.cambiarAsociacion(idPositivo(request, "idInsumo"),
                        idPositivo(request, "idProveedor"), "vincular".equals(accion));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/ProveedorServlet?guardado=1");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            mostrar(request, response);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo guardar el cambio de proveedores", e);
            request.setAttribute("error", e.getErrorCode() == 1062
                    ? "Ya existe un proveedor con ese RUC." : "No se pudo guardar el cambio.");
            mostrar(request, response);
        }
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Proveedor> lista = proveedores.listar();
            request.setAttribute("proveedores", lista);
            request.setAttribute("insumos", insumos.listarActivos());
            request.setAttribute("asociaciones", proveedores.listarAsociaciones());
            String editar = request.getParameter("editar");
            if (editar != null) {
                int id = idPositivo(request, "editar");
                Proveedor seleccionado = lista.stream().filter(p -> p.id() == id).findFirst().orElse(null);
                if (seleccionado == null) {
                    request.setAttribute("error", "El proveedor ya no existe.");
                }
                request.setAttribute("edicion", seleccionado);
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo cargar el módulo de proveedores", e);
            request.setAttribute("error", "No se pudo cargar proveedores. Verifica la migración de la fase 1.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        request.getRequestDispatcher("/WEB-INF/proveedores/listar.jsp").forward(request, response);
    }

    private Proveedor leerProveedor(HttpServletRequest request) {
        int id = "0".equals(request.getParameter("idProveedor")) ? 0 : idPositivo(request, "idProveedor");
        String ruc = campo(request, "ruc", 11);
        String razonSocial = campo(request, "razonSocial", 150);
        String telefono = campo(request, "telefono", 30);
        String correo = campo(request, "correo", 150);
        String estado = request.getParameter("estado");
        Proveedor proveedor = new Proveedor(id, ruc, razonSocial, telefono, correo, estado);
        request.setAttribute("edicion", proveedor);
        if (!ruc.matches("[0-9]{11}") || razonSocial.isEmpty()
                || (!"activo".equals(estado) && !"inactivo".equals(estado))) {
            throw new IllegalArgumentException("Ingresa un RUC de 11 dígitos, la razón social y un estado válido.");
        }
        return proveedor;
    }

    private String campo(HttpServletRequest request, String nombre, int maximo) {
        String valor = request.getParameter(nombre);
        valor = valor == null ? "" : valor.strip();
        if (valor.length() > maximo) {
            throw new IllegalArgumentException("Uno de los campos excede la longitud permitida.");
        }
        return valor;
    }

    private int idPositivo(HttpServletRequest request, String nombre) {
        try {
            int id = Integer.parseInt(request.getParameter(nombre));
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException e) {
            // Los identificadores enviados por el formulario deben ser enteros positivos.
        }
        throw new IllegalArgumentException("La selección no es válida.");
    }
}
