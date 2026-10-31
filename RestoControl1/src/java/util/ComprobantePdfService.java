package util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import modelo.DetallePedido;
import modelo.Pago;
import modelo.Pedido;

public final class ComprobantePdfService {

    private static final String NOMBRE_NEGOCIO = "Central";
    private static final String RUC_NEGOCIO = "20123456789";
    private static final String DIRECCION_NEGOCIO = "Av. Gastronomica 123, Lima - Peru";

    private ComprobantePdfService() {
    }

    public static byte[] generar(Pedido pedido, Pago pago, List<DetallePedido> detalles, int totalPartes) {
        if (pedido == null || pago == null) {
            throw new IllegalArgumentException("Pedido y pago son obligatorios para generar el comprobante.");
        }

        boolean esFactura = "factura".equalsIgnoreCase(pago.getTipoDocumento());
        boolean esPagoParcial = totalPartes > 1;
        BigDecimal montoComprobante = valor(pago.getMontoPago());
        BigDecimal subtotal = montoComprobante.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        BigDecimal igv = montoComprobante.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A5, 28, 28, 28, 28);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font pequeno = FontFactory.getFont(FontFactory.HELVETICA, 8);

            documento.add(new Paragraph(NOMBRE_NEGOCIO, titulo));
            documento.add(new Paragraph("RUC: " + RUC_NEGOCIO, normal));
            documento.add(new Paragraph(DIRECCION_NEGOCIO, pequeno));
            documento.add(espacio(8));

            String serie = esFactura ? "F001" : "B001";
            String numero = String.format("%08d", pago.getIdPago());
            documento.add(new Paragraph(esFactura ? "FACTURA ELECTRONICA" : "BOLETA DE VENTA", subtitulo));
            documento.add(new Paragraph(serie + "-" + numero, normal));
            documento.add(espacio(6));

            String fecha = pago.getFechaPago();
            if (fecha == null || fecha.trim().isEmpty()) {
                fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            documento.add(linea("Fecha", fecha, normal));
            documento.add(linea("Pedido", "#" + pedido.getIdPedido(), normal));
            documento.add(linea("Atencion", etiquetaAtencion(pedido), normal));
            documento.add(linea("Mesero", texto(pedido.getNombreUsuario(), "-"), normal));
            if (esPagoParcial) {
                documento.add(linea("Pago", "Parte " + pago.getNumeroParte() + " de " + totalPartes, normal));
                documento.add(linea("Cliente pago", texto(pago.getClientePago(), "-"), normal));
            }
            documento.add(espacio(8));

            if (esFactura) {
                documento.add(linea("RUC cliente", texto(pago.getNumeroDocumento(), "-"), normal));
                documento.add(linea("Razon social", texto(pago.getRazonSocial(), "-"), normal));
            } else if (tieneTexto(pago.getNumeroDocumento())) {
                documento.add(linea("Documento", pago.getNumeroDocumento().trim(), normal));
            } else if (tieneTexto(pedido.getCliente())) {
                documento.add(linea("Cliente", pedido.getCliente().trim(), normal));
            }

            documento.add(espacio(10));
            documento.add(new Paragraph("Detalle", subtitulo));
            documento.add(espacio(4));

            if (esPagoParcial) {
                documento.add(new Paragraph(
                        "Comprobante por monto parcial asignado en la division de cuenta del pedido.",
                        pequeno));
                documento.add(espacio(6));
                agregarResumenParcial(documento, detalles, normal, pequeno);
            } else {
                agregarTablaDetalle(documento, detalles, normal);
            }

            documento.add(espacio(10));
            PdfPTable totales = new PdfPTable(2);
            totales.setWidthPercentage(100);
            totales.setWidths(new float[]{3f, 1f});
            agregarCeldaTotal(totales, "Subtotal", "S/ " + formatear(subtotal), normal);
            agregarCeldaTotal(totales, "IGV (18%)", "S/ " + formatear(igv), normal);
            agregarCeldaTotal(totales, "TOTAL", "S/ " + formatear(montoComprobante), subtitulo);
            documento.add(totales);

            documento.add(espacio(10));
            documento.add(linea("Medio de pago", etiquetaMedio(pago.getMedioPago()), normal));
            if ("efectivo".equalsIgnoreCase(pago.getMedioPago())) {
                documento.add(linea("Monto recibido", "S/ " + formatear(pago.getMontoRecibido()), normal));
                documento.add(linea("Vuelto", "S/ " + formatear(pago.getVuelto()), normal));
            }

            documento.add(espacio(14));
            documento.add(new Paragraph("Gracias por su preferencia.", pequeno));
            documento.add(new Paragraph("Documento generado por Central.", pequeno));

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("No se pudo generar el comprobante PDF", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("No se pudo cerrar el comprobante PDF", e);
        }
    }

