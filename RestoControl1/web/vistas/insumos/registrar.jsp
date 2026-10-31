<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registrar insumo | Central</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body>
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="insumos"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeader" value="Insumos"/>
                    <jsp:param name="subtituloHeader" value="Control de ingredientes e inventario"/>
                </jsp:include>

                <main class="content-shell">
            <div class="panel">
                <div class="panel-header">
                    <h1 class="section-title">Registrar insumo</h1>
                    <p class="section-subtitle">Completa los datos para controlar este ingrediente o material.</p>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/InsumoServlet" method="post" class="needs-validation" novalidate>
                        <input type="hidden" name="accion" value="insertar">

                        <div class="row g-3">
                            <div class="col-md-8">
                                <label for="nombreInsumo" class="form-label">Nombre del insumo</label>
                                <input type="text" class="form-control" id="nombreInsumo" name="nombreInsumo" value="${insumo.nombreInsumo}" maxlength="150" required>
                                <div class="invalid-feedback">Ingresa el nombre del insumo.</div>
                            </div>

                            <div class="col-md-4">
                                <label for="unidadMedida" class="form-label">Unidad de medida</label>
                                <input type="text" class="form-control" id="unidadMedida" name="unidadMedida" value="${insumo.unidadMedida}" maxlength="50" placeholder="kg, lt, unidad" required>
                                <div class="invalid-feedback">Ingresa la unidad de medida.</div>
                            </div>

                            <div class="col-md-4">
                                <label for="stockActual" class="form-label">Stock actual</label>
                                <input type="number" class="form-control" id="stockActual" name="stockActual" value="${insumo.stockActual}" min="0" step="0.01" required>
                                <div class="invalid-feedback">El stock actual debe ser mayor o igual a 0.</div>
                            </div>

                            <div class="col-md-4">
                                <label for="stockMinimo" class="form-label">Stock mínimo</label>
                                <input type="number" class="form-control" id="stockMinimo" name="stockMinimo" value="${insumo.stockMinimo}" min="0" step="0.01" required>
                                <div class="invalid-feedback">El stock mínimo debe ser mayor o igual a 0.</div>
                            </div>

                            <div class="col-md-4">
                                <label for="estado" class="form-label">Estado</label>
                                <select class="form-select" id="estado" name="estado" required>
                                    <option value="activo" ${insumo.estado == 'activo' ? 'selected' : ''}>activo</option>
                                    <option value="inactivo" ${insumo.estado == 'inactivo' ? 'selected' : ''}>inactivo</option>
                                </select>
                            </div>

                            <div class="col-12">
                                <label for="descripcion" class="form-label">Descripción</label>
                                <textarea class="form-control" id="descripcion" name="descripcion" rows="3" maxlength="255">${insumo.descripcion}</textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-2 justify-content-end mt-4">
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/InsumoServlet">Cancelar</a>
                            <button class="btn btn-resto" type="submit">
                                <i class="bi bi-save me-1"></i> Guardar
                            </button>
                        </div>
                    </form>
                </div>
            </div>
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


