package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;

public class ProductoDAO {

    public List<Producto> listar() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre_producto, p.descripcion, p.precio, "
                + "p.disponibilidad, p.imagen, p.estado, p.id_categoria, c.nombre_categoria, "
                + "c.estado AS estado_categoria "
                + "FROM producto p INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "ORDER BY p.id_producto DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos", e);
        }

        return productos;
    }

    public List<Producto> listarActivosParaPedido() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre_producto, p.descripcion, p.precio, "
                + "p.disponibilidad, p.imagen, p.estado, p.id_categoria, c.nombre_categoria, "
                + "c.estado AS estado_categoria, "
                + "(d.cantidad_planificada - d.cantidad_vendida) AS cantidad_disponible "
                + "FROM producto p INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "INNER JOIN disponibilidad_producto d ON d.id_producto = p.id_producto AND d.fecha = CURDATE() "
                + "WHERE p.estado = 'activo' AND c.estado = 'activo' "
                + "AND p.disponibilidad = 'disponible' "
                + "AND d.estado = 'activo' "
                + "AND d.cantidad_planificada > d.cantidad_vendida "
                + "ORDER BY c.nombre_categoria, p.nombre_producto";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos activos para pedido", e);
        }

        return productos;
    }

    public Producto buscarPorId(int idProducto) {
        String sql = "SELECT p.id_producto, p.nombre_producto, p.descripcion, p.precio, "
                + "p.disponibilidad, p.imagen, p.estado, p.id_categoria, c.nombre_categoria, "
                + "c.estado AS estado_categoria "
                + "FROM producto p INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE p.id_producto = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto", e);
        }

        return null;
    }

    public boolean registrar(Producto producto) {
        String sql = "INSERT INTO producto (nombre_producto, descripcion, precio, disponibilidad, "
                + "imagen, estado, id_categoria) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, producto.getNombreProducto());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setString(4, producto.getDisponibilidad());
            ps.setString(5, producto.getImagen());
            ps.setString(6, producto.getEstado());
            ps.setInt(7, producto.getIdCategoria());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar producto", e);
        }
    }

    public boolean actualizar(Producto producto) {
        String sql = "UPDATE producto SET nombre_producto = ?, descripcion = ?, precio = ?, "
                + "disponibilidad = ?, imagen = ?, estado = ?, id_categoria = ? "
                + "WHERE id_producto = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, producto.getNombreProducto());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setString(4, producto.getDisponibilidad());
            ps.setString(5, producto.getImagen());
            ps.setString(6, producto.getEstado());
            ps.setInt(7, producto.getIdCategoria());
            ps.setInt(8, producto.getIdProducto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto", e);
        }
    }

    public boolean desactivar(int idProducto) {
        String sql = "UPDATE producto SET estado = 'inactivo' WHERE id_producto = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar producto", e);
        }
    }

    public boolean actualizarEstado(int idProducto, String estado) {
        String sql = "UPDATE producto SET estado = ? WHERE id_producto = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado del producto", e);
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombreProducto(rs.getString("nombre_producto"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setDisponibilidad(rs.getString("disponibilidad"));
        producto.setImagen(rs.getString("imagen"));
        producto.setEstado(rs.getString("estado"));
        producto.setIdCategoria(rs.getInt("id_categoria"));
        producto.setNombreCategoria(rs.getString("nombre_categoria"));
        producto.setEstadoCategoria(rs.getString("estado_categoria"));
        try {
            producto.setCantidadDisponible(rs.getInt("cantidad_disponible"));
        } catch (SQLException e) {
            producto.setCantidadDisponible(0);
        }
        return producto;
    }
}
