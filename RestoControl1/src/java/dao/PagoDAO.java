package dao;

import config.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Pago;
import modelo.Pedido;

public class PagoDAO {

    public PagoDAO() {
        asegurarColumnasPago();
    }

    public List<Pedido> listarPedidosPorCobrar() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.tipo_pedido, p.estado_pedido, "
                + "p.observacion, p.total, COALESCE(p.comensales, 1) AS comensales, "
                + "COALESCE(p.cliente, '') AS cliente, COALESCE(p.telefono, '') AS telefono, "
                + "COALESCE(p.direccion_entrega, '') AS direccion_entrega, COALESCE(p.hora_recojo, '') AS hora_recojo, "
                + "p.id_usuario, COALESCE(p.id_mesa, 0) AS id_mesa, COALESCE(m.numero_mesa, 0) AS numero_mesa, "
                + "CONCAT(u.nombres, ' ', u.apellidos) AS nombre_usuario, "
                + "CASE WHEN TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) >= 60 "
                + "THEN CONCAT(FLOOR(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) / 60), ' h ', "
                + "MOD(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 60), ' min') "
                + "ELSE CONCAT(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), ' min') END AS tiempo_abierto "
                + "FROM pedido p "
                + "LEFT JOIN mesa m ON p.id_mesa = m.id_mesa "
                + "INNER JOIN usuario u ON p.id_usuario = u.id_usuario "
                + "WHERE p.estado_pedido IN ('listo', 'entregado') AND p.total > 0 "
                + "ORDER BY p.fecha_pedido DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pedidos.add(mapearPedido(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pedidos por cobrar", e);
        }
        return pedidos;
    }

    public List<Integer> registrarPagos(int idPedido, List<Pago> pagos) {
        if (pagos == null || pagos.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un pago.");
        }
        BigDecimal totalPagado = sumarPagos(pagos);
        List<Integer> idsGenerados = new ArrayList<>();

        String sqlValidar = "SELECT estado_pedido, COALESCE(total, 0) AS total FROM pedido WHERE id_pedido = ? FOR UPDATE";
        String sqlPago = "INSERT INTO pago (fecha_pago, monto_pago, medio_pago, estado_pago, id_pedido, "
                + "tipo_documento, numero_documento, razon_social, monto_recibido, vuelto, numero_parte, cliente_pago) "
                + "VALUES (NOW(), ?, ?, 'pagado', ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPedido = "UPDATE pedido SET estado_pedido = 'pagado' WHERE id_pedido = ?";
        String sqlMesa = "UPDATE mesa m INNER JOIN pedido p ON p.id_mesa = m.id_mesa "
                + "SET m.estado_mesa = 'disponible' WHERE p.id_pedido = ?";
        String sqlMesasUnidas = "UPDATE mesa m INNER JOIN mesa_pedido mp ON mp.id_mesa = m.id_mesa "
                + "SET m.estado_mesa = 'disponible' WHERE mp.id_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psValidar = cn.prepareStatement(sqlValidar);
                 PreparedStatement psPago = cn.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psPedido = cn.prepareStatement(sqlPedido);
                 PreparedStatement psMesa = cn.prepareStatement(sqlMesa);
                 PreparedStatement psMesasUnidas = cn.prepareStatement(sqlMesasUnidas)) {

                validarPedidoCobrable(psValidar, idPedido, totalPagado);

                for (Pago pago : pagos) {
                    psPago.setBigDecimal(1, valor(pago.getMontoPago()));
                    psPago.setString(2, texto(pago.getMedioPago(), "efectivo"));
                    psPago.setInt(3, idPedido);
                    psPago.setString(4, texto(pago.getTipoDocumento(), "boleta"));
                    psPago.setString(5, limpiar(pago.getNumeroDocumento()));
                    psPago.setString(6, limpiar(pago.getRazonSocial()));
                    psPago.setBigDecimal(7, valor(pago.getMontoRecibido()));
                    psPago.setBigDecimal(8, valor(pago.getVuelto()));
                    psPago.setInt(9, Math.max(1, pago.getNumeroParte()));
                    psPago.setString(10, limpiar(pago.getClientePago()));
                    psPago.executeUpdate();

                    try (ResultSet rs = psPago.getGeneratedKeys()) {
                        if (rs.next()) {
                            int idGenerado = rs.getInt(1);
                            idsGenerados.add(idGenerado);
                            pago.setIdPago(idGenerado);
                        }
                    }
                }

                psPedido.setInt(1, idPedido);
                psPedido.executeUpdate();

                psMesa.setInt(1, idPedido);
                psMesa.executeUpdate();

                psMesasUnidas.setInt(1, idPedido);
                psMesasUnidas.executeUpdate();

                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar pagos", e);
        }
        return idsGenerados;
    }

    public Pago buscarPorId(int idPago) {
        String sql = "SELECT id_pago, id_pedido, fecha_pago, monto_pago, medio_pago, estado_pago, "
                + "tipo_documento, numero_documento, razon_social, monto_recibido, vuelto, numero_parte, cliente_pago "
                + "FROM pago WHERE id_pago = ?";
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPago(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pago", e);
        }
        return null;
    }

    public int contarPagosPorPedido(int idPedido) {
        String sql = "SELECT COUNT(*) FROM pago WHERE id_pedido = ?";
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.max(1, rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar pagos del pedido", e);
        }
        return 1;
    }

    private void asegurarColumnasPago() {
        agregarColumnaSiNoExiste("tipo_documento", "VARCHAR(20) NOT NULL DEFAULT 'boleta'");
        agregarColumnaSiNoExiste("numero_documento", "VARCHAR(20)");
        agregarColumnaSiNoExiste("razon_social", "VARCHAR(160)");
        agregarColumnaSiNoExiste("monto_recibido", "DECIMAL(10,2) NOT NULL DEFAULT 0");
        agregarColumnaSiNoExiste("vuelto", "DECIMAL(10,2) NOT NULL DEFAULT 0");
        agregarColumnaSiNoExiste("numero_parte", "INT NOT NULL DEFAULT 1");
        agregarColumnaSiNoExiste("cliente_pago", "VARCHAR(120)");
    }

    private void agregarColumnaSiNoExiste(String columna, String definicion) {
        String existe = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pago' AND COLUMN_NAME = ?";
        String alter = "ALTER TABLE pago ADD COLUMN " + columna + " " + definicion;
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(existe)) {
            ps.setString(1, columna);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
            try (Statement st = cn.createStatement()) {
                st.executeUpdate(alter);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al preparar la tabla de pagos", e);
        }
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(rs.getInt("id_pedido"));
        pedido.setFechaPedido(rs.getString("fecha_pedido"));
        pedido.setTipoPedido(rs.getString("tipo_pedido"));
        pedido.setEstadoPedido(rs.getString("estado_pedido"));
        pedido.setObservacion(rs.getString("observacion"));
        pedido.setTotal(rs.getBigDecimal("total"));
        pedido.setComensales(rs.getInt("comensales"));
        pedido.setCliente(rs.getString("cliente"));
        pedido.setTelefono(rs.getString("telefono"));
        pedido.setDireccionEntrega(rs.getString("direccion_entrega"));
        pedido.setHoraRecojo(rs.getString("hora_recojo"));
        pedido.setIdUsuario(rs.getInt("id_usuario"));
        pedido.setIdMesa(rs.getInt("id_mesa"));
        pedido.setNumeroMesa(rs.getInt("numero_mesa"));
        pedido.setNombreUsuario(rs.getString("nombre_usuario"));
        pedido.setTiempoAbierto(rs.getString("tiempo_abierto"));
        return pedido;
    }

    private Pago mapearPago(ResultSet rs) throws SQLException {
        Pago pago = new Pago();
        pago.setIdPago(rs.getInt("id_pago"));
        pago.setIdPedido(rs.getInt("id_pedido"));
        pago.setFechaPago(rs.getString("fecha_pago"));
        pago.setMontoPago(rs.getBigDecimal("monto_pago"));
        pago.setMedioPago(rs.getString("medio_pago"));
        pago.setEstadoPago(rs.getString("estado_pago"));
        pago.setTipoDocumento(rs.getString("tipo_documento"));
        pago.setNumeroDocumento(rs.getString("numero_documento"));
        pago.setRazonSocial(rs.getString("razon_social"));
        pago.setMontoRecibido(rs.getBigDecimal("monto_recibido"));
        pago.setVuelto(rs.getBigDecimal("vuelto"));
        pago.setNumeroParte(rs.getInt("numero_parte"));
        pago.setClientePago(rs.getString("cliente_pago"));
        return pago;
    }

    private void validarPedidoCobrable(PreparedStatement ps, int idPedido, BigDecimal totalPagado) throws SQLException {
        ps.setInt(1, idPedido);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("El pedido no existe.");
            }
            String estadoPedido = rs.getString("estado_pedido");
            if ("pagado".equals(estadoPedido)) {
                throw new SQLException("El pedido ya fue pagado.");
            }
            if (!"listo".equals(estadoPedido) && !"entregado".equals(estadoPedido)) {
                throw new SQLException("El pedido aun no esta listo para cobrar.");
            }
            BigDecimal totalPedido = valor(rs.getBigDecimal("total"));
            if (totalPedido.compareTo(BigDecimal.ZERO) <= 0) {
                throw new SQLException("El pedido no tiene monto por cobrar.");
            }
            if (totalPedido.compareTo(totalPagado) != 0) {
                throw new SQLException("El monto pagado no coincide con el total del pedido.");
            }
        }
    }

    private BigDecimal sumarPagos(List<Pago> pagos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pago pago : pagos) {
            total = total.add(valor(pago.getMontoPago()));
        }
        return total;
    }

    private BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO : numero;
    }

    private String texto(String valor, String fallback) {
        return valor == null || valor.trim().isEmpty() ? fallback : valor.trim();
    }

    private String limpiar(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }
}
