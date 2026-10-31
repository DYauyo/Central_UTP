package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Mesa;

public class MesaDAO {

    public List<Mesa> listarConPedidoAbierto() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT m.id_mesa, m.numero_mesa, m.capacidad, m.estado_mesa, "
                + "m.ubicacion, m.estado, COALESCE(p.id_pedido, 0) AS id_pedido, "
                + "COALESCE(p.estado_pedido, '') AS estado_pedido, "
                + "COALESCE(p.total, 0) AS total, COALESCE(p.observacion, '') AS cliente, "
                + "COALESCE(NULLIF(u.username, ''), 'mozo') AS mozo, "
                + "COALESCE(mu.mesas_unidas, '') AS mesas_unidas, "
                + "COALESCE(mp.principal, 0) AS principal_union, "
                + "CASE WHEN p.id_pedido IS NULL THEN '' "
                + "WHEN TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) >= 60 "
                + "THEN CONCAT(FLOOR(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) / 60), 'h ', "
                + "MOD(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 60), 'min') "
                + "ELSE CONCAT(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 'min') END AS tiempo_abierto "
                + "FROM mesa m "
                + "LEFT JOIN mesa_pedido mp ON mp.id_mesa = m.id_mesa "
                + "LEFT JOIN pedido p ON (p.id_mesa = m.id_mesa OR p.id_pedido = mp.id_pedido) "
                + "AND p.estado_pedido IN ('pendiente', 'recibido', 'en preparacion', 'listo', 'entregado') "
                + "LEFT JOIN (SELECT x.id_pedido, GROUP_CONCAT(m2.numero_mesa ORDER BY m2.numero_mesa SEPARATOR ', ') AS mesas_unidas "
                + "FROM mesa_pedido x INNER JOIN mesa m2 ON m2.id_mesa = x.id_mesa GROUP BY x.id_pedido) mu ON mu.id_pedido = p.id_pedido "
                + "LEFT JOIN usuario u ON u.id_usuario = p.id_usuario "
                + "WHERE m.estado = 'activo' "
                + "ORDER BY m.numero_mesa";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mesas.add(mapearMesa(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar mesas", e);
        }

        return mesas;
    }

    public Mesa buscarPorId(int idMesa) {
        String sql = "SELECT m.id_mesa, m.numero_mesa, m.capacidad, m.estado_mesa, "
                + "m.ubicacion, m.estado, COALESCE(p.id_pedido, 0) AS id_pedido, "
                + "COALESCE(p.estado_pedido, '') AS estado_pedido, "
                + "COALESCE(p.total, 0) AS total, COALESCE(p.observacion, '') AS cliente, "
                + "COALESCE(NULLIF(u.username, ''), 'mozo') AS mozo, "
                + "COALESCE(mu.mesas_unidas, '') AS mesas_unidas, "
                + "COALESCE(mp.principal, 0) AS principal_union, "
                + "CASE WHEN p.id_pedido IS NULL THEN '' "
                + "WHEN TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) >= 60 "
                + "THEN CONCAT(FLOOR(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) / 60), 'h ', "
                + "MOD(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 60), 'min') "
                + "ELSE CONCAT(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 'min') END AS tiempo_abierto "
                + "FROM mesa m "
                + "LEFT JOIN mesa_pedido mp ON mp.id_mesa = m.id_mesa "
                + "LEFT JOIN pedido p ON (p.id_mesa = m.id_mesa OR p.id_pedido = mp.id_pedido) "
                + "AND p.estado_pedido IN ('pendiente', 'recibido', 'en preparacion', 'listo', 'entregado') "
                + "LEFT JOIN (SELECT x.id_pedido, GROUP_CONCAT(m2.numero_mesa ORDER BY m2.numero_mesa SEPARATOR ', ') AS mesas_unidas "
                + "FROM mesa_pedido x INNER JOIN mesa m2 ON m2.id_mesa = x.id_mesa GROUP BY x.id_pedido) mu ON mu.id_pedido = p.id_pedido "
                + "LEFT JOIN usuario u ON u.id_usuario = p.id_usuario "
                + "WHERE m.id_mesa = ? LIMIT 1";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMesa(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar mesa", e);
        }

        return null;
    }

    public boolean actualizarEstadoMesa(int idMesa, String estadoMesa) {
        String sql = "UPDATE mesa SET estado_mesa = ? WHERE id_mesa = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estadoMesa);
            ps.setInt(2, idMesa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de mesa", e);
        }
    }

    private Mesa mapearMesa(ResultSet rs) throws SQLException {
        Mesa mesa = new Mesa();
        mesa.setIdMesa(rs.getInt("id_mesa"));
        mesa.setNumeroMesa(rs.getInt("numero_mesa"));
        mesa.setCapacidad(rs.getInt("capacidad"));
        mesa.setEstadoMesa(rs.getString("estado_mesa"));
        mesa.setUbicacion(rs.getString("ubicacion"));
        mesa.setEstado(rs.getString("estado"));
        mesa.setIdPedidoAbierto(rs.getInt("id_pedido"));
        mesa.setTotalPedido(rs.getBigDecimal("total"));
        mesa.setCliente(rs.getString("cliente"));
        mesa.setMozo(rs.getString("mozo"));
        mesa.setTiempoAbierto(rs.getString("tiempo_abierto"));
        mesa.setMesasUnidas(rs.getString("mesas_unidas"));
        mesa.setEstadoPedido(rs.getString("estado_pedido"));
        mesa.setMesaPrincipalUnion(rs.getBoolean("principal_union"));
        return mesa;
    }
}
