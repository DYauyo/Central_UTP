package dao;

import config.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.InsumoRequerido;
import modelo.Producto;
import modelo.RecetaProducto;

public class ProductoInsumoDAO {

    public List<RecetaProducto> listarResumenRecetas() {
        List<RecetaProducto> recetasResumen = new ArrayList<>();
        Map<Integer, List<InsumoRequerido>> recetas = listarRecetas();
        String sql = "SELECT p.id_producto, p.nombre_producto, p.descripcion, p.precio, "
                + "p.disponibilidad, p.imagen, p.estado, p.id_categoria, c.nombre_categoria, "
                + "c.estado AS estado_categoria "
                + "FROM producto p "
                + "INNER JOIN categoria c ON p.id_categoria = c.id_categoria "
                + "ORDER BY c.nombre_categoria, p.nombre_producto";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
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

                RecetaProducto receta = new RecetaProducto(producto);
                receta.setInsumos(recetas.get(producto.getIdProducto()));
                recetasResumen.add(receta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar resumen de recetas", e);
        }

        return recetasResumen;
    }

    public List<InsumoRequerido> listarPorProducto(int idProducto) {
        List<InsumoRequerido> insumos = new ArrayList<>();
        String sql = "SELECT pi.id_producto, pi.id_insumo, pi.cantidad_requerida, "
                + "i.nombre_insumo, i.unidad_medida, i.stock_actual, i.stock_minimo "
                + "FROM producto_insumo pi "
                + "INNER JOIN insumo i ON pi.id_insumo = i.id_insumo "
                + "WHERE pi.id_producto = ? "
                + "ORDER BY i.nombre_insumo";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    insumos.add(mapearInsumoRequerido(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar receta del producto", e);
        }

        return insumos;
    }

    public Map<Integer, List<InsumoRequerido>> listarRecetas() {
        Map<Integer, List<InsumoRequerido>> recetas = new HashMap<>();
        String sql = "SELECT pi.id_producto, pi.id_insumo, pi.cantidad_requerida, "
                + "i.nombre_insumo, i.unidad_medida, i.stock_actual, i.stock_minimo "
                + "FROM producto_insumo pi "
                + "INNER JOIN insumo i ON pi.id_insumo = i.id_insumo "
                + "WHERE i.estado = 'activo' "
                + "ORDER BY pi.id_producto, i.nombre_insumo";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InsumoRequerido insumo = mapearInsumoRequerido(rs);
                recetas.computeIfAbsent(insumo.getIdProducto(), id -> new ArrayList<>()).add(insumo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar recetas de productos", e);
        }

        return recetas;
    }

    public void guardarReceta(int idProducto, Map<Integer, BigDecimal> cantidadesPorInsumo) {
        String eliminarSql = "DELETE FROM producto_insumo WHERE id_producto = ?";
        String insertarSql = "INSERT INTO producto_insumo "
                + "(cantidad_requerida, id_producto, id_insumo) VALUES (?, ?, ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);

            try (PreparedStatement eliminar = cn.prepareStatement(eliminarSql)) {
                eliminar.setInt(1, idProducto);
                eliminar.executeUpdate();
            }

            try (PreparedStatement insertar = cn.prepareStatement(insertarSql)) {
                for (Map.Entry<Integer, BigDecimal> entry : cantidadesPorInsumo.entrySet()) {
                    insertar.setBigDecimal(1, entry.getValue());
                    insertar.setInt(2, idProducto);
                    insertar.setInt(3, entry.getKey());
                    insertar.addBatch();
                }
                insertar.executeBatch();
            }

            cn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar receta del producto", e);
        }
    }

    public void eliminarReceta(int idProducto) {
        String sql = "DELETE FROM producto_insumo WHERE id_producto = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al limpiar receta del producto", e);
        }
    }

    private InsumoRequerido mapearInsumoRequerido(ResultSet rs) throws SQLException {
        InsumoRequerido insumo = new InsumoRequerido();
        insumo.setIdProducto(rs.getInt("id_producto"));
        insumo.setIdInsumo(rs.getInt("id_insumo"));
        insumo.setNombreInsumo(rs.getString("nombre_insumo"));
        insumo.setUnidadMedida(rs.getString("unidad_medida"));
        insumo.setCantidadPorPorcion(rs.getBigDecimal("cantidad_requerida"));
        insumo.setStockActual(rs.getBigDecimal("stock_actual"));
        insumo.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        return insumo;
    }
}
