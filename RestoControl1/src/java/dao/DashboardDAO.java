package dao;

import config.ConexionBD;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import modelo.PanelActividad;
import modelo.PanelAlertaInsumo;
import modelo.PanelPedidoReciente;
import modelo.PanelPrincipal;
import modelo.PanelVentaSemanal;

public class DashboardDAO {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public PanelPrincipal cargarPanel() {
        PanelPrincipal panel = new PanelPrincipal();
        String hoy = LocalDate.now().toString();

        panel.setPedidosDia(reporteDAO.contarPedidosHoy());
        panel.setVentasDia(reporteDAO.obtenerVentasHoy());
        panel.setPedidosPendientes(contarPedidosPendientes());
        panel.setInsumosStockBajo(reporteDAO.contarInsumosStockBajo());
        panel.setProductosTopVendidos(contarProductosTopVendidos());
        panel.setInsumosPorVencer(contarInsumosPorVencer());
        panel.setPedidosRecientes(listarPedidosRecientes());
        panel.setAlertasInventario(listarAlertasInventario());
        panel.setVentasSemanales(listarVentasSemanales());
        panel.setActividadReciente(listarActividadReciente());
        return panel;
    }

    private int contarPedidosPendientes() {
        String sql = "SELECT COUNT(*) FROM pedido "
                + "WHERE estado_pedido IN ('recibido', 'en preparacion', 'listo', 'entregado') "
                + "AND COALESCE(total, 0) > 0 "
                + "AND DATE(fecha_pedido) = CURDATE()";
        return consultarEntero(sql);
    }

    private int contarProductosTopVendidos() {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT dp.id_producto FROM detalle_pedido dp "
                + "INNER JOIN pedido p ON p.id_pedido = dp.id_pedido "
                + "WHERE DATE(p.fecha_pedido) = CURDATE() "
                + "GROUP BY dp.id_producto "
                + "ORDER BY SUM(dp.cantidad) DESC LIMIT 5"
                + ") top_productos";
        return consultarEntero(sql);
    }

    private int contarInsumosPorVencer() {
        String sql = "SELECT COUNT(*) FROM insumo "
                + "WHERE estado = 'activo' AND stock_actual > 0 "
                + "AND stock_actual <= (stock_minimo * 1.25)";
        return consultarEntero(sql);
    }

    private List<PanelPedidoReciente> listarPedidosRecientes() {
        List<PanelPedidoReciente> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.tipo_pedido, p.estado_pedido, COALESCE(p.total, 0) AS total, "
                + "DATE_FORMAT(p.fecha_pedido, '%H:%i') AS hora, COALESCE(m.numero_mesa, 0) AS numero_mesa "
                + "FROM pedido p "
                + "LEFT JOIN mesa m ON m.id_mesa = p.id_mesa "
                + "ORDER BY p.fecha_pedido DESC LIMIT 8";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PanelPedidoReciente pedido = new PanelPedidoReciente();
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setTipoEtiqueta(etiquetaTipo(rs.getString("tipo_pedido"), rs.getInt("numero_mesa")));
                pedido.setEstadoPedido(rs.getString("estado_pedido"));
                pedido.setTotal(valor(rs.getBigDecimal("total")));
                pedido.setHora(rs.getString("hora"));
                pedidos.add(pedido);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pedidos recientes", e);
        }
        return pedidos;
    }

    private List<PanelAlertaInsumo> listarAlertasInventario() {
        List<PanelAlertaInsumo> alertas = new ArrayList<>();
        String sql = "SELECT nombre_insumo, stock_actual, stock_minimo "
                + "FROM insumo WHERE estado = 'activo' AND stock_actual <= stock_minimo "
                + "ORDER BY stock_actual ASC LIMIT 8";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal actual = valor(rs.getBigDecimal("stock_actual"));
                BigDecimal minimo = valor(rs.getBigDecimal("stock_minimo"));
                PanelAlertaInsumo alerta = new PanelAlertaInsumo();
                alerta.setNombreInsumo(rs.getString("nombre_insumo"));
                alerta.setStockActual(actual);
                alerta.setStockMinimo(minimo);
                alerta.setVencimiento(actual.compareTo(minimo) < 0 ? "Revisar hoy" : "Proximo");
                alerta.setEstado(actual.compareTo(minimo) < 0 ? "Critico" : "Stock bajo");
                alertas.add(alerta);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar alertas de inventario", e);
        }
        return alertas;
    }

    private List<PanelVentaSemanal> listarVentasSemanales() {
        List<PanelVentaSemanal> ventas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            PanelVentaSemanal venta = new PanelVentaSemanal();
            venta.setDia(fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")));
            venta.setMonto(obtenerVentasDia(fecha.toString()));
            ventas.add(venta);
        }
        return ventas;
    }

    private List<PanelActividad> listarActividadReciente() {
        List<PanelActividad> actividades = new ArrayList<>();
        String sql = "SELECT descripcion, fecha_evento FROM ("
                + "SELECT CONCAT('Pedido #', LPAD(p.id_pedido, 5, '0'), ' registrado') AS descripcion, "
                + "p.fecha_pedido AS fecha_evento FROM pedido p "
                + "UNION ALL "
                + "SELECT CONCAT('Pago registrado del pedido #', LPAD(pa.id_pedido, 5, '0')) AS descripcion, "
                + "pa.fecha_pago AS fecha_evento FROM pago pa "
                + ") eventos ORDER BY fecha_evento DESC LIMIT 8";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                actividades.add(new PanelActividad(
                        rs.getString("descripcion"),
                        formatearTiempoRelativo(rs.getString("fecha_evento"))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar actividad reciente", e);
        }
        return actividades;
    }

    private BigDecimal obtenerVentasDia(String fecha) {
        String sql = "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                + "WHERE estado_pago = 'pagado' AND DATE(fecha_pago) = ?";
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return valor(rs.getBigDecimal(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar ventas del dia", e);
        }
        return BigDecimal.ZERO;
    }

    private String etiquetaTipo(String tipoPedido, int numeroMesa) {
        if ("delivery".equals(tipoPedido)) {
            return "Delivery";
        }
        if ("para_llevar".equals(tipoPedido)) {
            return "Para llevar";
        }
        return numeroMesa > 0 ? "Mesa " + numeroMesa : "Salon";
    }

    private String formatearTiempoRelativo(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            return "Reciente";
        }
        try {
            LocalDateTime fecha = LocalDateTime.parse(fechaTexto.replace(' ', 'T'));
            long minutos = Duration.between(fecha, LocalDateTime.now()).toMinutes();
            if (minutos < 1) {
                return "Hace un momento";
            }
            if (minutos < 60) {
                return "Hace " + minutos + " min";
            }
            long horas = minutos / 60;
            if (horas < 24) {
                return "Hace " + horas + " h";
            }
            return fecha.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        } catch (Exception e) {
            return fechaTexto;
        }
    }

    private int consultarEntero(String sql) {
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar indicador del panel", e);
        }
        return 0;
    }

    private BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : numero.setScale(2, RoundingMode.HALF_UP);
    }
}
