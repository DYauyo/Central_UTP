package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Rol;
import modelo.Usuario;

public class UsuarioDAO {

    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombres, u.apellidos, u.correo, u.username, "
                + "u.password, u.telefono, u.estado, u.id_rol, r.nombre_rol, r.estado AS estado_rol "
                + "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol "
                + "ORDER BY u.id_usuario DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios", e);
        }

        return usuarios;
    }

    public Usuario buscarPorId(int idUsuario) {
        String sql = "SELECT u.id_usuario, u.nombres, u.apellidos, u.correo, u.username, "
                + "u.password, u.telefono, u.estado, u.id_rol, r.nombre_rol, r.estado AS estado_rol "
                + "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol "
                + "WHERE u.id_usuario = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario", e);
        }

        return null;
    }

    public Usuario validarLogin(String username, String password) {
        String sql = "SELECT u.id_usuario, u.nombres, u.apellidos, u.correo, u.username, "
                + "u.password, u.telefono, u.estado, u.id_rol, r.nombre_rol, r.estado AS estado_rol "
                + "FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol "
                + "WHERE u.username = ? AND u.estado = 'activo' AND r.estado = 'activo' "
                + "AND (u.password = ? OR u.password = SHA2(?, 256))";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al validar login", e);
        }

        return null;
    }

    public List<Rol> listarRolesActivos() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre_rol, descripcion, estado "
                + "FROM rol WHERE estado = 'activo' ORDER BY nombre_rol";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("id_rol"));
                rol.setNombreRol(rs.getString("nombre_rol"));
                rol.setDescripcion(rs.getString("descripcion"));
                rol.setEstado(rs.getString("estado"));
                roles.add(rol);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar roles activos", e);
        }

        return roles;
    }

    public List<Rol> listarRoles() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT r.id_rol, r.nombre_rol, r.descripcion, r.estado, "
                + "COUNT(u.id_usuario) AS cantidad_usuarios "
                + "FROM rol r "
                + "LEFT JOIN usuario u ON r.id_rol = u.id_rol "
                + "GROUP BY r.id_rol, r.nombre_rol, r.descripcion, r.estado "
                + "ORDER BY r.id_rol";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roles.add(mapearRol(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar roles", e);
        }

        return roles;
    }

    public Rol buscarRolPorId(int idRol) {
        String sql = "SELECT r.id_rol, r.nombre_rol, r.descripcion, r.estado, "
                + "COUNT(u.id_usuario) AS cantidad_usuarios "
                + "FROM rol r "
                + "LEFT JOIN usuario u ON r.id_rol = u.id_rol "
                + "WHERE r.id_rol = ? "
                + "GROUP BY r.id_rol, r.nombre_rol, r.descripcion, r.estado";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idRol);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearRol(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol", e);
        }

        return null;
    }

    public boolean registrarRol(Rol rol) {
        String sql = "INSERT INTO rol (nombre_rol, descripcion, estado) VALUES (?, ?, ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, rol.getNombreRol());
            ps.setString(2, rol.getDescripcion());
            ps.setString(3, rol.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar rol", e);
        }
    }

    public boolean actualizarRol(Rol rol) {
        String sql = "UPDATE rol SET nombre_rol = ?, descripcion = ?, estado = ? WHERE id_rol = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, rol.getNombreRol());
            ps.setString(2, rol.getDescripcion());
            ps.setString(3, rol.getEstado());
            ps.setInt(4, rol.getIdRol());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar rol", e);
        }
    }

    public boolean actualizarEstadoRol(int idRol, String estado) {
        String sql = "UPDATE rol SET estado = ? WHERE id_rol = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idRol);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado del rol", e);
        }
    }

    public boolean registrar(Usuario usuario) {
        // La contrasena se almacena cifrada con SHA-256 (RNF1)
        String sql = "INSERT INTO usuario (nombres, apellidos, correo, username, password, "
                + "telefono, estado, id_rol) VALUES (?, ?, ?, ?, SHA2(?, 256), ?, ?, ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getUsername());
            ps.setString(5, usuario.getPassword());
            ps.setString(6, usuario.getTelefono());
            ps.setString(7, usuario.getEstado());
            ps.setInt(8, usuario.getIdRol());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar usuario", e);
        }
    }

    public boolean actualizar(Usuario usuario) {
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            return actualizarSinPassword(usuario);
        }
        return actualizarConPassword(usuario);
    }

    private boolean actualizarConPassword(Usuario usuario) {
        String sql = "UPDATE usuario SET nombres = ?, apellidos = ?, correo = ?, username = ?, "
                + "password = SHA2(?, 256), telefono = ?, estado = ?, id_rol = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getUsername());
            ps.setString(5, usuario.getPassword());
            ps.setString(6, usuario.getTelefono());
            ps.setString(7, usuario.getEstado());
            ps.setInt(8, usuario.getIdRol());
            ps.setInt(9, usuario.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    private boolean actualizarSinPassword(Usuario usuario) {
        String sql = "UPDATE usuario SET nombres = ?, apellidos = ?, correo = ?, username = ?, "
                + "telefono = ?, estado = ?, id_rol = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getUsername());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getEstado());
            ps.setInt(7, usuario.getIdRol());
            ps.setInt(8, usuario.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    /**
     * Verifica si el username o el correo ya pertenecen a otro usuario.
     * idUsuarioExcluir = 0 al registrar; el id del usuario al editar.
     */
    public boolean existeUsernameOCorreo(String username, String correo, int idUsuarioExcluir) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE (username = ? OR (? <> '' AND correo = ?)) "
                + "AND id_usuario <> ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            String correoNormalizado = correo == null ? "" : correo.trim();
            ps.setString(1, username == null ? "" : username.trim());
            ps.setString(2, correoNormalizado);
            ps.setString(3, correoNormalizado);
            ps.setInt(4, idUsuarioExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar duplicados de usuario", e);
        }
    }

    public boolean desactivar(int idUsuario) {
        String sql = "UPDATE usuario SET estado = 'inactivo' WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar usuario", e);
        }
    }

    public boolean actualizarEstado(int idUsuario, String estado) {
        String sql = "UPDATE usuario SET estado = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado del usuario", e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setEstado(rs.getString("estado"));
        usuario.setIdRol(rs.getInt("id_rol"));
        usuario.setNombreRol(rs.getString("nombre_rol"));
        usuario.setEstadoRol(rs.getString("estado_rol"));
        return usuario;
    }

    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("descripcion"));
        rol.setEstado(rs.getString("estado"));
        rol.setCantidadUsuarios(rs.getInt("cantidad_usuarios"));
        return rol;
    }
}
