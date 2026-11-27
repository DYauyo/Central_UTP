<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:url var="ruta" value="/ProveedorServlet"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Proveedores | Central</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/estilos.css" rel="stylesheet">
</head>
<body>
<div class="app-layout">
    <jsp:include page="/vistas/layout/sidebar.jsp"><jsp:param name="moduloActivo" value="insumos"/></jsp:include>
    <div class="app-main">
        <jsp:include page="/vistas/layout/header.jsp"><jsp:param name="tituloHeader" value="Proveedores"/></jsp:include>
        <main class="content-shell">
            <a href="<c:url value='/InsumoServlet'/>">Volver a insumos</a>
            <h1 class="section-title my-3">Proveedores</h1>
            <c:if test="${not empty error}"><div class="alert alert-danger" data-message-popup="danger" role="alert"><c:out value="${error}"/></div></c:if>
            <c:if test="${empty error and param.guardado == '1'}"><div class="alert alert-success" data-message-popup="success" role="status">Cambio guardado.</div></c:if>
            <section class="panel mb-4 p-3">
                <h2 class="h5">${empty edicion or edicion.id() == 0 ? 'Registrar proveedor' : 'Editar proveedor'}</h2>
                <form action="${ruta}" method="post" class="row g-3">
                    <input type="hidden" name="accion" value="guardar">
                    <input type="hidden" name="idProveedor" value="${empty edicion ? 0 : edicion.id()}">
                    <label class="col-md-3">RUC
                        <input class="form-control" name="ruc" value="${empty edicion ? '' : fn:escapeXml(edicion.ruc())}" maxlength="11" pattern="[0-9]{11}" inputmode="numeric" required>
                    </label>
                    <label class="col-md-6">Razón social
                        <input class="form-control" name="razonSocial" value="${empty edicion ? '' : fn:escapeXml(edicion.razonSocial())}" maxlength="150" required>
                    </label>
                    <label class="col-md-3">Teléfono
                        <input class="form-control" type="tel" name="telefono" value="${empty edicion ? '' : fn:escapeXml(edicion.telefono())}" maxlength="30">
                    </label>
                    <label class="col-md-6">Correo
                        <input class="form-control" type="email" name="correo" value="${empty edicion ? '' : fn:escapeXml(edicion.correo())}" maxlength="150">
                    </label>
                    <label class="col-md-3">Estado
                        <select class="form-select" name="estado">
                            <option value="activo">Activo</option>
                            <option value="inactivo" ${not empty edicion and edicion.estado() == 'inactivo' ? 'selected' : ''}>Inactivo</option>
                        </select>
                    </label>
                    <div class="col-12">
                        <button class="btn btn-resto" type="submit">Guardar proveedor</button>
                        <c:if test="${not empty edicion}"><a class="btn btn-outline-secondary" href="${ruta}">Cancelar</a></c:if>
                    </div>
                </form>
            </section>
            <section class="panel mb-4 p-3">
                <h2 class="h5">Directorio</h2>
                <c:if test="${empty proveedores}"><p>Sin proveedores registrados.</p></c:if>
                <c:if test="${not empty proveedores}">
                    <div class="table-responsive"><table class="table align-middle">
                        <thead><tr><th>RUC</th><th>Razón social</th><th>Teléfono</th><th>Correo</th><th>Estado</th><th>Acciones</th></tr></thead>
                        <tbody>
                            <c:forEach var="p" items="${proveedores}">
                                <tr>
                                    <td><c:out value="${p.ruc()}"/></td>
                                    <td><c:out value="${p.razonSocial()}"/></td>
                                    <td><c:out value="${p.telefono()}"/></td>
                                    <td><c:out value="${p.correo()}"/></td>
                                    <td><c:out value="${p.estado()}"/></td>
                                    <td><a href="${ruta}?editar=${p.id()}">Editar</a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table></div>
                </c:if>
            </section>
            <section class="panel p-3">
                <h2 class="h5">Proveedores por insumo</h2>
                <form action="${ruta}" method="post" class="row g-3 align-items-end mb-3">
                    <input type="hidden" name="accion" value="vincular">
                    <label class="col-md-5">Insumo
                        <select class="form-select" name="idInsumo" required>
                            <option value="">Selecciona un insumo</option>
                            <c:forEach var="i" items="${insumos}">
                                <option value="${i.idInsumo}"><c:out value="${i.nombreInsumo}"/></option>
                            </c:forEach>
                        </select>
                    </label>
                    <label class="col-md-5">Proveedor
                        <select class="form-select" name="idProveedor" required>
                            <option value="">Selecciona un proveedor</option>
                            <c:forEach var="p" items="${proveedores}">
                                <c:if test="${p.estado() == 'activo'}">
                                    <option value="${p.id()}"><c:out value="${p.razonSocial()}"/></option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </label>
                    <div class="col-md-2"><button class="btn btn-resto" type="submit">Asociar</button></div>
                </form>
                <c:if test="${empty asociaciones}"><p>Sin asociaciones registradas.</p></c:if>
                <c:forEach var="a" items="${asociaciones}">
                    <form action="${ruta}" method="post" class="d-flex align-items-center justify-content-between gap-3 border-top py-2">
                        <input type="hidden" name="accion" value="desvincular">
                        <input type="hidden" name="idInsumo" value="${a.idInsumo()}">
                        <input type="hidden" name="idProveedor" value="${a.idProveedor()}">
                        <span><c:out value="${a.insumo()}"/> · <c:out value="${a.proveedor()}"/></span>
                        <button class="btn btn-outline-secondary btn-sm" type="submit">Quitar asociación</button>
                    </form>
                </c:forEach>
            </section>
        </main>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