    public static String nombreArchivo(Pago pago) {
        String tipo = "factura".equalsIgnoreCase(pago.getTipoDocumento()) ? "factura" : "boleta";
        return tipo + "_" + pago.getIdPago() + ".pdf";
    }

    private static void agregarTablaDetalle(Document documento, List<DetallePedido> detalles, Font normal)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4f, 1f, 2f, 2f});
        agregarEncabezado(tabla, "Producto");
        agregarEncabezado(tabla, "Cant.");
        agregarEncabezado(tabla, "P. Unit.");
        agregarEncabezado(tabla, "Subtotal");

        if (detalles == null || detalles.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("Sin detalle registrado", normal));
            vacio.setColspan(4);
            vacio.setPadding(6f);
            tabla.addCell(vacio);
        } else {
            for (DetallePedido detalle : detalles) {
                agregarCelda(tabla, texto(detalle.getNombreProducto(), "-"), normal);
                agregarCelda(tabla, String.valueOf(detalle.getCantidad()), normal);
                agregarCelda(tabla, "S/ " + formatear(detalle.getPrecioUnitario()), normal);
                agregarCelda(tabla, "S/ " + formatear(detalle.getSubtotal()), normal);
            }
        }
        documento.add(tabla);
    }

    private static void agregarResumenParcial(Document documento, List<DetallePedido> detalles, Font normal, Font pequeno)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4f, 2f});
        agregarEncabezado(tabla, "Producto del pedido");
        agregarEncabezado(tabla, "Subtotal pedido");

        if (detalles == null || detalles.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("Sin detalle registrado", normal));
            vacio.setColspan(2);
            vacio.setPadding(6f);
            tabla.addCell(vacio);
        } else {
            for (DetallePedido detalle : detalles) {
                agregarCelda(tabla, detalle.getCantidad() + " x " + texto(detalle.getNombreProducto(), "-"), pequeno);
                agregarCelda(tabla, "S/ " + formatear(detalle.getSubtotal()), pequeno);
            }
        }
        documento.add(tabla);
    }

    private static void agregarEncabezado(PdfPTable tabla, String texto) {
        Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        PdfPCell celda = new PdfPCell(new Phrase(texto, encabezado));
        celda.setBackgroundColor(new Color(240, 240, 240));
        celda.setPadding(5f);
        tabla.addCell(celda);
    }

    private static void agregarCelda(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(5f);
        tabla.addCell(celda);
    }

    private static void agregarCeldaTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        PdfPCell etiquetaCelda = new PdfPCell(new Phrase(etiqueta, fuente));
        etiquetaCelda.setBorder(PdfPCell.NO_BORDER);
        etiquetaCelda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        etiquetaCelda.setPadding(4f);
        tabla.addCell(etiquetaCelda);

        PdfPCell valorCelda = new PdfPCell(new Phrase(valor, fuente));
        valorCelda.setBorder(PdfPCell.NO_BORDER);
        valorCelda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valorCelda.setPadding(4f);
        tabla.addCell(valorCelda);
    }

    private static Paragraph linea(String etiqueta, String valor, Font fuente) {
        return new Paragraph(etiqueta + ": " + valor, fuente);
    }

    private static Paragraph espacio(float altura) {
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(altura);
        return espacio;
    }

    private static String etiquetaAtencion(Pedido pedido) {
        if ("delivery".equals(pedido.getTipoPedido())) {
            return "Delivery";
        }
        if ("para_llevar".equals(pedido.getTipoPedido())) {
            return "Para llevar";
        }
        return pedido.getNumeroMesa() > 0 ? "Mesa " + pedido.getNumeroMesa() : "Salon";
    }

    private static String etiquetaMedio(String medio) {
        if ("tarjeta".equalsIgnoreCase(medio)) {
            return "Tarjeta";
        }
        if ("yape_plin".equalsIgnoreCase(medio)) {
            return "Yape / Plin";
        }
        return "Efectivo";
    }

    private static String formatear(BigDecimal numero) {
        return valor(numero).toPlainString();
    }

    private static BigDecimal valor(BigDecimal numero) {
        return numero == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : numero.setScale(2, RoundingMode.HALF_UP);
    }

    private static String texto(String valor, String fallback) {
        return tieneTexto(valor) ? valor.trim() : fallback;
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
