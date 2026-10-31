package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Categoria;

public class CategoriaDAO {

    public List<Categoria> listar() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre_categoria, descripcion, estado "
                + "FROM categoria ORDER BY id_categoria DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                categoria.setDescripcion(rs.getString("descripcion"));
                categoria.setEstado(rs.getString("estado"));
                categorias.add(categoria);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorias", e);
        }

        return categorias;
    }

    public Categoria buscarPorId(int idCategoria) {
        String sql = "SELECT id_categoria, nombre_categoria, descripcion, estado "
                + "FROM categoria WHERE id_categoria = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Categoria categoria = new Categoria();
                    categoria.setIdCategoria(rs.getInt("id_categoria"));
                    categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                    categoria.setDescripcion(rs.getString("descripcion"));
                    categoria.setEstado(rs.getString("estado"));
                    return categoria;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoria", e);
        }

        return null;
    }

    public List<Categoria> listarActivas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre_categoria, descripcion, estado "
                + "FROM categoria WHERE estado = 'activo' ORDER BY nombre_categoria";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                categoria.setDescripcion(rs.getString("descripcion"));
                categoria.setEstado(rs.getString("estado"));
                categorias.add(categoria);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorias activas", e);
        }

        return categorias;
    }

    public List<Categoria> listarConCantidadProductos() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT c.id_categoria, c.nombre_categoria, c.descripcion, c.estado, "
                + "COUNT(p.id_producto) AS cantidad_productos, "
                + "SUM(CASE WHEN p.disponibilidad = 'disponible' AND p.estado = 'activo' THEN 1 ELSE 0 END) AS cantidad_disponibles, "
                + "SUM(CASE WHEN p.estado = 'inactivo' THEN 1 ELSE 0 END) AS cantidad_inactivos "
                + "FROM categoria c "
                + "LEFT JOIN producto p ON p.id_categoria = c.id_categoria "
                + "GROUP BY c.id_categoria, c.nombre_categoria, c.descripcion, c.estado "
                + "ORDER BY c.nombre_categoria";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setNombreCategoria(rs.getString("nombre_categoria"));
                categoria.setDescripcion(rs.getString("descripcion"));
                categoria.setEstado(rs.getString("estado"));
                categoria.setCantidadProductos(rs.getInt("cantidad_productos"));
                categoria.setCantidadDisponibles(rs.getInt("cantidad_disponibles"));
                categoria.setCantidadInactivos(rs.getInt("cantidad_inactivos"));
                categorias.add(categoria);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorias con productos", e);
        }

        return categorias;
    }

    public boolean registrar(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre_categoria, descripcion, estado) VALUES (?, ?, ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombreCategoria());
            ps.setString(2, categoria.getDescripcion());
            ps.setString(3, categoria.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar categoria", e);
        }
    }

    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre_categoria = ?, descripcion = ?, estado = ? "
                + "WHERE id_categoria = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombreCategoria());
            ps.setString(2, categoria.getDescripcion());
            ps.setString(3, categoria.getEstado());
            ps.setInt(4, categoria.getIdCategoria());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar categoria", e);
        }
    }

    public boolean desactivar(int idCategoria) {
        String sql = "UPDATE categoria SET estado = 'inactivo' WHERE id_categoria = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idCategoria);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar categoria", e);
        }
    }

    public boolean actualizarEstado(int idCategoria, String estado) {
        String sql = "UPDATE categoria SET estado = ? WHERE id_categoria = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idCategoria);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de categoria", e);
        }
    }
}
