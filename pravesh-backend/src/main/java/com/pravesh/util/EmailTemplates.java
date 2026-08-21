package com.pravesh.util;

public class EmailTemplates {

	private static String wrapper(String title, String bodyHtml) {
		return """
				<div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
				    <h2 style="color: #2c3e50;">%s</h2>
				    %s
				    <p style="color: #888; font-size: 12px; margin-top: 24px;">— Pravesh Security System</p>
				</div>
				"""
				.formatted(title, bodyHtml);
	}

	public static String passCreated(String residentName, String flatNumber, String visitorName, String uuid,
			String validFrom, String validUntil) {
		String body = """
				<p>Dear %s,</p>
				<p>A visitor pass has been created for your flat <b>%s</b>.</p>
				<table style="width:100%%; border-collapse: collapse; margin: 12px 0;">
				    <tr><td style="padding:4px 0; color:#555;">Visitor Name</td><td><b>%s</b></td></tr>
				    <tr><td style="padding:4px 0; color:#555;">Pass UUID</td><td>%s</td></tr>
				    <tr><td style="padding:4px 0; color:#555;">Valid From</td><td>%s</td></tr>
				    <tr><td style="padding:4px 0; color:#555;">Valid Until</td><td>%s</td></tr>
				</table>
				<p>Scan this QR code at the gate:</p>
				<img src="cid:qrImage" style="width:200px; height:200px;" />
				<p>Share the Pass UUID or QR code with your visitor for entry.</p>
				""".formatted(residentName, flatNumber, visitorName, uuid, validFrom, validUntil);
		return wrapper("Visitor Pass Created", body);
	}

	public static String passRevoked(String residentName, String visitorName, String uuid) {
		String body = """
				<p>Dear %s,</p>
				<p>The visitor pass for <b>%s</b> has been revoked and is no longer valid.</p>
				<p style="color:#888;">Pass UUID: %s</p>
				""".formatted(residentName, visitorName, uuid);
		return wrapper("Visitor Pass Revoked", body);
	}

	public static String visitorEntered(String residentName, String visitorName, String gateName, String enteredAt) {
		String body = """
				<p>Dear %s,</p>
				<p><b>%s</b> entered via <b>%s</b> at %s.</p>
				""".formatted(residentName, visitorName, gateName, enteredAt);
		return wrapper("Visitor Entered", body);
	}

	public static String residentApproved(String residentName, String flatNumber, String appLink) {
		String body = """
				<p>Dear %s,</p>
				<p>Great news — your onboarding request has been <b>approved</b>! You're now a verified resident of flat <b>%s</b>.</p>
				<p>You now have full access to Pravesh — create visitor passes, raise SOS alerts, connect with your community, and more.</p>
				<div style="text-align:center; margin: 24px 0;">
				    <a href="%s" style="background:#2c3e50; color:#fff; padding:12px 24px; text-decoration:none; border-radius:6px; display:inline-block;">
				        Open Pravesh
				    </a>
				</div>
				<p>Welcome aboard!</p>
				"""
				.formatted(residentName, flatNumber, appLink);
		return wrapper("Welcome to Pravesh 🎉", body);
	}

	public static String societyAdminApproved(String adminName, String societyName, String appLink) {
		String body = """
				<p>Dear %s,</p>
				<p>Congratulations — your society <b>%s</b> has been <b>approved</b> and is now live on Pravesh!</p>
				<p>You now have full administrative access — manage residents, guards, gates, analytics, and every module of your society's platform.</p>
				<div style="text-align:center; margin: 24px 0;">
				    <a href="%s" style="background:#2c3e50; color:#fff; padding:12px 24px; text-decoration:none; border-radius:6px; display:inline-block;">
				        Open Pravesh Admin Dashboard
				    </a>
				</div>
				<p>Welcome to Pravesh!</p>
				"""
				.formatted(adminName, societyName, appLink);
		return wrapper("Welcome to Pravesh 🎉", body);
	}

