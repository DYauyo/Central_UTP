<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Registrar producto | Central</title>
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
                    <h1 class="section-title">Registrar producto</h1>
                    <p class="section-subtitle">Completa los datos del producto y asócialo a una categoría activa.</p>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>
                    <c:if test="${empty categorias}">
                        <div class="alert alert-warning">
                            Primero debes registrar una categoría activa para crear productos.
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/ProductoServlet" method="post" class="needs-validation" novalidate>
                        <input type="hidden" name="accion" value="insertar">

                        <div class="row g-3">
                            <div class="col-md-8">
                                <label for="nombreProducto" class="form-label">Nombre del producto</label>
                                <input type="text" class="form-control" id="nombreProducto" name="nombreProducto" value="${producto.nombreProducto}" maxlength="150" required>
                                <div class="invalid-feedback">Ingresa el nombre del producto.</div>
                            </div>

                            <div class="col-md-4">
                                <label for="precio" class="form-label">Precio</label>
                                <input type="number" class="form-control" id="precio" name="precio" value="${producto.precio}" min="0" step="0.01" required>
                                <div class="invalid-feedback">Ingresa un precio válido.</div>
                            </div>

                            <div class="col-md-6">
                                <label for="idCategoria" class="form-label">Categoría</label>
                                <select class="form-select" id="idCategoria" name="idCategoria" required>
                                    <option value="">Selecciona una categoría</option>
                                    <c:forEach var="categoria" items="${categorias}">
                                        <option value="${categoria.idCategoria}" ${categoria.idCategoria == producto.idCategoria ? 'selected' : ''}>${categoria.nombreCategoria}</option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">Selecciona una categoría.</div>
                            </div>

                            <div class="col-md-3">
                                <label for="disponibilidad" class="form-label">Disponibilidad</label>
                                <select class="form-select" id="disponibilidad" name="disponibilidad" required>
                                    <option value="disponible" ${producto.disponibilidad == 'no disponible' ? '' : 'selected'}>disponible</option>
                                    <option value="no disponible" ${producto.disponibilidad == 'no disponible' ? 'selected' : ''}>no disponible</option>
                                </select>
                            </div>

                            <div class="col-md-3">
                                <label for="estado" class="form-label">Estado</label>
                                <select class="form-select" id="estado" name="estado" required>
                                    <option value="activo" ${producto.estado == 'inactivo' ? '' : 'selected'}>activo</option>
                                    <option value="inactivo" ${producto.estado == 'inactivo' ? 'selected' : ''}>inactivo</option>
                                </select>
                            </div>

                            <div class="col-12">
                                <label for="descripcion" class="form-label">Descripción</label>
                                <textarea class="form-control" id="descripcion" name="descripcion" rows="3" maxlength="255">${producto.descripcion}</textarea>
                            </div>

                            <div class="col-12">
                                <label for="imagen" class="form-label">Imagen o referencia</label>
                                <input type="text" class="form-control" id="imagen" name="imagen" value="${producto.imagen}" maxlength="255" placeholder="Ejemplo: imagenes/lomo-saltado.jpg">
                            </div>
                        </div>

                        <div class="d-flex gap-2 justify-content-end mt-4">
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/ProductoServlet">Cancelar</a>
                            <button class="btn btn-resto" type="submit" ${empty categorias ? 'disabled' : ''}>
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


