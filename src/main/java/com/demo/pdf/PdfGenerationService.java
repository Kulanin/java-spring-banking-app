package com.demo.pdf;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.demo.account.Account;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfGenerationService {

    private final PdfStorageService pdfStorageService;

    public PdfGenerationService(PdfStorageService pdfStorageService) {
        this.pdfStorageService = pdfStorageService;
    }

    @Async("pdfGeneratorExecutor")
    public CompletableFuture<String> generateStatement(Account account) {

        log.info("Starting PDF generation for account : {} ", account.generateAccountNumber());
        long startTime = System.currentTimeMillis();

        try {
        
            byte[] pdfBytes = generatePdf(account);

            String fileUrl = pdfStorageService.savePdf("account-" + account.generateAccountNumber() + ".pdf", pdfBytes);

            long duration = System.currentTimeMillis() - startTime;

            log.info("PDF generated successfully in {}ms : {}", duration, fileUrl);

            return CompletableFuture.completedFuture(fileUrl);

        } catch (Exception e) {
            log.error("Failed to generate PDF for account: {}", account.generateAccountNumber(), e); // Good practice to
                                                                                                     // log errors
            return CompletableFuture.failedFuture(e);
        }
    }

    private byte[] generatePdf(Account account) throws Exception {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add title text
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Account Statement"); // Added missing text here
                contentStream.endText();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                float y = 700;
                String[] details = {
                        "Account Number : " + account.generateAccountNumber(),
                        "Balance : R " + account.getBalance(), // Removed duplicate colon
                        "Account Name : " + account.getAccountName(),
                };

                for (String detail : details) {
                    y -= 25;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(detail);
                    contentStream.endText();
                }
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                document.save(baos);
                return baos.toByteArray();
            }
        }
    }

}