	public static String relocationApproved(String residentName, String oldFlatNumber, String oldSocietyName,
			String newFlatNumber, String newTower, String newSocietyName, String appLink) {
		String towerPart = (newTower != null && !newTower.isBlank()) ? " (Tower " + newTower + ")" : "";
		return """
				<div style="font-family:'Segoe UI',Arial,sans-serif;max-width:560px;margin:0 auto;
				border:1px solid #e4eaf1;border-radius:14px;overflow:hidden;">
				<div style="background:linear-gradient(135deg,#1a3c5e,#102841);padding:28px 24px;text-align:center;">
				<h2 style="color:#ffffff;margin:0;font-size:20px;">Relocation Approved</h2>
				</div>
				<div style="padding:28px 24px;color:#1f2b3a;">
				<p style="font-size:15px;line-height:1.7;margin:0 0 18px;">
				Hi <strong>%s</strong>,
				</p>
				<p style="font-size:15px;line-height:1.7;margin:0 0 22px;">
				Your request to change flat has been <strong style="color:#16a34a;">approved</strong>.
				Your Pravesh account is now linked to your new residence.
				</p>

				<div style="background:#f5f7fa;border-radius:10px;padding:16px 18px;margin-bottom:22px;">
				<table style="width:100%%;font-size:14px;border-collapse:collapse;">
				<tr>
				<td style="color:#5c6b7a;padding:6px 0;">Previous</td>
				<td style="text-align:right;padding:6px 0;">%s · %s</td>
				</tr>
				<tr>
				<td style="color:#5c6b7a;padding:6px 0;border-top:1px dashed #e4eaf1;">New</td>
				<td style="text-align:right;padding:6px 0;border-top:1px dashed #e4eaf1;">
				<strong>%s%s · %s</strong>
				</td>
				</tr>
				</table>
				</div>

				<p style="font-size:14px;line-height:1.7;color:#5c6b7a;margin:0 0 24px;">
				Any visitor passes and entry records from your previous flat remain unchanged in your
				history — they stay correctly associated with where they actually happened. All new
				passes you create will be for your new flat.
				</p>

				<div style="text-align:center;">
				<a href="%s" style="display:inline-block;background:linear-gradient(135deg,#e8871a,#f5a83f);
				color:#ffffff;text-decoration:none;padding:13px 30px;border-radius:10px;
				font-weight:700;font-size:14px;">Open Pravesh</a>
				</div>
				</div>
				<div style="background:#f5f7fa;padding:16px;text-align:center;font-size:12px;color:#5c6b7a;">
				Pravesh — Visitor Access Control
				</div>
				</div>
				""".formatted(residentName, oldFlatNumber, oldSocietyName, newFlatNumber, towerPart, newSocietyName,
				appLink);
	}

	private static String otpCode(String heading, String introHtml, String otp, int expiryMinutes) {
		String body = """
				%s
				<div style="text-align:center; margin: 28px 0;">
				    <span style="display:inline-block; background:#f5f7fa; border:1px dashed #c7d2dd;
				        border-radius:10px; padding:16px 32px; font-size:32px; font-weight:700;
				        letter-spacing:8px; color:#2c3e50;">%s</span>
				</div>
				<p style="color:#5c6b7a; font-size:13px; text-align:center; margin:0;">
				    This code expires in %d minutes. Never share it with anyone.
				</p>
				"""
				.formatted(introHtml, otp, expiryMinutes);
		return wrapper(heading, body);
	}

	public static String otpPasswordReset(String otp, int expiryMinutes) {
		String intro = "<p>We received a request to reset your Pravesh account password. "
				+ "Use the code below to continue:</p>";
		return otpCode("Reset Your Password", intro, otp, expiryMinutes);
	}

	public static String otpRegistrationVerification(String otp, int expiryMinutes) {
		String intro = "<p>Use the code below to verify this email or phone number and finish "
				+ "creating your Pravesh account:</p>";
		return otpCode("Verify Your Details", intro, otp, expiryMinutes);
	}
}