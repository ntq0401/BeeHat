package com.beehat.service;

import com.beehat.entity.Invoice;
import com.beehat.entity.InvoiceDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.ByteArrayOutputStream;
@Service
public class InvoiceService {
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    public byte[] generateInvoicePdf(Invoice invoice) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByInvoiceId(invoice.getId());
        document.setMargins(20, 20, 20, 20);

        // Load font
        String fontPath = new ClassPathResource("arial.ttf").getFile().getAbsolutePath();
        PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
        document.setFont(font);

        // Đảm bảo sử dụng đường dẫn chính xác tới logo trong thư mục resources/static/img
        String logoPath = new ClassPathResource("static/img/logo3.png").getFile().getAbsolutePath();
        Image logo = new Image(ImageDataFactory.create(logoPath))
                .setHeight(50)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(logo);


        Paragraph companyInfo = new Paragraph("CÔNG TY TNHH BEEHAT\nĐịa chỉ: 123 Đường ABC, Quận XYZ, TP.Hà Nội\nHotline: 0969 21 21 09 | Email: support@beehat.com")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(companyInfo);

        document.add(new LineSeparator(new SolidLine()));

        // Header
        Paragraph header = new Paragraph("HOÁ ĐƠN THANH TOÁN")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);
        document.add(header);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = invoice.getCreatedDate().format(formatter);

        document.add(new LineSeparator(new SolidLine()));

        // Info Section (Invoice & Customer Information)
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginTop(10);
        infoTable.addCell(new Cell().add(new Paragraph("Mã hoá đơn:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(invoice.getId()+"")));
        infoTable.addCell(new Cell().add(new Paragraph("Ngày tạo:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(formattedDate)));
        infoTable.addCell(new Cell().add(new Paragraph("Khách hàng:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(invoice.getCustomer() != null && invoice.getCustomer().getFullname() != null ? invoice.getCustomer().getFullname() : "N/A")));
        infoTable.addCell(new Cell().add(new Paragraph("Email:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(invoice.getCustomer() != null && invoice.getCustomer().getEmail() != null ? invoice.getCustomer().getEmail() : "N/A")));
        infoTable.addCell(new Cell().add(new Paragraph("Số điện thoại:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(invoice.getCustomer() != null && invoice.getCustomer().getPhone() != null ?invoice.getCustomer().getPhone() : "N/A")));
        document.add(infoTable);

        document.add(new Paragraph(" ")); // Blank line for spacing

        // Product Section
        Table productTable = new Table(UnitValue.createPercentArray(new float[]{1, 4, 2, 1, 2}))
                .useAllAvailableWidth();

        // Table Header
        productTable.addHeaderCell(new Cell().add(new Paragraph("STT").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Sản phẩm").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Đơn giá").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("SL").setBold()));
        productTable.addHeaderCell(new Cell().add(new Paragraph("Thành tiền").setBold()));

        // Add product rows
        int index = 1;
        for (InvoiceDetail invoiceDetail : invoiceDetails) {
            productTable.addCell(new Cell().add(new Paragraph(String.valueOf(index++))));
            productTable.addCell(new Cell().add(new Paragraph(invoiceDetail.getProductDetail().getProduct().getName())));
            productTable.addCell(new Cell().add(new Paragraph(invoiceDetail.getUnitPrice()+"")));
            productTable.addCell(new Cell().add(new Paragraph(String.valueOf(invoiceDetail.getQuantity()))));
            productTable.addCell(new Cell().add(new Paragraph(invoiceDetail.getFinalPrice()+"")));
        }
        document.add(productTable);

        document.add(new Paragraph(" ")); // Blank line for spacing

        // Totals Section
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{6, 2}))
                .useAllAvailableWidth();
        totalTable.addCell(new Cell().add(new Paragraph("Tổng cộng").setBold().setTextAlignment(TextAlignment.RIGHT)));
        totalTable.addCell(new Cell().add(new Paragraph(invoice.getTotalPrice()+"")).setBold());
        totalTable.addCell(new Cell().add(new Paragraph("Giảm giá").setBold().setTextAlignment(TextAlignment.RIGHT)));
        totalTable.addCell(new Cell().add(new Paragraph(invoice.getPromotionDiscount()+"")));
        // Kiểm tra null cho voucher
        if (invoice.getVoucher() != null) {
            int discountAmount = invoice.getVoucherDiscount();
            discountAmount = discountAmount > invoice.getVoucher().getDiscountMax() ? invoice.getVoucher().getDiscountMax() : discountAmount;
            totalTable.addCell(new Cell().add(new Paragraph("Giảm giá voucher").setBold().setTextAlignment(TextAlignment.RIGHT)));
            totalTable.addCell(new Cell().add(new Paragraph(discountAmount + "")));
        } else {
            totalTable.addCell(new Cell().add(new Paragraph("Giảm giá voucher").setBold().setTextAlignment(TextAlignment.RIGHT)));
            totalTable.addCell(new Cell().add(new Paragraph("0")));
        }        totalTable.addCell(new Cell().add(new Paragraph("Thực thu").setBold().setTextAlignment(TextAlignment.RIGHT)));
        totalTable.addCell(new Cell().add(new Paragraph(String.format(invoice.getFinalPrice()+"")).setBold()));
        document.add(totalTable);

        document.add(new Paragraph(" ")); // Blank line for spacing

        // Notes Section
        Paragraph notes = new Paragraph("Ghi chú:\n- Vui lòng kiểm tra hàng hoá trước khi rời cửa hàng.\n- Hoá đơn này là bằng chứng mua hàng hợp lệ.")
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY);
        document.add(notes);

        document.add(new LineSeparator(new SolidLine()));

        // Footer
        Paragraph footer = new Paragraph("Cảm ơn bạn đã mua hàng tại BeeHat!\nHẹn gặp lại quý khách.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginTop(20);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }


}
