package util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import modelo.ReporteCostoDistribucion;
import modelo.ReporteDashboard;
import modelo.ReporteHoraPunta;
import modelo.ReporteMetodoPago;
import modelo.ReporteRentabilidadDia;
import modelo.ReporteVentaFila;

public final class ReportePdfService {

    private ReportePdfService() {
    }

    public static byte[] generar(ReporteDashboard dashboard) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font pequeno = FontFactory.getFont(FontFactory.HELVETICA, 9);

            documento.add(new Paragraph("Central - Reportes", titulo));
            documento.add(new Paragraph("Tipo: " + etiquetaTipo(dashboard.getTipo()), normal));
            documento.add(new Paragraph("Periodo: " + dashboard.getDesde() + " " + dashboard.getHoraDesde()
                    + " al " + dashboard.getHasta() + " " + dashboard.getHoraHasta(), normal));
            documento.add(espacio(12));

            if (dashboard.isTipoVentas()) {
                agregarResumenVentas(documento, dashboard, subtitulo, normal);
                agregarGraficoMetodosPago(documento, dashboard.getMetodosPago(), subtitulo);
                agregarGraficoHorasPunta(documento, dashboard.getHorasPunta(), subtitulo);
                agregarTablaMetodos(documento, dashboard.getMetodosPago(), subtitulo, normal, pequeno);
                agregarTablaHoras(documento, dashboard.getHorasPunta(), subtitulo, normal, pequeno);
                agregarTablaVentas(documento, dashboard.getFilasVentas(), subtitulo, normal, pequeno);
            } else {
                agregarResumenRentabilidad(documento, dashboard, subtitulo, normal);
                agregarGraficoDistribucionCostos(documento, dashboard.getDistribucionCostos(), subtitulo);
                agregarGraficoUtilidadDiaria(documento, dashboard.getRentabilidadDiaria(), subtitulo);
                agregarTablaCostos(documento, dashboard.getDistribucionCostos(), subtitulo, normal, pequeno);
                agregarTablaRentabilidadDiaria(documento, dashboard.getRentabilidadDiaria(), subtitulo, normal, pequeno);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("No se pudo generar el reporte PDF", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("No se pudo cerrar el reporte PDF", e);
        }
    }

