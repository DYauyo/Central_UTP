<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Desactivar producto | Central</title>
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
                <div class="panel-header">
                    <h1 class="section-title">Desactivar producto</h1>
                    <p class="section-subtitle">El producto quedará inactivo sin borrar su historial.</p>
                </div>
                <div class="panel-body">
                    <div class="alert alert-warning">
                        Confirma la desactivación de <strong>${producto.nombreProducto}</strong>.
                    </div>

                    <dl class="row mb-4">
                        <dt class="col-sm-3">ID</dt>
                        <dd class="col-sm-9">${producto.idProducto}</dd>
                        <dt class="col-sm-3">Categoría</dt>
                        <dd class="col-sm-9">${producto.nombreCategoria}</dd>
                        <dt class="col-sm-3">Precio</dt>
                        <dd class="col-sm-9">S/ ${producto.precio}</dd>
                        <dt class="col-sm-3">Estado actual</dt>
                        <dd class="col-sm-9">${producto.estado}</dd>
                    </dl>

                    <form action="${pageContext.request.contextPath}/ProductoServlet" method="post" class="d-flex gap-2 justify-content-end">
                        <input type="hidden" name="accion" value="desactivar">
                        <input type="hidden" name="idProducto" value="${producto.idProducto}">
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/ProductoServlet">Cancelar</a>
                        <button class="btn btn-danger" type="submit">
                            <i class="bi bi-slash-circle me-1"></i> Desactivar
                        </button>
                    </form>
                </div>
            </div>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>


