<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registrar usuario | Central</title>
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
                    <jsp:param name="tituloHeader" value="Usuarios y Roles"/>
                    <jsp:param name="subtituloHeader" value="Administracion de usuarios internos, roles y accesos"/>
                </jsp:include>

                <main class="content-shell user-form-shell">
                    <section class="resto-modal modal-user user-form-card">
                        <form action="${pageContext.request.contextPath}/UsuarioServlet" method="post" class="needs-validation" novalidate>
                            <input type="hidden" name="accion" value="insertar">

                            <div class="modal-header resto-modal-head">
                                <div class="resto-modal-title">
                                    <span class="resto-modal-icon"><i class="bi bi-person-plus"></i></span>
                                    <div>
                                        <small>Usuarios</small>
                                        <h2 class="modal-title h5">Registrar usuario</h2>
                                    </div>
                                </div>
                                <a class="btn-close btn-close-white" href="${pageContext.request.contextPath}/UsuarioServlet" aria-label="Cerrar"></a>
                            </div>

                            <div class="modal-body user-form-body">
                                <c:if test="${not empty error}">
                                    <div class="alert alert-danger">${error}</div>
                                </c:if>
                                <c:if test="${empty roles}">
                                    <div class="alert alert-warning">Primero debe existir al menos un rol activo.</div>
                                </c:if>

                                <div class="user-form-grid">
                                    <section class="user-form-section">
                                        <div class="row g-3">
                                            <div class="col-md-6">
                                                <label for="nombres" class="form-label">Nombres</label>
                                                <input type="text" class="form-control" id="nombres" name="nombres" value="${usuario.nombres}" maxlength="100" required>
                                                <div class="invalid-feedback">Ingresa los nombres.</div>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="apellidos" class="form-label">Apellidos</label>
                                                <input type="text" class="form-control" id="apellidos" name="apellidos" value="${usuario.apellidos}" maxlength="100" required>
                                                <div class="invalid-feedback">Ingresa los apellidos.</div>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="correo" class="form-label">Correo</label>
                                                <input type="email" class="form-control" id="correo" name="correo" value="${usuario.correo}" maxlength="150" required>
                                                <div class="invalid-feedback">Ingresa un correo valido.</div>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="telefono" class="form-label">Telefono</label>
                                                <input type="text" class="form-control" id="telefono" name="telefono" value="${usuario.telefono}" maxlength="20">
                                            </div>
                                        </div>
                                    </section>

                                    <section class="user-form-section user-access-section">
                                        <div class="row g-3">
                                            <div class="col-md-6">
                                                <label for="username" class="form-label">Usuario</label>
                                                <input type="text" class="form-control" id="username" name="username" value="${usuario.username}" maxlength="100" required>
                                                <div class="invalid-feedback">Ingresa el usuario.</div>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="password" class="form-label">Password</label>
                                                <input type="password" class="form-control" id="password" name="password" maxlength="255" required>
                                                <div class="invalid-feedback">Ingresa el password.</div>
                                            </div>
                                            <div class="col-md-8">
                                                <label for="idRol" class="form-label">Rol</label>
                                                <select class="form-select" id="idRol" name="idRol" required>
                                                    <option value="">Selecciona un rol</option>
                                                    <c:forEach var="rol" items="${roles}">
                                                        <option value="${rol.idRol}" ${rol.idRol == usuario.idRol ? 'selected' : ''}>${rol.nombreRol}</option>
                                                    </c:forEach>
                                                </select>
                                                <div class="invalid-feedback">Selecciona un rol.</div>
                                            </div>
                                            <div class="col-md-4">
                                                <label for="estado" class="form-label">Estado</label>
                                                <select class="form-select" id="estado" name="estado" required>
                                                    <option value="activo" ${usuario.estado == 'activo' ? 'selected' : ''}>Activo</option>
                                                    <option value="inactivo" ${usuario.estado == 'inactivo' ? 'selected' : ''}>Inactivo</option>
                                                </select>
                                            </div>
                                        </div>
                                    </section>
                                </div>
                            </div>

                            <div class="modal-footer">
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/UsuarioServlet">Cancelar</a>
                                <button class="btn btn-resto-dark" type="submit" ${empty roles ? 'disabled' : ''}>
                                    <i class="bi bi-save"></i> Guardar usuario
                                </button>
                            </div>
                        </form>
                    </section>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            (() => {
                const forms = document.querySelectorAll('.needs-validation');
                Array.from(forms).forEach(form => {
                    form.addEventListener('submit', event => {
                        if (!form.checkValidity()) {
                            event.preventDefault();
                            event.stopPropagation();
                        }
                        form.classList.add('was-validated');
                    }, false);
                });
            })();
        </script>
    </body>
</html>

