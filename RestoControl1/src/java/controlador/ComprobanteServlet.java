package controlador;

import dao.PagoDAO;
import dao.PedidoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import modelo.DetallePedido;
import modelo.Pago;
import modelo.Pedido;
import util.ComprobantePdfService;

@WebServlet(name = "ComprobanteServlet", urlPatterns = {"/ComprobanteServlet"})
public class ComprobanteServlet extends HttpServlet {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Integer> idsPagos = obtenerIdsPagos(request);
        if (idsPagos.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Debe indicar al menos un pago.");
            return;
        }

        if (idsPagos.size() == 1) {
            enviarPdfUnico(request, response, idsPagos.get(0));
            return;
        }

        enviarZip(response, idsPagos);
    }

    private void enviarPdfUnico(HttpServletRequest request, HttpServletResponse response, int idPago) throws IOException {
        ComprobanteGenerado comprobante = generarComprobante(idPago);
        if (comprobante == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontro el comprobante solicitado.");
            return;
        }

        boolean inline = "true".equalsIgnoreCase(request.getParameter("inline"));
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                (inline ? "inline" : "attachment") + "; filename=\"" + comprobante.nombreArchivo + "\"");
        response.setContentLength(comprobante.contenido.length);
        response.getOutputStream().write(comprobante.contenido);
        response.flushBuffer();
    }

    private void enviarZip(HttpServletResponse response, List<Integer> idsPagos) throws IOException {
        Map<String, byte[]> archivos = new LinkedHashMap<>();
        for (int idPago : idsPagos) {
            ComprobanteGenerado comprobante = generarComprobante(idPago);
            if (comprobante != null) {
                archivos.put(comprobante.nombreArchivo, comprobante.contenido);
            }
        }

        if (archivos.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron comprobantes para descargar.");
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"comprobantes.zip\"");

        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
            for (Map.Entry<String, byte[]> entrada : archivos.entrySet()) {
                zip.putNextEntry(new ZipEntry(entrada.getKey()));
                zip.write(entrada.getValue());
                zip.closeEntry();
            }
            zip.finish();
        }
        response.flushBuffer();
    }

    private ComprobanteGenerado generarComprobante(int idPago) {
        Pago pago = pagoDAO.buscarPorId(idPago);
        if (pago == null) {
            return null;
        }

        Pedido pedido = pedidoDAO.buscarPorId(pago.getIdPedido());
        if (pedido == null) {
            return null;
        }

        List<DetallePedido> detalles = pedidoDAO.listarDetalles(pago.getIdPedido());
        int totalPartes = pagoDAO.contarPagosPorPedido(pago.getIdPedido());
        byte[] contenido = ComprobantePdfService.generar(pedido, pago, detalles, totalPartes);
        return new ComprobanteGenerado(ComprobantePdfService.nombreArchivo(pago), contenido);
    }

    private List<Integer> obtenerIdsPagos(HttpServletRequest request) {
        List<Integer> ids = new ArrayList<>();
        String idsParametro = request.getParameter("ids");
        if (idsParametro != null && !idsParametro.trim().isEmpty()) {
            for (String parte : idsParametro.split(",")) {
                int id = convertirEntero(parte);
                if (id > 0 && !ids.contains(id)) {
                    ids.add(id);
                }
            }
            return ids;
        }

        int idPago = convertirEntero(request.getParameter("idPago"));
        if (idPago > 0) {
            ids.add(idPago);
        }
        return ids;
    }

    private int convertirEntero(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private static final class ComprobanteGenerado {

        private final String nombreArchivo;
        private final byte[] contenido;

        private ComprobanteGenerado(String nombreArchivo, byte[] contenido) {
            this.nombreArchivo = nombreArchivo;
            this.contenido = contenido;
        }
    }
}
