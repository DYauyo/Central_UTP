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
import modelo.CocinaPedido;
import modelo.DetallePedido;
import modelo.Pedido;

public class PedidoDAO {

    public int abrirPedidoSalon(int idMesa, int idUsuario, String observacion) {
        return abrirPedidoSalon(idMesa, idUsuario, observacion, 1);
    }

    public int abrirPedidoSalon(int idMesa, int idUsuario, String observacion, int comensales) {
        Pedido abierto = buscarAbiertoPorMesa(idMesa);
        if (abierto != null) {
            return abierto.getIdPedido();
        }

        if (comensales <= 0) {
            comensales = 1;
        }

        String sqlPedido = "INSERT INTO pedido (fecha_pedido, tipo_pedido, estado_pedido, observacion, total, comensales, id_usuario, id_mesa) "
                + "VALUES (NOW(), 'salon', 'pendiente', ?, 0, ?, ?, ?)";
        String sqlMesa = "UPDATE mesa SET estado_mesa = 'ocupada' WHERE id_mesa = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psPedido = cn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psMesa = cn.prepareStatement(sqlMesa)) {

                psPedido.setString(1, observacion);
                psPedido.setInt(2, comensales);
                psPedido.setInt(3, idUsuario);
                psPedido.setInt(4, idMesa);
                psPedido.executeUpdate();

                int idPedido;
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se obtuvo el id del pedido generado");
                    }
                    idPedido = rs.getInt(1);
                }

                psMesa.setInt(1, idMesa);
                psMesa.executeUpdate();

                cn.commit();
                return idPedido;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al abrir pedido de salon", e);
        }
    }

    public int abrirPedidoDelivery(int idUsuario, String cliente, String telefono, String direccionEntrega,
            String observacion) {
        return abrirPedidoSinMesa("delivery", idUsuario, cliente, telefono, direccionEntrega, null, observacion);
    }

    public int abrirPedidoParaLlevar(int idUsuario, String cliente, String telefono, String horaRecojo,
            String observacion) {
        return abrirPedidoSinMesa("para_llevar", idUsuario, cliente, telefono, null, horaRecojo, observacion);
    }

    public int contarPedidosAbiertosPorTipo(String tipoPedido) {
        String sql = "SELECT COUNT(*) FROM pedido "
                + "WHERE tipo_pedido = ? "
                + "AND estado_pedido IN ('pendiente', 'recibido', 'en preparacion', 'listo', 'entregado')";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, tipoPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar pedidos por tipo", e);
        }
        return 0;
    }

    public int abrirPedidoMesasUnidas(List<Integer> idsMesa, int idUsuario, String cliente, int comensales,
            String observacion) {
        if (idsMesa == null || idsMesa.size() < 2) {
            throw new IllegalArgumentException("Selecciona al menos dos mesas libres para unir.");
        }
        if (comensales <= 0) {
            comensales = calcularCapacidadMesas(idsMesa);
        }

        int idMesaPrincipal = idsMesa.get(0);
        String notaUnion = construirNotaUnion(idsMesa, cliente, observacion);
        String sqlPedido = "INSERT INTO pedido (fecha_pedido, tipo_pedido, estado_pedido, observacion, total, comensales, cliente, id_usuario, id_mesa) "
                + "VALUES (NOW(), 'salon', 'pendiente', ?, 0, ?, ?, ?, ?)";
        String sqlUnion = "INSERT INTO mesa_pedido (id_pedido, id_mesa, principal) VALUES (?, ?, ?)";
        String sqlMesa = "UPDATE mesa SET estado_mesa = 'ocupada' WHERE id_mesa = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try {
                validarMesasLibres(cn, idsMesa);
                int idPedido;
                try (PreparedStatement psPedido = cn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    psPedido.setString(1, notaUnion);
                    psPedido.setInt(2, comensales);
                    psPedido.setString(3, cliente);
                    psPedido.setInt(4, idUsuario);
                    psPedido.setInt(5, idMesaPrincipal);
                    psPedido.executeUpdate();
                    try (ResultSet rs = psPedido.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se obtuvo el id del pedido generado");
                        }
                        idPedido = rs.getInt(1);
                    }
                }

                try (PreparedStatement psUnion = cn.prepareStatement(sqlUnion);
                     PreparedStatement psMesa = cn.prepareStatement(sqlMesa)) {
                    for (int idMesa : idsMesa) {
                        psUnion.setInt(1, idPedido);
                        psUnion.setInt(2, idMesa);
                        psUnion.setBoolean(3, idMesa == idMesaPrincipal);
                        psUnion.addBatch();

                        psMesa.setInt(1, idMesa);
                        psMesa.addBatch();
                    }
                    psUnion.executeBatch();
                    psMesa.executeBatch();
                }

                cn.commit();
                return idPedido;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al unir mesas", e);
        }
    }

    private int abrirPedidoSinMesa(String tipoPedido, int idUsuario, String cliente, String telefono,
            String direccionEntrega, String horaRecojo, String observacion) {
        String sql = "INSERT INTO pedido (fecha_pedido, tipo_pedido, estado_pedido, observacion, total, comensales, "
                + "cliente, telefono, direccion_entrega, hora_recojo, id_usuario, id_mesa) "
                + "VALUES (NOW(), ?, 'pendiente', ?, 0, 1, ?, ?, ?, ?, ?, NULL)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tipoPedido);
            ps.setString(2, observacion);
            ps.setString(3, cliente);
            ps.setString(4, telefono);
            ps.setString(5, direccionEntrega);
            ps.setString(6, normalizarFechaHora(horaRecojo));
            ps.setInt(7, idUsuario);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al abrir pedido " + tipoPedido, e);
        }
        throw new RuntimeException("No se obtuvo el id del pedido generado");
    }

    private void validarMesasLibres(Connection cn, List<Integer> idsMesa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mesa m "
                + "LEFT JOIN pedido p ON p.id_mesa = m.id_mesa AND p.estado_pedido IN ('pendiente', 'recibido', 'en preparacion', 'listo', 'entregado') "
                + "LEFT JOIN mesa_pedido mp ON mp.id_mesa = m.id_mesa "
                + "LEFT JOIN pedido pu ON pu.id_pedido = mp.id_pedido AND pu.estado_pedido IN ('pendiente', 'recibido', 'en preparacion', 'listo', 'entregado') "
                + "WHERE m.id_mesa = ? AND m.estado = 'activo' AND m.estado_mesa = 'disponible' "
                + "AND p.id_pedido IS NULL AND pu.id_pedido IS NULL";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int idMesa : idsMesa) {
                ps.setInt(1, idMesa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new SQLException("La mesa " + idMesa + " ya no esta disponible para unir.");
                    }
                }
            }
        }
    }

    private int calcularCapacidadMesas(List<Integer> idsMesa) {
        if (idsMesa == null || idsMesa.isEmpty()) {
            return 1;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < idsMesa.size(); i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }
        String sql = "SELECT COALESCE(SUM(capacidad), 0) FROM mesa WHERE id_mesa IN (" + placeholders + ")";
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < idsMesa.size(); i++) {
                ps.setInt(i + 1, idsMesa.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.max(1, rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular capacidad de mesas", e);
        }
        return idsMesa.size() * 4;
    }

    private String construirNotaUnion(List<Integer> idsMesa, String cliente, String observacion) {
        StringBuilder nota = new StringBuilder("Mesas unidas");
        if (idsMesa != null && !idsMesa.isEmpty()) {
            nota.append(": ");
            for (int i = 0; i < idsMesa.size(); i++) {
                if (i > 0) {
                    nota.append(", ");
                }
                nota.append(idsMesa.get(i));
            }
        }
        if (cliente != null && !cliente.trim().isEmpty()) {
            nota.append(" - ").append(cliente.trim());
        }
        if (observacion != null && !observacion.trim().isEmpty()) {
            nota.append(" - ").append(observacion.trim());
        }
        return nota.toString();
    }

    public void agregarDetalle(int idPedido, int idProducto, int cantidad, String observacion) {
        if (cantidad <= 0) {
            cantidad = 1;
        }

        String sqlProducto = "SELECT precio FROM producto WHERE id_producto = ?";
        String sqlDetalleExistente = "SELECT id_detalle_pedido, cantidad FROM detalle_pedido "
                + "WHERE id_pedido = ? AND id_producto = ? AND COALESCE(observacion_detalle, '') = COALESCE(?, '') LIMIT 1";
        String sqlInsertar = "INSERT INTO detalle_pedido (cantidad, precio_unitario, subtotal, observacion_detalle, id_pedido, id_producto) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlActualizar = "UPDATE detalle_pedido SET cantidad = ?, subtotal = precio_unitario * ? "
                + "WHERE id_detalle_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try {
                BigDecimal precio = obtenerPrecio(cn, sqlProducto, idProducto);
                Integer idDetalleExistente = null;
                int cantidadActual = 0;

                try (PreparedStatement ps = cn.prepareStatement(sqlDetalleExistente)) {
                    ps.setInt(1, idPedido);
                    ps.setInt(2, idProducto);
                    ps.setString(3, observacion);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            idDetalleExistente = rs.getInt("id_detalle_pedido");
                            cantidadActual = rs.getInt("cantidad");
                        }
                    }
                }

                ajustarDisponibilidadDiaria(cn, idProducto, cantidad);

                if (idDetalleExistente == null) {
                    try (PreparedStatement ps = cn.prepareStatement(sqlInsertar)) {
                        ps.setInt(1, cantidad);
                        ps.setBigDecimal(2, precio);
                        ps.setBigDecimal(3, precio.multiply(BigDecimal.valueOf(cantidad)));
                        ps.setString(4, observacion);
                        ps.setInt(5, idPedido);
                        ps.setInt(6, idProducto);
                        ps.executeUpdate();
                    }
                } else {
                    int nuevaCantidad = cantidadActual + cantidad;
                    try (PreparedStatement ps = cn.prepareStatement(sqlActualizar)) {
                        ps.setInt(1, nuevaCantidad);
                        ps.setInt(2, nuevaCantidad);
                        ps.setInt(3, idDetalleExistente);
                        ps.executeUpdate();
                    }
                }

                recalcularTotal(cn, idPedido);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al agregar producto al pedido", e);
        }
    }

    public void actualizarCantidadDetalle(int idDetallePedido, int cantidad) {
        if (cantidad <= 0) {
            eliminarDetalle(idDetallePedido);
            return;
        }

        String sqlPedido = "SELECT id_pedido, id_producto, cantidad FROM detalle_pedido WHERE id_detalle_pedido = ? FOR UPDATE";
        String sqlActualizar = "UPDATE detalle_pedido SET cantidad = ?, subtotal = precio_unitario * ? "
                + "WHERE id_detalle_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try {
                DetalleMovimiento detalle = obtenerDetalleMovimiento(cn, sqlPedido, idDetallePedido);
                int diferencia = cantidad - detalle.cantidad;
                if (diferencia != 0) {
                    ajustarDisponibilidadDiaria(cn, detalle.idProducto, diferencia);
                }
                try (PreparedStatement ps = cn.prepareStatement(sqlActualizar)) {
                    ps.setInt(1, cantidad);
                    ps.setInt(2, cantidad);
                    ps.setInt(3, idDetallePedido);
                    ps.executeUpdate();
                }
                recalcularTotal(cn, detalle.idPedido);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cantidad del detalle", e);
        }
    }

    public void eliminarDetalle(int idDetallePedido) {
        String sqlPedido = "SELECT id_pedido, id_producto, cantidad FROM detalle_pedido WHERE id_detalle_pedido = ? FOR UPDATE";
        String sqlEliminar = "DELETE FROM detalle_pedido WHERE id_detalle_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try {
                DetalleMovimiento detalle = obtenerDetalleMovimiento(cn, sqlPedido, idDetallePedido);
                ajustarDisponibilidadDiaria(cn, detalle.idProducto, -detalle.cantidad);
                try (PreparedStatement ps = cn.prepareStatement(sqlEliminar)) {
                    ps.setInt(1, idDetallePedido);
                    ps.executeUpdate();
                }
                recalcularTotal(cn, detalle.idPedido);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar detalle de pedido", e);
        }
    }

    public void actualizarEstadoPedido(int idPedido, String estadoPedido) {
        String sql = "UPDATE pedido SET estado_pedido = ? WHERE id_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estadoPedido);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado del pedido", e);
        }
    }

    public void registrarPago(int idPedido, BigDecimal montoPago, String medioPago) {
        String sqlPago = "INSERT INTO pago (fecha_pago, monto_pago, medio_pago, estado_pago, id_pedido) "
                + "VALUES (NOW(), ?, ?, 'pagado', ?)";
        String sqlPedido = "UPDATE pedido SET estado_pedido = 'pagado' WHERE id_pedido = ?";
        String sqlMesa = "UPDATE mesa m INNER JOIN pedido p ON p.id_mesa = m.id_mesa "
                + "SET m.estado_mesa = 'disponible' WHERE p.id_pedido = ?";
        String sqlMesasUnidas = "UPDATE mesa m INNER JOIN mesa_pedido mp ON mp.id_mesa = m.id_mesa "
                + "SET m.estado_mesa = 'disponible' WHERE mp.id_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psPago = cn.prepareStatement(sqlPago);
                 PreparedStatement psPedido = cn.prepareStatement(sqlPedido);
                 PreparedStatement psMesa = cn.prepareStatement(sqlMesa);
                 PreparedStatement psMesasUnidas = cn.prepareStatement(sqlMesasUnidas)) {

                psPago.setBigDecimal(1, montoPago);
                psPago.setString(2, medioPago);
                psPago.setInt(3, idPedido);
                psPago.executeUpdate();

                psPedido.setInt(1, idPedido);
                psPedido.executeUpdate();

                psMesa.setInt(1, idPedido);
                psMesa.executeUpdate();

                psMesasUnidas.setInt(1, idPedido);
                psMesasUnidas.executeUpdate();

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar pago", e);
        }
    }

    public void cancelarPedido(int idPedido) {
        String sqlPedido = "UPDATE pedido SET estado_pedido = 'cancelado' WHERE id_pedido = ? AND estado_pedido <> 'pagado'";
        String sqlMesa = "UPDATE mesa m INNER JOIN pedido p ON p.id_mesa = m.id_mesa "
                + "SET m.estado_mesa = 'disponible' WHERE p.id_pedido = ?";
        String sqlMesasUnidas = "UPDATE mesa m INNER JOIN mesa_pedido mp ON mp.id_mesa = m.id_mesa "
                + "SET m.estado_mesa = 'disponible' WHERE mp.id_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psPedido = cn.prepareStatement(sqlPedido);
                 PreparedStatement psMesa = cn.prepareStatement(sqlMesa);
                 PreparedStatement psMesasUnidas = cn.prepareStatement(sqlMesasUnidas)) {

                psPedido.setInt(1, idPedido);
                psPedido.executeUpdate();

                psMesa.setInt(1, idPedido);
                psMesa.executeUpdate();

                psMesasUnidas.setInt(1, idPedido);
                psMesasUnidas.executeUpdate();

                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cancelar pedido", e);
        }
    }

    public Pedido buscarAbiertoPorMesa(int idMesa) {
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.tipo_pedido, p.estado_pedido, "
                + "p.observacion, p.total, COALESCE(p.comensales, 1) AS comensales, "
                + "COALESCE(p.cliente, '') AS cliente, COALESCE(p.telefono, '') AS telefono, "
                + "COALESCE(p.direccion_entrega, '') AS direccion_entrega, COALESCE(p.hora_recojo, '') AS hora_recojo, "
                + "p.id_usuario, p.id_mesa, m.numero_mesa, "
                + "CONCAT(u.nombres, ' ', u.apellidos) AS nombre_usuario, "
                + "CASE WHEN TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) >= 60 "
                + "THEN CONCAT(FLOOR(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) / 60), ' h ', "
                + "MOD(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 60), ' min') "
                + "ELSE CONCAT(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), ' min') END AS tiempo_abierto "
                + "FROM pedido p "
                + "INNER JOIN mesa m ON p.id_mesa = m.id_mesa "
                + "INNER JOIN usuario u ON p.id_usuario = u.id_usuario "
                + "WHERE p.id_mesa = ? AND p.estado_pedido IN ('pendiente', 'recibido', 'en preparacion', 'listo', 'entregado') "
                + "ORDER BY p.fecha_pedido DESC LIMIT 1";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido por mesa", e);
        }

        return null;
    }

    public Pedido buscarPorId(int idPedido) {
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.tipo_pedido, p.estado_pedido, "
                + "p.observacion, p.total, COALESCE(p.comensales, 1) AS comensales, "
                + "COALESCE(p.cliente, '') AS cliente, COALESCE(p.telefono, '') AS telefono, "
                + "COALESCE(p.direccion_entrega, '') AS direccion_entrega, COALESCE(p.hora_recojo, '') AS hora_recojo, "
                + "p.id_usuario, p.id_mesa, COALESCE(m.numero_mesa, 0) AS numero_mesa, "
                + "CONCAT(u.nombres, ' ', u.apellidos) AS nombre_usuario, "
                + "CASE WHEN TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) >= 60 "
                + "THEN CONCAT(FLOOR(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) / 60), ' h ', "
                + "MOD(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), 60), ' min') "
                + "ELSE CONCAT(TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()), ' min') END AS tiempo_abierto "
                + "FROM pedido p "
                + "LEFT JOIN mesa m ON p.id_mesa = m.id_mesa "
                + "INNER JOIN usuario u ON p.id_usuario = u.id_usuario "
                + "WHERE p.id_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido", e);
        }

        return null;
    }

    public List<DetallePedido> listarDetalles(int idPedido) {
        List<DetallePedido> detalles = new ArrayList<>();
        String sql = "SELECT d.id_detalle_pedido, d.cantidad, d.precio_unitario, d.subtotal, "
                + "d.observacion_detalle, d.id_pedido, d.id_producto, p.nombre_producto, "
                + "COALESCE(m.numero_mesa, 0) AS numero_mesa, pe.estado_pedido, "
                + "CONCAT(TIMESTAMPDIFF(MINUTE, pe.fecha_pedido, NOW()), ' min') AS tiempo_abierto "
                + "FROM detalle_pedido d "
                + "INNER JOIN producto p ON d.id_producto = p.id_producto "
                + "INNER JOIN pedido pe ON d.id_pedido = pe.id_pedido "
                + "LEFT JOIN mesa m ON pe.id_mesa = m.id_mesa "
                + "WHERE d.id_pedido = ? "
                + "ORDER BY d.id_detalle_pedido";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapearDetalle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar detalles de pedido", e);
        }

        return detalles;
    }

    public List<CocinaPedido> listarPedidosCocina() {
        List<CocinaPedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.fecha_pedido, DATE_FORMAT(p.fecha_pedido, '%H:%i') AS hora_pedido, "
                + "p.tipo_pedido, p.estado_pedido, COALESCE(NULLIF(p.cliente, ''), p.observacion) AS observacion, "
                + "p.id_mesa, COALESCE(m.numero_mesa, 0) AS numero_mesa, "
                + "CONCAT(u.nombres, ' ', u.apellidos) AS mozo, "
                + "TIMESTAMPDIFF(MINUTE, p.fecha_pedido, NOW()) AS minutos_espera "
                + "FROM pedido p "
                + "LEFT JOIN mesa m ON p.id_mesa = m.id_mesa "
                + "INNER JOIN usuario u ON p.id_usuario = u.id_usuario "
                + "WHERE p.estado_pedido IN ('recibido', 'en preparacion', 'listo') "
                + "OR (p.estado_pedido = 'entregado' AND DATE(p.fecha_pedido) = CURDATE()) "
                + "ORDER BY FIELD(p.estado_pedido, 'recibido', 'en preparacion', 'listo', 'entregado'), p.fecha_pedido";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CocinaPedido pedido = mapearCocinaPedido(rs);
                pedido.setDetalles(listarDetalles(pedido.getIdPedido()));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pedidos de cocina", e);
        }

        marcarPrimeroEnCola(pedidos);
        return pedidos;
    }

    public int obtenerPrimerPedidoRecibido() {
        String sql = "SELECT id_pedido FROM pedido "
                + "WHERE estado_pedido = 'recibido' "
                + "ORDER BY fecha_pedido, id_pedido LIMIT 1";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("id_pedido");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener primer pedido recibido", e);
        }

        return 0;
    }

    public String obtenerEstadoPedido(int idPedido) {
        String sql = "SELECT estado_pedido FROM pedido WHERE id_pedido = ?";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("estado_pedido");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener estado del pedido", e);
        }

        return "";
    }

    public void iniciarPreparacion(int idPedido) {
        actualizarEstadoPedido(idPedido, "en preparacion");
    }

    public void marcarListoCocina(int idPedido) {
        actualizarEstadoPedido(idPedido, "listo");
    }

    public void marcarEntregadoCocina(int idPedido) {
        actualizarEstadoPedido(idPedido, "entregado");
    }

    private BigDecimal obtenerPrecio(Connection cn, String sql, int idProducto) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("precio");
                }
            }
        }
        throw new SQLException("Producto no encontrado: " + idProducto);
    }

    private int obtenerIdPedidoPorDetalle(Connection cn, String sql, int idDetallePedido) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetallePedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_pedido");
                }
            }
        }
        throw new SQLException("Detalle no encontrado: " + idDetallePedido);
    }

    private DetalleMovimiento obtenerDetalleMovimiento(Connection cn, String sql, int idDetallePedido) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetallePedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DetalleMovimiento(
                            rs.getInt("id_pedido"),
                            rs.getInt("id_producto"),
                            rs.getInt("cantidad"));
                }
            }
        }
        throw new SQLException("Detalle no encontrado: " + idDetallePedido);
    }

    private void ajustarDisponibilidadDiaria(Connection cn, int idProducto, int diferencia) throws SQLException {
        if (diferencia == 0) {
            return;
        }

        if (diferencia > 0) {
            String sql = "UPDATE disponibilidad_producto "
                    + "SET cantidad_vendida = cantidad_vendida + ? "
                    + "WHERE id_producto = ? AND fecha = CURDATE() AND estado = 'activo' "
                    + "AND cantidad_planificada - cantidad_vendida >= ?";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, diferencia);
                ps.setInt(2, idProducto);
                ps.setInt(3, diferencia);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("No hay disponibilidad diaria suficiente para el producto " + idProducto);
                }
            }
            return;
        }

        String sql = "UPDATE disponibilidad_producto "
                + "SET cantidad_vendida = GREATEST(cantidad_vendida - ?, 0) "
                + "WHERE id_producto = ? AND fecha = CURDATE()";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Math.abs(diferencia));
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    private void recalcularTotal(Connection cn, int idPedido) throws SQLException {
        String sql = "UPDATE pedido SET total = (SELECT COALESCE(SUM(subtotal), 0) "
                + "FROM detalle_pedido WHERE id_pedido = ?) WHERE id_pedido = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
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

    private DetallePedido mapearDetalle(ResultSet rs) throws SQLException {
        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetallePedido(rs.getInt("id_detalle_pedido"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        detalle.setObservacionDetalle(rs.getString("observacion_detalle"));
        detalle.setIdPedido(rs.getInt("id_pedido"));
        detalle.setIdProducto(rs.getInt("id_producto"));
        detalle.setNombreProducto(rs.getString("nombre_producto"));
        detalle.setNumeroMesa(rs.getInt("numero_mesa"));
        detalle.setEstadoPedido(rs.getString("estado_pedido"));
        detalle.setTiempoAbierto(rs.getString("tiempo_abierto"));
        return detalle;
    }

    private CocinaPedido mapearCocinaPedido(ResultSet rs) throws SQLException {
        CocinaPedido pedido = new CocinaPedido();
        pedido.setIdPedido(rs.getInt("id_pedido"));
        pedido.setFechaPedido(rs.getString("fecha_pedido"));
        pedido.setHoraPedido(rs.getString("hora_pedido"));
        pedido.setTipoPedido(rs.getString("tipo_pedido"));
        pedido.setEstadoPedido(rs.getString("estado_pedido"));
        pedido.setCliente(rs.getString("observacion"));
        pedido.setIdMesa(rs.getInt("id_mesa"));
        pedido.setNumeroMesa(rs.getInt("numero_mesa"));
        pedido.setMozo(rs.getString("mozo"));
        pedido.setMinutosEspera(rs.getInt("minutos_espera"));
        return pedido;
    }

    private static class DetalleMovimiento {

        private final int idPedido;
        private final int idProducto;
        private final int cantidad;

        DetalleMovimiento(int idPedido, int idProducto, int cantidad) {
            this.idPedido = idPedido;
            this.idProducto = idProducto;
            this.cantidad = cantidad;
        }
    }

    private String normalizarFechaHora(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String limpio = valor.trim().replace('T', ' ');
        if (limpio.length() == 16) {
            limpio += ":00";
        }
        return limpio;
    }

    private void marcarPrimeroEnCola(List<CocinaPedido> pedidos) {
        boolean encontrado = false;
        for (CocinaPedido pedido : pedidos) {
            if ("recibido".equals(pedido.getEstadoPedido()) && !encontrado) {
                pedido.setPrimeroEnCola(true);
                encontrado = true;
            } else {
                pedido.setPrimeroEnCola(false);
            }
        }
    }
}
