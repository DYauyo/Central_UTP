<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Desactivar usuario | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="usuarios"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Usuarios"/>
                    <jsp:param name="subtituloHeader" value="Administración de usuarios y roles"/>
                </jsp:include>

                <main class="content-shell">
            <div class="panel">
                <div class="panel-header">
                    <h1 class="section-title">Desactivar usuario</h1>
                    <p class="section-subtitle">El usuario quedará inactivo sin borrar su historial.</p>
                </div>
                <div class="panel-body">
                    <div class="alert alert-warning">
                        Confirma la desactivación de <strong>${usuario.nombres} ${usuario.apellidos}</strong>.
                    </div>

                    <dl class="row mb-4">
                        <dt class="col-sm-3">ID</dt>
                        <dd class="col-sm-9">${usuario.idUsuario}</dd>
                        <dt class="col-sm-3">Username</dt>
                        <dd class="col-sm-9">${usuario.username}</dd>
                        <dt class="col-sm-3">Correo</dt>
                        <dd class="col-sm-9">${usuario.correo}</dd>
                        <dt class="col-sm-3">Rol</dt>
                        <dd class="col-sm-9">${usuario.nombreRol}</dd>
                        <dt class="col-sm-3">Estado actual</dt>
                        <dd class="col-sm-9">${usuario.estado}</dd>
                    </dl>

                    <form action="${pageContext.request.contextPath}/UsuarioServlet" method="post" class="d-flex gap-2 justify-content-end">
                        <input type="hidden" name="accion" value="desactivar">
                        <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/UsuarioServlet">Cancelar</a>
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


