<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}" scope="session"/>
<fmt:setBundle basename="i18n.messages"/>
<fmt:message key="salon.search" var="salonSearch"/>
<c:if test="${empty sessionScope.usuarioAutenticado}">
    <c:redirect url="/LoginServlet"/>
</c:if>
<c:if test="${empty requestScope.totalMesas}">
    <c:redirect url="/DashboardServlet"/>
</c:if>
<!DOCTYPE html>
<html lang="${empty sessionScope.idioma ? 'es' : sessionScope.idioma}">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><fmt:message key="salon.title"/> | <fmt:message key="app.name"/></title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link href="${pageContext.request.contextPath}/assets/css/estilos.css?v=20260929-tema-central" rel="stylesheet">
    </head>
    <body class="salon-page">
        <div class="app-layout">
            <jsp:include page="/vistas/layout/sidebar.jsp">
                <jsp:param name="moduloActivo" value="mesas"/>
            </jsp:include>

            <div class="app-main">
                <jsp:include page="/vistas/layout/header.jsp">
                    <jsp:param name="tituloHeaderKey" value="salon.title"/>
                    <jsp:param name="subtituloHeaderKey" value="salon.subtitle"/>
                    <jsp:param name="tituloHeaderIcon" value="bi-shop-window"/>
                </jsp:include>

                <main class="content-shell salon-shell">
                    <c:if test="${param.mensaje == 'sin_permiso'}">
                        <div class="alert alert-warning"><fmt:message key="salon.permission.denied"/></div>
                    </c:if>
                    <c:if test="${param.mensaje == 'reserva_registrada'}">
                        <div class="alert alert-success alert-dismissible fade show auto-dismiss" role="alert">
                            Reserva registrada correctamente.
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                        </div>
                    </c:if>

                    <div class="pos-toolbar">
                        <label class="pos-search">
                            <i class="bi bi-search"></i>
                            <input type="search" placeholder="${salonSearch}">
                        </label>
                        <div class="d-flex flex-wrap gap-2 justify-content-lg-end">
                            <span class="pos-pill active"><i class="bi bi-shop"></i> <fmt:message key="salon.local"/></span>
                            <span class="pos-pill"><i class="bi bi-clock"></i> <fmt:message key="salon.shift"/></span>
                            <button class="pos-pill join-table-trigger" type="button" data-bs-toggle="modal" data-bs-target="#modalUnirMesas">
                                <i class="bi bi-link-45deg"></i> Unir mesas
                            </button>
                        </div>
                    </div>

                    <div class="pos-chip-row">
                        <span class="pos-pill active"><fmt:message key="salon.all"/> <small>${totalMesas}</small></span>
                        <div class="dropdown">
                            <button class="pos-pill salon-area-select" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-grid"></i>
                                <span data-area-label><fmt:message key="salon.main"/></span>
                                <i class="bi bi-chevron-down"></i>
                            </button>
                            <ul class="dropdown-menu salon-area-menu">
                                <li><button class="dropdown-item active" type="button" data-area-option data-area-target="salonPrincipal"><fmt:message key="salon.main"/></button></li>
                                <li><button class="dropdown-item" type="button" data-area-option data-area-target="segundoPiso"><fmt:message key="salon.second_floor"/></button></li>
                            </ul>
                        </div>
                        <span class="pos-pill"><i class="bi bi-person-check"></i> <fmt:message key="salon.occupied"/> <small>${mesasOcupadas}</small></span>
                        <span class="pos-pill"><i class="bi bi-bag"></i> <fmt:message key="salon.takeaway"/> <small>${pedidosParaLlevar}</small></span>
                        <span class="pos-pill"><i class="bi bi-calendar-check"></i> Reservas <small>${totalReservas}</small></span>
                    </div>

                    <section class="pos-screen salon-full">
                        <div class="pos-grid salon-grid" id="salonPrincipal" data-salon-area>
                            <a class="pos-tile tile-pink action-tile" href="#" data-bs-toggle="modal" data-bs-target="#modalDelivery">
                                <span class="tile-icon"><i class="bi bi-plus-lg"></i> <i class="bi bi-scooter"></i></span>
                                <span class="tile-label"><fmt:message key="salon.delivery"/></span>
                            </a>
                            <a class="pos-tile tile-cyan action-tile" href="#" data-bs-toggle="modal" data-bs-target="#modalParaLlevar">
                                <span class="tile-icon"><i class="bi bi-plus-lg"></i> <i class="bi bi-bag"></i></span>
                                <span class="tile-label"><fmt:message key="salon.takeaway"/></span>
                            </a>
                            <a class="pos-tile tile-orange action-tile" href="#" data-bs-toggle="modal" data-bs-target="#modalReserva">
                                <span class="tile-icon"><i class="bi bi-plus-lg"></i> <i class="bi bi-calendar-check"></i></span>
                                <span class="tile-label"><fmt:message key="salon.reserve"/></span>
                            </a>

                            <c:forEach var="mesa" items="${mesas}">
                                <c:choose>
                                    <c:when test="${mesa.ocupada}">
                                        <a class="pos-tile mesa-tile mesa-ocupada ${mesa.claseColor} ${mesa.claseReserva}" href="${pageContext.request.contextPath}/jsf/pedidos.xhtml?idPedido=${mesa.idPedidoAbierto}">
                                            <span class="tile-title">${mesa.numeroMesa}</span>
                                            <c:if test="${not empty mesa.estadoVisual}">
                                                <span class="mesa-state-label ${mesa.listaParaCobro ? 'charge' : mesa.mesaUnida ? 'joined' : 'busy'}">${mesa.estadoVisual}</span>
                                            </c:if>
                                            <span class="tile-receipt">
                                                <span class="tile-amount">S/ <fmt:formatNumber value="${mesa.totalPedido}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                                <span class="tile-waiter">${mesa.mozo}</span>
                                                <span class="tile-time">
                                                    <c:choose>
                                                        <c:when test="${mesa.mesaUnida}">
                                                            Mesas ${mesa.mesasUnidas}
                                                        </c:when>
                                                        <c:otherwise>${mesa.tiempoAbierto}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </span>
                                            <c:if test="${mesa.tieneReservaProxima}">
                                                <span class="reservation-badge ${mesa.claseBadgeReserva}"><i class="bi bi-calendar-event"></i> ${mesa.reservaProxima.horaReserva} · ${mesa.reservaProxima.etiquetaTiempo}</span>
                                            </c:if>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="pos-tile mesa-tile mesa-libre ${mesa.claseColor} ${mesa.claseReserva}" href="#"
                                           data-bs-toggle="modal"
                                           data-bs-target="#modalAbrirMesa"
                                           data-id-mesa="${mesa.idMesa}"
                                           data-numero-mesa="${mesa.numeroMesa}"
                                           data-capacidad="${mesa.capacidad}">
                                            <span class="tile-title">${mesa.numeroMesa}</span>
                                            <span class="tile-meta"><i class="bi bi-people"></i> ${mesa.capacidad}</span>
                                            <c:if test="${mesa.tieneReservaProxima}">
                                                <span class="mesa-state-label reserved ${mesa.claseBadgeReserva}">${mesa.etiquetaReserva}</span>
                                                <span class="reservation-badge ${mesa.claseBadgeReserva}"><i class="bi bi-calendar-event"></i> ${mesa.reservaProxima.horaReserva} · ${mesa.reservaProxima.etiquetaTiempo}</span>
                                            </c:if>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>
                        <div class="pos-grid salon-grid d-none" id="segundoPiso" data-salon-area>
                            <c:forEach var="mesa" items="${mesasSegundoPiso}">
                                <c:choose>
                                    <c:when test="${mesa.ocupada}">
                                        <a class="pos-tile mesa-tile mesa-ocupada ${mesa.claseColor} ${mesa.claseReserva}" href="${pageContext.request.contextPath}/jsf/pedidos.xhtml?idPedido=${mesa.idPedidoAbierto}">
                                            <span class="tile-title">${mesa.numeroMesa}</span>
                                            <c:if test="${not empty mesa.estadoVisual}">
                                                <span class="mesa-state-label ${mesa.listaParaCobro ? 'charge' : mesa.mesaUnida ? 'joined' : 'busy'}">${mesa.estadoVisual}</span>
                                            </c:if>
                                            <span class="tile-receipt">
                                                <span class="tile-amount">S/ <fmt:formatNumber value="${mesa.totalPedido}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                                <span class="tile-waiter">${mesa.mozo}</span>
                                                <span class="tile-time">
                                                    <c:choose>
                                                        <c:when test="${mesa.mesaUnida}">Mesas ${mesa.mesasUnidas}</c:when>
                                                        <c:otherwise>${mesa.tiempoAbierto}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </span>
                                            <c:if test="${mesa.tieneReservaProxima}">
                                                <span class="reservation-badge ${mesa.claseBadgeReserva}"><i class="bi bi-calendar-event"></i> ${mesa.reservaProxima.horaReserva} · ${mesa.reservaProxima.etiquetaTiempo}</span>
                                            </c:if>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="pos-tile mesa-tile mesa-libre ${mesa.claseColor} ${mesa.claseReserva}" href="#"
                                           data-bs-toggle="modal"
                                           data-bs-target="#modalAbrirMesa"
                                           data-id-mesa="${mesa.idMesa}"
                                           data-numero-mesa="${mesa.numeroMesa}"
                                           data-capacidad="${mesa.capacidad}">
                                            <span class="tile-title">${mesa.numeroMesa}</span>
                                            <span class="tile-meta"><i class="bi bi-people"></i> ${mesa.capacidad}</span>
                                            <c:if test="${mesa.tieneReservaProxima}">
                                                <span class="mesa-state-label reserved ${mesa.claseBadgeReserva}">${mesa.etiquetaReserva}</span>
                                                <span class="reservation-badge ${mesa.claseBadgeReserva}"><i class="bi bi-calendar-event"></i> ${mesa.reservaProxima.horaReserva} · ${mesa.reservaProxima.etiquetaTiempo}</span>
                                            </c:if>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>
                    </section>
                </main>
            </div>
        </div>

        <div class="modal fade resto-modal modal-fast-order" id="modalDelivery" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-fast-order-dialog">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/OperacionServlet">
                        <input type="hidden" name="accion" value="abrirDelivery">
                        <div class="modal-header resto-modal-head">
                            <div class="resto-modal-title">
                                <span>Pedido rapido</span>
                                <h2>Delivery</h2>
                            </div>
                            <span class="resto-modal-icon"><i class="bi bi-scooter"></i></span>
                        </div>
                        <div class="modal-body fast-order-body">
                            <div class="fast-order-hero delivery">
                                <i class="bi bi-geo-alt"></i>
                                <div>
                                    <strong>Datos de entrega</strong>
                                    <span>Registra al cliente y continua a la toma de pedido.</span>
                                </div>
                            </div>
                            <label class="form-label">Cliente</label>
                            <input class="form-control" name="cliente" maxlength="120" placeholder="Ej. Ana Torres" required>
                            <label class="form-label">Telefono</label>
                            <input class="form-control" name="telefono" maxlength="30" placeholder="Ej. 987654321" required>
                            <label class="form-label">Direccion</label>
                            <input class="form-control" name="direccion" maxlength="255" placeholder="Av. Principal 123, distrito" required>
                            <label class="form-label">Referencia</label>
                            <input class="form-control" name="referencia" maxlength="160" placeholder="Frente al parque, puerta negra...">
                            <label class="form-label">Observacion</label>
                            <textarea class="form-control" name="observacion" rows="3" maxlength="180" placeholder="Indicaciones del pedido o entrega"></textarea>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-resto-primary"><i class="bi bi-arrow-right-circle"></i> Continuar con pedido</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="modal fade resto-modal modal-fast-order" id="modalParaLlevar" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-fast-order-dialog">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/OperacionServlet">
                        <input type="hidden" name="accion" value="abrirParaLlevar">
                        <div class="modal-header resto-modal-head">
                            <div class="resto-modal-title">
                                <span>Pedido rapido</span>
                                <h2>Para llevar</h2>
                            </div>
                            <span class="resto-modal-icon"><i class="bi bi-bag"></i></span>
                        </div>
                        <div class="modal-body fast-order-body">
                            <div class="fast-order-hero takeaway">
                                <i class="bi bi-clock-history"></i>
                                <div>
                                    <strong>Datos de recojo</strong>
                                    <span>El pedido se arma igual que una mesa, pero sin ocupar salon.</span>
                                </div>
                            </div>
                            <label class="form-label">Cliente</label>
                            <input class="form-control" name="cliente" maxlength="120" placeholder="Ej. Miguel Diaz" required>
                            <label class="form-label">Telefono</label>
                            <input class="form-control" name="telefono" maxlength="30" placeholder="Opcional">
                            <label class="form-label">Hora estimada de recojo</label>
                            <input class="form-control" name="horaRecojo" type="datetime-local">
                            <label class="form-label">Observacion</label>
                            <textarea class="form-control" name="observacion" rows="3" maxlength="180" placeholder="Ej. Cubiertos, salsas aparte..."></textarea>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-resto-primary"><i class="bi bi-arrow-right-circle"></i> Continuar con pedido</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="modal fade resto-modal modal-fast-order" id="modalReserva" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-fast-order-dialog">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/OperacionServlet">
                        <input type="hidden" name="accion" value="registrarReserva">
                        <div class="modal-header resto-modal-head">
                            <div class="resto-modal-title">
                                <span>Programacion</span>
                                <h2>Registrar reserva</h2>
                            </div>
                            <span class="resto-modal-icon"><i class="bi bi-calendar-check"></i></span>
                        </div>
                        <div class="modal-body fast-order-body">
                            <div class="fast-order-hero reserve">
                                <i class="bi bi-bell"></i>
                                <div>
                                    <strong>La mesa avisara cuando se acerque la hora</strong>
                                    <span>30 minutos antes se marca naranja; 10 minutos antes se vuelve urgente.</span>
                                </div>
                            </div>
                            <label class="form-label">Cliente</label>
                            <input class="form-control" name="cliente" maxlength="120" placeholder="Ej. Familia Herrera" required>
                            <label class="form-label">Telefono</label>
                            <input class="form-control" name="telefono" maxlength="30" placeholder="Ej. 999888777" required>
                            <div class="fast-order-two-cols">
                                <div>
                                    <label class="form-label">Personas</label>
                                    <input class="form-control" name="cantidadPersonas" type="number" min="1" max="30" value="4" required>
                                </div>
                                <div>
                                    <label class="form-label">Mesa a reservar</label>
                                    <select class="form-select" name="idMesa" required>
                                        <option value="" disabled selected>Selecciona una mesa</option>
                                        <c:forEach var="mesa" items="${mesas}">
                                            <option value="${mesa.idMesa}">Mesa ${mesa.numeroMesa} · ${mesa.capacidad} pers.</option>
                                        </c:forEach>
                                        <c:forEach var="mesa" items="${mesasSegundoPiso}">
                                            <option value="${mesa.idMesa}">Mesa ${mesa.numeroMesa} · ${mesa.capacidad} pers.</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <label class="form-label">Fecha y hora</label>
                            <input class="form-control" name="fechaReserva" type="datetime-local" required>
                            <label class="form-label">Observacion</label>
                            <textarea class="form-control" name="observacion" rows="3" maxlength="180" placeholder="Ej. cumpleaños, silla de bebe, ventana..."></textarea>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-resto-primary"><i class="bi bi-calendar-plus"></i> Registrar reserva</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="modal fade resto-modal modal-open-table" id="modalAbrirMesa" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-open-table-dialog">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/OperacionServlet">
                        <input type="hidden" name="accion" value="abrirMesa">
                        <input type="hidden" name="idMesa" id="aperturaIdMesa">
                        <div class="modal-header resto-modal-head">
                            <div class="resto-modal-title">
                                <span>Apertura de mesa</span>
                                <h2 id="aperturaTitulo">Mesa</h2>
                            </div>
                            <span class="resto-modal-icon"><i class="bi bi-table"></i></span>
                        </div>
                        <div class="modal-body open-table-modal-body">
                            <section class="open-table-block">
                                <label class="form-label" for="aperturaComensales">¿Cuantos comensales se encuentran en la mesa?</label>
                                <input class="form-control open-table-number" id="aperturaComensales" name="comensales" type="number" min="1" max="20" value="1" required>
                                <div class="open-table-keypad" aria-label="Teclado de comensales">
                                    <button type="button" data-key="1">1</button>
                                    <button type="button" data-key="2">2</button>
                                    <button type="button" data-key="3">3</button>
                                    <button type="button" data-key="4">4</button>
                                    <button type="button" data-key="5">5</button>
                                    <button type="button" data-key="6">6</button>
                                    <button type="button" data-key="7">7</button>
                                    <button type="button" data-key="8">8</button>
                                    <button type="button" data-key="9">9</button>
                                    <button type="button" data-key="0" class="wide">0</button>
                                    <button type="button" data-key="back"><i class="bi bi-backspace"></i></button>
                                </div>
                            </section>

                            <section class="open-table-block">
                                <label class="form-label" for="aperturaCliente">Cliente o referencia opcional</label>
                                <input class="form-control" id="aperturaCliente" name="cliente" maxlength="120" placeholder="Ej. Familia Perez, reserva 2:00 PM">
                            </section>

                            <section class="open-table-block">
                                <label class="form-label" for="aperturaObservacion">Observacion inicial</label>
                                <textarea class="form-control" id="aperturaObservacion" name="observacion" maxlength="180" rows="3" placeholder="Ej. unir con otra mesa despues, silla de bebe, sin apuro..."></textarea>
                            </section>

                            <div class="open-table-join-note compact">
                                <i class="bi bi-info-circle"></i>
                                <span>Para grupos grandes usa el boton Unir mesas ubicado arriba del salon.</span>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-resto-primary">
                                <i class="bi bi-arrow-right-circle"></i> Confirmar y tomar pedido
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="modal fade resto-modal modal-join-table" id="modalUnirMesas" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-join-table-dialog">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/OperacionServlet">
                        <input type="hidden" name="accion" value="unirMesas">
                        <div class="modal-header resto-modal-head">
                            <div class="resto-modal-title">
                                <span>Selecciona dos o mas mesas libres</span>
                                <h2>Unir mesas disponibles</h2>
                            </div>
                            <span class="resto-modal-icon"><i class="bi bi-link-45deg"></i></span>
                        </div>
                        <div class="modal-body join-table-modal-body">
                            <div class="join-table-summary">
                                <i class="bi bi-people"></i>
                                <div>
                                    <strong id="joinTableCount">Selecciona 2 o mas mesas libres</strong>
                                    <span>Se creara un solo pedido y todas las mesas elegidas quedaran ocupadas.</span>
                                </div>
                            </div>
                            <div class="join-table-grid">
                                <c:forEach var="mesa" items="${mesas}">
                                    <c:if test="${not mesa.ocupada}">
                                        <label class="join-table-option">
                                            <input type="checkbox" name="mesasUnidas" value="${mesa.idMesa}" data-join-table data-capacity="${mesa.capacidad}">
                                            <span>
                                                <strong>Mesa ${mesa.numeroMesa}</strong>
                                                <small>${mesa.capacidad} personas</small>
                                            </span>
                                        </label>
                                    </c:if>
                                </c:forEach>
                                <c:forEach var="mesa" items="${mesasSegundoPiso}">
                                    <c:if test="${not mesa.ocupada}">
                                        <label class="join-table-option">
                                            <input type="checkbox" name="mesasUnidas" value="${mesa.idMesa}" data-join-table data-capacity="${mesa.capacidad}">
                                            <span>
                                                <strong>Mesa ${mesa.numeroMesa}</strong>
                                                <small>${mesa.capacidad} personas</small>
                                            </span>
                                        </label>
                                    </c:if>
                                </c:forEach>
                            </div>
                            <div class="join-table-fields">
                                <div>
                                    <label class="form-label">Comensales del grupo</label>
                                    <input class="form-control" id="joinComensales" name="comensales" type="number" min="1" max="40" value="1" required>
                                </div>
                                <div>
                                    <label class="form-label">Cliente o referencia</label>
                                    <input class="form-control" name="cliente" maxlength="120" placeholder="Ej. Familia Ramos">
                                </div>
                            </div>
                            <label class="form-label">Observacion</label>
                            <textarea class="form-control" name="observacion" rows="3" maxlength="180" placeholder="Ej. grupo grande, unir sillas, cumpleaños..."></textarea>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="submit" class="btn btn-resto-primary" id="btnPrepararUnion" disabled>
                                <i class="bi bi-check2-circle"></i> Unir y tomar pedido
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            document.querySelectorAll('[data-area-option]').forEach(function (button) {
                button.addEventListener('click', function () {
                    var label = document.querySelector('[data-area-label]');
                    if (label) {
                        label.textContent = button.textContent.trim();
                    }
                    document.querySelectorAll('[data-salon-area]').forEach(function (area) {
                        area.classList.toggle('d-none', area.id !== button.dataset.areaTarget);
                    });
                    button.closest('.dropdown-menu').querySelectorAll('.dropdown-item').forEach(function (item) {
                        item.classList.toggle('active', item === button);
                    });
                });
            });

            const modalAbrirMesa = document.getElementById('modalAbrirMesa');
            const inputIdMesa = document.getElementById('aperturaIdMesa');
            const inputComensales = document.getElementById('aperturaComensales');
            const tituloApertura = document.getElementById('aperturaTitulo');
            let reemplazarComensales = true;

            modalAbrirMesa?.addEventListener('show.bs.modal', function (event) {
                const trigger = event.relatedTarget;
                const numeroMesa = trigger?.getAttribute('data-numero-mesa') || '';
                const idMesa = trigger?.getAttribute('data-id-mesa') || '';
                inputIdMesa.value = idMesa;
                inputComensales.value = '1';
                reemplazarComensales = true;
                tituloApertura.textContent = 'Mesa ' + numeroMesa;
            });

            inputComensales?.addEventListener('input', function () {
                reemplazarComensales = false;
            });

            document.querySelectorAll('.open-table-keypad [data-key]').forEach(function (button) {
                button.addEventListener('click', function () {
                    const key = button.getAttribute('data-key');
                    let value = reemplazarComensales ? '' : (inputComensales.value || '');

                    if (key === 'back') {
                        value = value.slice(0, -1);
                        if (value === '') {
                            inputComensales.value = '';
                            reemplazarComensales = true;
                            return;
                        }
                    } else {
                        value = value + key;
                    }

                    reemplazarComensales = false;
                    const numberValue = Math.max(1, Math.min(20, parseInt(value, 10)));
                    inputComensales.value = numberValue;
                });
            });

            const joinChecks = Array.from(document.querySelectorAll('[data-join-table]'));
            const joinCount = document.getElementById('joinTableCount');
            const btnPrepararUnion = document.getElementById('btnPrepararUnion');
            const joinComensales = document.getElementById('joinComensales');

            function actualizarUnionMesas() {
                const seleccionadas = joinChecks.filter(function (check) { return check.checked; });
                const capacidad = seleccionadas.reduce(function (total, check) {
                    return total + parseInt(check.getAttribute('data-capacity') || '0', 10);
                }, 0);
                if (seleccionadas.length >= 2) {
                    joinCount.textContent = seleccionadas.length + ' mesas seleccionadas · capacidad ' + capacidad + ' personas';
                    btnPrepararUnion.disabled = false;
                    joinComensales.value = capacidad;
                } else {
                    joinCount.textContent = 'Selecciona 2 o mas mesas libres';
                    btnPrepararUnion.disabled = true;
                    joinComensales.value = Math.max(1, capacidad);
                }
            }

            joinChecks.forEach(function (check) {
                check.addEventListener('change', actualizarUnionMesas);
            });

            document.querySelectorAll('.auto-dismiss').forEach(function (alert) {
                setTimeout(function () {
                    alert.classList.add('alert-fading');
                    setTimeout(function () {
                        bootstrap.Alert.getOrCreateInstance(alert).close();
                    }, 360);
                }, 4200);
            });
        </script>
    </body>
</html>

