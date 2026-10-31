<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}" scope="session"/>
<fmt:setBundle basename="i18n.messages"/>
<c:set var="nombreSesion" value="${empty sessionScope.nombreUsuario ? 'Usuario' : sessionScope.nombreUsuario}"/>
<c:set var="nombrePartes" value="${fn:split(nombreSesion, ' ')}"/>
<header class="content-header">
    <div>
        <p class="content-kicker"><fmt:message key="app.name"/></p>
        <h1 class="${not empty param.tituloHeaderIcon ? 'header-title-with-icon' : ''}">
            <c:if test="${not empty param.tituloHeaderIcon}">
                <span class="header-title-icon"><i class="bi ${param.tituloHeaderIcon}"></i></span>
            </c:if>
            <span>
                <c:choose>
                    <c:when test="${not empty param.tituloHeaderKey}">
                        <fmt:message key="${param.tituloHeaderKey}"/>
                    </c:when>
                    <c:otherwise>${param.tituloHeader}</c:otherwise>
                </c:choose>
            </span>
        </h1>
        <p>
            <c:choose>
                <c:when test="${not empty param.subtituloHeaderKey}">
                    <fmt:message key="${param.subtituloHeaderKey}"/>
                </c:when>
                <c:otherwise>${param.subtituloHeader}</c:otherwise>
            </c:choose>
        </p>
    </div>
    <div class="header-actions">
        <button class="theme-trigger" type="button" title="Ajustes" aria-label="Ajustes">
            <i class="bi bi-gear"></i>
        </button>
        <jsp:include page="/vistas/layout/language_switch.jsp"/>
        <span class="header-divider" aria-hidden="true"></span>
        <div class="user-greeting">
            <strong>Hola, ${empty sessionScope.nombreCortoUsuario ? nombrePartes[0] : sessionScope.nombreCortoUsuario}</strong>
            <span>${sessionScope.rolUsuario}</span>
        </div>
        <button class="user-menu-button" type="button" aria-label="Perfil de usuario">
            <span class="user-avatar">
                <i class="bi bi-person-fill"></i>
            </span>
            <i class="bi bi-chevron-down"></i>
        </button>
    </div>
</header>
