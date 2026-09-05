package evidence_verification.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Case;
import evidence_verification.Entity.Evidence;
import evidence_verification.repository.CaseRepository;
import evidence_verification.repository.EvidenceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import evidence_verification.Entity.EvidenceVersion;
import evidence_verification.repository.CaseRepository;
import evidence_verification.repository.EvidenceRepository;
import evidence_verification.repository.EvidenceVersionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    private final EvidenceRepository evidenceRepository;
    private final BlockchainService blockchainService;
    private final AuditLogService auditLogService;
    private final CaseRepository caseRepository;
    private final EvidenceVersionRepository evidenceVersionRepository;

    public PdfReportService(
            EvidenceRepository evidenceRepository,
            BlockchainService blockchainService,
            AuditLogService auditLogService,
            CaseRepository caseRepository,
            EvidenceVersionRepository evidenceVersionRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.blockchainService = blockchainService;
        this.auditLogService = auditLogService;
        this.caseRepository = caseRepository;
        this.evidenceVersionRepository = evidenceVersionRepository;
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Color PRIMARY_COLOR = new Color(30, 41, 59); // Slate Dark
    private static final Color ACCENT_COLOR = new Color(37, 99, 235); // Blue Accent
    private static final Color BG_LIGHT = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color SUCCESS_COLOR = new Color(22, 101, 52);
    private static final Color DANGER_COLOR = new Color(153, 27, 27);

    public byte[] generateVerificationReport(String evidenceId, String requestedBy) {
        return generateVerificationReport(evidenceId, null, requestedBy);
    }

    public byte[] generateVerificationReport(String evidenceId, Integer versionNumber, String requestedBy) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        // Security check: verify user authorization
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean canViewAll = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_FORENSIC_ANALYST")
                        || a.getAuthority().equals("ROLE_VIEWER"));

        if (!canViewAll && !evidence.getUploadedBy().equalsIgnoreCase(requestedBy)) {
            // Check if user is creator or assigned investigator of associated case
            Case caseRecord = evidence.getCaseRecord();
            boolean isCaseAuthorized = caseRecord != null && (
                    requestedBy.equalsIgnoreCase(caseRecord.getCreatedBy())
                    || requestedBy.equalsIgnoreCase(caseRecord.getAssignedInvestigator())
            );
            if (!isCaseAuthorized) {
                throw new SecurityException("Unauthorized access: You do not have permission to generate a report for this evidence record.");
            }
        }

        EvidenceVersion versionObj = null;
        if (versionNumber != null) {
            versionObj = evidenceVersionRepository.findByEvidence_EvidenceIdAndVersionNumber(evidenceId, versionNumber).orElse(null);
        }

        String targetFileName = versionObj != null ? versionObj.getFileName() : evidence.getFileName();
        String targetExtension = versionObj != null ? versionObj.getFileExtension() : evidence.getFileExtension();
        String targetFileType = versionObj != null ? versionObj.getFileType() : evidence.getFileType();
        Long targetFileSize = versionObj != null ? versionObj.getFileSize() : evidence.getFileSize();
        String targetFileHash = versionObj != null ? versionObj.getFileHash() : evidence.getFileHash();
        String targetStatus = versionObj != null ? versionObj.getStatus() : evidence.getStatus();
        String targetUploadedBy = versionObj != null ? versionObj.getUploadedBy() : evidence.getUploadedBy();
        LocalDateTime targetUploadedAt = versionObj != null ? versionObj.getUploadedAt() : evidence.getUploadedAt();
        LocalDateTime targetCreated = versionObj != null ? versionObj.getCreatedTimestamp() : evidence.getCreatedTimestamp();
        LocalDateTime targetModified = versionObj != null ? versionObj.getModifiedTimestamp() : evidence.getModifiedTimestamp();
        String lookupId = versionObj != null ? versionObj.getVersionId() : evidenceId;

        BlockchainRecord blockchainRecord = blockchainService.getRecord(lookupId).orElseGet(() -> blockchainService.getRecord(evidenceId).orElse(null));
        List<AuditLog> auditLogs = auditLogService.getChainOfCustodyForEvidence(evidenceId);
        Case caseRecord = evidence.getCaseRecord();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 40, 50);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // Font Definitions
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
            Font hashFont = FontFactory.getFont(FontFactory.COURIER, 8, TEXT_DARK);

            // 1. REPORT HEADER
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            
            PdfPCell titleCell = new PdfPCell(new Paragraph("Blockchain Digital Evidence Verification Report", titleFont));
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setPaddingBottom(4);
            headerTable.addCell(titleCell);

            PdfPCell subCell = new PdfPCell(new Paragraph("Official Cryptographic & Chain of Custody Audit Document • Generated " + LocalDateTime.now().format(DATE_FORMATTER) + " UTC", subtitleFont));
            subCell.setBorder(Rectangle.NO_BORDER);
            subCell.setPaddingBottom(12);
            headerTable.addCell(subCell);

            document.add(headerTable);

            // Horizontal Line
            Paragraph line = new Paragraph();
            line.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, ACCENT_COLOR, Element.ALIGN_CENTER, -2)));
            line.setSpacingAfter(12);
            document.add(line);

            // 2. EVIDENCE METADATA SECTION
            document.add(createSectionHeader("1. Digital Evidence Metadata", sectionTitleFont));

            PdfPTable metaTable = new PdfPTable(4);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{20, 30, 20, 30});
            metaTable.setSpacingAfter(14);

            addTableCellPair(metaTable, "Evidence ID:", evidence.getEvidenceId() + (versionObj != null ? " (v" + versionObj.getVersionNumber() + ")" : ""), labelFont, valueFont);
            addTableCellPair(metaTable, "Case ID:", caseRecord != null ? caseRecord.getCaseId() : "Unassigned", labelFont, valueFont);
            addTableCellPair(metaTable, "File Name:", targetFileName, labelFont, valueFont);
            addTableCellPair(metaTable, "File Extension:", targetExtension != null ? targetExtension.toUpperCase() : "N/A", labelFont, valueFont);
            addTableCellPair(metaTable, "MIME Type:", targetFileType != null ? targetFileType : "application/octet-stream", labelFont, valueFont);
            addTableCellPair(metaTable, "File Size:", formatFileSize(targetFileSize), labelFont, valueFont);
            addTableCellPair(metaTable, "Uploaded By:", targetUploadedBy, labelFont, valueFont);
            addTableCellPair(metaTable, "Upload Date:", targetUploadedAt != null ? targetUploadedAt.format(DATE_FORMATTER) : "N/A", labelFont, valueFont);
            addTableCellPair(metaTable, "Created Date:", targetCreated != null ? targetCreated.format(DATE_FORMATTER) : "Unavailable", labelFont, valueFont);
            addTableCellPair(metaTable, "Modified Date:", targetModified != null ? targetModified.format(DATE_FORMATTER) : "Unavailable", labelFont, valueFont);

            document.add(metaTable);

            // 3. INTEGRITY & 3-WAY VERIFICATION COMPARISON
            document.add(createSectionHeader("2. Cryptographic Integrity & 3-Way Verification Comparison", sectionTitleFont));

            String calculatedHash = targetFileHash;
            String storedDbHash = targetFileHash;
            String blockchainHash = blockchainRecord != null ? blockchainRecord.getFileHash() : "N/A";

            boolean dbMatch = calculatedHash.equalsIgnoreCase(storedDbHash);
            boolean bcMatch = blockchainRecord != null && calculatedHash.equalsIgnoreCase(blockchainHash);
            boolean isVerified = dbMatch && bcMatch;
            boolean isTampered = "TAMPERED".equalsIgnoreCase(targetStatus);

            PdfPTable hashTable = new PdfPTable(3);
            hashTable.setWidthPercentage(100);
            hashTable.setWidths(new float[]{25, 60, 15});
            hashTable.setSpacingAfter(10);

            // Header
            addTableHeader(hashTable, "Verification Node", labelFont);
            addTableHeader(hashTable, "SHA-256 Fingerprint / Hash", labelFont);
            addTableHeader(hashTable, "Status", labelFont);

            // Rows
            addHashRow(hashTable, "Calculated File Hash", calculatedHash, "MATCHED", true, hashFont, valueFont);
            addHashRow(hashTable, "PostgreSQL Database Hash", storedDbHash, dbMatch ? "MATCHED" : "MISMATCH", dbMatch, hashFont, valueFont);
            addHashRow(hashTable, "Polygon Blockchain Hash", blockchainHash, bcMatch ? "MATCHED" : (blockchainRecord != null ? "MISMATCH" : "N/A"), bcMatch, hashFont, valueFont);

            document.add(hashTable);

            // Verification Result Banner Box
            PdfPTable bannerTable = new PdfPTable(1);
            bannerTable.setWidthPercentage(100);
            bannerTable.setSpacingAfter(14);

            String resultText;
            Color bannerBg;
            Color bannerFg;

            if (isVerified) {
                resultText = "3-WAY VERIFICATION PASSED: File Hash, Database Record, and Polygon Blockchain Anchor match perfectly.";
                bannerBg = new Color(220, 252, 231);
                bannerFg = SUCCESS_COLOR;
            } else if (isTampered || (!dbMatch || (blockchainRecord != null && !bcMatch))) {
                resultText = "CRITICAL WARNING - VERIFICATION FAILED: Cryptographic hash mismatch detected. Evidence is marked TAMPERED / COMPROMISED.";
                bannerBg = new Color(254, 226, 226);
                bannerFg = DANGER_COLOR;
            } else {
                resultText = "CURRENT STATUS: " + evidence.getStatus() + " • Formal forensic verification pending examination.";
                bannerBg = new Color(241, 245, 249);
                bannerFg = PRIMARY_COLOR;
            }

            PdfPCell bannerCell = new PdfPCell(new Paragraph(resultText, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, bannerFg)));
            bannerCell.setBackgroundColor(bannerBg);
            bannerCell.setBorderColor(bannerFg);
            bannerCell.setPadding(10);
            bannerTable.addCell(bannerCell);

            document.add(bannerTable);

            // 4. BLOCKCHAIN ANCHORING PROOF
            document.add(createSectionHeader("3. Polygon Blockchain Anchor Proof", sectionTitleFont));

            PdfPTable bcTable = new PdfPTable(4);
            bcTable.setWidthPercentage(100);
            bcTable.setWidths(new float[]{20, 30, 20, 30});
            bcTable.setSpacingAfter(14);

            addTableCellPair(bcTable, "Network:", "Polygon Amoy Testnet", labelFont, valueFont);
            addTableCellPair(bcTable, "Status:", blockchainRecord != null ? blockchainRecord.getStatus() : "UNANCHORED", labelFont, valueFont);
            addTableCellPair(bcTable, "Transaction Hash:", blockchainRecord != null ? blockchainRecord.getTransactionHash() : "N/A", labelFont, hashFont);
            addTableCellPair(bcTable, "Anchor Timestamp:", (blockchainRecord != null && blockchainRecord.getBlockchainTimestamp() != null) ? blockchainRecord.getBlockchainTimestamp().format(DATE_FORMATTER) : "N/A", labelFont, valueFont);

            document.add(bcTable);

            // 5. CASE & INVESTIGATION CONTEXT
            if (caseRecord != null) {
                document.add(createSectionHeader("4. Case & Investigation Context", sectionTitleFont));

                PdfPTable caseTable = new PdfPTable(4);
                caseTable.setWidthPercentage(100);
                caseTable.setWidths(new float[]{20, 30, 20, 30});
                caseTable.setSpacingAfter(14);

                addTableCellPair(caseTable, "Case ID:", caseRecord.getCaseId(), labelFont, valueFont);
                addTableCellPair(caseTable, "Case Title:", caseRecord.getTitle(), labelFont, valueFont);
                addTableCellPair(caseTable, "Case Priority:", caseRecord.getPriority(), labelFont, valueFont);
                addTableCellPair(caseTable, "Case Status:", caseRecord.getStatus(), labelFont, valueFont);
                addTableCellPair(caseTable, "Assigned Investigator:", caseRecord.getAssignedInvestigator() != null ? caseRecord.getAssignedInvestigator() : "Unassigned", labelFont, valueFont);
                addTableCellPair(caseTable, "Case Created By:", caseRecord.getCreatedBy(), labelFont, valueFont);

                document.add(caseTable);
            }

            // 6. CHAIN OF CUSTODY TIMELINE
            document.add(createSectionHeader((caseRecord != null ? "5." : "4.") + " Integrated Chain of Custody Audit Log", sectionTitleFont));

            if (auditLogs != null && !auditLogs.isEmpty()) {
                PdfPTable auditTable = new PdfPTable(5);
                auditTable.setWidthPercentage(100);
                auditTable.setWidths(new float[]{20, 20, 15, 18, 27});
                auditTable.setSpacingAfter(14);

                addTableHeader(auditTable, "Action / Event", labelFont);
                addTableHeader(auditTable, "Performed By", labelFont);
                addTableHeader(auditTable, "Role", labelFont);
                addTableHeader(auditTable, "Timestamp", labelFont);
                addTableHeader(auditTable, "Event Details", labelFont);

                for (AuditLog log : auditLogs) {
                    addAuditRow(auditTable, log, valueFont);
                }

                document.add(auditTable);
            } else {
                Paragraph noLogs = new Paragraph("No custodial events recorded.", valueFont);
                noLogs.setSpacingAfter(14);
                document.add(noLogs);
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF verification report: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private Paragraph createSectionHeader(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(10);
        p.setSpacingAfter(6);
        return p;
    }

    private void addTableCellPair(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Paragraph(label, labelFont));
        lCell.setBackgroundColor(BG_LIGHT);
        lCell.setBorderColor(BORDER_COLOR);
        lCell.setPadding(6);

        PdfPCell vCell = new PdfPCell(new Paragraph(value != null ? value : "N/A", valueFont));
        vCell.setBorderColor(BORDER_COLOR);
        vCell.setPadding(6);

        table.addCell(lCell);
        table.addCell(vCell);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setBorderColor(PRIMARY_COLOR);
        cell.setPadding(6);
        cell.getPhrase().getFont().setColor(Color.WHITE);
        table.addCell(cell);
    }

    private void addHashRow(PdfPTable table, String node, String hash, String statusText, boolean isMatch, Font hashFont, Font valueFont) {
        PdfPCell nodeCell = new PdfPCell(new Paragraph(node, valueFont));
        nodeCell.setBorderColor(BORDER_COLOR);
        nodeCell.setPadding(6);
        table.addCell(nodeCell);

        PdfPCell hashCell = new PdfPCell(new Paragraph(hash, hashFont));
        hashCell.setBorderColor(BORDER_COLOR);
        hashCell.setPadding(6);
        table.addCell(hashCell);

        Font statusFont = isMatch 
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, SUCCESS_COLOR)
                : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, DANGER_COLOR);

        PdfPCell statusCell = new PdfPCell(new Paragraph(statusText, statusFont));
        statusCell.setBorderColor(BORDER_COLOR);
        statusCell.setPadding(6);
        table.addCell(statusCell);
    }

    private void addAuditRow(PdfPTable table, AuditLog log, Font font) {
        PdfPCell actionCell = new PdfPCell(new Paragraph(log.getAction(), font));
        actionCell.setBorderColor(BORDER_COLOR);
        actionCell.setPadding(5);
        table.addCell(actionCell);

        PdfPCell userCell = new PdfPCell(new Paragraph(log.getPerformedBy(), font));
        userCell.setBorderColor(BORDER_COLOR);
        userCell.setPadding(5);
        table.addCell(userCell);

        PdfPCell roleCell = new PdfPCell(new Paragraph(log.getActorRole() != null ? log.getActorRole() : "USER", font));
        roleCell.setBorderColor(BORDER_COLOR);
        roleCell.setPadding(5);
        table.addCell(roleCell);

        PdfPCell timeCell = new PdfPCell(new Paragraph(log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "", font));
        timeCell.setBorderColor(BORDER_COLOR);
        timeCell.setPadding(5);
        table.addCell(timeCell);

        PdfPCell detailCell = new PdfPCell(new Paragraph(log.getDetails() != null ? log.getDetails() : "", font));
        detailCell.setBorderColor(BORDER_COLOR);
        detailCell.setPadding(5);
        table.addCell(detailCell);
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        long k = 1024;
        String[] sizes = new String[]{"B", "KB", "MB", "GB"};
        int i = (int) (Math.log(bytes) / Math.log(k));
        if (i >= sizes.length) i = sizes.length - 1;
        return String.format("%.2f %s (%d bytes)", bytes / Math.pow(k, i), sizes[i], bytes);
    }

    // OpenPDF Page Event Helper for Page X of Y Footer
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private PdfTemplate totalPagesTemplate;
        private BaseFont baseFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPagesTemplate = writer.getDirectContent().createTemplate(30, 16);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                // fallback
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();

            // Footer separator line
            cb.setColorStroke(BORDER_COLOR);
            cb.setLineWidth(0.5f);
            cb.moveTo(document.left(), document.bottom() + 18);
            cb.lineTo(document.right(), document.bottom() + 18);
            cb.stroke();

            String pageText = "Page " + writer.getPageNumber() + " of ";
            float textSize = 8;
            float textWidth = (baseFont != null) ? baseFont.getWidthPoint(pageText, textSize) : 30;

            cb.beginText();
            if (baseFont != null) cb.setFontAndSize(baseFont, textSize);
            cb.setColorFill(new Color(100, 116, 139));

            // Left footer
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "CONFIDENTIAL FORENSIC REPORT • DIGITAL EVIDENCE VERIFICATION SYSTEM", document.left(), document.bottom() + 6, 0);

            // Right footer
            float xPos = document.right() - textWidth - 14;
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, pageText, xPos, document.bottom() + 6, 0);
            cb.endText();

            cb.addTemplate(totalPagesTemplate, xPos + textWidth, document.bottom() + 6);
            cb.restoreState();
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            if (totalPagesTemplate != null && baseFont != null) {
                totalPagesTemplate.beginText();
                totalPagesTemplate.setFontAndSize(baseFont, 8);
                totalPagesTemplate.setColorFill(new Color(100, 116, 139));
                totalPagesTemplate.showText(String.valueOf(writer.getPageNumber()));
                totalPagesTemplate.endText();
            }
        }
    }
}
