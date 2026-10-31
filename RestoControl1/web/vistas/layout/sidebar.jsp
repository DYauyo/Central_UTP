<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}" scope="session"/>
<fmt:setBundle basename="i18n.messages"/>
<fmt:message key="sidebar.dashboard" var="txtDashboard"/>
<fmt:message key="sidebar.account" var="txtAccount"/>
<fmt:message key="sidebar.kitchen" var="txtKitchen"/>
<fmt:message key="sidebar.cash" var="txtCash"/>
<fmt:message key="sidebar.reports" var="txtReports"/>
<fmt:message key="sidebar.menu" var="txtMenu"/>
<fmt:message key="sidebar.categories" var="txtCategories"/>
<fmt:message key="sidebar.products" var="txtProducts"/>
<fmt:message key="sidebar.supplies" var="txtSupplies"/>
<fmt:message key="sidebar.users" var="txtUsers"/>
<fmt:message key="app.logout_session" var="txtLogoutSession"/>
<c:set var="homePath" value="/DashboardServlet"/>
<c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 6}">
    <c:set var="homePath" value="/PanelServlet"/>
</c:if>
<c:if test="${sessionScope.idRol == 2}">
    <c:set var="homePath" value="/jsf/caja.xhtml"/>
</c:if>
<c:if test="${sessionScope.idRol == 4}">
    <c:set var="homePath" value="/jsf/cocina.xhtml"/>
</c:if>
<c:if test="${sessionScope.idRol == 5}">
    <c:set var="homePath" value="/InsumoServlet"/>
</c:if>
<aside class="app-sidebar">
    <a class="sidebar-brand" href="${pageContext.request.contextPath}${homePath}">
        <span class="brand-badge">C</span>
        <span>
            <strong><fmt:message key="app.name"/></strong>
            <small><fmt:message key="app.admin_panel"/></small>
        </span>
    </a>

    <nav class="sidebar-nav">
        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 6}">
            <a class="sidebar-link ${param.moduloActivo == 'panel' ? 'active' : ''}" href="${pageContext.request.contextPath}/PanelServlet" title="Panel principal">
                <i class="bi bi-speedometer2"></i>
                <span>Panel principal</span>
            </a>
        </c:if>
        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 3}">
            <a class="sidebar-link ${param.moduloActivo == 'mesas' || param.moduloActivo == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/DashboardServlet" title="${txtDashboard}">
                <i class="bi bi-grid-3x3-gap"></i>
                <span><fmt:message key="sidebar.dashboard"/></span>
            </a>
        </c:if>
        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 3}">
            <a class="sidebar-link ${param.moduloActivo == 'pedidos' || param.moduloActivo == 'cuenta' ? 'active' : ''}" href="${pageContext.request.contextPath}/jsf/pedidos.xhtml" title="${txtAccount}">
                <i class="bi bi-clipboard2-check"></i>
                <span><fmt:message key="sidebar.account"/></span>
            </a>
        </c:if>
        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 4}">
            <a class="sidebar-link ${param.moduloActivo == 'cocina' ? 'active' : ''}" href="${pageContext.request.contextPath}/jsf/cocina.xhtml" title="${txtKitchen}">
                <i class="bi bi-list-check"></i>
                <span><fmt:message key="sidebar.kitchen"/></span>
            </a>
        </c:if>
        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 2}">
            <a class="sidebar-link ${param.moduloActivo == 'caja' ? 'active' : ''}" href="${pageContext.request.contextPath}/jsf/caja.xhtml" title="${txtCash}">
                <i class="bi bi-cash-coin"></i>
                <span><fmt:message key="sidebar.cash"/></span>
            </a>
        </c:if>
        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 6}">
            <a class="sidebar-link ${param.moduloActivo == 'reportes' ? 'active' : ''}" href="${pageContext.request.contextPath}/ReporteServlet" title="${txtReports}">
                <i class="bi bi-bar-chart-line"></i>
                <span><fmt:message key="sidebar.reports"/></span>
            </a>
        </c:if>
        <c:if test="${sessionScope.idRol == 1}">
            <a class="sidebar-link ${param.moduloActivo == 'menu' ? 'active' : ''}" href="${pageContext.request.contextPath}/MenuServlet" title="${txtMenu}">
                <i class="bi bi-menu-button-wide"></i>
                <span><fmt:message key="sidebar.menu"/></span>
            </a>
        </c:if>

        <c:if test="${sessionScope.idRol == 1 || sessionScope.idRol == 5}">
            <a class="sidebar-link ${param.moduloActivo == 'insumos' ? 'active' : ''}" href="${pageContext.request.contextPath}/InsumoServlet" title="${txtSupplies}">
                <i class="bi bi-box-seam"></i>
                <span><fmt:message key="sidebar.supplies"/></span>
            </a>
        </c:if>

        <c:if test="${sessionScope.idRol == 1}">
            <a class="sidebar-link ${param.moduloActivo == 'usuarios' ? 'active' : ''}" href="${pageContext.request.contextPath}/UsuarioServlet" title="${txtUsers}">
                <i class="bi bi-people"></i>
                <span><fmt:message key="sidebar.users"/></span>
            </a>
        </c:if>
    </nav>

    <div class="sidebar-footer">
        <a class="sidebar-link logout" href="${pageContext.request.contextPath}/LoginServlet?accion=cerrar" title="${txtLogoutSession}">
            <i class="bi bi-box-arrow-right"></i>
            <span><fmt:message key="app.logout_session"/></span>
        </a>
    </div>
</aside>
