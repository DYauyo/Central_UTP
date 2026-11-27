package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public record Proveedor(int id, String ruc, String razonSocial, String telefono,
                            String correo, String estado) {
    }

    public record Asociacion(int idInsumo, int idProveedor, String insumo, String proveedor) {
    }

    public List<Proveedor> listar() throws SQLException {
        String sql = "SELECT id_proveedor, ruc, razon_social, telefono, correo, estado "
                + "FROM proveedor ORDER BY razon_social, id_proveedor";
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(new Proveedor(rs.getInt("id_proveedor"), rs.getString("ruc"),
                        rs.getString("razon_social"), rs.getString("telefono"),
                        rs.getString("correo"), rs.getString("estado")));
            }
        }
        return proveedores;
    }

    public void guardar(Proveedor proveedor) throws SQLException {
        boolean nuevo = proveedor.id() == 0;
        String sql = nuevo
                ? "INSERT INTO proveedor (ruc, razon_social, telefono, correo, estado) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE proveedor SET ruc=?, razon_social=?, telefono=?, correo=?, estado=? WHERE id_proveedor=?";
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, proveedor.ruc());
            ps.setString(2, proveedor.razonSocial());
            ps.setString(3, proveedor.telefono());
            ps.setString(4, proveedor.correo());
            ps.setString(5, proveedor.estado());
            if (!nuevo) {
                ps.setInt(6, proveedor.id());
            }
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("El proveedor ya no existe.");
            }
        }
    }

    public List<Asociacion> listarAsociaciones() throws SQLException {
        String sql = "SELECT i.id_insumo, p.id_proveedor, i.nombre_insumo, p.razon_social "
                + "FROM insumo_proveedor ip JOIN insumo i ON ip.id_insumo=i.id_insumo "
                + "JOIN proveedor p ON ip.id_proveedor=p.id_proveedor "
                + "ORDER BY i.nombre_insumo, p.razon_social";
        List<Asociacion> asociaciones = new ArrayList<>();
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                asociaciones.add(new Asociacion(rs.getInt("id_insumo"), rs.getInt("id_proveedor"),
                        rs.getString("nombre_insumo"), rs.getString("razon_social")));
            }
        }
        return asociaciones;
    }

    public void cambiarAsociacion(int insumo, int proveedor, boolean vincular) throws SQLException {
        String sql = vincular
                ? "INSERT INTO insumo_proveedor (id_insumo, id_proveedor) "
                    + "SELECT i.id_insumo, p.id_proveedor FROM insumo i JOIN proveedor p ON p.id_proveedor=? "
                    + "WHERE i.id_insumo=? AND i.estado='activo' AND p.estado='activo' "
                    + "ON DUPLICATE KEY UPDATE insumo_proveedor.id_proveedor=insumo_proveedor.id_proveedor"
                : "DELETE FROM insumo_proveedor WHERE id_proveedor=? AND id_insumo=?";
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, proveedor);
            ps.setInt(2, insumo);
            if (ps.executeUpdate() == 0 && vincular) {
                throw new IllegalArgumentException("Selecciona un insumo y un proveedor activos.");
            }
        }
    }
}
