package dao;

import config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Reserva;

public class ReservaDAO {

    public void registrar(Reserva reserva) {
        String sql = "INSERT INTO reserva (cliente, telefono, cantidad_personas, fecha_reserva, observacion, estado_reserva, id_mesa) "
                + "VALUES (?, ?, ?, ?, ?, 'programada', ?)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, reserva.getCliente());
            ps.setString(2, reserva.getTelefono());
            ps.setInt(3, reserva.getCantidadPersonas());
            ps.setString(4, reserva.getFechaReserva());
            ps.setString(5, reserva.getObservacion());
            if (reserva.getIdMesa() > 0) {
                ps.setInt(6, reserva.getIdMesa());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar reserva", e);
        }
    }

    public List<Reserva> listarVigentes() {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.cliente, r.telefono, r.cantidad_personas, "
                + "DATE_FORMAT(r.fecha_reserva, '%Y-%m-%d %H:%i') AS fecha_reserva, "
                + "DATE_FORMAT(r.fecha_reserva, '%H:%i') AS hora_reserva, "
                + "r.observacion, r.estado_reserva, COALESCE(r.id_mesa, 0) AS id_mesa, "
                + "COALESCE(m.numero_mesa, 0) AS numero_mesa, "
                + "TIMESTAMPDIFF(MINUTE, NOW(), r.fecha_reserva) AS minutos_para_reserva "
                + "FROM reserva r "
                + "LEFT JOIN mesa m ON m.id_mesa = r.id_mesa "
                + "WHERE r.estado_reserva = 'programada' "
                + "AND r.fecha_reserva >= DATE_SUB(NOW(), INTERVAL 20 MINUTE) "
                + "AND r.fecha_reserva <= DATE_ADD(NOW(), INTERVAL 24 HOUR) "
                + "ORDER BY r.fecha_reserva, r.id_reserva";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservas.add(mapearReserva(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar reservas vigentes", e);
        }
        return reservas;
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(rs.getInt("id_reserva"));
        reserva.setCliente(rs.getString("cliente"));
        reserva.setTelefono(rs.getString("telefono"));
        reserva.setCantidadPersonas(rs.getInt("cantidad_personas"));
        reserva.setFechaReserva(rs.getString("fecha_reserva"));
        reserva.setHoraReserva(rs.getString("hora_reserva"));
        reserva.setObservacion(rs.getString("observacion"));
        reserva.setEstadoReserva(rs.getString("estado_reserva"));
        reserva.setIdMesa(rs.getInt("id_mesa"));
        reserva.setNumeroMesa(rs.getInt("numero_mesa"));
        reserva.setMinutosParaReserva(rs.getInt("minutos_para_reserva"));
        return reserva;
    }
}
