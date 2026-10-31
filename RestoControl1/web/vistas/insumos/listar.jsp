<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="activeTab" value="${empty tabActivo ? 'inventario' : tabActivo}"/>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Insumos y Recetas | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body class="inventory-page">
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="insumos"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Insumos y Recetas"/>
                    <jsp:param name="subtituloHeader" value="Controla almacen y recetas para activar la estimacion diaria"/>
                </jsp:include>

                <main class="content-shell inventory-shell">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible auto-alert" data-auto-alert role="alert">
                            ${error}
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>

                    <c:if test="${param.mensaje == 'registrado'}">
                        <div class="alert alert-success alert-dismissible auto-alert" data-auto-alert role="alert">
                            Insumo registrado correctamente.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'actualizado'}">
                        <div class="alert alert-success alert-dismissible auto-alert" data-auto-alert role="alert">
                            Insumo actualizado correctamente.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'desactivado'}">
                        <div class="alert alert-warning alert-dismissible auto-alert" data-auto-alert role="alert">
                            Insumo desactivado correctamente.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'estado_insumo'}">
                        <div class="alert alert-success alert-dismissible auto-alert" data-auto-alert role="alert">
                            Estado del insumo actualizado correctamente.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'no_encontrado'}">
                        <div class="alert alert-danger alert-dismissible auto-alert" data-auto-alert role="alert">
                            No se encontro el insumo solicitado.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'receta_guardada'}">
                        <div class="alert alert-success alert-dismissible auto-alert" data-auto-alert role="alert">
                            Receta guardada correctamente. Ya puede ser validada desde Estimacion diaria.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'receta_limpiada'}">
                        <div class="alert alert-warning alert-dismissible auto-alert" data-auto-alert role="alert">
                            Receta limpiada correctamente.
                            <button type="button" class="btn-close" aria-label="Cerrar" data-alert-close></button>
                        </div>
                    </c:if>

                    <section class="inventory-stat-grid">
                        <article class="inventory-stat-card">
                            <i class="bi bi-box-seam"></i>
                            <div>
                                <strong>${totalInsumos}</strong>
                                <span>Insumos registrados</span>
                                <small>en almacen</small>
                            </div>
                        </article>
                        <article class="inventory-stat-card">
                            <i class="bi bi-exclamation-triangle"></i>
                            <div>
                                <strong>${insumosStockBajo}</strong>
                                <span>Stock bajo</span>
                                <small>requieren revision</small>
                            </div>
                        </article>
                        <article class="inventory-stat-card">
                            <i class="bi bi-journal-check"></i>
                            <div>
                                <strong>${recetasCreadas}</strong>
                                <span>Recetas creadas</span>
                                <small>listas para validar</small>
                            </div>
                        </article>
                        <article class="inventory-stat-card">
                            <i class="bi bi-journal-x"></i>
                            <div>
                                <strong>${recetasPendientes}</strong>
                                <span>Sin receta</span>
                                <small>no validan stock</small>
                            </div>
                        </article>
                    </section>

                    <nav class="inventory-tabs" aria-label="Secciones de insumos">
                        <a class="${activeTab == 'inventario' ? 'active' : ''}" href="${pageContext.request.contextPath}/InsumoServlet?tab=inventario">
                            <i class="bi bi-boxes"></i> Inventario
                        </a>
                        <a class="${activeTab == 'recetas' ? 'active' : ''}" href="${pageContext.request.contextPath}/InsumoServlet?tab=recetas">
                            <i class="bi bi-journal-richtext"></i> Recetas por plato
                        </a>
                    </nav>

                    <c:choose>
                        <c:when test="${activeTab == 'recetas'}">
                            <section class="inventory-surface recipe-surface">
                                <div class="inventory-section-head">
                                    <div>
                                        <h1>Recetas por plato</h1>
                                        <p>Define los insumos por porcion para que Estimacion diaria calcule stock suficiente, bajo o critico.</p>
                                    </div>
                                    <button type="button" class="btn user-primary-btn" data-bs-toggle="modal" data-bs-target="#modalReceta" data-mode="new">
                                        <i class="bi bi-plus-lg"></i> Registrar receta
                                    </button>
                                </div>

                                <div class="inventory-actionbar">
                                    <label class="search-pill">
                                        <i class="bi bi-search"></i>
                                        <input type="search" placeholder="Buscar plato, categoria o estado..." data-inventory-search data-target="recipesGrid">
                                    </label>
                                    <div class="inventory-filter-row">
                                        <button class="filter-chip active" type="button" data-card-filter data-target="recipesGrid" data-filter-type="recipeState" data-filter-value="">Todas</button>
                                        <button class="filter-chip" type="button" data-card-filter data-target="recipesGrid" data-filter-type="recipeState" data-filter-value="lista">Con receta</button>
                                        <button class="filter-chip" type="button" data-card-filter data-target="recipesGrid" data-filter-type="recipeState" data-filter-value="pendiente">Sin receta</button>
                                        <button class="filter-chip" type="button" data-card-filter data-target="recipesGrid" data-filter-type="stockState" data-filter-value="bajo">Stock bajo</button>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${empty recetas}">
                                        <div class="empty-state">
                                            <h2 class="h5">No hay platos registrados</h2>
                                            <p>Primero registra platos en Menu y Categorias para poder crear sus recetas.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="recipe-card-grid" id="recipesGrid">
                                            <c:forEach var="receta" items="${recetas}">
                                                <article class="recipe-card ${receta.producto.iconoColorCss} ${receta.listaParaEstimacion ? '' : 'needs-recipe'}"
                                                         data-search="${fn:escapeXml(receta.producto.nombreProducto)} ${fn:escapeXml(receta.producto.nombreCategoria)} ${fn:escapeXml(receta.estadoReceta)}"
                                                         data-category-id="${receta.producto.idCategoria}"
                                                         data-recipe-state="${receta.tieneReceta ? 'lista' : 'pendiente'}"
                                                         data-stock-state="${receta.insumosBajoMinimo > 0 ? 'bajo' : 'ok'}">
                                                    <div class="recipe-hero">
                                                        <span class="recipe-icon">
                                                            <i class="bi ${receta.producto.iconoCss}"></i>
                                                        </span>
                                                        <div>
                                                            <h2>${receta.producto.nombreProducto}</h2>
                                                            <p>${receta.producto.nombreCategoria}</p>
                                                        </div>
                                                        <span class="soft-pill ${receta.estadoCss}">${receta.estadoReceta}</span>
                                                    </div>

                                                    <div class="recipe-metrics">
                                                        <span>
                                                            <strong>${receta.cantidadInsumos}</strong>
                                                            <small>Insumos</small>
                                                        </span>
                                                        <span>
                                                            <strong>${receta.insumosBajoMinimo}</strong>
                                                            <small>Bajo minimo</small>
                                                        </span>
                                                        <span>
                                                            <strong>${receta.listaParaEstimacion ? 'Si' : 'No'}</strong>
                                                            <small>Estimacion</small>
                                                        </span>
                                                    </div>

                                                    <div class="recipe-ingredients">
                                                        <c:choose>
                                                            <c:when test="${empty receta.insumos}">
                                                                <p class="recipe-empty-note">
                                                                    <i class="bi bi-info-circle"></i>
                                                                    Aun no tiene insumos. Este plato se marcara como sin receta en Estimacion diaria.
                                                                </p>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:forEach var="ing" items="${receta.insumos}">
                                                                    <div class="ingredient-line">
                                                                        <span>${ing.nombreInsumo}</span>
                                                                        <strong>${ing.cantidadPorPorcion} ${ing.unidadMedida}/porcion</strong>
                                                                    </div>
                                                                </c:forEach>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <div class="d-none" data-recipe-data>
                                                        <c:forEach var="ing" items="${receta.insumos}">
                                                            <span data-id="${ing.idInsumo}" data-cantidad="${ing.cantidadPorPorcion}"></span>
                                                        </c:forEach>
                                                    </div>

                                                    <div class="recipe-actions">
                                                        <button type="button"
                                                                class="btn btn-outline-resto"
                                                                data-bs-toggle="modal"
                                                                data-bs-target="#modalReceta"
                                                                data-mode="edit"
                                                                data-producto="${receta.producto.idProducto}"
                                                                data-nombre="${fn:escapeXml(receta.producto.nombreProducto)}">
                                                            <i class="bi bi-pencil"></i> Editar receta
                                                        </button>
                                                        <c:if test="${receta.tieneReceta}">
                                                            <form method="post" action="${pageContext.request.contextPath}/InsumoServlet">
                                                                <input type="hidden" name="accion" value="limpiarReceta">
                                                                <input type="hidden" name="idProducto" value="${receta.producto.idProducto}">
                                                                <button type="submit" class="btn btn-outline-secondary">
                                                                    <i class="bi bi-trash3"></i> Limpiar
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                </article>
                                            </c:forEach>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </c:when>
                        <c:otherwise>
                            <section class="inventory-surface">
                                <div class="inventory-section-head">
                                    <div>
                                        <h1>Inventario de insumos</h1>
                                        <p>Controla ingredientes, unidades de medida y niveles minimos para cocina.</p>
                                    </div>
                                    <a class="btn user-primary-btn" href="${pageContext.request.contextPath}/InsumoServlet?accion=nuevo">
                                        <i class="bi bi-plus-lg"></i> Nuevo insumo
                                    </a>
                                </div>

                                <div class="inventory-actionbar">
                                    <label class="search-pill">
                                        <i class="bi bi-search"></i>
                                        <input type="search" placeholder="Buscar insumo, unidad o estado..." data-inventory-search data-target="insumosGrid">
                                    </label>
                                    <div class="inventory-filter-row">
                                        <button class="filter-chip active" type="button" data-card-filter data-target="insumosGrid" data-filter-type="status" data-filter-value="">Todos</button>
                                        <button class="filter-chip" type="button" data-card-filter data-target="insumosGrid" data-filter-type="status" data-filter-value="activo">Activos</button>
                                        <button class="filter-chip" type="button" data-card-filter data-target="insumosGrid" data-filter-type="status" data-filter-value="inactivo">Inactivos</button>
                                        <button class="filter-chip" type="button" data-card-filter data-target="insumosGrid" data-filter-type="stock" data-filter-value="bajo">Stock bajo</button>
                                        <select class="form-select inventory-unit-filter" data-inventory-unit data-target="insumosGrid">
                                            <option value="">Unidad de medida</option>
                                        </select>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${empty insumos}">
                                        <div class="empty-state">
                                            <h2 class="h5">No hay insumos registrados</h2>
                                            <p>Registra el primer insumo para controlar el inventario del restaurante.</p>
                                            <a class="btn user-primary-btn" href="${pageContext.request.contextPath}/InsumoServlet?accion=nuevo">Registrar insumo</a>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="inventory-card-grid" id="insumosGrid">
                                            <c:forEach var="insumo" items="${insumos}">
                                                <article class="inventory-card ${insumo.stockBajo ? 'stock-low' : ''} ${insumo.estado == 'inactivo' ? 'is-inactive' : ''}"
                                                         data-search="${fn:escapeXml(insumo.nombreInsumo)} ${fn:escapeXml(insumo.descripcion)} ${fn:escapeXml(insumo.unidadMedida)} ${fn:escapeXml(insumo.estado)}"
                                                         data-status="${fn:escapeXml(insumo.estado)}"
                                                         data-stock="${insumo.stockBajo ? 'bajo' : 'ok'}"
                                                         data-unit="${fn:escapeXml(insumo.unidadMedida)}">
                                                    <div class="inventory-card-head">
                                                        <span class="inventory-icon ${insumo.stockBajo ? 'warning' : insumo.estado == 'inactivo' ? 'muted' : 'ok'}">
                                                            <i class="bi bi-box-seam"></i>
                                                        </span>
                                                        <form method="post" action="${pageContext.request.contextPath}/InsumoServlet" class="status-toggle-form">
                                                            <input type="hidden" name="accion" value="cambiarEstadoInsumo">
                                                            <input type="hidden" name="idInsumo" value="${insumo.idInsumo}">
                                                            <input type="hidden" name="estado" value="${insumo.estado == 'activo' ? 'inactivo' : 'activo'}">
                                                            <button type="submit" class="soft-pill status-toggle ${insumo.estado == 'activo' ? 'success' : 'danger'}">
                                                                <i class="bi bi-power"></i> ${insumo.estado}
                                                            </button>
                                                        </form>
                                                    </div>
                                                    <h2>${insumo.nombreInsumo}</h2>
                                                    <p>${insumo.descripcion}</p>
                                                    <div class="inventory-stock-box">
                                                        <span>
                                                            <small>Stock actual</small>
                                                            <strong>${insumo.stockActual} ${insumo.unidadMedida}</strong>
                                                        </span>
                                                        <span>
                                                            <small>Stock minimo</small>
                                                            <strong>${insumo.stockMinimo} ${insumo.unidadMedida}</strong>
                                                        </span>
                                                    </div>
                                                    <c:if test="${insumo.stockBajo}">
                                                        <div class="inventory-warning">
                                                            <i class="bi bi-exclamation-triangle"></i>
                                                            Stock bajo para produccion
                                                        </div>
                                                    </c:if>
                                                    <div class="inventory-card-actions">
                                                        <a class="btn btn-outline-resto" href="${pageContext.request.contextPath}/InsumoServlet?accion=editar&idInsumo=${insumo.idInsumo}">
                                                            <i class="bi bi-pencil"></i> Editar
                                                        </a>
                                                    </div>
                                                </article>
                                            </c:forEach>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </c:otherwise>
                    </c:choose>
                </main>
            </div>
        </div>

        <select class="d-none" id="insumoOptionsTemplate" aria-hidden="true">
            <option value="" data-unit="">Selecciona insumo</option>
            <c:forEach var="insumo" items="${insumosActivos}">
                <option value="${insumo.idInsumo}" data-unit="${fn:escapeXml(insumo.unidadMedida)}">
                    ${insumo.nombreInsumo} (${insumo.unidadMedida})
                </option>
            </c:forEach>
        </select>

        <div class="modal fade" id="modalReceta" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-recipe-dialog">
                <form class="modal-content resto-modal modal-recipe needs-validation" method="post" action="${pageContext.request.contextPath}/InsumoServlet" novalidate>
                    <input type="hidden" name="accion" value="guardarReceta">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span><i class="bi bi-journal-richtext"></i></span>
                            <div>
                                <small>Recetas</small>
                                <h2 data-recipe-title>Registrar receta</h2>
                            </div>
                        </div>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <label class="form-label">Plato</label>
                        <select class="form-select mb-3" name="idProducto" data-recipe-product required>
                            <option value="">Selecciona un plato</option>
                            <c:forEach var="receta" items="${recetas}">
                                <option value="${receta.producto.idProducto}">
                                    ${receta.producto.nombreProducto} - ${receta.producto.nombreCategoria}
                                </option>
                            </c:forEach>
                        </select>

                        <div class="recipe-builder-head">
                            <div>
                                <strong>Insumos por porcion</strong>
                                <span>Ejemplo: arroz 0.10 kg por porcion.</span>
                            </div>
                            <button type="button" class="btn btn-sm btn-outline-resto" data-add-recipe-line>
                                <i class="bi bi-plus"></i> Agregar insumo
                            </button>
                        </div>

                        <div class="recipe-lines" data-recipe-lines></div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-resto-dark">Guardar receta</button>
                    </div>
                </form>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            document.querySelectorAll('[data-auto-alert]').forEach(function (alertBox) {
                var close = function () {
                    alertBox.classList.add('alert-fading');
                    window.setTimeout(function () {
                        alertBox.remove();
                    }, 350);
                };
                var closeButton = alertBox.querySelector('[data-alert-close]');
                if (closeButton) {
                    closeButton.addEventListener('click', close);
                }
                window.setTimeout(close, 4000);
            });

            document.querySelectorAll('[data-inventory-search]').forEach(function (input) {
                input.addEventListener('input', function () {
                    var target = document.getElementById(input.dataset.target);
                    if (!target) {
                        return;
                    }
                    var term = input.value.trim().toLowerCase();
                    applyInventoryFilters(target);
                });
            });

            function applyInventoryFilters(target) {
                var searchInput = document.querySelector('[data-inventory-search][data-target="' + target.id + '"]');
                var unitInput = document.querySelector('[data-inventory-unit][data-target="' + target.id + '"]');
                var term = searchInput ? searchInput.value.trim().toLowerCase() : '';
                var unit = unitInput ? unitInput.value.trim().toLowerCase() : '';
                target.querySelectorAll('[data-search]').forEach(function (card) {
                    var content = (card.dataset.search || '').toLowerCase();
                    var matchesSearch = !term || content.indexOf(term) !== -1;
                    var matchesUnit = !unit || (card.dataset.unit || '').toLowerCase() === unit;
                    var matchesFilters = true;
                    document.querySelectorAll('[data-card-filter][data-target="' + target.id + '"].active').forEach(function (button) {
                        var value = button.dataset.filterValue || '';
                        if (!value) {
                            return;
                        }
                        matchesFilters = matchesFilters && (card.dataset[button.dataset.filterType] || '') === value;
                    });
                    card.hidden = !matchesSearch || !matchesUnit || !matchesFilters;
                });
            }

            document.querySelectorAll('[data-inventory-unit]').forEach(function (select) {
                var target = document.getElementById(select.dataset.target);
                var units = [];
                if (target) {
                    target.querySelectorAll('[data-unit]').forEach(function (card) {
                        var unit = card.dataset.unit || '';
                        if (unit && units.indexOf(unit) === -1) {
                            units.push(unit);
                        }
                    });
                }
                units.sort().forEach(function (unit) {
                    var option = document.createElement('option');
                    option.value = unit;
                    option.textContent = unit;
                    select.appendChild(option);
                });
                select.addEventListener('change', function () {
                    if (target) {
                        applyInventoryFilters(target);
                    }
                });
            });

            document.querySelectorAll('[data-card-filter]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var targetName = button.dataset.target;
                    var type = button.dataset.filterType;
                    var value = button.dataset.filterValue || '';
                    var selector = '[data-card-filter][data-target="' + targetName + '"]';
                    if (!value) {
                        document.querySelectorAll(selector).forEach(function (item) {
                            item.classList.toggle('active', item === button);
                        });
                    } else {
                        document.querySelectorAll(selector + '[data-filter-type="' + type + '"]').forEach(function (item) {
                            item.classList.toggle('active', item === button);
                        });
                        document.querySelectorAll(selector + '[data-filter-value=""]').forEach(function (item) {
                            item.classList.remove('active');
                        });
                    }
                    var hasSpecificFilter = false;
                    document.querySelectorAll(selector).forEach(function (item) {
                        if (item.classList.contains('active') && (item.dataset.filterValue || '')) {
                            hasSpecificFilter = true;
                        }
                    });
                    if (!hasSpecificFilter) {
                        document.querySelectorAll(selector + '[data-filter-value=""]').forEach(function (item) {
                            item.classList.add('active');
                        });
                    }
                    var target = document.getElementById(button.dataset.target);
                    if (target) {
                        applyInventoryFilters(target);
                    }
                });
            });

            (function () {
                var modal = document.getElementById('modalReceta');
                var template = document.getElementById('insumoOptionsTemplate');
                if (!modal || !template) {
                    return;
                }

                var lines = modal.querySelector('[data-recipe-lines]');
                var product = modal.querySelector('[data-recipe-product]');
                var title = modal.querySelector('[data-recipe-title]');
                var addButton = modal.querySelector('[data-add-recipe-line]');
                var optionsHtml = template.innerHTML;

                function updateUnit(row) {
                    var select = row.querySelector('select');
                    var unit = row.querySelector('[data-unit-label]');
                    var selected = select.options[select.selectedIndex];
                    unit.textContent = selected ? selected.dataset.unit || 'unidad' : 'unidad';
                }

                function addRow(id, cantidad) {
                    var row = document.createElement('div');
                    row.className = 'recipe-row';
                    row.innerHTML =
                            '<select class="form-select" name="idInsumo" required>' + optionsHtml + '</select>' +
                            '<input class="form-control" type="number" step="0.001" min="0.001" name="cantidadRequerida" placeholder="Cantidad" required>' +
                            '<span class="recipe-unit" data-unit-label>unidad</span>' +
                            '<button type="button" class="btn btn-outline-secondary" aria-label="Quitar insumo"><i class="bi bi-x-lg"></i></button>';
                    lines.appendChild(row);

                    var select = row.querySelector('select');
                    var input = row.querySelector('input');
                    select.value = id || '';
                    input.value = cantidad || '';
                    updateUnit(row);

                    select.addEventListener('change', function () {
                        updateUnit(row);
                    });
                    row.querySelector('button').addEventListener('click', function () {
                        row.remove();
                        if (!lines.children.length) {
                            addRow('', '');
                        }
                    });
                }

                addButton.addEventListener('click', function () {
                    addRow('', '');
                });

                modal.addEventListener('show.bs.modal', function (event) {
                    var button = event.relatedTarget;
                    var mode = button ? button.dataset.mode : 'new';
                    lines.innerHTML = '';
                    product.value = button && button.dataset.producto ? button.dataset.producto : '';
                    title.textContent = mode === 'edit' ? 'Editar receta' : 'Registrar receta';

                    if (mode === 'edit' && button) {
                        var card = button.closest('.recipe-card');
                        var savedRows = card ? card.querySelectorAll('[data-recipe-data] span') : [];
                        savedRows.forEach(function (item) {
                            addRow(item.dataset.id, item.dataset.cantidad);
                        });
                    }

                    if (!lines.children.length) {
                        addRow('', '');
                    }
                });

                modal.addEventListener('hidden.bs.modal', function () {
                    var form = modal.querySelector('form');
                    form.reset();
                    form.classList.remove('was-validated');
                    lines.innerHTML = '';
                });
            })();
        </script>
    </body>
</html>
