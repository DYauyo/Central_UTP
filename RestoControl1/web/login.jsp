<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}" scope="session"/>
<fmt:setBundle basename="i18n.messages"/>
<c:if test="${not empty sessionScope.usuarioAutenticado}">
    <c:redirect url="/DashboardServlet"/>
</c:if>
<!DOCTYPE html>
<html lang="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><fmt:message key="login.page_title"/> | <fmt:message key="app.name"/></title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body class="login-page">
        <main class="login-card">
            <div class="login-topline">
                <div class="login-brand">
                    <span class="brand-badge">C</span>
                    <span>
                        <strong><fmt:message key="app.name"/></strong>
                        <span><fmt:message key="app.restaurant_panel"/></span>
                    </span>
                </div>
                <jsp:include page="/vistas/layout/language_switch.jsp"/>
            </div>

            <h1 class="section-title"><fmt:message key="login.title"/></h1>
            <p class="section-subtitle mb-4"><fmt:message key="login.subtitle"/></p>

            <c:if test="${param.mensaje == 'cerrado'}">
                <div class="alert alert-success"><fmt:message key="login.message.closed"/></div>
            </c:if>
            <c:if test="${param.mensaje == 'expirada'}">
                <div class="alert alert-warning"><fmt:message key="login.message.expired"/></div>
            </c:if>
            <c:if test="${param.mensaje == 'sesion'}">
                <div class="alert alert-warning"><fmt:message key="login.message.session"/></div>
            </c:if>
            <c:if test="${not empty errorKey}">
                <div class="alert alert-danger"><fmt:message key="${errorKey}"/></div>
            </c:if>
            <c:if test="${empty errorKey and not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/LoginServlet" method="post" class="needs-validation" novalidate>
                <div class="mb-3">
                    <label for="username" class="form-label"><fmt:message key="app.user"/></label>
                    <input type="text" class="form-control" id="username" name="username" value="${username}" maxlength="100" required autofocus>
                    <div class="invalid-feedback"><fmt:message key="login.validation.username"/></div>
                </div>

                <div class="mb-4">
                    <label for="password" class="form-label"><fmt:message key="app.password"/></label>
                    <input type="password" class="form-control" id="password" name="password" maxlength="255" required>
                    <div class="invalid-feedback"><fmt:message key="login.validation.password"/></div>
                </div>

                <button class="btn btn-resto w-100" type="submit">
                    <i class="bi bi-box-arrow-in-right me-1"></i> <fmt:message key="login.submit"/>
                </button>
            </form>
        </main>

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

