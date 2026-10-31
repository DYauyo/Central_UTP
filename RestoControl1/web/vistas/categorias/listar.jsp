<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Categorías | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="categorias"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Categorías"/>
                    <jsp:param name="subtituloHeader" value="Gestión de categorías del menú"/>
                </jsp:include>

                <main class="content-shell">
            <div class="panel">
                <div class="panel-header d-flex flex-column flex-md-row gap-3 justify-content-between align-items-md-center">
                    <div>
                        <h1 class="section-title">Gestión de categorías</h1>
                        <p class="section-subtitle">Administra los grupos del menú del restaurante.</p>
                    </div>
                    <a class="btn btn-resto" href="${pageContext.request.contextPath}/CategoriaServlet?accion=nuevo">
                        <i class="bi bi-plus-circle me-1"></i> Nueva categoría
                    </a>
                </div>

                <div class="panel-body">
                    <c:if test="${param.mensaje == 'registrado'}">
                        <div class="alert alert-success">Categoría registrada correctamente.</div>
                    </c:if>
                    <c:if test="${param.mensaje == 'actualizado'}">
                        <div class="alert alert-success">Categoría actualizada correctamente.</div>
                    </c:if>
                    <c:if test="${param.mensaje == 'desactivado'}">
                        <div class="alert alert-warning">Categoría desactivada correctamente.</div>
                    </c:if>
                    <c:if test="${param.mensaje == 'no_encontrado'}">
                        <div class="alert alert-danger">No se encontró la categoría solicitada.</div>
                    </c:if>

                    <c:choose>
                        <c:when test="${empty categorias}">
                            <div class="empty-state">
                                <h2 class="h5">No hay categorías registradas</h2>
                                <p>Registra la primera categoría para organizar los productos.</p>
                                <a class="btn btn-resto" href="${pageContext.request.contextPath}/CategoriaServlet?accion=nuevo">Registrar categoría</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Nombre</th>
                                            <th>Descripción</th>
                                            <th>Estado</th>
                                            <th class="text-end">Acciones</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="categoria" items="${categorias}">
                                            <tr>
                                                <td>${categoria.idCategoria}</td>
                                                <td class="fw-semibold">${categoria.nombreCategoria}</td>
                                                <td>${categoria.descripcion}</td>
                                                <td>
                                                    <span class="badge rounded-pill ${categoria.estado == 'activo' ? 'estado-activo' : 'estado-inactivo'}">
                                                        ${categoria.estado}
                                                    </span>
                                                </td>
                                                <td class="text-end">
                                                    <a class="btn btn-sm btn-outline-resto" href="${pageContext.request.contextPath}/CategoriaServlet?accion=editar&idCategoria=${categoria.idCategoria}">
                                                        <i class="bi bi-pencil-square"></i> Editar
                                                    </a>
                                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/CategoriaServlet?accion=eliminar&idCategoria=${categoria.idCategoria}">
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


