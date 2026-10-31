package controlador;

import dao.ReporteDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import modelo.ReporteDashboard;
import util.ReportePdfService;

@WebServlet(name = "ReporteServlet", urlPatterns = {"/ReporteServlet"})
public class ReporteServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if ("exportar".equalsIgnoreCase(accion)) {
            exportarReporte(request, response);
            return;
        }

        String tipo = request.getParameter("tipo");
        String desde = request.getParameter("desde");
        String hasta = request.getParameter("hasta");
        String horaDesde = request.getParameter("horaDesde");
        String horaHasta = request.getParameter("horaHasta");

        if (desde == null || desde.trim().isEmpty()) {
            desde = LocalDate.now().withDayOfMonth(1).toString();
        }
        if (hasta == null || hasta.trim().isEmpty()) {
            hasta = LocalDate.now().toString();
        }
        if (horaDesde == null || horaDesde.trim().isEmpty()) {
            horaDesde = "00:00";
        }
        if (horaHasta == null || horaHasta.trim().isEmpty()) {
            horaHasta = "23:59";
        }

        ReporteDashboard dashboard = reporteDAO.generarDashboard(tipo, desde, hasta, horaDesde, horaHasta);
        request.setAttribute("reporte", dashboard);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/vistas/reportes/listar.jsp");
        dispatcher.forward(request, response);
    }

    private void exportarReporte(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tipo = request.getParameter("tipo");
        String desde = request.getParameter("desde");
        String hasta = request.getParameter("hasta");
        String horaDesde = request.getParameter("horaDesde");
        String horaHasta = request.getParameter("horaHasta");
        String formato = request.getParameter("formato");

        ReporteDashboard dashboard = reporteDAO.generarDashboard(tipo, desde, hasta, horaDesde, horaHasta);
        boolean excel = "excel".equalsIgnoreCase(formato) || "csv".equalsIgnoreCase(formato);

        if (excel) {
            String nombre = "reporte_" + dashboard.getTipo() + "_" + dashboard.getDesde() + "_" + dashboard.getHasta() + ".csv";
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
            response.getOutputStream().write(ReportePdfService.generarCsv(dashboard));
        } else {
            String nombre = "reporte_" + dashboard.getTipo() + "_" + dashboard.getDesde() + "_" + dashboard.getHasta() + ".pdf";
            byte[] pdf = ReportePdfService.generar(dashboard);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
        }
        response.flushBuffer();
    }
}