    public static byte[] generarCsv(ReporteDashboard dashboard) {
        StringBuilder csv = new StringBuilder();
        csv.append("Central;Reporte;").append(etiquetaTipo(dashboard.getTipo())).append('\n');
        csv.append("Periodo;").append(dashboard.getDesde()).append(';').append(dashboard.getHasta()).append('\n').append('\n');

        if (dashboard.isTipoVentas()) {
            csv.append("Ventas brutas;").append(formatear(dashboard.getVentasBrutas())).append('\n');
            csv.append("Total tickets;").append(dashboard.getTotalTickets()).append('\n');
            csv.append("Ticket promedio;").append(formatear(dashboard.getTicketPromedio())).append('\n').append('\n');
            csv.append("Metodo;Monto;Porcentaje\n");
            for (ReporteMetodoPago metodo : dashboard.getMetodosPago()) {
                csv.append(metodo.getEtiqueta()).append(';')
                        .append(formatear(metodo.getMonto())).append(';')
                        .append(formatear(metodo.getPorcentaje())).append('\n');
            }
            csv.append('\n');
            csv.append("Fecha;Ticket;Mesa;Cliente;Metodo;Total\n");
            for (ReporteVentaFila venta : dashboard.getFilasVentas()) {
                csv.append(venta.getFechaHora()).append(';')
                        .append(venta.getTicket()).append(';')
                        .append(venta.getEtiquetaMesa()).append(';')
                        .append(venta.getCliente()).append(';')
                        .append(venta.getEtiquetaMedio()).append(';')
                        .append(formatear(venta.getTotal())).append('\n');
            }
        } else {
            csv.append("Ventas netas;").append(formatear(dashboard.getVentasNetas())).append('\n');
            csv.append("Utilidad bruta;").append(formatear(dashboard.getUtilidadBruta())).append('\n');
            csv.append("Utilidad neta;").append(formatear(dashboard.getUtilidadNeta())).append('\n');
            csv.append("Margen neto;").append(formatear(dashboard.getMargenNeto())).append('\n').append('\n');
            csv.append("Concepto;Monto;Porcentaje\n");
            for (ReporteCostoDistribucion costo : dashboard.getDistribucionCostos()) {
                csv.append(costo.getConcepto()).append(';')
                        .append(formatear(costo.getMonto())).append(';')
                        .append(formatear(costo.getPorcentaje())).append('\n');
            }
            csv.append('\n');
            csv.append("Fecha;Ventas netas;Costo ventas;Gastos operativos;Gastos personal;Otros gastos;Utilidad neta;Margen neto\n");
            for (ReporteRentabilidadDia dia : dashboard.getRentabilidadDiaria()) {
                csv.append(dia.getFecha()).append(';')
                        .append(formatear(dia.getVentasNetas())).append(';')
                        .append(formatear(dia.getCostoVentas())).append(';')
                        .append(formatear(dia.getGastosOperativos())).append(';')
                        .append(formatear(dia.getGastosPersonal())).append(';')
                        .append(formatear(dia.getOtrosGastos())).append(';')
                        .append(formatear(dia.getUtilidadNeta())).append(';')
                        .append(formatear(dia.getMargenNeto())).append('\n');
            }
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void agregarResumenVentas(Document documento, ReporteDashboard dashboard, Font subtitulo, Font normal)
            throws DocumentException {
        documento.add(new Paragraph("Resumen de ventas", subtitulo));
        documento.add(new Paragraph("Ventas brutas totales: S/ " + formatear(dashboard.getVentasBrutas()), normal));
        documento.add(new Paragraph("Total tickets emitidos: " + dashboard.getTotalTickets(), normal));
        documento.add(new Paragraph("Ticket promedio: S/ " + formatear(dashboard.getTicketPromedio()), normal));
        documento.add(espacio(10));
    }

    private static void agregarResumenRentabilidad(Document documento, ReporteDashboard dashboard, Font subtitulo, Font normal)
            throws DocumentException {
        documento.add(new Paragraph("Resumen de rentabilidad", subtitulo));
        documento.add(new Paragraph("Ventas netas: S/ " + formatear(dashboard.getVentasNetas()), normal));
        documento.add(new Paragraph("Utilidad bruta: S/ " + formatear(dashboard.getUtilidadBruta())
                + " (" + formatear(dashboard.getPorcentajeUtilidadBruta()) + "%)", normal));
        documento.add(new Paragraph("Utilidad neta: S/ " + formatear(dashboard.getUtilidadNeta()), normal));
        documento.add(new Paragraph("Margen neto: " + formatear(dashboard.getMargenNeto()) + "%", normal));
        documento.add(espacio(10));
    }

    private static void agregarGraficoMetodosPago(Document documento, List<ReporteMetodoPago> metodos, Font subtitulo)
            throws DocumentException {
        if (metodos == null || metodos.isEmpty()) {
            return;
        }
        documento.add(new Paragraph("Grafica - Metodos de pago", subtitulo));
        List<String> labels = new java.util.ArrayList<>();
        List<BigDecimal> valores = new java.util.ArrayList<>();
        for (ReporteMetodoPago metodo : metodos) {
            labels.add(metodo.getEtiqueta());
            valores.add(metodo.getMonto());
        }
        documento.add(crearGraficoBarras(labels, valores, "Monto S/"));
        documento.add(espacio(8));
    }

    private static void agregarGraficoHorasPunta(Document documento, List<ReporteHoraPunta> horas, Font subtitulo)
            throws DocumentException {
        if (horas == null || horas.isEmpty()) {
            return;
        }
        documento.add(new Paragraph("Grafica - Horas punta de atencion", subtitulo));
        List<String> labels = new java.util.ArrayList<>();
        List<BigDecimal> valores = new java.util.ArrayList<>();
        for (ReporteHoraPunta hora : horas) {
            labels.add(hora.getEtiqueta());
            valores.add(hora.getMonto());
        }
        documento.add(crearGraficoLinea(labels, valores, "Ventas S/"));
        documento.add(espacio(8));
    }

    private static void agregarGraficoDistribucionCostos(Document documento, List<ReporteCostoDistribucion> costos, Font subtitulo)
            throws DocumentException {
        if (costos == null || costos.isEmpty()) {
            return;
        }
        documento.add(new Paragraph("Grafica - Distribucion de costos", subtitulo));
        List<String> labels = new java.util.ArrayList<>();
        List<BigDecimal> valores = new java.util.ArrayList<>();
        for (ReporteCostoDistribucion costo : costos) {
            labels.add(costo.getConcepto());
            valores.add(costo.getMonto());
        }
        documento.add(crearGraficoBarras(labels, valores, "Monto S/"));
        documento.add(espacio(8));
    }

    private static void agregarGraficoUtilidadDiaria(Document documento, List<ReporteRentabilidadDia> filas, Font subtitulo)
            throws DocumentException {
        if (filas == null || filas.isEmpty()) {
            return;
        }
        documento.add(new Paragraph("Grafica - Utilidad neta por dia", subtitulo));
        List<String> labels = new java.util.ArrayList<>();
        List<BigDecimal> valores = new java.util.ArrayList<>();
        for (ReporteRentabilidadDia fila : filas) {
            labels.add(fila.getFecha());
            valores.add(fila.getUtilidadNeta());
        }
        documento.add(crearGraficoLinea(labels, valores, "Utilidad S/"));
        documento.add(espacio(8));
    }

    private static void agregarTablaMetodos(Document documento, List<ReporteMetodoPago> metodos, Font subtitulo,
            Font normal, Font pequeno) throws DocumentException {
        documento.add(new Paragraph("Metodos de pago", subtitulo));
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        agregarEncabezado(tabla, "Metodo", pequeno);
        agregarEncabezado(tabla, "Monto", pequeno);
        agregarEncabezado(tabla, "%", pequeno);
        for (ReporteMetodoPago metodo : metodos) {
            agregarCelda(tabla, metodo.getEtiqueta(), normal);
            agregarCelda(tabla, "S/ " + formatear(metodo.getMonto()), normal);
            agregarCelda(tabla, formatear(metodo.getPorcentaje()) + "%", normal);
        }
        documento.add(tabla);
        documento.add(espacio(10));
    }

    private static void agregarTablaHoras(Document documento, List<ReporteHoraPunta> horas, Font subtitulo,
            Font normal, Font pequeno) throws DocumentException {
        documento.add(new Paragraph("Horas punta de atencion", subtitulo));
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        agregarEncabezado(tabla, "Hora", pequeno);
        agregarEncabezado(tabla, "Ventas", pequeno);
        for (ReporteHoraPunta hora : horas) {
            agregarCelda(tabla, hora.getEtiqueta(), normal);
            agregarCelda(tabla, "S/ " + formatear(hora.getMonto()), normal);
        }
        documento.add(tabla);
        documento.add(espacio(10));
    }

    private static void agregarTablaVentas(Document documento, List<ReporteVentaFila> ventas, Font subtitulo,
            Font normal, Font pequeno) throws DocumentException {
        documento.add(new Paragraph("Ultimas ventas registradas", subtitulo));
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        agregarEncabezado(tabla, "Fecha", pequeno);
        agregarEncabezado(tabla, "Ticket", pequeno);
        agregarEncabezado(tabla, "Mesa", pequeno);
        agregarEncabezado(tabla, "Cliente", pequeno);
        agregarEncabezado(tabla, "Metodo", pequeno);
        agregarEncabezado(tabla, "Total", pequeno);
        for (ReporteVentaFila venta : ventas) {
            agregarCelda(tabla, venta.getFechaHora(), normal);
            agregarCelda(tabla, venta.getTicket(), normal);
            agregarCelda(tabla, venta.getEtiquetaMesa(), normal);
            agregarCelda(tabla, venta.getCliente(), normal);
            agregarCelda(tabla, venta.getEtiquetaMedio(), normal);
            agregarCelda(tabla, "S/ " + formatear(venta.getTotal()), normal);
        }
        documento.add(tabla);
    }

    private static void agregarTablaCostos(Document documento, List<ReporteCostoDistribucion> costos, Font subtitulo,
            Font normal, Font pequeno) throws DocumentException {
        documento.add(new Paragraph("Distribucion de costos", subtitulo));
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        agregarEncabezado(tabla, "Concepto", pequeno);
        agregarEncabezado(tabla, "Monto", pequeno);
        agregarEncabezado(tabla, "%", pequeno);
        for (ReporteCostoDistribucion costo : costos) {
            agregarCelda(tabla, costo.getConcepto(), normal);
            agregarCelda(tabla, "S/ " + formatear(costo.getMonto()), normal);
            agregarCelda(tabla, formatear(costo.getPorcentaje()) + "%", normal);
        }
        documento.add(tabla);
        documento.add(espacio(10));
    }

    private static void agregarTablaRentabilidadDiaria(Document documento, List<ReporteRentabilidadDia> filas,
            Font subtitulo, Font normal, Font pequeno) throws DocumentException {
        documento.add(new Paragraph("Resumen de rentabilidad por dia", subtitulo));
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        agregarEncabezado(tabla, "Fecha", pequeno);
        agregarEncabezado(tabla, "Ventas netas", pequeno);
        agregarEncabezado(tabla, "Costo ventas", pequeno);
        agregarEncabezado(tabla, "Utilidad neta", pequeno);
        agregarEncabezado(tabla, "Margen", pequeno);
        for (ReporteRentabilidadDia fila : filas) {
            agregarCelda(tabla, fila.getFecha(), normal);
            agregarCelda(tabla, "S/ " + formatear(fila.getVentasNetas()), normal);
            agregarCelda(tabla, "S/ " + formatear(fila.getCostoVentas()), normal);
            agregarCelda(tabla, "S/ " + formatear(fila.getUtilidadNeta()), normal);
            agregarCelda(tabla, formatear(fila.getMargenNeto()) + "%", normal);
        }
        documento.add(tabla);
    }

    private static Image crearGraficoBarras(List<String> labels, List<BigDecimal> valores, String etiquetaY) {
        try {
            int width = 520;
            int height = 220;
            int left = 42;
            int right = 16;
            int top = 20;
            int bottom = 42;
            int plotWidth = width - left - right;
            int plotHeight = height - top - bottom;

            BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imagen.createGraphics();
            prepararCanvas(g, width, height);

            BigDecimal max = BigDecimal.ZERO;
            for (BigDecimal valor : valores) {
                if (valor != null && valor.compareTo(max) > 0) {
                    max = valor;
                }
            }
            if (max.compareTo(BigDecimal.ZERO) <= 0) {
                max = BigDecimal.ONE;
            }

            g.setColor(new Color(100, 116, 139));
            g.drawString(etiquetaY, 8, 14);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(left, top + plotHeight, left + plotWidth, top + plotHeight);
            g.drawLine(left, top, left, top + plotHeight);

            int total = Math.max(1, valores.size());
            int step = Math.max(12, plotWidth / total);
            int barW = Math.max(8, Math.min(24, step - 4));

            for (int i = 0; i < total; i++) {
                BigDecimal valor = i < valores.size() && valores.get(i) != null ? valores.get(i) : BigDecimal.ZERO;
                int barH = valor.multiply(BigDecimal.valueOf(plotHeight))
                        .divide(max, 2, RoundingMode.HALF_UP).intValue();
                int x = left + i * step + (step - barW) / 2;
                int y = top + plotHeight - barH;

                g.setColor(new Color(37, 99, 235));
                g.fillRoundRect(x, y, barW, barH, 6, 6);

                g.setColor(new Color(148, 163, 184));
                String label = abreviar(labels, i);
                g.drawString(label, x, top + plotHeight + 14);
            }

            g.dispose();
            return imagenParaPdf(imagen);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo construir grafico de barras para PDF", e);
        }
    }

    private static Image crearGraficoLinea(List<String> labels, List<BigDecimal> valores, String etiquetaY) {
        try {
            int width = 520;
            int height = 220;
            int left = 42;
            int right = 16;
            int top = 20;
            int bottom = 42;
            int plotWidth = width - left - right;
            int plotHeight = height - top - bottom;

            BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imagen.createGraphics();
            prepararCanvas(g, width, height);

            BigDecimal max = BigDecimal.ZERO;
            for (BigDecimal valor : valores) {
                if (valor != null && valor.compareTo(max) > 0) {
                    max = valor;
                }
            }
            if (max.compareTo(BigDecimal.ZERO) <= 0) {
                max = BigDecimal.ONE;
            }

            g.setColor(new Color(100, 116, 139));
            g.drawString(etiquetaY, 8, 14);
            g.setColor(new Color(226, 232, 240));
            g.drawLine(left, top + plotHeight, left + plotWidth, top + plotHeight);
            g.drawLine(left, top, left, top + plotHeight);

            int total = Math.max(1, valores.size());
            int step = total <= 1 ? plotWidth : plotWidth / (total - 1);
            int prevX = -1;
            int prevY = -1;

            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < total; i++) {
                BigDecimal valor = i < valores.size() && valores.get(i) != null ? valores.get(i) : BigDecimal.ZERO;
                int y = top + plotHeight - valor.multiply(BigDecimal.valueOf(plotHeight))
                        .divide(max, 2, RoundingMode.HALF_UP).intValue();
                int x = left + i * step;

                if (prevX >= 0) {
                    g.setColor(new Color(37, 99, 235));
                    g.drawLine(prevX, prevY, x, y);
                }
                g.setColor(new Color(22, 163, 74));
                g.fillOval(x - 2, y - 2, 5, 5);
                g.setColor(new Color(148, 163, 184));
                g.drawString(abreviar(labels, i), x - 4, top + plotHeight + 14);

                prevX = x;
                prevY = y;
            }

            g.dispose();
            return imagenParaPdf(imagen);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo construir grafico de linea para PDF", e);
        }
    }

    private static void prepararCanvas(Graphics2D g, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    }

    private static String abreviar(List<String> labels, int idx) {
        if (labels == null || idx >= labels.size() || labels.get(idx) == null) {
            return "";
        }
        String label = labels.get(idx);
        if (label.length() > 8) {
            return label.substring(0, 8) + ".";
        }
        return label;
    }

    private static Image imagenParaPdf(BufferedImage imagen) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", bytes);
        Image chart = Image.getInstance(bytes.toByteArray());
        chart.scaleToFit(520, 220);
        chart.setAlignment(Element.ALIGN_CENTER);
        return chart;
    }

    private static void agregarEncabezado(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(new Color(240, 240, 240));
        celda.setPadding(5f);
        tabla.addCell(celda);
    }

    private static void agregarCelda(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(5f);
        tabla.addCell(celda);
    }

    private static Paragraph espacio(float altura) {
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(altura);
        return espacio;
    }

    private static String etiquetaTipo(String tipo) {
        return "rentabilidad".equalsIgnoreCase(tipo) ? "Rentabilidad" : "Ventas";
    }

    private static String formatear(BigDecimal numero) {
        return numero == null ? "0.00" : numero.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
