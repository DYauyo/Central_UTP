<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Productos | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="productos"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Productos"/>
                    <jsp:param name="subtituloHeader" value="Gestión de productos del restaurante"/>
                </jsp:include>

                <main class="content-shell">
            <div class="panel">
                <div class="panel-header d-flex flex-column flex-md-row gap-3 justify-content-between align-items-md-center">
                    <div>
                        <h1 class="section-title">Gestión de productos</h1>
                        <p class="section-subtitle">Administra los platos y bebidas del restaurante.</p>
                    </div>
                    <a class="btn btn-resto" href="${pageContext.request.contextPath}/ProductoServlet?accion=nuevo">
                        <i class="bi bi-plus-circle me-1"></i> Nuevo producto
                    </a>
                </div>

                <div class="panel-body">
                    <c:if test="${param.mensaje == 'registrado'}">
                        <div class="alert alert-success">Producto registrado correctamente.</div>
                    </c:if>
                    <c:if test="${param.mensaje == 'actualizado'}">
                        <div class="alert alert-success">Producto actualizado correctamente.</div>
                    </c:if>
                    <c:if test="${param.mensaje == 'desactivado'}">
                        <div class="alert alert-warning">Producto desactivado correctamente.</div>
                    </c:if>
                    <c:if test="${param.mensaje == 'no_encontrado'}">
                        <div class="alert alert-danger">No se encontró el producto solicitado.</div>
                    </c:if>

                    <c:choose>
                        <c:when test="${empty productos}">
                            <div class="empty-state">
                                <h2 class="h5">No hay productos registrados</h2>
                                <p>Registra el primer producto para construir la carta del restaurante.</p>
                                <a class="btn btn-resto" href="${pageContext.request.contextPath}/ProductoServlet?accion=nuevo">Registrar producto</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Producto</th>
                                            <th>Categoría</th>
                                            <th>Precio</th>
                                            <th>Disponibilidad</th>
                                            <th>Estado</th>
                                            <th class="text-end">Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="producto" items="${productos}">
                                            <tr>
                                                <td>${producto.idProducto}</td>
                                                <td>
                                                    <div class="fw-semibold">${producto.nombreProducto}</div>
                                                    <small class="text-secondary">${producto.descripcion}</small>
                                                </td>
                                                <td>${producto.nombreCategoria}</td>
                                                <td>S/ ${producto.precio}</td>
                                                <td>
                                                    <span class="badge rounded-pill ${producto.disponibilidad == 'disponible' ? 'estado-activo' : 'estado-inactivo'}">
                                                        ${producto.disponibilidad}
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge rounded-pill ${producto.estado == 'activo' ? 'estado-activo' : 'estado-inactivo'}">
                                                        ${producto.estado}
                                                    </span>
                                                </td>
                                                <td class="text-end">
                                                    <a class="btn btn-sm btn-outline-resto" href="${pageContext.request.contextPath}/ProductoServlet?accion=editar&idProducto=${producto.idProducto}">
                                                        <i class="bi bi-pencil-square"></i> Editar
                                                    </a>
                                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/ProductoServlet?accion=eliminar&idProducto=${producto.idProducto}">
                                                        <i class="bi bi-slash-circle"></i> Desactivar
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>


