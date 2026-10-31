package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Insumo;

public class InsumoDAO {

    public List<Insumo> listar() {
        List<Insumo> insumos = new ArrayList<>();
        String sql = "SELECT id_insumo, nombre_insumo, descripcion, unidad_medida, "
                + "stock_actual, stock_minimo, estado "
                + "FROM insumo ORDER BY id_insumo DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                insumos.add(mapearInsumo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar insumos", e);
        }

        return insumos;
    }

    public List<Insumo> listarActivos() {
        List<Insumo> insumos = new ArrayList<>();
        String sql = "SELECT id_insumo, nombre_insumo, descripcion, unidad_medida, "
                + "stock_actual, stock_minimo, estado "
                + "FROM insumo WHERE estado = 'activo' ORDER BY nombre_insumo";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                insumos.add(mapearInsumo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar insumos activos", e);
        }

        return insumos;
    }

    public Insumo buscarPorId(int idInsumo) {
        String sql = "SELECT id_insumo, nombre_insumo, descripcion, unidad_medida, "
                + "stock_actual, stock_minimo, estado "
                + "FROM insumo WHERE id_insumo = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idInsumo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearInsumo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar insumo", e);
        }

        return null;
    }

    public boolean registrar(Insumo insumo) {
        String sql = "INSERT INTO insumo (nombre_insumo, descripcion, unidad_medida, "
                + "stock_actual, stock_minimo, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, insumo.getNombreInsumo());
            ps.setString(2, insumo.getDescripcion());
            ps.setString(3, insumo.getUnidadMedida());
            ps.setBigDecimal(4, insumo.getStockActual());
            ps.setBigDecimal(5, insumo.getStockMinimo());
            ps.setString(6, insumo.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar insumo", e);
        }
    }

    public boolean actualizar(Insumo insumo) {
        String sql = "UPDATE insumo SET nombre_insumo = ?, descripcion = ?, unidad_medida = ?, "
                + "stock_actual = ?, stock_minimo = ?, estado = ? WHERE id_insumo = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, insumo.getNombreInsumo());
            ps.setString(2, insumo.getDescripcion());
            ps.setString(3, insumo.getUnidadMedida());
            ps.setBigDecimal(4, insumo.getStockActual());
            ps.setBigDecimal(5, insumo.getStockMinimo());
            ps.setString(6, insumo.getEstado());
            ps.setInt(7, insumo.getIdInsumo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar insumo", e);
        }
    }

    public boolean desactivar(int idInsumo) {
        String sql = "UPDATE insumo SET estado = 'inactivo' WHERE id_insumo = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idInsumo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar insumo", e);
        }
    }

    public boolean actualizarEstado(int idInsumo, String estado) {
        String sql = "UPDATE insumo SET estado = ? WHERE id_insumo = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idInsumo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado del insumo", e);
        }
    }

    private Insumo mapearInsumo(ResultSet rs) throws SQLException {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(rs.getInt("id_insumo"));
        insumo.setNombreInsumo(rs.getString("nombre_insumo"));
        insumo.setDescripcion(rs.getString("descripcion"));
        insumo.setUnidadMedida(rs.getString("unidad_medida"));
        insumo.setStockActual(rs.getBigDecimal("stock_actual"));
        insumo.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        insumo.setEstado(rs.getString("estado"));
        return insumo;
    }
}
