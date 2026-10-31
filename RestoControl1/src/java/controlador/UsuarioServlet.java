package controlador;

import dao.UsuarioDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import modelo.Rol;
import modelo.Usuario;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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
                listarUsuarios(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("insertar".equals(accion)) {
            registrarUsuario(request, response);
        } else if ("actualizar".equals(accion)) {
            actualizarUsuario(request, response);
        } else if ("desactivar".equals(accion)) {
            desactivarUsuario(request, response);
        } else if ("cambiarEstadoUsuario".equals(accion)) {
            cambiarEstadoUsuario(request, response);
        } else if ("insertarRol".equals(accion)) {
            registrarRol(request, response);
        } else if ("actualizarRol".equals(accion)) {
            actualizarRol(request, response);
        } else if ("cambiarEstadoRol".equals(accion)) {
            cambiarEstadoRol(request, response);
        } else {
            response.sendRedirect("UsuarioServlet?accion=listar");
        }
    }

    private void listarUsuarios(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cargarDatosPrincipal(request);
        reenviar(request, response, "/vistas/usuarios/listar.jsp");
    }

    private void cargarDatosPrincipal(HttpServletRequest request) {
        List<Usuario> usuarios = usuarioDAO.listar();
        List<Rol> roles = usuarioDAO.listarRoles();

        long usuariosActivos = usuarios.stream()
                .filter(usuario -> "activo".equalsIgnoreCase(usuario.getEstado()))
                .count();
        long rolesActivos = roles.stream()
                .filter(rol -> "activo".equalsIgnoreCase(rol.getEstado()))
                .count();

        Object tabForzado = request.getAttribute("tabActivo");
        String tab = tabForzado == null ? request.getParameter("tab") : tabForzado.toString();
        if (!"usuarios".equals(tab)) {
            tab = "roles";
        }

        request.setAttribute("usuarios", usuarios);
        request.setAttribute("rolesGestion", roles);
        request.setAttribute("totalUsuarios", usuarios.size());
        request.setAttribute("usuariosActivos", usuariosActivos);
        request.setAttribute("totalRoles", roles.size());
        request.setAttribute("rolesActivos", rolesActivos);
        request.setAttribute("tabActivo", tab);
    }

    private void mostrarFormularioRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("roles", usuarioDAO.listarRolesActivos());
        reenviar(request, response, "/vistas/usuarios/registrar.jsp");
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idUsuario = obtenerIdUsuario(request);
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);

        if (usuario == null) {
            response.sendRedirect("UsuarioServlet?tab=usuarios&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("usuario", usuario);
        request.setAttribute("roles", usuarioDAO.listarRolesActivos());
        reenviar(request, response, "/vistas/usuarios/editar.jsp");
    }

    private void mostrarConfirmacionEliminar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idUsuario = obtenerIdUsuario(request);
        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);

        if (usuario == null) {
            response.sendRedirect("UsuarioServlet?tab=usuarios&mensaje=no_encontrado");
            return;
        }

        request.setAttribute("usuario", usuario);
        reenviar(request, response, "/vistas/usuarios/eliminar.jsp");
    }

    private void registrarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = leerUsuarioDesdeFormulario(request);

        if (!datosValidos(usuario, true)) {
            request.setAttribute("error", "Completa nombres, apellidos, correo, username, password y rol.");
            request.setAttribute("tabActivo", "usuarios");
            cargarDatosPrincipal(request);
            reenviar(request, response, "/vistas/usuarios/listar.jsp");
            return;
        }

        if (usuarioDAO.existeUsernameOCorreo(usuario.getUsername(), usuario.getCorreo(), 0)) {
            request.setAttribute("error", "El username o el correo ya est\u00e1 registrado.");
            request.setAttribute("tabActivo", "usuarios");
            cargarDatosPrincipal(request);
            reenviar(request, response, "/vistas/usuarios/listar.jsp");
            return;
        }

        usuarioDAO.registrar(usuario);
        response.sendRedirect("UsuarioServlet?tab=usuarios&mensaje=registrado");
    }

    private void actualizarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario usuario = leerUsuarioDesdeFormulario(request);
        usuario.setIdUsuario(obtenerIdUsuario(request));

        if (!datosValidos(usuario, false)) {
            request.setAttribute("error", "Completa nombres, apellidos, correo, username y rol.");
            request.setAttribute("usuario", usuario);
            request.setAttribute("roles", usuarioDAO.listarRolesActivos());
            reenviar(request, response, "/vistas/usuarios/editar.jsp");
            return;
        }

        if (usuarioDAO.existeUsernameOCorreo(usuario.getUsername(), usuario.getCorreo(), usuario.getIdUsuario())) {
            request.setAttribute("error", "El username o el correo ya est\u00e1 registrado.");
            request.setAttribute("usuario", usuario);
            request.setAttribute("roles", usuarioDAO.listarRolesActivos());
            reenviar(request, response, "/vistas/usuarios/editar.jsp");
            return;
        }

        usuarioDAO.actualizar(usuario);
        response.sendRedirect("UsuarioServlet?tab=usuarios&mensaje=actualizado");
    }

    private void desactivarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idUsuario = obtenerIdUsuario(request);
        usuarioDAO.desactivar(idUsuario);
        response.sendRedirect("UsuarioServlet?tab=usuarios&mensaje=desactivado");
    }

    private void cambiarEstadoUsuario(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idUsuario = obtenerIdUsuario(request);
        String estado = "activo".equalsIgnoreCase(request.getParameter("estado")) ? "activo" : "inactivo";
        usuarioDAO.actualizarEstado(idUsuario, estado);
        response.sendRedirect("UsuarioServlet?tab=usuarios&mensaje=usuario_estado");
    }

    private void registrarRol(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Rol rol = leerRolDesdeFormulario(request);

        if (!rolValido(rol)) {
            request.setAttribute("error", "Completa el nombre del rol.");
            request.setAttribute("tabActivo", "roles");
            cargarDatosPrincipal(request);
            reenviar(request, response, "/vistas/usuarios/listar.jsp");
            return;
        }

        usuarioDAO.registrarRol(rol);
        response.sendRedirect("UsuarioServlet?tab=roles&mensaje=rol_registrado");
    }

    private void actualizarRol(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Rol rol = leerRolDesdeFormulario(request);
        rol.setIdRol(obtenerIdRol(request));

        if (!rolValido(rol)) {
            request.setAttribute("error", "Completa el nombre del rol.");
            request.setAttribute("tabActivo", "roles");
            cargarDatosPrincipal(request);
            reenviar(request, response, "/vistas/usuarios/listar.jsp");
            return;
        }

        usuarioDAO.actualizarRol(rol);
        response.sendRedirect("UsuarioServlet?tab=roles&mensaje=rol_actualizado");
    }

    private void cambiarEstadoRol(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int idRol = obtenerIdRol(request);
        String estado = "activo".equalsIgnoreCase(request.getParameter("estado")) ? "activo" : "inactivo";
        usuarioDAO.actualizarEstadoRol(idRol, estado);
        response.sendRedirect("UsuarioServlet?tab=roles&mensaje=rol_estado");
    }

    private Usuario leerUsuarioDesdeFormulario(HttpServletRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNombres(request.getParameter("nombres"));
        usuario.setApellidos(request.getParameter("apellidos"));
        usuario.setCorreo(request.getParameter("correo"));
        usuario.setUsername(request.getParameter("username"));
        usuario.setPassword(request.getParameter("password"));
        usuario.setTelefono(request.getParameter("telefono"));
        usuario.setEstado(request.getParameter("estado"));
        usuario.setIdRol(obtenerIdRol(request));
        return usuario;
    }

    private Rol leerRolDesdeFormulario(HttpServletRequest request) {
        Rol rol = new Rol();
        rol.setNombreRol(request.getParameter("nombreRol"));
        rol.setDescripcion(request.getParameter("descripcion"));
        rol.setEstado("inactivo".equalsIgnoreCase(request.getParameter("estado")) ? "inactivo" : "activo");
        return rol;
    }

    private boolean datosValidos(Usuario usuario, boolean validarPassword) {
        boolean datosObligatorios = tieneTexto(usuario.getNombres())
                && tieneTexto(usuario.getApellidos())
                && tieneTexto(usuario.getCorreo())
                && tieneTexto(usuario.getUsername())
                && usuario.getIdRol() > 0;

        if (validarPassword) {
            return datosObligatorios && tieneTexto(usuario.getPassword());
        }
        return datosObligatorios;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private boolean rolValido(Rol rol) {
        return rol != null && tieneTexto(rol.getNombreRol());
    }

    private int obtenerIdUsuario(HttpServletRequest request) {
        String idUsuario = request.getParameter("idUsuario");
        return Integer.parseInt(idUsuario);
    }

    private int obtenerIdRol(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getParameter("idRol"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void reenviar(HttpServletRequest request, HttpServletResponse response, String ruta)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(ruta);
        dispatcher.forward(request, response);
    }
}
