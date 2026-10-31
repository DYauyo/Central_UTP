<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Panel principal | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="panel"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Panel principal"/>
                    <jsp:param name="subtituloHeader" value="Resumen operativo del restaurante"/>
                    <jsp:param name="tituloHeaderIcon" value="bi-speedometer2"/>
                </jsp:include>

                <main class="content-shell panel-dashboard-shell">
                    <section class="panel-kpi-grid">
                        <article class="panel-kpi-card panel-kpi-blue">
                            <div class="panel-kpi-icon"><i class="bi bi-receipt-cutoff"></i></div>
                            <div class="panel-kpi-body">
                                <span>Pedidos del dia</span>
                                <strong>${panel.pedidosDia}</strong>
                                <small>Pedidos</small>
                            </div>
                        </article>
                        <article class="panel-kpi-card panel-kpi-green">
                            <div class="panel-kpi-icon"><i class="bi bi-cash-stack"></i></div>
                            <div class="panel-kpi-body">
                                <span>Ventas del dia</span>
                                <strong>S/ <fmt:formatNumber value="${panel.ventasDia}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                <small>Ventas totales</small>
                            </div>
                        </article>
                        <article class="panel-kpi-card panel-kpi-orange">
                            <div class="panel-kpi-icon"><i class="bi bi-hourglass-split"></i></div>
                            <div class="panel-kpi-body">
                                <span>Por cobrar / en curso</span>
                                <strong>${panel.pedidosPendientes}</strong>
                                <small>Pendientes de cierre (hoy)</small>
                            </div>
                        </article>
                        <article class="panel-kpi-card panel-kpi-red">
                            <div class="panel-kpi-icon"><i class="bi bi-box-seam"></i></div>
                            <div class="panel-kpi-body">
                                <span>Insumos con stock bajo</span>
                                <strong>${panel.insumosStockBajo}</strong>
                                <small>Insumos</small>
                            </div>
                        </article>
                        <article class="panel-kpi-card panel-kpi-purple">
                            <div class="panel-kpi-icon"><i class="bi bi-trophy"></i></div>
                            <div class="panel-kpi-body">
                                <span>Productos mas vendidos</span>
                                <strong>${panel.productosTopVendidos}</strong>
                                <small><a href="${pageContext.request.contextPath}/ReporteServlet">Ver detalle</a></small>
                            </div>
                        </article>
                        <article class="panel-kpi-card panel-kpi-teal">
                            <div class="panel-kpi-icon"><i class="bi bi-calendar2-x"></i></div>
                            <div class="panel-kpi-body">
                                <span>Insumos por vencer</span>
                                <strong>${panel.insumosPorVencer}</strong>
                                <small>Proximos a vencer</small>
                            </div>
                        </article>
                    </section>

                    <section class="panel-quick-card">
                        <div class="panel-section-head">
                            <span class="panel-section-icon"><i class="bi bi-lightning-charge"></i></span>
                            <h2>Accesos rapidos</h2>
                        </div>
                        <div class="panel-quick-grid">
                            <a class="panel-quick-link quick-blue" href="${pageContext.request.contextPath}/jsf/pedidos.xhtml">
                                <i class="bi bi-plus-circle"></i> Registrar pedido
                            </a>
                            <a class="panel-quick-link quick-orange" href="${pageContext.request.contextPath}/jsf/cocina.xhtml">
                                <i class="bi bi-fire"></i> Ver cocina
                            </a>
                            <a class="panel-quick-link quick-green" href="${pageContext.request.contextPath}/MenuServlet">
                                <i class="bi bi-menu-button-wide"></i> Gestionar productos
                            </a>
                            <a class="panel-quick-link quick-teal" href="${pageContext.request.contextPath}/InsumoServlet">
                                <i class="bi bi-box-arrow-in-down"></i> Registrar ingreso de inventario
                            </a>
                            <a class="panel-quick-link quick-red" href="${pageContext.request.contextPath}/InsumoServlet">
                                <i class="bi bi-exclamation-triangle"></i> Revisar alertas
                            </a>
                            <a class="panel-quick-link quick-purple" href="${pageContext.request.contextPath}/ReporteServlet">
                                <i class="bi bi-bar-chart-line"></i> Ver reportes
                            </a>
                        </div>
                    </section>

                    <section class="panel-content-grid">
                        <article class="panel-table-card">
                            <div class="panel-card-head">
                                <h3><i class="bi bi-receipt"></i> Pedidos recientes</h3>
                            </div>
                            <div class="table-responsive">
                                <table class="table panel-table align-middle">
                                    <thead>
                                        <tr>
                                            <th>N° Pedido</th>
                                            <th>Mesa / Tipo</th>
                                            <th>Estado</th>
                                            <th>Total</th>
                                            <th>Hora</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty panel.pedidosRecientes}">
                                                <tr><td colspan="5" class="text-center text-muted py-4">No hay pedidos recientes.</td></tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${panel.pedidosRecientes}" var="pedido">
                                                    <tr>
                                                        <td>${pedido.numeroPedido}</td>
                                                        <td>${pedido.tipoEtiqueta}</td>
                                                        <td><span class="panel-state-chip">${pedido.estadoTexto}</span></td>
                                                        <td>S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                        <td>${pedido.hora}</td>
                                                    </tr>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                            <div class="panel-card-foot">
                                <a href="${pageContext.request.contextPath}/jsf/pedidos.xhtml">Ver todos los pedidos</a>
                            </div>
                        </article>

                        <article class="panel-table-card">
                            <div class="panel-card-head">
                                <h3><i class="bi bi-exclamation-triangle"></i> Alertas de inventario</h3>
                            </div>
                            <div class="table-responsive">
                                <table class="table panel-table align-middle">
                                    <thead>
                                        <tr>
                                            <th>Insumo</th>
                                            <th>Stock actual</th>
                                            <th>Stock minimo</th>
                                            <th>Vencimiento</th>
                                            <th>Estado</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty panel.alertasInventario}">
                                                <tr><td colspan="5" class="text-center text-muted py-4">No hay alertas de inventario.</td></tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${panel.alertasInventario}" var="alerta">
                                                    <tr>
                                                        <td>${alerta.nombreInsumo}</td>
                                                        <td><fmt:formatNumber value="${alerta.stockActual}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                        <td><fmt:formatNumber value="${alerta.stockMinimo}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                        <td>${alerta.vencimiento}</td>
                                                        <td><span class="panel-alert-chip ${alerta.estado == 'Critico' ? 'critical' : 'warning'}">${alerta.estado}</span></td>
                                                    </tr>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                            <div class="panel-card-foot">
                                <a href="${pageContext.request.contextPath}/InsumoServlet">Ver todas las alertas</a>
                            </div>
                        </article>
                    </section>

                    <section class="panel-content-grid">
                        <article class="panel-chart-card">
                            <div class="panel-card-head">
                                <h3><i class="bi bi-bar-chart"></i> Ventas semanales</h3>
                            </div>
                            <canvas id="chartVentasSemanales"></canvas>
                        </article>

                        <article class="panel-activity-card">
                            <div class="panel-card-head">
                                <h3><i class="bi bi-clock-history"></i> Actividad reciente</h3>
                            </div>
                            <ul class="panel-activity-list">
                                <c:choose>
                                    <c:when test="${empty panel.actividadReciente}">
                                        <li class="panel-activity-item">
                                            <span>No hay actividad reciente registrada.</span>
                                        </li>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${panel.actividadReciente}" var="actividad">
                                            <li class="panel-activity-item">
                                                <span>${actividad.descripcion}</span>
                                                <small>${actividad.tiempo}</small>
                                            </li>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </ul>
                            <div class="panel-card-foot">
                                <a href="${pageContext.request.contextPath}/ReporteServlet">Ver toda la actividad</a>
                            </div>
                        </article>
                    </section>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
        <script>
            const ventasLabels = [
                <c:forEach items="${panel.ventasSemanales}" var="venta" varStatus="st">
                    '${venta.dia}'${not st.last ? ',' : ''}
                </c:forEach>
            ];
            const ventasValores = [
                <c:forEach items="${panel.ventasSemanales}" var="venta" varStatus="st">
                    ${venta.monto}${not st.last ? ',' : ''}
                </c:forEach>
            ];

            if (document.getElementById('chartVentasSemanales')) {
                new Chart(document.getElementById('chartVentasSemanales'), {
                    type: 'bar',
                    data: {
                        labels: ventasLabels,
                        datasets: [{
                            label: 'Ventas (S/)',
                            data: ventasValores,
                            backgroundColor: '#60a5fa',
                            borderColor: '#2563eb',
                            borderWidth: 1,
                            borderRadius: 8
                        }]
                    },
                    options: {
                        plugins: { legend: { display: false } },
                        scales: { y: { beginAtZero: true } },
                        maintainAspectRatio: false
                    }
                });
            }
        </script>
    </body>
</html>
