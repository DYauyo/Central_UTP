<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}" scope="session"/>
<fmt:setBundle basename="i18n.messages"/>
<c:set var="idiomaActual" value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}"/>
<c:set var="idiomaEtiqueta" value="${idiomaActual == 'en' ? 'EN' : idiomaActual == 'it' ? 'IT' : idiomaActual == 'pt' ? 'PT' : 'ES'}"/>
<c:set var="volver" value="${pageContext.request.requestURI}${empty pageContext.request.queryString ? '' : '?'}${empty pageContext.request.queryString ? '' : pageContext.request.queryString}"/>
<fmt:message key="language.selector" var="languageSelector"/>
<fmt:message key="language.es" var="languageEs"/>
<fmt:message key="language.en" var="languageEn"/>
<fmt:message key="language.it" var="languageIt"/>
<fmt:message key="language.pt" var="languagePt"/>
<form class="language-switch dropdown" action="${pageContext.request.contextPath}/IdiomaServlet" method="post" aria-label="${languageSelector}">
    <input type="hidden" name="volver" value="${volver}">
    <button class="language-trigger" type="button" data-bs-toggle="dropdown" aria-expanded="false" title="${languageSelector}" aria-label="${languageSelector}">
        <i class="bi bi-globe2"></i>
        <span class="visually-hidden">${idiomaEtiqueta}</span>
    </button>
    <ul class="dropdown-menu language-dropdown">
        <li>
            <button class="dropdown-item ${idiomaActual == 'es' ? 'active' : ''}" type="submit" name="idioma" value="es">
                <span class="language-code">ES</span>
                <span>${languageEs}</span>
                <i class="bi bi-check2 language-check"></i>
            </button>
        </li>
        <li>
            <button class="dropdown-item ${idiomaActual == 'en' ? 'active' : ''}" type="submit" name="idioma" value="en">
                <span class="language-code">EN</span>
                <span>${languageEn}</span>
                <i class="bi bi-check2 language-check"></i>
            </button>
        </li>
        <li>
            <button class="dropdown-item ${idiomaActual == 'it' ? 'active' : ''}" type="submit" name="idioma" value="it">
                <span class="language-code">IT</span>
                <span>${languageIt}</span>
                <i class="bi bi-check2 language-check"></i>
            </button>
        </li>
        <li>
            <button class="dropdown-item ${idiomaActual == 'pt' ? 'active' : ''}" type="submit" name="idioma" value="pt">
                <span class="language-code">PT</span>
                <span>${languagePt}</span>
                <i class="bi bi-check2 language-check"></i>
            </button>
        </li>
    </ul>
</form>
