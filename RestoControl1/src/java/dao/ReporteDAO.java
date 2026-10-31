package dao;

import config.ConexionBD;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import modelo.ReporteCostoDistribucion;
import modelo.ReporteDashboard;
import modelo.ReporteHoraPunta;
import modelo.ReporteMetodoPago;
import modelo.ReporteProductoVendido;
import modelo.ReporteRentabilidadDia;
import modelo.ReporteVentaFila;

public class ReporteDAO {

    private static final BigDecimal RATIO_COSTO_VENTAS = new BigDecimal("0.55");
    private static final BigDecimal RATIO_GASTOS_OPERATIVOS = new BigDecimal("0.15");
    private static final BigDecimal RATIO_GASTOS_PERSONAL = new BigDecimal("0.10");
    private static final BigDecimal RATIO_OTROS_GASTOS = new BigDecimal("0.05");

    public ReporteDashboard generarDashboard(String tipo, String desde, String hasta) {
        return generarDashboard(tipo, desde, hasta, "00:00", "23:59");
    }

    public ReporteDashboard generarDashboard(String tipo, String desde, String hasta, String horaDesde, String horaHasta) {
        String fechaDesde = normalizarFecha(desde, LocalDate.now().withDayOfMonth(1));
        String fechaHasta = normalizarFecha(hasta, LocalDate.now());
        String horaDesdeNormalizada = normalizarHora(horaDesde, LocalTime.MIN);
        String horaHastaNormalizada = normalizarHora(horaHasta, LocalTime.of(23, 59));
        if (fechaDesde.compareTo(fechaHasta) > 0) {
            String temporal = fechaDesde;
            fechaDesde = fechaHasta;
            fechaHasta = temporal;
        }
        if (fechaDesde.equals(fechaHasta) && horaDesdeNormalizada.compareTo(horaHastaNormalizada) > 0) {
            String temporalHora = horaDesdeNormalizada;
            horaDesdeNormalizada = horaHastaNormalizada;
            horaHastaNormalizada = temporalHora;
        }
        String fechaHoraDesde = fechaDesde + " " + horaDesdeNormalizada + ":00";
        String fechaHoraHasta = fechaHasta + " " + horaHastaNormalizada + ":59";

        ReporteDashboard dashboard = new ReporteDashboard();
        dashboard.setTipo("rentabilidad".equalsIgnoreCase(tipo) ? "rentabilidad" : "ventas");
        dashboard.setDesde(fechaDesde);
        dashboard.setHasta(fechaHasta);
        dashboard.setHoraDesde(horaDesdeNormalizada);
        dashboard.setHoraHasta(horaHastaNormalizada);

        BigDecimal ventasTotales = obtenerVentasEnRango(fechaHoraDesde, fechaHoraHasta);
        int tickets = contarTicketsEnRango(fechaHoraDesde, fechaHoraHasta);

        dashboard.setVentasBrutas(ventasTotales);
        dashboard.setTotalTickets(tickets);
        dashboard.setTicketPromedio(calcularPromedio(ventasTotales, tickets));
        dashboard.setMetodosPago(listarMetodosPago(fechaHoraDesde, fechaHoraHasta, ventasTotales));
        dashboard.setHorasPunta(listarHorasPunta(fechaHoraDesde, fechaHoraHasta));
        dashboard.setFilasVentas(listarVentasRecientes(fechaHoraDesde, fechaHoraHasta));
        dashboard.setProductosTop(listarProductosTop(fechaHoraDesde, fechaHoraHasta));

        calcularRentabilidad(dashboard, ventasTotales, fechaHoraDesde, fechaHoraHasta);
        return dashboard;
    }

    public BigDecimal obtenerVentasHoy() {
        String hoy = LocalDate.now().toString();
        return obtenerVentasEnRango(hoy + " 00:00:00", hoy + " 23:59:59");
    }

