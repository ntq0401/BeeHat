package com.beehat.service;

import com.beehat.entity.Invoice;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
@Service
public class InvoiceService {
    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Invoice ID: " + invoice.getId()));
        document.add(new Paragraph("Customer: " + invoice.getCustomer().getFullname()));
        document.add(new Paragraph("Total Price: " + invoice.getTotalPrice()));
        document.add(new Paragraph("Final Price: " + invoice.getFinalPrice()));
        // Thêm các thông tin cần thiết khác từ invoice

        document.close();
        return baos.toByteArray();
    }
}
