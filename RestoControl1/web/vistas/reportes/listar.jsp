<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Reportes | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="reportes"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Reportes Operativos y Financieros"/>
                    <jsp:param name="subtituloHeader" value="Analiza ventas, metodos de pago y rentabilidad del periodo"/>
                    <jsp:param name="tituloHeaderIcon" value="bi-bar-chart-line"/>
                </jsp:include>

                <main class="content-shell report-dashboard-shell">
                    <section class="report-filter-card report-filter-hero">
                        <div class="report-filter-top">
                            <div>
                                <span class="report-filter-kicker">Filtros del reporte</span>
                                <h2>Configura el periodo y tipo de analisis</h2>
                            </div>
                            <button class="report-filter-badge border-0"
                                    type="button"
                                    data-bs-toggle="modal"
                                    data-bs-target="#modalMetricaDetallada">
                                <i class="bi bi-graph-up-arrow"></i> Analitica
                            </button>
                        </div>
                        <form class="report-filter-form" method="get" action="${pageContext.request.contextPath}/ReporteServlet">
                            <div class="report-filter-grid">
                                <label>
                                    <span>Tipo de reporte</span>
                                    <select class="form-select" name="tipo">
                                        <option value="ventas" ${reporte.tipoVentas ? 'selected' : ''}>Ventas</option>
                                        <option value="rentabilidad" ${not reporte.tipoVentas ? 'selected' : ''}>Rentabilidad</option>
                                    </select>
                                </label>
                                <label>
                                    <span>Desde</span>
                                    <input class="form-control" type="date" name="desde" value="${reporte.desde}"/>
                                </label>
                                <label>
                                    <span>Hasta</span>
                                    <input class="form-control" type="date" name="hasta" value="${reporte.hasta}"/>
                                </label>
                                <label>
                                    <span>Hora desde</span>
                                    <input class="form-control" type="time" name="horaDesde" value="${reporte.horaDesde}"/>
                                </label>
                                <label>
                                    <span>Hora hasta</span>
                                    <input class="form-control" type="time" name="horaHasta" value="${reporte.horaHasta}"/>
                                </label>
                                <div class="report-filter-actions">
                                    <button class="btn btn-resto-dark" type="submit">
                                        <i class="bi bi-play-fill"></i> Generar
                                    </button>
                                    <button class="btn btn-outline-resto" type="button" data-bs-toggle="modal" data-bs-target="#modalExportarReporte">
                                        <i class="bi bi-upload"></i> Exportar reporte
                                    </button>
                                </div>
                            </div>
                        </form>
                    </section>

                    <c:choose>
                        <c:when test="${reporte.tipoVentas}">
                            <section class="report-kpi-grid">
                                <article class="report-kpi-card report-kpi-green">
                                    <div class="report-kpi-icon"><i class="bi bi-cash-coin"></i></div>
                                    <div>
                                        <span>Ventas brutas totales (S/)</span>
                                        <strong><fmt:formatNumber value="${reporte.ventasBrutas}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                    </div>
                                </article>
                                <article class="report-kpi-card report-kpi-blue">
                                    <div class="report-kpi-icon"><i class="bi bi-receipt"></i></div>
                                    <div>
                                        <span>Total tickets emitidos</span>
                                        <strong>${reporte.totalTickets}</strong>
                                    </div>
                                </article>
                                <article class="report-kpi-card report-kpi-purple">
                                    <div class="report-kpi-icon"><i class="bi bi-calculator"></i></div>
                                    <div>
                                        <span>Ticket promedio (S/)</span>
                                        <strong><fmt:formatNumber value="${reporte.ticketPromedio}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                    </div>
                                </article>
                            </section>

                            <section class="report-chart-grid">
                                <article class="report-chart-card report-chart-payments">
                                    <div class="report-chart-head">
                                        <span class="report-chart-icon"><i class="bi bi-pie-chart"></i></span>
                                        <h3>Metodos de pago</h3>
                                    </div>
                                    <canvas id="chartMetodosPago"></canvas>
                                </article>
                                <article class="report-chart-card report-chart-hours">
                                    <div class="report-chart-head">
                                        <span class="report-chart-icon"><i class="bi bi-clock-history"></i></span>
                                        <h3>Horas punta de atencion</h3>
                                    </div>
                                    <canvas id="chartHorasPunta"></canvas>
                                </article>
                            </section>

                            <section class="report-table-card">
                                <div class="report-table-head">
                                    <span class="report-table-icon"><i class="bi bi-table"></i></span>
                                    <h3>Ultimas ventas registradas</h3>
                                </div>
                                <div class="table-responsive">
                                    <table class="table report-table align-middle">
                                        <thead>
                                            <tr>
                                                <th>Fecha / Hora</th>
                                                <th>Ticket</th>
                                                <th>Mesa</th>
                                                <th>Cliente</th>
                                                <th>Metodo de pago</th>
                                                <th>Total (S/)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty reporte.filasVentas}">
                                                    <tr>
                                                        <td colspan="6" class="text-center text-muted py-4">No hay ventas registradas en el periodo seleccionado.</td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${reporte.filasVentas}" var="venta">
                                                        <tr>
                                                            <td>${venta.fechaHora}</td>
                                                            <td>${venta.ticket}</td>
                                                            <td>${venta.etiquetaMesa}</td>
                                                            <td>${venta.cliente}</td>
                                                            <td>${venta.etiquetaMedio}</td>
                                                            <td><fmt:formatNumber value="${venta.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </section>

                            <section class="report-table-card">
                                <div class="report-table-head">
                                    <span class="report-table-icon"><i class="bi bi-trophy"></i></span>
                                    <h3>Productos mas vendidos</h3>
                                </div>
                                <div class="table-responsive">
                                    <table class="table report-table align-middle">
                                        <thead>
                                            <tr>
                                                <th>Producto</th>
                                                <th>Categoria</th>
                                                <th>Cantidad vendida</th>
                                                <th>Total vendido (S/)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty reporte.productosTop}">
                                                    <tr>
                                                        <td colspan="4" class="text-center text-muted py-4">No hay productos vendidos en el periodo seleccionado.</td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${reporte.productosTop}" var="producto">
                                                        <tr>
                                                            <td>${producto.nombreProducto}</td>
                                                            <td>${empty producto.categoria ? 'Sin categoria' : producto.categoria}</td>
                                                            <td>${producto.cantidadVendida}</td>
                                                            <td><fmt:formatNumber value="${producto.totalVendido}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </section>
                        </c:when>
                        <c:otherwise>
                            <section class="report-kpi-grid report-kpi-grid-4">
                                <article class="report-kpi-card report-kpi-green">
                                    <div class="report-kpi-icon"><i class="bi bi-graph-up"></i></div>
                                    <div>
                                        <span>Ventas netas (S/)</span>
                                        <strong><fmt:formatNumber value="${reporte.ventasNetas}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                    </div>
                                </article>
                                <article class="report-kpi-card report-kpi-blue">
                                    <div class="report-kpi-icon"><i class="bi bi-piggy-bank"></i></div>
                                    <div>
                                        <span>Utilidad bruta (S/)</span>
                                        <strong>
                                            <fmt:formatNumber value="${reporte.utilidadBruta}" minFractionDigits="2" maxFractionDigits="2"/>
                                            (<fmt:formatNumber value="${reporte.porcentajeUtilidadBruta}" minFractionDigits="2" maxFractionDigits="2"/>%)
                                        </strong>
                                    </div>
                                </article>
                                <article class="report-kpi-card report-kpi-purple">
                                    <div class="report-kpi-icon"><i class="bi bi-currency-dollar"></i></div>
                                    <div>
                                        <span>Utilidad neta (S/)</span>
                                        <strong><fmt:formatNumber value="${reporte.utilidadNeta}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                    </div>
                                </article>
                                <article class="report-kpi-card report-kpi-orange">
                                    <div class="report-kpi-icon"><i class="bi bi-percent"></i></div>
                                    <div>
                                        <span>Margen neto (%)</span>
                                        <strong><fmt:formatNumber value="${reporte.margenNeto}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                    </div>
                                </article>
                            </section>

                            <section class="report-chart-grid">
                                <article class="report-chart-card report-chart-costs">
                                    <div class="report-chart-head">
                                        <span class="report-chart-icon"><i class="bi bi-pie-chart-fill"></i></span>
                                        <h3>Distribucion de costos (S/)</h3>
                                    </div>
                                    <canvas id="chartDistribucionCostos"></canvas>
                                </article>
                                <article class="report-chart-card report-chart-profit">
                                    <div class="report-chart-head">
                                        <span class="report-chart-icon"><i class="bi bi-activity"></i></span>
                                        <h3>Utilidad neta (S/)</h3>
                                    </div>
                                    <canvas id="chartUtilidadNeta"></canvas>
                                </article>
                            </section>

                            <section class="report-table-card">
                                <div class="report-table-head">
                                    <span class="report-table-icon"><i class="bi bi-calendar3"></i></span>
                                    <h3>Resumen de rentabilidad por dia</h3>
                                </div>
                                <div class="table-responsive">
                                    <table class="table report-table align-middle">
                                        <thead>
                                            <tr>
                                                <th>Fecha</th>
                                                <th>Ventas netas</th>
                                                <th>Costo ventas</th>
                                                <th>Gastos operativos</th>
                                                <th>Gastos personal</th>
                                                <th>Otros gastos</th>
                                                <th>Utilidad neta</th>
                                                <th>Margen neto</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty reporte.rentabilidadDiaria}">
                                                    <tr>
                                                        <td colspan="8" class="text-center text-muted py-4">No hay datos de rentabilidad en el periodo seleccionado.</td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${reporte.rentabilidadDiaria}" var="dia">
                                                        <tr>
                                                            <td>${dia.fecha}</td>
                                                            <td><fmt:formatNumber value="${dia.ventasNetas}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                            <td><fmt:formatNumber value="${dia.costoVentas}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                            <td><fmt:formatNumber value="${dia.gastosOperativos}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                            <td><fmt:formatNumber value="${dia.gastosPersonal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                            <td><fmt:formatNumber value="${dia.otrosGastos}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                            <td><fmt:formatNumber value="${dia.utilidadNeta}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                            <td><fmt:formatNumber value="${dia.margenNeto}" minFractionDigits="2" maxFractionDigits="2"/>%</td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>
                            </section>
                        </c:otherwise>
                    </c:choose>
                </main>
            </div>
        </div>

        <div class="modal fade resto-modal modal-report-export" id="modalExportarReporte" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span>Exportar reporte</span>
                            <h2>Seleccione el formato</h2>
                        </div>
                        <span class="resto-modal-icon"><i class="bi bi-download"></i></span>
                    </div>
                    <div class="modal-body">
                        <p class="report-export-hint">
                            Seleccione el formato para descargar el reporte de
                            <strong>${reporte.tipoVentas ? 'Ventas' : 'Rentabilidad'}</strong>.
                        </p>
                        <div class="report-export-options">
                            <label class="report-export-option">
                                <input type="radio" name="formatoExportacion" value="pdf" checked/>
                                <span>
                                    <strong>Exportar como PDF</strong>
                                    <small>Documento formal</small>
                                </span>
                            </label>
                            <label class="report-export-option">
                                <input type="radio" name="formatoExportacion" value="excel"/>
                                <span>
                                    <strong>Exportar como Excel</strong>
                                    <small>Datos para analisis</small>
                                </span>
                            </label>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn btn-resto-dark" id="btnDescargarReporte"
                                data-base="${pageContext.request.contextPath}/ReporteServlet"
                                data-tipo="${reporte.tipo}"
                                data-desde="${reporte.desde}"
                                data-hasta="${reporte.hasta}"
                                data-hora-desde="${reporte.horaDesde}"
                                data-hora-hasta="${reporte.horaHasta}">
                            Descargar
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="modal fade resto-modal" id="modalMetricaDetallada" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-xl">
                <div class="modal-content">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span>Vista metrica detallada</span>
                            <h2>${reporte.tipoVentas ? 'Analitica de ventas' : 'Analitica de rentabilidad'}</h2>
                        </div>
                        <span class="resto-modal-icon"><i class="bi bi-activity"></i></span>
                    </div>
                    <div class="modal-body">
                        <p class="text-muted mb-3">
                            Periodo analizado: <strong>${reporte.desde} ${reporte.horaDesde}</strong> al <strong>${reporte.hasta} ${reporte.horaHasta}</strong>
                        </p>

                        <c:choose>
                            <c:when test="${reporte.tipoVentas}">
                                <div class="row g-3 mb-3">
                                    <div class="col-md-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Ventas brutas</small>
                                            <strong>S/ <fmt:formatNumber value="${reporte.ventasBrutas}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Total tickets</small>
                                            <strong>${reporte.totalTickets}</strong>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Ticket promedio</small>
                                            <strong>S/ <fmt:formatNumber value="${reporte.ticketPromedio}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Productos top</small>
                                            <strong>${fn:length(reporte.productosTop)}</strong>
                                        </div>
                                    </div>
                                </div>

                                <div class="table-responsive">
                                    <table class="table table-sm align-middle">
                                        <thead>
                                            <tr>
                                                <th>Producto</th>
                                                <th>Categoria</th>
                                                <th>Cantidad</th>
                                                <th>Total (S/)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${reporte.productosTop}" var="producto">
                                                <tr>
                                                    <td>${producto.nombreProducto}</td>
                                                    <td>${empty producto.categoria ? 'Sin categoria' : producto.categoria}</td>
                                                    <td>${producto.cantidadVendida}</td>
                                                    <td><fmt:formatNumber value="${producto.totalVendido}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="row g-3">
                                    <div class="col-md-6 col-lg-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Ventas netas</small>
                                            <strong>S/ <fmt:formatNumber value="${reporte.ventasNetas}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                        </div>
                                    </div>
                                    <div class="col-md-6 col-lg-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Utilidad bruta</small>
                                            <strong>S/ <fmt:formatNumber value="${reporte.utilidadBruta}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                        </div>
                                    </div>
                                    <div class="col-md-6 col-lg-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Utilidad neta</small>
                                            <strong>S/ <fmt:formatNumber value="${reporte.utilidadNeta}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                        </div>
                                    </div>
                                    <div class="col-md-6 col-lg-3">
                                        <div class="p-3 rounded border bg-light h-100">
                                            <small class="text-muted d-block">Margen neto</small>
                                            <strong><fmt:formatNumber value="${reporte.margenNeto}" minFractionDigits="2" maxFractionDigits="2"/>%</strong>
                                        </div>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
        <script>
            const reporteVentas = ${reporte.tipoVentas};

            const metodosLabels = [
                <c:forEach items="${reporte.metodosPago}" var="metodo" varStatus="st">
                    '${metodo.etiqueta}'${not st.last ? ',' : ''}
                </c:forEach>
            ];
            const metodosValores = [
                <c:forEach items="${reporte.metodosPago}" var="metodo" varStatus="st">
                    ${metodo.monto}${not st.last ? ',' : ''}
                </c:forEach>
            ];

            const horasLabels = [
                <c:forEach items="${reporte.horasPunta}" var="hora" varStatus="st">
                    '${hora.etiqueta}'${not st.last ? ',' : ''}
                </c:forEach>
            ];
            const horasValores = [
                <c:forEach items="${reporte.horasPunta}" var="hora" varStatus="st">
                    ${hora.monto}${not st.last ? ',' : ''}
                </c:forEach>
            ];

            const costosLabels = [
                <c:forEach items="${reporte.distribucionCostos}" var="costo" varStatus="st">
                    '${costo.concepto}'${not st.last ? ',' : ''}
                </c:forEach>
            ];
            const costosValores = [
                <c:forEach items="${reporte.distribucionCostos}" var="costo" varStatus="st">
                    ${costo.monto}${not st.last ? ',' : ''}
                </c:forEach>
            ];

            const utilidadValores = horasValores.map(function (venta) {
                return Number((venta * 0.15).toFixed(2));
            });

            const chartColors = ['#2563eb', '#16a34a', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

            if (reporteVentas && document.getElementById('chartMetodosPago')) {
                new Chart(document.getElementById('chartMetodosPago'), {
                    type: 'doughnut',
                    data: {
                        labels: metodosLabels,
                        datasets: [{
                            data: metodosValores,
                            backgroundColor: chartColors,
                            borderWidth: 0
                        }]
                    },
                    options: {
                        plugins: { legend: { position: 'bottom' } },
                        maintainAspectRatio: false
                    }
                });

                new Chart(document.getElementById('chartHorasPunta'), {
                    type: 'line',
                    data: {
                        labels: horasLabels,
                        datasets: [{
                            label: 'Ventas (S/)',
                            data: horasValores,
                            borderColor: '#2563eb',
                            backgroundColor: 'rgba(37, 99, 235, 0.12)',
                            fill: true,
                            tension: 0.35
                        }]
                    },
                    options: {
                        plugins: { legend: { display: false } },
                        scales: {
                            y: { beginAtZero: true }
                        },
                        maintainAspectRatio: false
                    }
                });
            }

            if (!reporteVentas && document.getElementById('chartDistribucionCostos')) {
                new Chart(document.getElementById('chartDistribucionCostos'), {
                    type: 'pie',
                    data: {
                        labels: costosLabels,
                        datasets: [{
                            data: costosValores,
                            backgroundColor: chartColors,
                            borderWidth: 0
                        }]
                    },
                    options: {
                        plugins: { legend: { position: 'bottom' } },
                        maintainAspectRatio: false
                    }
                });

                new Chart(document.getElementById('chartUtilidadNeta'), {
                    type: 'line',
                    data: {
                        labels: horasLabels,
                        datasets: [{
                            label: 'Utilidad neta (S/)',
                            data: utilidadValores,
                            borderColor: '#2563eb',
                            backgroundColor: 'rgba(37, 99, 235, 0.12)',
                            fill: true,
                            tension: 0.35
                        }]
                    },
                    options: {
                        plugins: { legend: { display: false } },
                        scales: {
                            y: { beginAtZero: true }
                        },
                        maintainAspectRatio: false
                    }
                });
            }

            (function () {
                var btn = document.getElementById('btnDescargarReporte');
                if (!btn) {
                    return;
                }
                btn.addEventListener('click', function () {
                    var formato = document.querySelector('input[name="formatoExportacion"]:checked');
                    var url = btn.getAttribute('data-base')
                            + '?accion=exportar'
                            + '&tipo=' + encodeURIComponent(btn.getAttribute('data-tipo'))
                            + '&desde=' + encodeURIComponent(btn.getAttribute('data-desde'))
                            + '&hasta=' + encodeURIComponent(btn.getAttribute('data-hasta'))
                            + '&horaDesde=' + encodeURIComponent(btn.getAttribute('data-hora-desde'))
                            + '&horaHasta=' + encodeURIComponent(btn.getAttribute('data-hora-hasta'))
                            + '&formato=' + encodeURIComponent(formato ? formato.value : 'pdf');

                    fetch(url, { credentials: 'same-origin' })
                        .then(function (response) {
                            if (!response.ok) {
                                throw new Error('No se pudo exportar el reporte.');
                            }
                            var disposition = response.headers.get('Content-Disposition') || '';
                            var match = disposition.match(/filename="([^"]+)"/);
                            var filename = match ? match[1] : 'reporte.pdf';
                            return response.blob().then(function (blob) {
                                return { blob: blob, filename: filename };
                            });
                        })
                        .then(function (archivo) {
                            var objectUrl = URL.createObjectURL(archivo.blob);
                            var enlace = document.createElement('a');
                            enlace.href = objectUrl;
                            enlace.download = archivo.filename;
                            document.body.appendChild(enlace);
                            enlace.click();
                            enlace.remove();
                            URL.revokeObjectURL(objectUrl);
                            bootstrap.Modal.getInstance(document.getElementById('modalExportarReporte')).hide();
                        })
                        .catch(function () {
                            alert('No se pudo exportar el reporte. Intenta nuevamente.');
                        });
                });
            })();
        </script>
    </body>
</html>
