package com.banking.util;

import com.banking.dto.response.TransactionResponse;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates PDF account statements using iText 8.
 */
@Component
@Slf4j
public class PdfGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(BankingConstants.DATETIME_FORMAT);
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(30, 41, 82);
    private static final DeviceRgb ROW_ALT_COLOR = new DeviceRgb(240, 244, 255);

    /**
     * Generates a PDF bank statement for the given account and transactions.
     *
     * @param accountNumber the account number
     * @param customerName  the customer's full name
     * @param fromDate      statement start date
     * @param toDate        statement end date
     * @param transactions  the list of transactions to include
     * @return PDF as a byte array
     * @throws IOException if PDF generation fails
     */
    public byte[] generateStatement(
            String accountNumber,
            String customerName,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            List<TransactionResponse> transactions) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ─── Header ───────────────────────────────────────────────────────────
        Paragraph bankName = new Paragraph("🏦 NEXUS BANK")
                .setFontSize(24)
                .setBold()
                .setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(bankName);

        document.add(new Paragraph("Account Statement")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));

        document.add(new Paragraph("─".repeat(80))
                .setFontColor(HEADER_COLOR));

        // ─── Account Info ─────────────────────────────────────────────────────
        document.add(new Paragraph("Account Number: " + accountNumber).setFontSize(11));
        document.add(new Paragraph("Account Holder: " + customerName).setFontSize(11));
        document.add(new Paragraph("Statement Period: " + fromDate.format(FORMATTER)
                + "  to  " + toDate.format(FORMATTER)).setFontSize(11));
        document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(FORMATTER))
                .setFontSize(11));

        document.add(new Paragraph(" "));

        // ─── Transaction Table ────────────────────────────────────────────────
        Table table = new Table(UnitValue.createPercentArray(
                new float[]{15, 20, 15, 15, 15, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Table headers
        String[] headers = {"Date", "Description", "Type", "Amount (₹)", "Balance (₹)", "Reference"};
        for (String header : headers) {
            table.addHeaderCell(
                    new Cell()
                            .add(new Paragraph(header).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(HEADER_COLOR)
                            .setTextAlignment(TextAlignment.CENTER)
            );
        }

        // Table rows
        boolean alternate = false;
        for (TransactionResponse txn : transactions) {
            DeviceRgb rowColor = alternate ? ROW_ALT_COLOR : new DeviceRgb(255, 255, 255);
            String amountStr = txn.amount() != null ? txn.amount().toPlainString() : "-";
            String balanceStr = txn.balanceAfter() != null ? txn.balanceAfter().toPlainString() : "-";
            String dateStr = txn.createdAt() != null ? txn.createdAt().format(FORMATTER) : "-";

            table.addCell(styledCell(dateStr, rowColor));
            table.addCell(styledCell(txn.description() != null ? txn.description() : "-", rowColor));
            table.addCell(styledCell(txn.transactionType() != null ? txn.transactionType().name() : "-", rowColor));
            table.addCell(styledCell(amountStr, rowColor, TextAlignment.RIGHT));
            table.addCell(styledCell(balanceStr, rowColor, TextAlignment.RIGHT));
            table.addCell(styledCell(txn.referenceNumber() != null ? txn.referenceNumber() : "-", rowColor));
            alternate = !alternate;
        }

        document.add(table);

        // ─── Footer ───────────────────────────────────────────────────────────
        document.add(new Paragraph(" "));
        document.add(new Paragraph("This is a system-generated statement and does not require a signature.")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        log.info("PDF statement generated for account: {}", accountNumber);
        return baos.toByteArray();
    }

    private Cell styledCell(String content, DeviceRgb background) {
        return styledCell(content, background, TextAlignment.LEFT);
    }

    private Cell styledCell(String content, DeviceRgb background, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(content).setFontSize(9))
                .setBackgroundColor(background)
                .setTextAlignment(alignment)
                .setPadding(4);
    }
}