    public int contarPedidosHoy() {
        String sql = "SELECT COUNT(*) FROM pedido WHERE DATE(fecha_pedido) = CURDATE()";
        return consultarEntero(sql);
    }

    public int contarPedidosPorCobrar() {
        String sql = "SELECT COUNT(*) FROM pedido "
                + "WHERE estado_pedido IN ('listo', 'entregado') AND COALESCE(total, 0) > 0";
        return consultarEntero(sql);
    }

    public int contarPlatosDisponiblesHoy() {
        String sql = "SELECT COUNT(*) FROM disponibilidad_producto d "
                + "INNER JOIN producto p ON p.id_producto = d.id_producto "
                + "INNER JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "WHERE d.fecha = CURDATE() AND d.estado = 'activo' "
                + "AND p.estado = 'activo' AND c.estado = 'activo' "
                + "AND d.cantidad_planificada > d.cantidad_vendida";
        return consultarEntero(sql);
    }

    public int contarInsumosStockBajo() {
        String sql = "SELECT COUNT(*) FROM insumo "
                + "WHERE estado = 'activo' AND stock_actual <= stock_minimo";
        return consultarEntero(sql);
    }

    private BigDecimal obtenerVentasEnRango(String desde, String hasta) {
        String sql = "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                + "WHERE estado_pago = 'pagado' AND fecha_pago BETWEEN ? AND ?";
        return consultarDecimal(sql, desde, hasta);
    }

    private int contarTicketsEnRango(String desde, String hasta) {
        String sql = "SELECT COUNT(*) FROM pago "
                + "WHERE estado_pago = 'pagado' AND fecha_pago BETWEEN ? AND ?";
        return consultarEntero(sql, desde, hasta);
    }

    private List<ReporteMetodoPago> listarMetodosPago(String desde, String hasta, BigDecimal totalVentas) {
        List<ReporteMetodoPago> metodos = new ArrayList<>();
        String sql = "SELECT COALESCE(medio_pago, 'efectivo') AS medio_pago, COALESCE(SUM(monto_pago), 0) AS total "
                + "FROM pago WHERE estado_pago = 'pagado' AND fecha_pago BETWEEN ? AND ? "
                + "GROUP BY COALESCE(medio_pago, 'efectivo') ORDER BY total DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteMetodoPago metodo = new ReporteMetodoPago();
                    metodo.setMedioPago(rs.getString("medio_pago"));
                    metodo.setMonto(valor(rs.getBigDecimal("total")));
                    metodo.setPorcentaje(calcularPorcentaje(metodo.getMonto(), totalVentas));
                    metodos.add(metodo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar metodos de pago", e);
        }
        return metodos;
    }

    private List<ReporteHoraPunta> listarHorasPunta(String desde, String hasta) {
        List<ReporteHoraPunta> horas = new ArrayList<>();
        for (int hora = 0; hora <= 23; hora++) {
            ReporteHoraPunta punto = new ReporteHoraPunta();
            punto.setHora(hora);
            punto.setMonto(BigDecimal.ZERO);
            horas.add(punto);
        }

        String sql = "SELECT HOUR(fecha_pago) AS hora, COALESCE(SUM(monto_pago), 0) AS total "
                + "FROM pago WHERE estado_pago = 'pagado' AND fecha_pago BETWEEN ? AND ? "
                + "GROUP BY HOUR(fecha_pago)";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hora = rs.getInt("hora");
                    for (ReporteHoraPunta punto : horas) {
                        if (punto.getHora() == hora) {
                            punto.setMonto(valor(rs.getBigDecimal("total")));
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar horas punta", e);
        }
        return horas;
    }

    private List<ReporteVentaFila> listarVentasRecientes(String desde, String hasta) {
        List<ReporteVentaFila> ventas = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(pa.fecha_pago, '%d/%m/%Y %H:%i') AS fecha_hora, pa.id_pago, pa.id_pedido, "
                + "COALESCE(m.numero_mesa, 0) AS numero_mesa, COALESCE(NULLIF(p.cliente, ''), 'Cliente general') AS cliente, "
                + "COALESCE(pa.medio_pago, 'efectivo') AS medio_pago, pa.monto_pago "
                + "FROM pago pa "
                + "INNER JOIN pedido p ON p.id_pedido = pa.id_pedido "
                + "LEFT JOIN mesa m ON m.id_mesa = p.id_mesa "
                + "WHERE pa.estado_pago = 'pagado' AND pa.fecha_pago BETWEEN ? AND ? "
                + "ORDER BY pa.fecha_pago DESC LIMIT 20";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteVentaFila fila = new ReporteVentaFila();
                    fila.setFechaHora(rs.getString("fecha_hora"));
                    fila.setIdPago(rs.getInt("id_pago"));
                    fila.setIdPedido(rs.getInt("id_pedido"));
                    fila.setNumeroMesa(rs.getInt("numero_mesa"));
                    fila.setCliente(rs.getString("cliente"));
                    fila.setMedioPago(rs.getString("medio_pago"));
                    fila.setTotal(valor(rs.getBigDecimal("monto_pago")));
                    ventas.add(fila);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ventas recientes", e);
        }
        return ventas;
    }

