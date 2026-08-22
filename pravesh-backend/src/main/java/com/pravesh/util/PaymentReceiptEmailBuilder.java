package com.pravesh.util;

// Inline-styled HTML receipt -- email clients strip <style> blocks.
public final class PaymentReceiptEmailBuilder {

    private PaymentReceiptEmailBuilder() {}

    public static String build(String residentName, String purpose, double amount,
                                String formattedDate, Long paymentOrderId) {
        String purposeLabel = switch (purpose) {
            case "MAINTENANCE" -> "Maintenance";
            case "EVENT" -> "Event Fee";
            case "ACTIVITY" -> "Activity Fee";
            case "TRIP" -> "Trip Fee";
            default -> purpose;
        };

        return "<!DOCTYPE html>"
            + "<html><body style=\"margin:0;padding:0;background-color:#f4f5f7;font-family:'Segoe UI',Helvetica,Arial,sans-serif;\">"
            + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color:#f4f5f7;padding:32px 0;\">"
            + "<tr><td align=\"center\">"
            + "<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" "
            +   "style=\"background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.06);\">"

            + "<tr><td style=\"background:linear-gradient(135deg,#1e293b,#0f172a);padding:26px 32px;text-align:center;\">"
            +   "<span style=\"color:#ffffff;font-size:20px;font-weight:700;\">Payment Received</span>"
            + "</td></tr>"

            + "<tr><td style=\"padding:32px;\">"

            +   "<p style=\"margin:0 0 4px 0;color:#111827;font-size:15px;\">Hi <b>" + residentName + "</b>,</p>"
            +   "<p style=\"margin:0 0 20px 0;color:#111827;font-size:15px;line-height:1.5;\">"
            +     "Your <b>" + purposeLabel + "</b> payment has been "
            +     "<b style=\"color:#16a34a;\">received</b> successfully. This email confirms the payment "
            +     "against your Pravesh account.</p>"

            +   "<div style=\"background-color:#f8fafc;border-radius:8px;padding:4px 20px;\">"
            +   "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            +     detailRow("Amount Paid", "<span style=\"color:#c2410c;font-size:16px;\">&#8377;" + String.format("%.2f", amount) + "</span>")
            +     detailRow("Payment For", purposeLabel)
            +     detailRow("Receipt No.", "#PVS-" + paymentOrderId)
            +     detailRowLast("Paid On", formattedDate)
            +   "</table>"
            +   "</div>"

            +   "<p style=\"margin:20px 0 0 0;color:#6b7280;font-size:13px;line-height:1.5;\">"
            +     "Keep this email for your records. If you believe this payment was made in error, "
            +     "please contact your society admin through the Pravesh app.</p>"

            +   "<div style=\"text-align:center;margin-top:24px;\">"
            +     "<a href=\"#\" style=\"background:linear-gradient(135deg,#f97316,#ea580c);color:#ffffff;"
            +       "text-decoration:none;font-weight:700;font-size:14px;padding:12px 28px;border-radius:8px;"
            +       "display:inline-block;\">View Payment History</a>"
            +   "</div>"

            + "</td></tr>"

            + "<tr><td style=\"background-color:#f8fafc;padding:16px 32px;text-align:center;border-top:1px solid #e5e7eb;\">"
            +   "<span style=\"color:#6b7280;font-size:12px;\">Pravesh &mdash; Visitor Access Control</span>"
            + "</td></tr>"

            + "</table>"
            + "</td></tr>"
            + "</table>"
            + "</body></html>";
    }

    private static String detailRow(String label, String value) {
        return "<tr>"
            + "<td style=\"padding:8px 0;color:#6b7280;font-size:14px;border-bottom:1px dashed #e5e7eb;\">" + label + "</td>"
            + "<td style=\"padding:8px 0;text-align:right;font-weight:700;color:#111827;font-size:14px;border-bottom:1px dashed #e5e7eb;\">" + value + "</td>"
            + "</tr>";
    }

    private static String detailRowLast(String label, String value) {
        return "<tr>"
            + "<td style=\"padding:8px 0;color:#6b7280;font-size:14px;\">" + label + "</td>"
            + "<td style=\"padding:8px 0;text-align:right;font-weight:700;color:#111827;font-size:14px;\">" + value + "</td>"
            + "</tr>";
    }
}
