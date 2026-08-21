package com.pf.attendance.app.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import java.io.ByteArrayOutputStream;
import java.util.List;

/** Printable monthly timesheet PDF for paper submission workflows (demo quality). */
public final class PdfTimesheetRenderer {
  private PdfTimesheetRenderer() {}

  public static byte[] renderAll(List<EmployeeSheet> sheets, String disclaimer) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document doc = new Document();
      PdfWriter.getInstance(doc, out);
      doc.open();
      for (int i = 0; i < sheets.size(); i++) {
        if (i > 0) {
          doc.newPage();
        }
        addSheet(doc, sheets.get(i).employee(), sheets.get(i).summary(), disclaimer);
      }
      doc.close();
      return out.toByteArray();
    } catch (DocumentException e) {
      throw new IllegalStateException("pdf render failed", e);
    }
  }

  public static byte[] render(Employee employee, MonthSummary summary, String disclaimer) {
    return renderAll(List.of(new EmployeeSheet(employee, summary)), disclaimer);
  }

  private static void addSheet(Document doc, Employee employee, MonthSummary summary, String disclaimer)
      throws DocumentException {
    Font title = new Font(Font.HELVETICA, 14, Font.BOLD);
    Font body = new Font(Font.HELVETICA, 9, Font.NORMAL);
    doc.add(new Paragraph("Attendance timesheet (demo)", title));
    doc.add(new Paragraph(disclaimer, body));
    doc.add(
        new Paragraph(
            employee.displayName()
                + " ("
                + employee.sub()
                + ")  period="
                + summary.month()
                + "  engagement="
                + employee.engagement(),
            body));
    if (!employee.worksiteName().isBlank()) {
      doc.add(new Paragraph("Worksite: " + employee.worksiteName(), body));
    }
    doc.add(new Paragraph(" ", body));

    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);
    header(table, "Date");
    header(table, "Work min");
    header(table, "Break min");
    header(table, "Status");
    header(table, "Prov.");
    for (DailySummary day : summary.days()) {
      cell(table, day.workDate().toString(), body);
      cell(table, Integer.toString(day.workMinutes()), body);
      cell(table, Integer.toString(day.breakMinutes()), body);
      cell(table, day.status().name().toLowerCase(), body);
      cell(table, day.provisional() ? "Y" : "", body);
    }
    doc.add(table);
    doc.add(new Paragraph(" ", body));
    doc.add(new Paragraph("Manager signature: ____________________    Date: __________", body));
  }

  private static void header(PdfPTable table, String text) {
    table.addCell(new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 9, Font.BOLD))));
  }

  private static void cell(PdfPTable table, String text, Font font) {
    table.addCell(new PdfPCell(new Phrase(text, font)));
  }

  public record EmployeeSheet(Employee employee, MonthSummary summary) {}
}
