package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.DisponibilidadProducto;
import modelo.InsumoRequerido;

public class DisponibilidadProductoDAO {

    private final ProductoInsumoDAO productoInsumoDAO = new ProductoInsumoDAO();

    public List<DisponibilidadProducto> listarPorFecha(LocalDate fecha) {
        List<DisponibilidadProducto> disponibilidades = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.id_categoria, p.nombre_producto, p.imagen, p.disponibilidad, "
                + "p.estado AS estado_producto, c.nombre_categoria, "
                + "COALESCE(d.id_disponibilidad, 0) AS id_disponibilidad, "
                + "COALESCE(d.fecha, ?) AS fecha, "
                + "COALESCE(d.cantidad_planificada, 0) AS cantidad_planificada, "
                + "COALESCE(d.cantidad_vendida, 0) AS cantidad_vendida, "
                + "COALESCE(d.estado, 'pendiente') AS estado "
                + "FROM producto p "
                + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "LEFT JOIN disponibilidad_producto d "
                + "ON d.id_producto = p.id_producto AND d.fecha = ? "
                + "WHERE c.estado = 'activo' "
                + "AND p.estado = 'activo' "
                + "ORDER BY c.nombre_categoria, p.nombre_producto";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            Date fechaSql = Date.valueOf(fecha);
            ps.setDate(1, fechaSql);
            ps.setDate(2, fechaSql);

            Map<Integer, List<InsumoRequerido>> recetas = productoInsumoDAO.listarRecetas();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DisponibilidadProducto disponibilidad = mapear(rs);
                    disponibilidad.setInsumos(recetas.get(disponibilidad.getIdProducto()));
                    disponibilidades.add(disponibilidad);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar disponibilidad diaria", e);
        }

        return disponibilidades;
    }

    public void guardarEstimacion(LocalDate fecha, int idProducto, int cantidadPlanificada, String estado) {
        String sql = "INSERT INTO disponibilidad_producto "
                + "(fecha, cantidad_planificada, cantidad_vendida, estado, id_producto) "
                + "VALUES (?, ?, 0, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "cantidad_planificada = VALUES(cantidad_planificada), "
                + "cantidad_vendida = LEAST(cantidad_vendida, VALUES(cantidad_planificada)), "
                + "estado = VALUES(estado)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));
            ps.setInt(2, Math.max(0, cantidadPlanificada));
            ps.setString(3, estado == null || estado.trim().isEmpty() ? "activo" : estado);
            ps.setInt(4, idProducto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar estimacion diaria", e);
        }
    }

    public void asegurarDisponibilidadInicial(LocalDate fecha) {
        String sql = "INSERT INTO disponibilidad_producto "
                + "(fecha, cantidad_planificada, cantidad_vendida, estado, id_producto) "
                + "SELECT ?, 0, 0, 'pendiente', p.id_producto "
                + "FROM producto p "
                + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE c.estado = 'activo' "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM disponibilidad_producto d "
                + "WHERE d.id_producto = p.id_producto AND d.fecha = ?"
                + ")";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            Date fechaSql = Date.valueOf(fecha);
            ps.setDate(1, fechaSql);
            ps.setDate(2, fechaSql);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al preparar disponibilidad diaria", e);
        }
    }

    public int contarDisponibles(LocalDate fecha) {
        String sql = "SELECT COUNT(*) "
                + "FROM disponibilidad_producto d "
                + "INNER JOIN producto p ON d.id_producto = p.id_producto "
                + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE d.fecha = ? AND d.estado = 'activo' "
                + "AND c.estado = 'activo' "
                + "AND d.cantidad_planificada > d.cantidad_vendida";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar productos disponibles", e);
        }

        return 0;
    }

    public int contarEstimados(LocalDate fecha) {
        String sql = "SELECT COUNT(*) "
                + "FROM disponibilidad_producto d "
                + "INNER JOIN producto p ON d.id_producto = p.id_producto "
                + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "WHERE d.fecha = ? "
                + "AND c.estado = 'activo' "
                + "AND d.cantidad_planificada > 0";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar estimaciones", e);
        }

        return 0;
    }

    private DisponibilidadProducto mapear(ResultSet rs) throws SQLException {
        DisponibilidadProducto disponibilidad = new DisponibilidadProducto();
        disponibilidad.setIdProducto(rs.getInt("id_producto"));
        disponibilidad.setIdCategoria(rs.getInt("id_categoria"));
        disponibilidad.setNombreProducto(rs.getString("nombre_producto"));
        disponibilidad.setImagen(rs.getString("imagen"));
        disponibilidad.setDisponibilidadProducto(rs.getString("disponibilidad"));
        disponibilidad.setEstadoProducto(rs.getString("estado_producto"));
        disponibilidad.setNombreCategoria(rs.getString("nombre_categoria"));
        disponibilidad.setIdDisponibilidad(rs.getInt("id_disponibilidad"));
        disponibilidad.setFecha(rs.getDate("fecha").toLocalDate());
        disponibilidad.setCantidadPlanificada(rs.getInt("cantidad_planificada"));
        disponibilidad.setCantidadVendida(rs.getInt("cantidad_vendida"));
        disponibilidad.setEstado(rs.getString("estado"));
        return disponibilidad;
    }
}