    private List<ReporteProductoVendido> listarProductosTop(String desde, String hasta) {
        List<ReporteProductoVendido> productos = new ArrayList<>();
        String sql = "SELECT pr.nombre_producto, c.nombre_categoria, "
                + "COALESCE(SUM(dp.cantidad), 0) AS cantidad_vendida, "
                + "COALESCE(SUM(dp.subtotal), 0) AS total_vendido "
                + "FROM detalle_pedido dp "
                + "INNER JOIN pedido p ON p.id_pedido = dp.id_pedido "
                + "INNER JOIN producto pr ON pr.id_producto = dp.id_producto "
                + "LEFT JOIN categoria c ON c.id_categoria = pr.id_categoria "
                + "WHERE p.fecha_pedido BETWEEN ? AND ? "
                + "GROUP BY pr.id_producto, pr.nombre_producto, c.nombre_categoria "
                + "ORDER BY cantidad_vendida DESC, total_vendido DESC "
                + "LIMIT 10";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteProductoVendido producto = new ReporteProductoVendido();
                    producto.setNombreProducto(rs.getString("nombre_producto"));
                    producto.setCategoria(rs.getString("nombre_categoria"));
                    producto.setCantidadVendida(rs.getInt("cantidad_vendida"));
                    producto.setTotalVendido(valor(rs.getBigDecimal("total_vendido")));
                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos mas vendidos", e);
        }
        return productos;
    }

    private void calcularRentabilidad(ReporteDashboard dashboard, BigDecimal ventasTotales, String desde, String hasta) {
        BigDecimal ventas = valor(ventasTotales);
        BigDecimal costoVentas = ventas.multiply(RATIO_COSTO_VENTAS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gastosOperativos = ventas.multiply(RATIO_GASTOS_OPERATIVOS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gastosPersonal = ventas.multiply(RATIO_GASTOS_PERSONAL).setScale(2, RoundingMode.HALF_UP);
        BigDecimal otrosGastos = ventas.multiply(RATIO_OTROS_GASTOS).setScale(2, RoundingMode.HALF_UP);
        BigDecimal utilidadBruta = ventas.subtract(costoVentas).setScale(2, RoundingMode.HALF_UP);
        BigDecimal utilidadNeta = utilidadBruta.subtract(gastosOperativos).subtract(gastosPersonal).subtract(otrosGastos)
                .setScale(2, RoundingMode.HALF_UP);

        dashboard.setVentasNetas(ventas);
        dashboard.setUtilidadBruta(utilidadBruta);
        dashboard.setUtilidadNeta(utilidadNeta);
        dashboard.setMargenNeto(calcularPorcentaje(utilidadNeta, ventas));
        dashboard.setPorcentajeUtilidadBruta(calcularPorcentaje(utilidadBruta, ventas));

        List<ReporteCostoDistribucion> costos = new ArrayList<>();
        costos.add(new ReporteCostoDistribucion("Costo de ventas", costoVentas, calcularPorcentaje(costoVentas, ventas)));
        costos.add(new ReporteCostoDistribucion("Gastos operativos", gastosOperativos,
                calcularPorcentaje(gastosOperativos, ventas)));
        costos.add(new ReporteCostoDistribucion("Gastos de personal", gastosPersonal,
                calcularPorcentaje(gastosPersonal, ventas)));
        costos.add(new ReporteCostoDistribucion("Otros gastos", otrosGastos, calcularPorcentaje(otrosGastos, ventas)));
        dashboard.setDistribucionCostos(costos);
        dashboard.setRentabilidadDiaria(listarRentabilidadDiaria(desde, hasta));
    }

    private List<ReporteRentabilidadDia> listarRentabilidadDiaria(String desde, String hasta) {
        List<ReporteRentabilidadDia> filas = new ArrayList<>();
        String sql = "SELECT DATE(fecha_pago) AS fecha, COALESCE(SUM(monto_pago), 0) AS ventas "
                + "FROM pago WHERE estado_pago = 'pagado' AND fecha_pago BETWEEN ? AND ? "
                + "GROUP BY DATE(fecha_pago) ORDER BY fecha DESC";

        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal ventas = valor(rs.getBigDecimal("ventas"));
                    BigDecimal costoVentas = ventas.multiply(RATIO_COSTO_VENTAS).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal gastosOperativos = ventas.multiply(RATIO_GASTOS_OPERATIVOS).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal gastosPersonal = ventas.multiply(RATIO_GASTOS_PERSONAL).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal otrosGastos = ventas.multiply(RATIO_OTROS_GASTOS).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal utilidadBruta = ventas.subtract(costoVentas);
                    BigDecimal utilidadNeta = utilidadBruta.subtract(gastosOperativos).subtract(gastosPersonal)
                            .subtract(otrosGastos).setScale(2, RoundingMode.HALF_UP);

                    ReporteRentabilidadDia fila = new ReporteRentabilidadDia();
                    fila.setFecha(formatearFecha(rs.getString("fecha")));
                    fila.setVentasNetas(ventas);
                    fila.setCostoVentas(costoVentas);
                    fila.setGastosOperativos(gastosOperativos);
                    fila.setGastosPersonal(gastosPersonal);
                    fila.setOtrosGastos(otrosGastos);
                    fila.setUtilidadNeta(utilidadNeta);
                    fila.setMargenNeto(calcularPorcentaje(utilidadNeta, ventas));
                    filas.add(fila);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar rentabilidad diaria", e);
        }
        return filas;
    }

    private String normalizarFecha(String valor, LocalDate fallback) {
        if (valor == null || valor.trim().isEmpty()) {
            return fallback.toString();
        }
        try {
            return LocalDate.parse(valor.trim()).toString();
        } catch (Exception e) {
            return fallback.toString();
        }
    }

    private String formatearFecha(String fecha) {
        try {
            return LocalDate.parse(fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return fecha;
        }
    }

    private String normalizarHora(String valor, LocalTime fallback) {
        if (valor == null || valor.trim().isEmpty()) {
            return fallback.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        try {
            return LocalTime.parse(valor.trim()).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return fallback.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    private BigDecimal calcularPromedio(BigDecimal total, int cantidad) {
        if (cantidad <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPorcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return parte.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
    }

    private int consultarEntero(String sql) {
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar indicador", e);
        }
        return 0;
    }

    private int consultarEntero(String sql, String desde, String hasta) {
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar indicador", e);
        }
        return 0;
    }

    private BigDecimal consultarDecimal(String sql, String desde, String hasta) {
        try (Connection cn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return valor(rs.getBigDecimal(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar indicador monetario", e);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : numero.setScale(2, RoundingMode.HALF_UP);
    }
}
