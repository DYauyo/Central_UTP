<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Usuarios y Roles | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <c:set var="activeTab" value="${empty tabActivo ? 'roles' : tabActivo}"/>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="usuarios"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Usuarios y Roles"/>
                    <jsp:param name="subtituloHeader" value="Administracion de usuarios internos, roles y accesos"/>
                </jsp:include>

                <main class="content-shell user-admin-shell">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show auto-dismiss-alert" role="alert">
                            ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'registrado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Usuario registrado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'actualizado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Usuario actualizado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'desactivado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Usuario desactivado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'usuario_estado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Estado del usuario actualizado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'no_encontrado'}">
                        <div class="alert alert-danger alert-dismissible fade show auto-dismiss-alert" role="alert">
                            No se encontro el usuario solicitado.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'rol_registrado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Rol registrado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'rol_actualizado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Rol actualizado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>
                    <c:if test="${param.mensaje == 'rol_estado'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss-alert" role="alert">
                            Estado del rol actualizado correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>

                    <section class="user-summary-grid">
                        <article class="user-stat-card">
                            <i class="bi bi-people-fill"></i>
                            <div>
                                <strong>${totalUsuarios}</strong>
                                <span>Usuarios registrados</span>
                                <small>en el sistema</small>
                            </div>
                        </article>
                        <article class="user-stat-card">
                            <i class="bi bi-person-check-fill"></i>
                            <div>
                                <strong>${usuariosActivos}</strong>
                                <span>Usuarios activos</span>
                                <small>con acceso habilitado</small>
                            </div>
                        </article>
                        <article class="user-stat-card">
                            <i class="bi bi-shield-lock-fill"></i>
                            <div>
                                <strong>${totalRoles}</strong>
                                <span>Roles creados</span>
                                <small>perfiles de trabajo</small>
                            </div>
                        </article>
                        <article class="user-stat-card">
                            <i class="bi bi-shield-check"></i>
                            <div>
                                <strong>${rolesActivos}</strong>
                                <span>Roles activos</span>
                                <small>disponibles para asignar</small>
                            </div>
                        </article>
                    </section>

                    <nav class="user-admin-tabs" aria-label="Usuarios y roles">
                        <a class="user-tab ${activeTab == 'roles' ? 'active' : ''}" href="${pageContext.request.contextPath}/UsuarioServlet?tab=roles">
                            <i class="bi bi-shield-lock"></i> Roles
                        </a>
                        <a class="user-tab ${activeTab == 'usuarios' ? 'active' : ''}" href="${pageContext.request.contextPath}/UsuarioServlet?tab=usuarios">
                            <i class="bi bi-people"></i> Usuarios
                        </a>
                    </nav>

                    <c:choose>
                        <c:when test="${activeTab == 'roles'}">
                            <section class="user-panel">
                                <div class="user-panel-head">
                                    <div>
                                        <h2>Gestion de roles</h2>
                                        <p>Crea perfiles de trabajo y controla si estan disponibles para nuevos usuarios.</p>
                                    </div>
                                    <button type="button" class="btn user-primary-btn" data-bs-toggle="modal" data-bs-target="#modalRol" data-mode="new">
                                        <i class="bi bi-plus-lg"></i> Nuevo rol
                                    </button>
                                </div>

                                <div class="user-filter-row">
                                    <button class="filter-chip active" type="button" data-user-filter data-target="rolesGrid" data-filter-type="status" data-filter-value="">Todos</button>
                                    <button class="filter-chip" type="button" data-user-filter data-target="rolesGrid" data-filter-type="status" data-filter-value="activo">Activos</button>
                                    <button class="filter-chip" type="button" data-user-filter data-target="rolesGrid" data-filter-type="status" data-filter-value="inactivo">Inactivos</button>
                                </div>

                                <div class="role-card-grid" id="rolesGrid">
                                    <c:forEach var="rol" items="${rolesGestion}">
                                        <article class="role-card ${rol.estado == 'inactivo' ? 'is-inactive' : ''}"
                                                 data-search="${fn:escapeXml(rol.nombreRol)} ${fn:escapeXml(rol.descripcion)} ${fn:escapeXml(rol.estado)}"
                                                 data-status="${fn:escapeXml(rol.estado)}">
                                            <div class="role-card-top">
                                                <span class="role-icon"><i class="bi bi-shield-lock"></i></span>
                                                <form method="post" action="${pageContext.request.contextPath}/UsuarioServlet" class="status-toggle-form">
                                                    <input type="hidden" name="accion" value="cambiarEstadoRol">
                                                    <input type="hidden" name="idRol" value="${rol.idRol}">
                                                    <input type="hidden" name="estado" value="${rol.estado == 'activo' ? 'inactivo' : 'activo'}">
                                                    <button type="submit" class="soft-pill status-toggle ${rol.estado == 'activo' ? 'success' : 'danger'}">
                                                        <i class="bi bi-power"></i> ${rol.estado}
                                                    </button>
                                                </form>
                                            </div>
                                            <h3>${rol.nombreRol}</h3>
                                            <p>${rol.descripcion}</p>
                                            <div class="role-meta">
                                                <span><i class="bi bi-person-lines-fill"></i> ${rol.cantidadUsuarios} usuarios</span>
                                                <span><i class="bi bi-hash"></i> ID ${rol.idRol}</span>
                                            </div>
                                            <button type="button"
                                                    class="btn btn-outline-resto w-100"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#modalRol"
                                                    data-mode="edit"
                                                    data-id="${rol.idRol}"
                                                    data-nombre="${fn:escapeXml(rol.nombreRol)}"
                                                    data-descripcion="${fn:escapeXml(rol.descripcion)}"
                                                    data-estado="${rol.estado}">
                                                <i class="bi bi-pencil"></i> Editar rol
                                            </button>
                                        </article>
                                    </c:forEach>
                                </div>
                            </section>
                        </c:when>

                        <c:otherwise>
                            <section class="user-panel">
                                <div class="user-panel-head">
                                    <div>
                                        <h2>Gestion de usuarios</h2>
                                        <p>Administra los usuarios internos, su rol asignado y el estado de acceso.</p>
                                    </div>
                                    <button type="button" class="btn user-primary-btn" data-bs-toggle="modal" data-bs-target="#modalUsuario">
                                        <i class="bi bi-plus-lg"></i> Nuevo usuario
                                    </button>
                                </div>

                                <c:choose>
                                    <c:when test="${empty usuarios}">
                                        <div class="empty-state">
                                            <h2 class="h5">No hay usuarios registrados</h2>
                                            <p>Registra el primer usuario para administrar el sistema.</p>
                                            <button type="button" class="btn user-primary-btn" data-bs-toggle="modal" data-bs-target="#modalUsuario">Registrar usuario</button>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="user-filter-row">
                                            <button class="filter-chip active" type="button" data-user-filter data-target="usersGrid" data-filter-type="status" data-filter-value="">Todos</button>
                                            <button class="filter-chip" type="button" data-user-filter data-target="usersGrid" data-filter-type="status" data-filter-value="activo">Activos</button>
                                            <button class="filter-chip" type="button" data-user-filter data-target="usersGrid" data-filter-type="status" data-filter-value="inactivo">Inactivos</button>
                                            <button class="filter-chip" type="button" data-user-filter data-target="usersGrid" data-filter-type="role" data-filter-value="">Todos los roles</button>
                                            <c:forEach var="rolFiltro" items="${rolesGestion}">
                                                <button class="filter-chip" type="button" data-user-filter data-target="usersGrid" data-filter-type="role" data-filter-value="${rolFiltro.idRol}">${rolFiltro.nombreRol}</button>
                                            </c:forEach>
                                        </div>

                                        <div class="user-card-grid" id="usersGrid">
                                            <c:forEach var="usuario" items="${usuarios}">
                                                <c:set var="roleCss" value="role-custom"/>
                                                <c:choose>
                                                    <c:when test="${usuario.idRol == 1}">
                                                        <c:set var="roleCss" value="role-admin"/>
                                                    </c:when>
                                                    <c:when test="${usuario.idRol == 2}">
                                                        <c:set var="roleCss" value="role-cashier"/>
                                                    </c:when>
                                                    <c:when test="${usuario.idRol == 3}">
                                                        <c:set var="roleCss" value="role-waiter"/>
                                                    </c:when>
                                                    <c:when test="${usuario.idRol == 4}">
                                                        <c:set var="roleCss" value="role-kitchen"/>
                                                    </c:when>
                                                    <c:when test="${usuario.idRol == 5}">
                                                        <c:set var="roleCss" value="role-storage"/>
                                                    </c:when>
                                                    <c:when test="${usuario.idRol == 6}">
                                                        <c:set var="roleCss" value="role-manager"/>
                                                    </c:when>
                                                </c:choose>
                                                <article class="user-profile-card ${roleCss} ${usuario.estado == 'inactivo' ? 'is-inactive' : ''} ${usuario.estadoRol == 'inactivo' ? 'role-is-disabled' : ''}"
                                                         data-search="${fn:escapeXml(usuario.nombres)} ${fn:escapeXml(usuario.apellidos)} ${fn:escapeXml(usuario.username)} ${fn:escapeXml(usuario.nombreRol)} ${fn:escapeXml(usuario.estado)}"
                                                         data-status="${fn:escapeXml(usuario.estado)}"
                                                         data-role="${usuario.idRol}">
                                                    <div class="user-card-hero">
                                                        <span class="user-card-avatar">${fn:substring(usuario.nombres, 0, 1)}${fn:substring(usuario.apellidos, 0, 1)}</span>
                                                        <form method="post" action="${pageContext.request.contextPath}/UsuarioServlet" class="status-toggle-form">
                                                            <input type="hidden" name="accion" value="cambiarEstadoUsuario">
                                                            <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">
                                                            <input type="hidden" name="estado" value="${usuario.estado == 'activo' ? 'inactivo' : 'activo'}">
                                                            <button type="submit" class="soft-pill status-toggle ${usuario.estado == 'activo' ? 'success' : 'danger'}">
                                                                <i class="bi bi-power"></i> ${usuario.estado}
                                                            </button>
                                                        </form>
                                                    </div>
                                                    <div class="user-card-body">
                                                        <h3>${usuario.nombres} ${usuario.apellidos}</h3>
                                                        <p>@${usuario.username}</p>
                                                        <span class="role-chip ${roleCss} ${usuario.estadoRol == 'inactivo' ? 'disabled-role' : ''}">
                                                            <strong>${usuario.nombreRol}</strong>
                                                            <c:if test="${usuario.estadoRol == 'inactivo'}">
                                                                <small>Rol inactivo</small>
                                                            </c:if>
                                                        </span>
                                                    </div>
                                                    <div class="user-card-info">
                                                        <span><i class="bi bi-envelope"></i> ${empty usuario.correo ? 'Sin correo' : usuario.correo}</span>
                                                        <span><i class="bi bi-phone"></i> ${empty usuario.telefono ? 'Sin telefono' : usuario.telefono}</span>
                                                    </div>
                                                    <div class="user-card-actions">
                                                        <a class="btn btn-outline-resto" href="${pageContext.request.contextPath}/UsuarioServlet?accion=editar&idUsuario=${usuario.idUsuario}">
                                                            <i class="bi bi-pencil-square"></i> Editar
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

        <div class="modal fade" id="modalUsuario" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-user-dialog">
                <form class="modal-content resto-modal modal-user modal-new-user needs-validation" method="post" action="${pageContext.request.contextPath}/UsuarioServlet" novalidate>
                    <input type="hidden" name="accion" value="insertar">
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span class="resto-modal-icon"><i class="bi bi-person-plus"></i></span>
                            <div>
                                <small>Usuarios</small>
                                <h2 class="modal-title h5">Registrar usuario</h2>
                            </div>
                        </div>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body user-form-body">
                        <div class="user-form-grid user-modal-form-grid">
                            <section class="user-form-section">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label for="modalNombres" class="form-label">Nombres</label>
                                        <input type="text" class="form-control" id="modalNombres" name="nombres" maxlength="100" required>
                                        <div class="invalid-feedback">Ingresa los nombres.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="modalApellidos" class="form-label">Apellidos</label>
                                        <input type="text" class="form-control" id="modalApellidos" name="apellidos" maxlength="100" required>
                                        <div class="invalid-feedback">Ingresa los apellidos.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="modalCorreo" class="form-label">Correo</label>
                                        <input type="email" class="form-control" id="modalCorreo" name="correo" maxlength="150">
                                        <div class="invalid-feedback">Ingresa un correo valido.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="modalTelefono" class="form-label">Telefono</label>
                                        <input type="text" class="form-control" id="modalTelefono" name="telefono" maxlength="20">
                                    </div>
                                </div>
                            </section>

                            <section class="user-form-section user-access-section">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label for="modalUsername" class="form-label">Usuario</label>
                                        <input type="text" class="form-control" id="modalUsername" name="username" maxlength="100" required>
                                        <div class="invalid-feedback">Ingresa el usuario.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="modalPassword" class="form-label">Password</label>
                                        <input type="password" class="form-control" id="modalPassword" name="password" maxlength="255" required>
                                        <div class="invalid-feedback">Ingresa el password.</div>
                                    </div>
                                    <div class="col-md-8">
                                        <label for="modalIdRol" class="form-label">Rol</label>
                                        <select class="form-select" id="modalIdRol" name="idRol" required>
                                            <option value="">Selecciona un rol</option>
                                            <c:forEach var="rol" items="${rolesGestion}">
                                                <c:if test="${rol.estado == 'activo'}">
                                                    <option value="${rol.idRol}">${rol.nombreRol}</option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                        <div class="invalid-feedback">Selecciona un rol.</div>
                                    </div>
                                    <div class="col-md-4">
                                        <label for="modalEstadoUsuario" class="form-label">Estado</label>
                                        <select class="form-select" id="modalEstadoUsuario" name="estado" required>
                                            <option value="activo" selected>Activo</option>
                                            <option value="inactivo">Inactivo</option>
                                        </select>
                                    </div>
                                </div>
                            </section>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-resto-dark">Guardar usuario</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="modal fade" id="modalRol" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <form class="modal-content resto-modal modal-user modal-role needs-validation" method="post" action="${pageContext.request.contextPath}/UsuarioServlet" novalidate>
                    <input type="hidden" name="accion" value="insertarRol" data-role-action>
                    <input type="hidden" name="idRol" data-role-id>
                    <div class="modal-header resto-modal-head">
                        <div class="resto-modal-title">
                            <span class="resto-modal-icon"><i class="bi bi-shield-lock"></i></span>
                            <div>
                                <small>Roles</small>
                                <h2 class="modal-title h5" data-role-title>Nuevo rol</h2>
                            </div>
                        </div>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <section class="user-form-section role-modal-section">
                            <div class="row g-3">
                                <div class="col-12">
                                    <label class="form-label">Nombre del rol</label>
                                    <input class="form-control" name="nombreRol" maxlength="100" required data-role-name>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Descripcion</label>
                                    <textarea class="form-control" name="descripcion" rows="3" maxlength="255" data-role-description></textarea>
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Estado</label>
                                    <select class="form-select" name="estado" data-role-status>
                                        <option value="activo">Activo</option>
                                        <option value="inactivo">Inactivo</option>
                                    </select>
                                </div>
                            </div>
                        </section>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-resto-dark">Guardar rol</button>
                    </div>
                </form>
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
                        bootstrap.Alert.getOrCreateInstance(alertNode).close();
                    }, 450);
                }, 4000);
            });

            document.querySelectorAll('.needs-validation').forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false);
            });

            var modalUsuario = document.getElementById('modalUsuario');
            if (modalUsuario) {
                modalUsuario.addEventListener('hidden.bs.modal', function () {
                    var form = modalUsuario.querySelector('form');
                    if (form) {
                        form.reset();
                        form.classList.remove('was-validated');
                    }
                });
            }

            var modalRol = document.getElementById('modalRol');
            if (modalRol) {
                modalRol.addEventListener('show.bs.modal', function (event) {
                    var button = event.relatedTarget;
                    var mode = button ? button.dataset.mode : 'new';
                    var title = modalRol.querySelector('[data-role-title]');
                    var action = modalRol.querySelector('[data-role-action]');
                    var id = modalRol.querySelector('[data-role-id]');
                    var name = modalRol.querySelector('[data-role-name]');
                    var description = modalRol.querySelector('[data-role-description]');
                    var status = modalRol.querySelector('[data-role-status]');

                    if (mode === 'edit') {
                        title.textContent = 'Editar rol';
                        action.value = 'actualizarRol';
                        id.value = button.dataset.id || '';
                        name.value = button.dataset.nombre || '';
                        description.value = button.dataset.descripcion || '';
                        status.value = button.dataset.estado || 'activo';
                        return;
                    }

                    title.textContent = 'Nuevo rol';
                    action.value = 'insertarRol';
                    id.value = '';
                    name.value = '';
                    description.value = '';
                    status.value = 'activo';
                });
            }

            function applyUserFilters(targetId) {
                var grid = document.getElementById(targetId);
                if (!grid) {
                    return;
                }

                var activeFilters = {};
                document.querySelectorAll('[data-user-filter][data-target="' + targetId + '"].active').forEach(function (button) {
                    var type = button.dataset.filterType;
                    var value = button.dataset.filterValue || '';
                    if (value) {
                        activeFilters[type] = value.toLowerCase();
                    }
                });

                grid.querySelectorAll('[data-search]').forEach(function (card) {
                    var visible = true;
                    Object.keys(activeFilters).forEach(function (type) {
                        if ((card.dataset[type] || '').toLowerCase() !== activeFilters[type]) {
                            visible = false;
                        }
                    });
                    card.hidden = !visible;
                });
            }

            document.querySelectorAll('[data-user-filter]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var target = button.dataset.target;
                    var type = button.dataset.filterType;
                    var value = button.dataset.filterValue || '';

                    document.querySelectorAll('[data-user-filter][data-target="' + target + '"][data-filter-type="' + type + '"]').forEach(function (peer) {
                        peer.classList.remove('active');
                    });

                    if (!value && type === 'role') {
                        document.querySelectorAll('[data-user-filter][data-target="' + target + '"][data-filter-type="role"]').forEach(function (peer) {
                            peer.classList.remove('active');
                        });
                    }

                    button.classList.add('active');
                    applyUserFilters(target);
                });
            });
        </script>
    </body>
</html>

