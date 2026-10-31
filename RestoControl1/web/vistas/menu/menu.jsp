<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}" scope="session"/>
<fmt:setBundle basename="i18n.messages"/>
<fmt:message key="menu.modal.product.new" var="txtProductNew"/>
<fmt:message key="menu.modal.product.edit" var="txtProductEdit"/>
<fmt:message key="menu.modal.category.new" var="txtCategoryNew"/>
<fmt:message key="menu.modal.category.edit" var="txtCategoryEdit"/>
<fmt:message key="menu.search.products" var="txtSearchProducts"/>
<fmt:message key="menu.search.categories" var="txtSearchCategories"/>
<fmt:message key="menu.placeholder.name" var="txtPlaceholderName"/>
<fmt:message key="menu.placeholder.description" var="txtPlaceholderDescription"/>
<fmt:message key="menu.placeholder.price" var="txtPlaceholderPrice"/>
<fmt:message key="menu.placeholder.image" var="txtPlaceholderImage"/>
<c:if test="${empty sessionScope.usuarioAutenticado}">
    <c:redirect url="/LoginServlet"/>
</c:if>
<!DOCTYPE html>
<html lang="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><fmt:message key="menu.title"/> | <fmt:message key="app.name"/></title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body class="menu-page">
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="menu"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeaderKey" value="menu.title"/>
                    <jsp:param name="subtituloHeaderKey" value="menu.subtitle"/>
                </jsp:include>

                <main class="content-shell menu-shell">
                    <c:if test="${not empty requestScope.error}">
                        <div class="alert alert-danger alert-dismissible fade show auto-dismiss-alert" role="alert">
                            ${requestScope.error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'guardado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            <fmt:message key="menu.message.saved"/>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'desactivado'}">
                        <div class="alert alert-warning alert-dismissible fade show auto-dismiss-alert" role="alert">
                            <fmt:message key="menu.message.disabled"/>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'estimacion_guardada'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            <fmt:message key="menu.message.estimate_saved"/>
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>

                    <section class="menu-summary-grid">
                        <div class="menu-stat-card stat-products">
                            <i class="bi bi-bell-fill"></i>
                            <div>
                                <strong>${totalPlatosRegistrados}</strong>
                                <span><fmt:message key="menu.total.products"/></span>
                                <small><fmt:message key="menu.stat.total"/></small>
                            </div>
                        </div>
                        <div class="menu-stat-card stat-categories">
                            <i class="bi bi-grid-3x3-gap"></i>
                            <div>
                                <strong>${totalCategoriasActivas}</strong>
                                <span><fmt:message key="menu.total.categories"/></span>
                                <small><fmt:message key="menu.stat.active"/></small>
                            </div>
                        </div>
                        <div class="menu-stat-card stat-estimate">
                            <i class="bi bi-calendar2-date-fill"></i>
                            <div>
                                <strong>${productosEstimadosHoy}</strong>
                                <span><fmt:message key="menu.estimated.today"/></span>
                                <small><fmt:message key="menu.stat.production"/></small>
                            </div>
                        </div>
                        <div class="menu-stat-card stat-available">
                            <i class="bi bi-check-circle-fill"></i>
                            <div>
                                <strong>${productosDisponiblesHoy}</strong>
                                <span><fmt:message key="menu.available.today"/></span>
                                <small><fmt:message key="menu.stat.sell"/></small>
                            </div>
                        </div>
                    </section>

                    <nav class="menu-tabs menu-tabs-visual" aria-label="Menu">
                        <a class="menu-tab ${tabActivo == 'categorias' ? 'active' : ''}" href="${pageContext.request.contextPath}/MenuServlet?tab=categorias&fecha=${fechaSeleccionada}">
                            <i class="bi bi-grid-3x3-gap"></i>
                            <span><fmt:message key="menu.tab.categories"/></span>
                        </a>
                        <a class="menu-tab ${tabActivo == 'productos' ? 'active' : ''}" href="${pageContext.request.contextPath}/MenuServlet?tab=productos&fecha=${fechaSeleccionada}">
                            <i class="bi bi-fork-knife"></i>
                            <span><fmt:message key="menu.tab.products"/></span>
                        </a>
                        <a class="menu-tab ${tabActivo == 'estimacion' ? 'active' : ''}" href="${pageContext.request.contextPath}/MenuServlet?tab=estimacion&fecha=${fechaSeleccionada}">
                            <i class="bi bi-calendar2-check"></i>
                            <span><fmt:message key="menu.tab.estimate"/></span>
                        </a>
                    </nav>

                    <c:choose>
                        <c:when test="${tabActivo == 'categorias'}">
                            <section class="menu-card-surface">
                                <div class="menu-actionbar visual-actionbar">
                                    <label class="menu-search">
                                        <i class="bi bi-search"></i>
                                        <input type="search" data-menu-search data-target="categoriasGrid" placeholder="${txtSearchCategories}">
                                    </label>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            Estado <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-filter data-target="categoriasGrid" data-filter-type="countGroup" data-filter-value="" data-param="catPlatos">Todas</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="categoriasGrid" data-filter-type="countGroup" data-filter-value="con" data-param="catPlatos">Con platos</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="categoriasGrid" data-filter-type="countGroup" data-filter-value="sin" data-param="catPlatos">Sin platos</button>
                                        </div>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            Estado: Todas <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-filter data-target="categoriasGrid" data-filter-type="status" data-filter-value="" data-param="catEstado">Todas</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="categoriasGrid" data-filter-type="status" data-filter-value="activo" data-param="catEstado">Activas</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="categoriasGrid" data-filter-type="status" data-filter-value="inactivo" data-param="catEstado">Inactivas</button>
                                        </div>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <i class="bi bi-sort-alpha-down"></i> Ordenar: A-Z <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-sort data-target="categoriasGrid" data-sort="nameAsc" data-param="catOrden">Nombre A-Z</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="categoriasGrid" data-sort="nameDesc" data-param="catOrden">Nombre Z-A</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="categoriasGrid" data-sort="countAsc" data-param="catOrden">Menos platos</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="categoriasGrid" data-sort="recent" data-param="catOrden">Mas recientes</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="categoriasGrid" data-sort="countDesc" data-param="catOrden">Más platos</button>
                                        </div>
                                    </div>
                                    <button type="button" class="btn btn-resto-primary" data-bs-toggle="modal" data-bs-target="#modalCategoria" data-mode="new">
                                        <i class="bi bi-plus-lg me-1"></i> <fmt:message key="menu.new.category"/>
                                    </button>
                                </div>

                                <div class="category-card-grid" id="categoriasGrid">
                                    <c:forEach var="categoria" items="${categorias}">
                                        <article class="category-card category-dashboard-card ${categoria.iconoColorCss} ${categoria.estado == 'inactivo' ? 'is-inactive' : ''}"
                                                 data-search="${fn:escapeXml(categoria.nombreCategoria)} ${fn:escapeXml(categoria.descripcion)} ${fn:escapeXml(categoria.estado)}"
                                                 data-status="${fn:escapeXml(categoria.estado)}"
                                                 data-count-group="${categoria.cantidadProductos > 0 ? 'con' : 'sin'}"
                                                 data-name="${fn:escapeXml(categoria.nombreCategoria)}"
                                                 data-id="${categoria.idCategoria}"
                                                 data-count="${categoria.cantidadProductos}">
                                            <div class="category-hero">
                                                <span class="category-icon-xl ${categoria.iconoColorCss}">
                                                    <i class="bi ${categoria.iconoCss}"></i>
                                                </span>
                                                <div class="category-hero-text">
                                                    <h2>${categoria.nombreCategoria}</h2>
                                                    <p>${categoria.descripcion}</p>
                                                </div>
                                                <form method="post" action="${pageContext.request.contextPath}/MenuServlet" class="status-toggle-form">
                                                    <input type="hidden" name="accion" value="cambiarEstadoCategoria">
                                                    <input type="hidden" name="tab" value="categorias">
                                                    <input type="hidden" name="fecha" value="${fechaSeleccionada}">
                                                    <input type="hidden" name="idCategoria" value="${categoria.idCategoria}">
                                                    <input type="hidden" name="estado" value="${categoria.estado == 'activo' ? 'inactivo' : 'activo'}">
                                                    <button type="submit" class="category-status-badge status-toggle ${categoria.estado == 'activo' ? 'active' : 'inactive'}" title="${categoria.estado == 'activo' ? 'Desactivar categoria' : 'Activar categoria'}">
                                                        <i class="bi bi-power"></i> ${categoria.estado}
                                                    </button>
                                                </form>
                                            </div>
                                            <div class="category-metrics">
                                                <span>
                                                    <i class="bi bi-calendar2-check"></i>
                                                    <strong>${categoria.cantidadProductos}</strong>
                                                    <small>Platos asociados</small>
                                                </span>
                                                <span>
                                                    <i class="bi bi-bag-check"></i>
                                                    <strong>${categoria.cantidadDisponibles}</strong>
                                                    <small>Disponibles</small>
                                                </span>
                                                <span>
                                                    <i class="bi bi-shop-window"></i>
                                                    <strong>${categoria.cantidadInactivos}</strong>
                                                    <small>Inactivos</small>
                                                </span>
                                            </div>
                                            <div class="category-updated">
                                                <i class="bi bi-calendar3"></i> Actualizado: <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy"/>
                                            </div>
                                            <div class="category-card-actions category-dashboard-actions">
                                                <button type="button" class="btn btn-outline-resto"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#modalCategoria"
                                                        data-mode="edit"
                                                        data-id="${categoria.idCategoria}"
                                                        data-nombre="${fn:escapeXml(categoria.nombreCategoria)}"
                                                        data-descripcion="${fn:escapeXml(categoria.descripcion)}"
                                                        data-estado="${categoria.estado}">
                                                    <i class="bi bi-pencil"></i> <fmt:message key="menu.edit"/>
                                                </button>
                                                <a class="btn btn-outline-resto" href="${pageContext.request.contextPath}/MenuServlet?tab=productos&fecha=${fechaSeleccionada}&categoria=${categoria.idCategoria}">
                                                    <i class="bi bi-list-ul"></i> Ver platos
                                                </a>
                                            </div>
                                        </article>
                                    </c:forEach>
                                </div>

                            </section>
                        </c:when>

                        <c:when test="${tabActivo == 'estimacion'}">
                            <form method="post" action="${pageContext.request.contextPath}/MenuServlet" class="menu-card-surface estimate-surface">
                                <input type="hidden" name="accion" value="guardarEstimacion">
                                <input type="hidden" name="tab" value="estimacion">
                                <input type="hidden" name="fecha" value="${fechaSeleccionada}">

                                <div class="estimate-head">
                                    <div>
                                        <h2><fmt:message key="menu.estimate.title"/></h2>
                                        <p><fmt:message key="menu.estimate.subtitle"/></p>
                                    </div>
                                    <label class="menu-date-control">
                                        <i class="bi bi-calendar3"></i>
                                        <input type="date" value="${fechaSeleccionada}" data-date-redirect="${pageContext.request.contextPath}/MenuServlet?tab=estimacion&fecha=">
                                    </label>
                                </div>

                                <div class="menu-actionbar visual-actionbar estimate-actionbar">
                                    <label class="menu-search">
                                        <i class="bi bi-search"></i>
                                        <input type="search" data-menu-search data-target="estimacionGrid" placeholder="Buscar plato, categoría o estado...">
                                    </label>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <i class="bi bi-filter-left"></i> Filtros
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="" data-param="estFiltro">Todos</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="planificado" data-param="estFiltro">Planificados</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="pendiente" data-param="estFiltro">Sin planificar</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="listo" data-param="estFiltro">Listos para vender</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="no-disponible" data-param="estFiltro">No disponibles para vender</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="sin-receta" data-param="estFiltro">Sin receta registrada</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="suficiente" data-param="estFiltro">Stock suficiente</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="bajo" data-param="estFiltro">Stock bajo</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="estimateState" data-filter-value="critico" data-param="estFiltro">Stock critico</button>
                                        </div>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            Categoría <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="categoryId" data-filter-value="" data-param="estCategoria">Todas</button>
                                            <c:forEach var="categoria" items="${categoriasActivas}">
                                                <button class="dropdown-item" type="button" data-menu-filter data-target="estimacionGrid" data-filter-type="categoryId" data-filter-value="${categoria.idCategoria}" data-param="estCategoria">${categoria.nombreCategoria}</button>
                                            </c:forEach>
                                        </div>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            Ordenar <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-sort data-target="estimacionGrid" data-sort="nameAsc" data-param="estOrden">Nombre A-Z</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="estimacionGrid" data-sort="nameDesc" data-param="estOrden">Nombre Z-A</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="estimacionGrid" data-sort="plannedDesc" data-param="estOrden">Más porciones</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="estimacionGrid" data-sort="plannedAsc" data-param="estOrden">Menos porciones</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="estimacionGrid" data-sort="stockPriority" data-param="estOrden">Stock critico primero</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="estimacionGrid" data-sort="recipeMissing" data-param="estOrden">Sin receta primero</button>
                                        </div>
                                    </div>
                                </div>

                                <div class="estimate-card-grid" id="estimacionGrid">
                                    <c:forEach var="estimacion" items="${estimaciones}">
                                        <article class="estimate-card ${estimacion.iconoColorCss}"
                                                 data-estimate-card
                                                 data-search="${fn:escapeXml(estimacion.nombreProducto)} ${fn:escapeXml(estimacion.nombreCategoria)} ${fn:escapeXml(estimacion.stockEstado)} ${fn:escapeXml(estimacion.estadoProducto)}"
                                                 data-name="${fn:escapeXml(estimacion.nombreProducto)}"
                                                 data-category-id="${estimacion.idCategoria}"
                                                 data-availability="${fn:escapeXml(estimacion.disponibilidadProducto)}"
                                                 data-product-status="${fn:escapeXml(estimacion.estadoProducto)}"
                                                 data-stock="${estimacion.stockEstado}"
                                                 data-has-recipe="${empty estimacion.insumos ? 'no' : 'si'}"
                                                 data-planned="${estimacion.cantidadPlanificada > 0 ? 'planificado' : 'pendiente'}"
                                                 data-planned-count="${estimacion.cantidadPlanificada}"
                                                 data-available-count="${estimacion.cantidadDisponible}">
                                            <span class="card-corner"><i class="bi ${estimacion.iconoCss}"></i></span>
                                            <div class="estimate-product-head">
                                                <span class="estimate-thumb ${estimacion.iconoColorCss}">
                                                    <c:choose>
                                                        <c:when test="${estimacion.tieneImagen && estimacion.imagenExterna}">
                                                            <img src="${estimacion.imagen}" alt="${estimacion.nombreProducto}">
                                                        </c:when>
                                                        <c:when test="${estimacion.tieneImagen}">
                                                            <img src="${pageContext.request.contextPath}/${estimacion.imagenLocal}" alt="${estimacion.nombreProducto}">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <i class="bi ${estimacion.iconoCss}"></i>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <div>
                                                    <h3>${estimacion.nombreProducto}</h3>
                                                    <span class="category-chip ${estimacion.iconoColorCss}">${estimacion.nombreCategoria}</span>
                                                </div>
                                            </div>

                                            <div class="stock-box ${estimacion.stockEstado == 'critico' ? 'danger' : estimacion.stockEstado == 'bajo' ? 'warning' : 'success'}" data-stock-box>
                                                <i class="bi ${estimacion.stockEstado == 'critico' ? 'bi-exclamation-circle' : estimacion.stockEstado == 'bajo' ? 'bi-exclamation-triangle' : 'bi-box-seam'}" data-stock-icon></i>
                                                <div>
                                                    <strong data-stock-title>${estimacion.stockTitulo}</strong>
                                                    <span data-stock-desc>${estimacion.stockDescripcion}</span>
                                                    <button type="button" class="stock-detail-link"
                                                            data-bs-toggle="modal"
                                                            data-bs-target="#modalDetalleInsumos"
                                                            data-product="${fn:escapeXml(estimacion.nombreProducto)}"
                                                            data-category="${fn:escapeXml(estimacion.nombreCategoria)}">
                                                        <i class="bi bi-clipboard-data"></i> Ver detalle de insumos
                                                    </button>
                                                </div>
                                            </div>

                                            <script type="application/json" data-recipe-json>
                                                [
                                                <c:forEach var="insumo" items="${estimacion.insumos}" varStatus="estadoInsumo">
                                                    {
                                                        "nombre": "${fn:escapeXml(insumo.nombreInsumo)}",
                                                        "unidad": "${fn:escapeXml(insumo.unidadMedida)}",
                                                        "porPorcion": ${insumo.cantidadPorPorcion},
                                                        "requerido": ${insumo.cantidadRequerida},
                                                        "stock": ${insumo.stockActual},
                                                        "minimo": ${insumo.stockMinimo},
                                                        "diferencia": ${insumo.diferencia},
                                                        "estado": "${insumo.estado}"
                                                    }${estadoInsumo.last ? '' : ','}
                                                </c:forEach>
                                                ]
                                            </script>

                                            <input type="hidden" name="idProducto" value="${estimacion.idProducto}">
                                            <label class="estimate-label"><fmt:message key="menu.quantity.prepare"/></label>
                                            <div class="qty-stepper">
                                                <button type="button" data-step="-1" data-input="cantidad_${estimacion.idProducto}">-</button>
                                                <input type="number" min="0" name="cantidad_${estimacion.idProducto}" value="${estimacion.cantidadPlanificada}" data-qty-input>
                                                <button type="button" data-step="1" data-input="cantidad_${estimacion.idProducto}">+</button>
                                            </div>

                                            <span class="availability-pill ${estimacion.disponibleParaVenta ? 'success' : 'danger'}" data-availability-pill>
                                                ${estimacion.disponibleParaVenta ? 'Disponible para vender' : 'No disponible para vender'}
                                            </span>

                                            <div class="estimate-actions">
                                                <button type="button" class="btn btn-outline-resto" data-clear-estimate data-input="cantidad_${estimacion.idProducto}"><i class="bi bi-trash3"></i> Limpiar</button>
                                            </div>
                                        </article>
                                    </c:forEach>
                                </div>

                                <footer class="estimate-footer">
                                    <div class="estimate-summary">
                                        <span><i class="bi bi-bar-chart"></i></span>
                                        <div>
                                            <strong><fmt:message key="menu.daily.summary"/></strong>
                                            <p>${porcionesPlanificadas} <fmt:message key="menu.portions.planned"/> · ${productosEstimadosHoy} <fmt:message key="menu.dishes"/></p>
                                        </div>
                                    </div>
                                    <button type="submit" class="btn btn-gradient-save">
                                        <i class="bi bi-check2-circle"></i> <fmt:message key="menu.save.estimate"/>
                                    </button>
                                </footer>
                            </form>
                        </c:when>

                        <c:otherwise>
                            <section class="menu-card-surface">
                                <c:if test="${categoriaFiltroId > 0}">
                                    <div class="active-filter-chip">
                                        <i class="bi bi-funnel"></i>
                                        Mostrando: ${categoriaFiltroNombre}
                                        <a href="${pageContext.request.contextPath}/MenuServlet?tab=productos&fecha=${fechaSeleccionada}">Quitar filtro</a>
                                    </div>
                                </c:if>
                                <div class="menu-actionbar visual-actionbar">
                                    <label class="menu-search">
                                        <i class="bi bi-search"></i>
                                        <input type="search" data-menu-search data-target="productosGrid" placeholder="${txtSearchProducts}">
                                    </label>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <i class="bi bi-filter-left"></i> <fmt:message key="menu.filters"/>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-filter data-target="productosGrid" data-filter-type="effectiveStatus" data-filter-value="" data-param="prodEstado">Todos</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="productosGrid" data-filter-type="effectiveStatus" data-filter-value="activo" data-param="prodEstado">Activos</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="productosGrid" data-filter-type="effectiveStatus" data-filter-value="inactivo" data-param="prodEstado">Inactivos</button>
                                            <button class="dropdown-item" type="button" data-menu-filter data-target="productosGrid" data-filter-type="effectiveStatus" data-filter-value="bloqueado" data-param="prodEstado">Bloqueados</button>
                                        </div>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            Categoria <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-filter data-target="productosGrid" data-filter-type="categoryId" data-filter-value="" data-param="prodCategoria">Todas</button>
                                            <c:forEach var="categoria" items="${categorias}">
                                                <button class="dropdown-item" type="button" data-menu-filter data-target="productosGrid" data-filter-type="categoryId" data-filter-value="${categoria.idCategoria}" data-param="prodCategoria">${categoria.nombreCategoria}</button>
                                            </c:forEach>
                                        </div>
                                    </div>
                                    <div class="dropdown">
                                        <button class="btn btn-filter" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <fmt:message key="menu.sort.recent"/> <i class="bi bi-chevron-down ms-2"></i>
                                        </button>
                                        <div class="dropdown-menu filter-menu">
                                            <button class="dropdown-item active" type="button" data-menu-sort data-target="productosGrid" data-sort="recent" data-param="prodOrden">Más recientes</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="productosGrid" data-sort="nameAsc" data-param="prodOrden">Nombre A-Z</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="productosGrid" data-sort="nameDesc" data-param="prodOrden">Nombre Z-A</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="productosGrid" data-sort="priceAsc" data-param="prodOrden">Precio menor</button>
                                            <button class="dropdown-item" type="button" data-menu-sort data-target="productosGrid" data-sort="priceDesc" data-param="prodOrden">Precio mayor</button>
                                        </div>
                                    </div>
                                    <button type="button" class="btn btn-resto-primary" data-bs-toggle="modal" data-bs-target="#modalProducto" data-mode="new">
                                        <i class="bi bi-plus-lg me-1"></i> <fmt:message key="menu.new.product"/>
                                    </button>
                                </div>

                                <div class="product-card-grid" id="productosGrid">
                                    <c:forEach var="producto" items="${productos}">
                                        <article class="product-card ${producto.iconoColorCss} ${producto.estado == 'inactivo' || producto.estadoCategoria == 'inactivo' ? 'is-inactive is-category-blocked' : ''}"
                                                 data-search="${fn:escapeXml(producto.nombreProducto)} ${fn:escapeXml(producto.nombreCategoria)} ${fn:escapeXml(producto.disponibilidad)} ${fn:escapeXml(producto.estado)}"
                                                 data-availability="${fn:escapeXml(producto.disponibilidad)}"
                                                 data-status="${fn:escapeXml(producto.estado)}"
                                                 data-effective-status="${producto.estadoCategoria == 'inactivo' ? 'bloqueado' : producto.estado}"
                                                 data-category-id="${producto.idCategoria}"
                                                 data-name="${fn:escapeXml(producto.nombreProducto)}"
                                                 data-id="${producto.idProducto}"
                                                 data-price="${producto.precio}">
                                            <span class="card-corner"><i class="bi ${producto.iconoCss}"></i></span>
                                            <div class="product-card-main">
                                                <span class="product-photo ${producto.tieneImagen ? '' : producto.iconoColorCss}">
                                                    <c:choose>
                                                        <c:when test="${producto.tieneImagen && producto.imagenExterna}">
                                                            <img src="${producto.imagen}" alt="${producto.nombreProducto}">
                                                        </c:when>
                                                        <c:when test="${producto.tieneImagen}">
                                                            <img src="${pageContext.request.contextPath}/${producto.imagenLocal}" alt="${producto.nombreProducto}">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <i class="bi ${producto.iconoCss}"></i>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <div>
                                                    <h2>${producto.nombreProducto}</h2>
                                                    <p>${producto.descripcion}</p>
                                                    <div class="product-card-line">
                                                        <span class="category-chip ${producto.iconoColorCss}">${producto.nombreCategoria}</span>
                                                        <strong>S/ <fmt:formatNumber value="${producto.precio}" minFractionDigits="2" maxFractionDigits="2"/></strong>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="product-card-footer">
                                                <form method="post" action="${pageContext.request.contextPath}/MenuServlet" class="status-toggle-form">
                                                    <input type="hidden" name="accion" value="cambiarEstadoProducto">
                                                    <input type="hidden" name="tab" value="productos">
                                                    <input type="hidden" name="fecha" value="${fechaSeleccionada}">
                                                    <input type="hidden" name="idProducto" value="${producto.idProducto}">
                                                    <input type="hidden" name="estado" value="${producto.estado == 'activo' ? 'inactivo' : 'activo'}">
                                                    <button type="submit"
                                                            class="soft-pill status-toggle ${producto.estadoCategoria == 'inactivo' ? 'danger' : producto.estado == 'activo' ? 'success' : 'danger'}"
                                                            ${producto.estadoCategoria == 'inactivo' ? 'disabled' : ''}
                                                            title="${producto.estadoCategoria == 'inactivo' ? 'Primero activa la categoria' : producto.estado == 'activo' ? 'Desactivar plato' : 'Activar plato'}">
                                                        <i class="bi ${producto.estadoCategoria == 'inactivo' ? 'bi-lock-fill' : 'bi-power'}"></i>
                                                        ${producto.estadoCategoria == 'inactivo' ? 'Bloqueado por categoria inactiva' : producto.estado}
                                                    </button>
                                                </form>
                                                <button type="button" class="btn btn-outline-resto"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#modalProducto"
                                                        data-mode="edit"
                                                        data-id="${producto.idProducto}"
                                                        data-nombre="${fn:escapeXml(producto.nombreProducto)}"
                                                        data-descripcion="${fn:escapeXml(producto.descripcion)}"
                                                        data-precio="${producto.precio}"
                                                        data-disponibilidad="${producto.disponibilidad}"
                                                        data-estado="${producto.estado}"
                                                        data-categoria="${producto.idCategoria}"
                                                        data-categoria-nombre="${fn:escapeXml(producto.nombreCategoria)}"
                                                        data-categoria-estado="${fn:escapeXml(producto.estadoCategoria)}"
                                                        data-imagen="${fn:escapeXml(producto.imagen)}">
                                                    <i class="bi bi-pencil"></i> <fmt:message key="menu.edit"/>
                                                </button>
                                            </div>
                                        </article>
                                    </c:forEach>
                                </div>

                            </section>
                        </c:otherwise>
                    </c:choose>
                </main>
            </div>
        </div>

        <div class="modal fade" id="modalProducto" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <form class="modal-content resto-modal modal-product" method="post" action="${pageContext.request.contextPath}/MenuServlet">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span class="resto-modal-icon"><i class="bi bi-cup-hot"></i></span>
                            <div>
                                <small><fmt:message key="menu.tab.products"/></small>
                                <h2 class="modal-title h5" data-modal-title data-new-title="${txtProductNew}" data-edit-title="${txtProductEdit}">${txtProductNew}</h2>
                            </div>
                        </div>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <input type="hidden" name="accion" value="guardarProducto">
                        <input type="hidden" name="tab" value="productos">
                        <input type="hidden" name="fecha" value="${fechaSeleccionada}">
                        <input type="hidden" name="idProducto">
                        <input type="hidden" name="disponibilidad" value="disponible">
                        <div class="alert alert-warning d-none" data-product-category-warning></div>

                        <div class="mb-3">
                            <label class="form-label"><fmt:message key="menu.name"/></label>
                            <input class="form-control" name="nombreProducto" required maxlength="150" placeholder="${txtPlaceholderName}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label"><fmt:message key="menu.description"/></label>
                            <textarea class="form-control" name="descripcion" rows="2" maxlength="255" placeholder="${txtPlaceholderDescription}"></textarea>
                        </div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label"><fmt:message key="menu.price"/></label>
                                <input class="form-control" name="precio" type="number" min="0" step="0.01" required placeholder="${txtPlaceholderPrice}">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><fmt:message key="menu.category"/></label>
                                <select class="form-select" name="idCategoria" required>
                                    <option value="" selected disabled hidden>Selecciona una categoría...</option>
                                    <c:forEach var="categoria" items="${categoriasActivas}">
                                        <option value="${categoria.idCategoria}">${categoria.nombreCategoria}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label"><fmt:message key="menu.status"/></label>
                                <select class="form-select" name="estado">
                                    <option value="activo">activo</option>
                                    <option value="inactivo">inactivo</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="form-label"><fmt:message key="menu.image"/></label>
                                <input class="form-control" name="imagen" maxlength="255" placeholder="${txtPlaceholderImage}">
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><fmt:message key="menu.cancel"/></button>
                        <button type="submit" class="btn btn-resto-dark"><fmt:message key="menu.save"/></button>
                    </div>
                </form>
            </div>
        </div>

        <div class="modal fade" id="modalCategoria" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <form class="modal-content resto-modal modal-category" method="post" action="${pageContext.request.contextPath}/MenuServlet">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span class="resto-modal-icon"><i class="bi bi-menu-button-wide"></i></span>
                            <div>
                                <small><fmt:message key="menu.tab.categories"/></small>
                                <h2 class="modal-title h5" data-modal-title data-new-title="${txtCategoryNew}" data-edit-title="${txtCategoryEdit}">${txtCategoryNew}</h2>
                            </div>
                        </div>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <input type="hidden" name="accion" value="guardarCategoria">
                        <input type="hidden" name="tab" value="categorias">
                        <input type="hidden" name="fecha" value="${fechaSeleccionada}">
                        <input type="hidden" name="idCategoria">

                        <div class="mb-3">
                            <label class="form-label"><fmt:message key="menu.name"/></label>
                            <input class="form-control" name="nombreCategoria" required maxlength="100" placeholder="${txtPlaceholderName}">
                        </div>
                        <div class="mb-3">
                            <label class="form-label"><fmt:message key="menu.description"/></label>
                            <textarea class="form-control" name="descripcion" rows="3" maxlength="255" placeholder="${txtPlaceholderDescription}"></textarea>
                        </div>
                        <div>
                            <label class="form-label"><fmt:message key="menu.status"/></label>
                            <select class="form-select" name="estado">
                                <option value="activo">activo</option>
                                <option value="inactivo">inactivo</option>
                            </select>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><fmt:message key="menu.cancel"/></button>
                        <button type="submit" class="btn btn-resto-dark"><fmt:message key="menu.save"/></button>
                    </div>
                </form>
            </div>
        </div>

        <div class="modal fade" id="modalDetalleInsumos" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content resto-modal modal-ingredients">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span class="resto-modal-icon"><i class="bi bi-clipboard-data"></i></span>
                            <div>
                                <small data-ingredient-category>Estimacion diaria</small>
                                <h2 class="modal-title h5" data-ingredient-title>Detalle de insumos</h2>
                            </div>
                        </div>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <div class="ingredient-summary" data-ingredient-summary>
                            Revisa los insumos necesarios para la cantidad indicada.
                        </div>
                        <div class="ingredient-table-wrap">
                            <table class="ingredient-table">
                                <thead>
                                    <tr>
                                        <th>Insumo</th>
                                        <th>Requerido</th>
                                        <th>Stock actual</th>
                                        <th>Diferencia</th>
                                        <th>Estado</th>
                                    </tr>
                                </thead>
                                <tbody data-ingredient-body>
                                    <tr>
                                        <td colspan="5">Selecciona un plato para ver el detalle.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-resto-dark" data-bs-dismiss="modal">Entendido</button>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            document.querySelectorAll('.auto-dismiss-alert').forEach(function (alertNode) {
                window.setTimeout(function () {
                    if (!document.body.contains(alertNode)) {
                        return;
                    }
                    alertNode.classList.add('alert-hiding');
                    window.setTimeout(function () {
                        var alertInstance = bootstrap.Alert.getOrCreateInstance(alertNode);
                        alertInstance.close();
                    }, 450);
                }, 4000);
            });

            var MENU_FILTER_TYPES = [
                'status',
                'availability',
                'categoryId',
                'countGroup',
                'planned',
                'productStatus',
                'stock',
                'effectiveStatus',
                'estimateState'
            ];

            var MENU_PARAM_CONFIG = {
                categoriasGrid: {
                    search: 'catBuscar',
                    sort: 'catOrden',
                    filters: {
                        countGroup: 'catPlatos',
                        status: 'catEstado'
                    },
                    defaultSort: 'nameAsc'
                },
                productosGrid: {
                    search: 'prodBuscar',
                    sort: 'prodOrden',
                    filters: {
                        effectiveStatus: 'prodEstado',
                        categoryId: 'prodCategoria'
                    },
                    defaultSort: 'recent'
                },
                estimacionGrid: {
                    search: 'estBuscar',
                    sort: 'estOrden',
                    filters: {
                        estimateState: 'estFiltro',
                        categoryId: 'estCategoria'
                    },
                    defaultSort: 'nameAsc'
                }
            };

            function getConfig(containerId) {
                return MENU_PARAM_CONFIG[containerId] || {filters: {}, defaultSort: 'nameAsc'};
            }

            function updateUrlParam(param, value) {
                if (!param) {
                    return;
                }
                var url = new URL(window.location.href);
                if (value) {
                    url.searchParams.set(param, value);
                } else {
                    url.searchParams.delete(param);
                }
                window.history.replaceState({}, '', url.pathname + '?' + url.searchParams.toString());
            }

            function filterParam(button) {
                var config = getConfig(button.dataset.target);
                return button.dataset.param || config.filters[button.dataset.filterType || ''];
            }

            function sortParam(button) {
                return button.dataset.param || getConfig(button.dataset.target).sort;
            }

            function searchParam(input) {
                return getConfig(input.dataset.target).search;
            }

            function isEstimateReady(item) {
                return item.dataset.planned === 'planificado'
                        && item.dataset.hasRecipe === 'si'
                        && item.dataset.stock !== 'critico'
                        && item.dataset.productStatus !== 'inactivo';
            }

            function itemMatchesFilter(item, type, expected) {
                if (!expected) {
                    return true;
                }

                if (type === 'effectiveStatus') {
                    return (item.dataset.effectiveStatus || '').toLowerCase() === expected;
                }

                if (type === 'estimateState') {
                    var planned = item.dataset.planned === 'planificado';
                    var hasRecipe = item.dataset.hasRecipe === 'si';
                    var stock = item.dataset.stock || '';

                    if (expected === 'planificado') {
                        return planned;
                    }
                    if (expected === 'pendiente') {
                        return !planned;
                    }
                    if (expected === 'listo') {
                        return isEstimateReady(item);
                    }
                    if (expected === 'no-disponible') {
                        return planned && (!hasRecipe || stock === 'critico' || item.dataset.productStatus === 'inactivo');
                    }
                    if (expected === 'sin-receta') {
                        return !hasRecipe;
                    }
                    if (expected === 'suficiente' || expected === 'bajo' || expected === 'critico') {
                        return stock === expected;
                    }
                }

                return (item.dataset[type] || '').toLowerCase() === expected;
            }

            function applyMenuFilters(containerId) {
                var container = document.getElementById(containerId);
                if (!container) {
                    return;
                }

                var search = document.querySelector('[data-menu-search][data-target="' + containerId + '"]');
                var term = search ? search.value.trim().toLowerCase() : '';

                container.querySelectorAll('[data-search]').forEach(function (item) {
                    var textMatch = !term || item.dataset.search.toLowerCase().includes(term);
                    var filterMatch = true;

                    MENU_FILTER_TYPES.forEach(function (type) {
                        var expected = (container.dataset[type + 'Filter'] || '').toLowerCase();
                        if (!itemMatchesFilter(item, type, expected)) {
                            filterMatch = false;
                        }
                    });

                    item.hidden = !(textMatch && filterMatch);
                });
            }

            function sortMenuCards(containerId, mode) {
                var container = document.getElementById(containerId);
                if (!container) {
                    return;
                }

                var items = Array.prototype.slice.call(container.querySelectorAll('[data-search]'));
                items.sort(function (a, b) {
                    if (mode === 'nameDesc') {
                        return (b.dataset.name || '').localeCompare(a.dataset.name || '');
                    }
                    if (mode === 'countAsc') {
                        return Number(a.dataset.count || 0) - Number(b.dataset.count || 0);
                    }
                    if (mode === 'countDesc') {
                        return Number(b.dataset.count || 0) - Number(a.dataset.count || 0);
                    }
                    if (mode === 'priceAsc') {
                        return Number(a.dataset.price || 0) - Number(b.dataset.price || 0);
                    }
                    if (mode === 'priceDesc') {
                        return Number(b.dataset.price || 0) - Number(a.dataset.price || 0);
                    }
                    if (mode === 'plannedDesc') {
                        return Number(b.dataset.plannedCount || 0) - Number(a.dataset.plannedCount || 0);
                    }
                    if (mode === 'plannedAsc') {
                        return Number(a.dataset.plannedCount || 0) - Number(b.dataset.plannedCount || 0);
                    }
                    if (mode === 'availableDesc') {
                        return Number(b.dataset.availableCount || 0) - Number(a.dataset.availableCount || 0);
                    }
                    if (mode === 'stockPriority') {
                        var priority = {critico: 0, bajo: 1, suficiente: 2};
                        return (priority[a.dataset.stock] ?? 3) - (priority[b.dataset.stock] ?? 3);
                    }
                    if (mode === 'recipeMissing') {
                        var recipePriority = {'no': 0, 'si': 1};
                        return (recipePriority[a.dataset.hasRecipe] ?? 2) - (recipePriority[b.dataset.hasRecipe] ?? 2);
                    }
                    if (mode === 'recent') {
                        return Number(b.dataset.id || 0) - Number(a.dataset.id || 0);
                    }
                    return (a.dataset.name || '').localeCompare(b.dataset.name || '');
                });

                items.forEach(function (item) {
                    container.appendChild(item);
                });
            }

            document.querySelectorAll('[data-menu-search]').forEach(function (input) {
                input.addEventListener('input', function () {
                    updateUrlParam(searchParam(input), input.value.trim());
                    applyMenuFilters(input.dataset.target);
                });
            });

            document.querySelectorAll('[data-menu-filter]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var container = document.getElementById(button.dataset.target);
                    if (!container) {
                        return;
                    }

                    var type = button.dataset.filterType || '';
                    if (type === 'all') {
                        MENU_FILTER_TYPES.forEach(function (filterType) {
                            delete container.dataset[filterType + 'Filter'];
                        });
                    } else if (type) {
                        container.dataset[type + 'Filter'] = button.dataset.filterValue || '';
                        updateUrlParam(filterParam(button), button.dataset.filterValue || '');
                    }

                    button.closest('.dropdown-menu').querySelectorAll('.dropdown-item').forEach(function (item) {
                        item.classList.toggle('active', item === button);
                    });

                    applyMenuFilters(button.dataset.target);
                });
            });

            document.querySelectorAll('[data-menu-sort]').forEach(function (button) {
                button.addEventListener('click', function () {
                    button.closest('.dropdown-menu').querySelectorAll('.dropdown-item').forEach(function (item) {
                        item.classList.toggle('active', item === button);
                    });
                    sortMenuCards(button.dataset.target, button.dataset.sort || 'nameAsc');
                    updateUrlParam(sortParam(button), button.dataset.sort || 'nameAsc');
                    applyMenuFilters(button.dataset.target);
                });
            });

            var initialCategoryId = '${categoriaFiltroId}';
            if (initialCategoryId && initialCategoryId !== '0') {
                var productosGrid = document.getElementById('productosGrid');
                if (productosGrid) {
                    productosGrid.dataset.categoryIdFilter = initialCategoryId;
                    applyMenuFilters('productosGrid');
                }
            }

            document.querySelectorAll('[data-date-redirect]').forEach(function (input) {
                input.addEventListener('change', function () {
                    var url = new URL(window.location.href);
                    url.searchParams.set('tab', 'estimacion');
                    url.searchParams.set('fecha', input.value);
                    window.location.href = url.pathname + '?' + url.searchParams.toString();
                });
            });

            function applyInitialMenuState() {
                var params = new URLSearchParams(window.location.search);

                document.querySelectorAll('[data-menu-search]').forEach(function (input) {
                    var param = searchParam(input);
                    if (param && params.has(param)) {
                        input.value = params.get(param);
                    }
                });

                document.querySelectorAll('[data-menu-filter]').forEach(function (button) {
                    var param = filterParam(button);
                    var value = param && params.has(param) ? params.get(param) : '';
                    if (!value
                            && button.dataset.target === 'productosGrid'
                            && button.dataset.filterType === 'categoryId'
                            && initialCategoryId
                            && initialCategoryId !== '0') {
                        value = initialCategoryId;
                    }
                    if ((button.dataset.filterValue || '') === value) {
                        var container = document.getElementById(button.dataset.target);
                        if (container && button.dataset.filterType) {
                            container.dataset[button.dataset.filterType + 'Filter'] = value;
                        }
                        button.closest('.dropdown-menu').querySelectorAll('.dropdown-item').forEach(function (item) {
                            item.classList.toggle('active', item === button);
                        });
                    }
                });

                document.querySelectorAll('[data-menu-sort]').forEach(function (button) {
                    var param = sortParam(button);
                    var config = getConfig(button.dataset.target);
                    var value = param && params.has(param) ? params.get(param) : config.defaultSort;
                    if ((button.dataset.sort || 'nameAsc') === value) {
                        button.closest('.dropdown-menu').querySelectorAll('.dropdown-item').forEach(function (item) {
                            item.classList.toggle('active', item === button);
                        });
                        sortMenuCards(button.dataset.target, value);
                    }
                });

                Object.keys(MENU_PARAM_CONFIG).forEach(applyMenuFilters);
            }

            applyInitialMenuState();

            function readRecipe(card) {
                var node = card.querySelector('[data-recipe-json]');
                if (!node) {
                    return [];
                }
                try {
                    return JSON.parse(node.textContent || '[]');
                } catch (error) {
                    return [];
                }
            }

            function formatAmount(value, unit) {
                var number = Number(value || 0);
                return number.toLocaleString('es-PE', {
                    minimumFractionDigits: number % 1 === 0 ? 0 : 2,
                    maximumFractionDigits: 2
                }) + (unit ? ' ' + unit : '');
            }

            function calculateRecipe(recipe, quantity) {
                var lowCount = 0;
                var criticalCount = 0;
                var rows = recipe.map(function (item) {
                    var required = Number(item.porPorcion || 0) * quantity;
                    var stock = Number(item.stock || 0);
                    var minimum = Number(item.minimo || 0);
                    var diff = stock - required;
                    var status = 'suficiente';

                    if (diff < 0) {
                        status = 'critico';
                        criticalCount++;
                    } else if (diff <= minimum) {
                        status = 'bajo';
                        lowCount++;
                    }

                    return {
                        nombre: item.nombre || 'Insumo',
                        unidad: item.unidad || '',
                        requerido: required,
                        stock: stock,
                        diferencia: diff,
                        estado: status
                    };
                });

                var stock = 'suficiente';
                if (!recipe.length) {
                    stock = 'critico';
                } else if (criticalCount > 0) {
                    stock = 'critico';
                } else if (lowCount > 0) {
                    stock = 'bajo';
                }

                return {
                    stock: stock,
                    rows: rows,
                    lowCount: lowCount,
                    criticalCount: criticalCount
                };
            }

            function stockCopy(result) {
                if (result.stock === 'critico') {
                    return result.rows.length
                            ? {title: 'Stock critico', desc: result.criticalCount + ' insumo(s) no alcanzan', icon: 'bi-exclamation-circle', css: 'danger'}
                            : {title: 'Sin receta registrada', desc: 'Registra insumos para validar stock', icon: 'bi-exclamation-circle', css: 'danger'};
                }
                if (result.stock === 'bajo') {
                    return {title: 'Stock bajo', desc: result.lowCount + ' insumo(s) con stock bajo', icon: 'bi-exclamation-triangle', css: 'warning'};
                }
                return {title: 'Stock suficiente', desc: 'Todos los insumos disponibles', icon: 'bi-box-seam', css: 'success'};
            }

            function updateEstimateCard(card) {
                var input = card.querySelector('[data-qty-input]');
                var quantity = Math.max(0, Number(input ? input.value : 0));
                if (input) {
                    input.value = quantity;
                }

                var result = calculateRecipe(readRecipe(card), quantity);
                var copy = stockCopy(result);
                var stockBox = card.querySelector('[data-stock-box]');
                var stockIcon = card.querySelector('[data-stock-icon]');
                var stockTitle = card.querySelector('[data-stock-title]');
                var stockDesc = card.querySelector('[data-stock-desc]');
                var pill = card.querySelector('[data-availability-pill]');

                card.dataset.stock = result.stock;
                card.dataset.planned = quantity > 0 ? 'planificado' : 'pendiente';
                card.dataset.plannedCount = String(quantity);
                card.dataset.availableCount = result.stock === 'critico' ? '0' : String(quantity);

                if (stockBox) {
                    stockBox.classList.remove('success', 'warning', 'danger');
                    stockBox.classList.add(copy.css);
                }
                if (stockIcon) {
                    stockIcon.className = 'bi ' + copy.icon;
                }
                if (stockTitle) {
                    stockTitle.textContent = copy.title;
                }
                if (stockDesc) {
                    stockDesc.textContent = copy.desc;
                }
                if (pill) {
                    pill.classList.toggle('success', result.stock !== 'critico');
                    pill.classList.toggle('danger', result.stock === 'critico');
                    pill.textContent = result.stock === 'critico' ? 'No disponible para vender' : 'Disponible para vender';
                }

                updateEstimateSummary();
                applyMenuFilters('estimacionGrid');
            }

            function updateEstimateSummary() {
                var total = 0;
                var products = 0;
                document.querySelectorAll('[data-estimate-card]').forEach(function (card) {
                    var quantity = Number(card.dataset.plannedCount || 0);
                    if (quantity > 0) {
                        total += quantity;
                        products++;
                    }
                });

                var footerText = document.querySelector('.estimate-summary p');
                if (footerText) {
                    footerText.textContent = total + ' porciones planificadas · ' + products + ' platos';
                }
            }

            document.querySelectorAll('.qty-stepper button[data-step]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var input = document.querySelector('[name="' + button.dataset.input + '"]');
                    if (!input) {
                        return;
                    }
                    var step = Number(button.dataset.step);
                    var current = Number(input.value || 0);
                    input.value = Math.max(0, current + step);
                    var card = input.closest('[data-estimate-card]');
                    if (card) {
                        updateEstimateCard(card);
                    }
                });
            });

            document.querySelectorAll('[data-qty-input]').forEach(function (input) {
                input.addEventListener('input', function () {
                    var card = input.closest('[data-estimate-card]');
                    if (card) {
                        updateEstimateCard(card);
                    }
                });
            });

            document.querySelectorAll('[data-clear-estimate]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var input = document.querySelector('[name="' + button.dataset.input + '"]');
                    if (!input) {
                        return;
                    }
                    input.value = 0;
                    var card = input.closest('[data-estimate-card]');
                    if (card) {
                        updateEstimateCard(card);
                    }
                });
            });

            document.querySelectorAll('[data-estimate-card]').forEach(function (card) {
                updateEstimateCard(card);
            });

            var ingredientModal = document.getElementById('modalDetalleInsumos');
            if (ingredientModal) {
                ingredientModal.addEventListener('show.bs.modal', function (event) {
                    var trigger = event.relatedTarget;
                    var card = trigger ? trigger.closest('[data-estimate-card]') : null;
                    if (!card) {
                        return;
                    }

                    var quantity = Number((card.querySelector('[data-qty-input]') || {}).value || 0);
                    var result = calculateRecipe(readRecipe(card), Math.max(0, quantity));
                    var title = ingredientModal.querySelector('[data-ingredient-title]');
                    var category = ingredientModal.querySelector('[data-ingredient-category]');
                    var summary = ingredientModal.querySelector('[data-ingredient-summary]');
                    var body = ingredientModal.querySelector('[data-ingredient-body]');

                    if (title) {
                        title.textContent = trigger.dataset.product || 'Detalle de insumos';
                    }
                    if (category) {
                        category.textContent = trigger.dataset.category || 'Estimacion diaria';
                    }
                    if (summary) {
                        summary.textContent = 'Cantidad a preparar: ' + quantity + '.';
                    }
                    if (body) {
                        body.innerHTML = '';
                        if (!result.rows.length) {
                            body.innerHTML = '<tr><td colspan="5">Este plato aun no tiene receta registrada.</td></tr>';
                            return;
                        }
                        result.rows.forEach(function (row) {
                            var tr = document.createElement('tr');
                            tr.innerHTML =
                                    '<td>' + row.nombre + '</td>' +
                                    '<td>' + formatAmount(row.requerido, row.unidad) + '</td>' +
                                    '<td>' + formatAmount(row.stock, row.unidad) + '</td>' +
                                    '<td>' + formatAmount(row.diferencia, row.unidad) + '</td>' +
                                    '<td><span class="ingredient-status ' + row.estado + '">' + row.estado + '</span></td>';
                            body.appendChild(tr);
                        });
                    }
                });
            }

            function setField(form, name, value) {
                var field = form.querySelector('[name="' + name + '"]');
                if (field) {
                    field.value = value || '';
                }
            }

            function setCategoryField(form, data) {
                var select = form.querySelector('[name="idCategoria"]');
                if (!select) {
                    return;
                }

                select.querySelectorAll('[data-inactive-category]').forEach(function (option) {
                    option.remove();
                });

                setField(form, 'idCategoria', data.categoria);

                if (data.categoria && select.value !== data.categoria) {
                    var option = document.createElement('option');
                    option.value = '';
                    option.textContent = 'Categoría inactiva: ' + (data.categoriaNombre || 'selecciona otra');
                    option.disabled = true;
                    option.selected = true;
                    option.dataset.inactiveCategory = 'true';
                    select.prepend(option);
                    select.setCustomValidity('Selecciona una categoría activa.');
                } else {
                    select.setCustomValidity('');
                }

                select.addEventListener('change', function () {
                    select.setCustomValidity('');
                    select.querySelectorAll('[data-inactive-category]').forEach(function (option) {
                        option.remove();
                    });
                }, {once: true});
            }

            function configureModal(modalId, fillCallback) {
                var modal = document.getElementById(modalId);
                if (!modal) {
                    return;
                }
                modal.addEventListener('show.bs.modal', function (event) {
                    var button = event.relatedTarget;
                    var form = modal.querySelector('form');
                    var title = modal.querySelector('[data-modal-title]');
                    var editMode = button && button.dataset.mode === 'edit';
                    form.reset();
                    title.textContent = editMode ? title.dataset.editTitle : title.dataset.newTitle;
                    fillCallback(form, editMode ? button.dataset : {});
                });
            }

            function setProductCategoryField(form, data) {
                var select = form.querySelector('[name="idCategoria"]');
                if (!select) {
                    return;
                }

                var statusSelect = form.querySelector('[name="estado"]');
                var warning = form.querySelector('[data-product-category-warning]');
                var activeStatusOption = statusSelect ? statusSelect.querySelector('option[value="activo"]') : null;

                select.querySelectorAll('[data-inactive-category]').forEach(function (option) {
                    option.remove();
                });
                if (warning) {
                    warning.classList.add('d-none');
                    warning.textContent = '';
                }
                if (activeStatusOption) {
                    activeStatusOption.disabled = false;
                }

                setField(form, 'idCategoria', data.categoria);

                if (data.categoria && select.value !== data.categoria) {
                    var option = document.createElement('option');
                    option.value = data.categoria;
                    option.textContent = (data.categoriaNombre || 'Categoria') + ' (inactiva)';
                    option.selected = true;
                    option.dataset.inactiveCategory = 'true';
                    select.prepend(option);
                    setField(form, 'idCategoria', data.categoria);
                }

                if ((data.categoriaEstado || '').toLowerCase() === 'inactivo') {
                    var nombreCategoria = data.categoriaNombre || 'esta categoria';
                    if (warning) {
                        warning.textContent = 'No puedes activar este plato porque su categoria esta inactiva. Primero activa la categoria ' + nombreCategoria + '.';
                        warning.classList.remove('d-none');
                    }
                    if (statusSelect) {
                        statusSelect.value = 'inactivo';
                    }
                    if (activeStatusOption) {
                        activeStatusOption.disabled = true;
                    }
                }

                select.addEventListener('change', function () {
                    select.querySelectorAll('[data-inactive-category]').forEach(function (option) {
                        option.remove();
                    });
                    if (warning) {
                        warning.classList.add('d-none');
                        warning.textContent = '';
                    }
                    if (activeStatusOption) {
                        activeStatusOption.disabled = false;
                    }
                }, {once: true});
            }

            configureModal('modalProducto', function (form, data) {
                setField(form, 'idProducto', data.id);
                setField(form, 'nombreProducto', data.nombre);
                setField(form, 'descripcion', data.descripcion);
                setField(form, 'precio', data.precio);
                setField(form, 'disponibilidad', data.disponibilidad || 'disponible');
                setField(form, 'estado', data.estado || 'activo');
                setProductCategoryField(form, data);
                setField(form, 'imagen', data.imagen);
            });

            configureModal('modalCategoria', function (form, data) {
                setField(form, 'idCategoria', data.id);
                setField(form, 'nombreCategoria', data.nombre);
                setField(form, 'descripcion', data.descripcion);
                setField(form, 'estado', data.estado || 'activo');
            });
        </script>
    </body>
</html>

