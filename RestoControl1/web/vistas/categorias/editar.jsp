<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Editar categoría | Central</title>
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
                <div class="panel-header">
                    <h1 class="section-title">Editar categoría</h1>
                    <p class="section-subtitle">Actualiza los datos de la categoría seleccionada.</p>
                </div>
                <div class="panel-body">
                    <form action="${pageContext.request.contextPath}/CategoriaServlet" method="post" class="needs-validation" novalidate>
                        <input type="hidden" name="accion" value="actualizar">
                        <input type="hidden" name="idCategoria" value="${categoria.idCategoria}">

                        <div class="mb-3">
                            <label for="nombreCategoria" class="form-label">Nombre de categoría</label>
                            <input type="text" class="form-control" id="nombreCategoria" name="nombreCategoria" value="${categoria.nombreCategoria}" maxlength="100" required>
                            <div class="invalid-feedback">Ingresa el nombre de la categoría.</div>
                        </div>

                        <div class="mb-3">
                            <label for="descripcion" class="form-label">Descripción</label>
                            <textarea class="form-control" id="descripcion" name="descripcion" rows="4" maxlength="255">${categoria.descripcion}</textarea>
                        </div>

                        <div class="mb-4">
                            <label for="estado" class="form-label">Estado</label>
                            <select class="form-select" id="estado" name="estado" required>
                                <option value="activo" ${categoria.estado == 'activo' ? 'selected' : ''}>activo</option>
                                <option value="inactivo" ${categoria.estado == 'inactivo' ? 'selected' : ''}>inactivo</option>
                            </select>
                        </div>

                        <div class="d-flex gap-2 justify-content-end">
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/CategoriaServlet">Cancelar</a>
                            <button class="btn btn-resto" type="submit">
                                <i class="bi bi-save me-1"></i> Actualizar
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


