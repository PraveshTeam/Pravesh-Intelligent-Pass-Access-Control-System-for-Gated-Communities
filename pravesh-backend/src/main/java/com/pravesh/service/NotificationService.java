package com.pravesh.service;

import com.pravesh.dto.request.GateEntryNotifyRequest;
import com.pravesh.dto.request.GuardCredentialsRequest;
import com.pravesh.dto.request.PassCreatedRequest;
import com.pravesh.dto.request.PassRevokedRequest;
import com.pravesh.dto.request.RelocationApprovedRequest;
import com.pravesh.dto.request.ResidentApprovedRequest;
import com.pravesh.dto.request.SocietyAdminApprovedRequest;
import com.pravesh.dto.request.VisitorEnteredRequest;
import com.pravesh.dto.response.WebSocketNotificationPayload;
import com.pravesh.dto.response.UserContactResponse;
import com.pravesh.util.DateFormatUtil;
import com.pravesh.util.EmailTemplates;
import com.pravesh.util.SmsTemplates;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Sends OTP / pass / SOS / payment / onboarding / relocation alerts via
 * email, SMS and WebSocket, directly and synchronously.
 *
 * There is no notification history/panel anymore: nothing here is persisted
 * (no Mongo, no "notification center" API) — this class only dispatches
 * messages, it never stores them. The app has one datastore (MySQL) now.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

	@org.springframework.beans.factory.annotation.Value("${pravesh.app-link}")
    private String appLink;

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final SimpMessagingTemplate messagingTemplate;
	private final EmailService emailService;
	private final SmsService smsService;
	private final com.pravesh.service.UserDirectoryService userDirectoryService;

	public void handleVisitorEntered(VisitorEnteredRequest req) {
		messagingTemplate.convertAndSend("/topic/flat/" + req.residentId() + "/notifications",
				new WebSocketNotificationPayload("VISITOR_ENTERED", req.visitorName(), req.scannedAt(),
						req.gateName()));

		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendSms(contact.phone(),
					SmsTemplates.visitorEntered(req.visitorName(), req.gateName(), req.scannedAt()));
			safeSendHtmlEmail(contact.email(), "Visitor Entered",
					EmailTemplates.visitorEntered(contact.name(), req.visitorName(), req.gateName(), req.scannedAt()));
		}
	}

	public void handlePassCreated(PassCreatedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		String formattedFrom = DateFormatUtil.format(req.validFrom());
		String formattedUntil = DateFormatUtil.format(req.validUntil());

		if (contact != null) {
			if (req.qrBase64() != null) {
				try {
					emailService.sendHtmlEmailWithInlineImage(
							contact.email(), "Visitor Pass Created", EmailTemplates.passCreated(contact.name(),
									req.flatNumber(), req.visitorName(), req.passUuid(), formattedFrom, formattedUntil),
							req.qrBase64(), "qrImage");
				} catch (Exception e) {
					log.warn("Failed to send pass-created email with QR to {}: {}", contact.email(), e.getMessage());
				}
			} else {
				safeSendHtmlEmail(contact.email(), "Visitor Pass Created", EmailTemplates.passCreated(contact.name(),
						req.flatNumber(), req.visitorName(), req.passUuid(), formattedFrom, formattedUntil));
			}
			safeSendSms(contact.phone(),
					SmsTemplates.passCreated(req.visitorName(), req.validFrom(), req.validUntil()));
		}
	}

	public void handlePassRevoked(PassRevokedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Visitor Pass Revoked",
					EmailTemplates.passRevoked(contact.name(), req.visitorName(), req.passUuid()));
			safeSendSms(contact.phone(), SmsTemplates.passRevoked(req.visitorName()));
		}
	}

	private UserContactResponse fetchContact(Long userId) {
		try {
			return userDirectoryService.getContact(userId);
		} catch (Exception e) {
			log.warn("Failed to fetch contact for userId {}: {}", userId, e.getMessage());
			return null;
		}
	}

	private void safeSendHtmlEmail(String email, String subject, String html) {
		try {
			emailService.sendHtmlEmail(email, subject, html);
		} catch (Exception e) {
			log.warn("Failed to send email to {}: {}", email, e.getMessage());
		}
	}

	private void safeSendSms(String phone, String body) {
		try {
			smsService.sendSms(phone, body);
		} catch (Exception e) {
			log.warn("Failed to send SMS to {}: {}", phone, e.getMessage());
		}
	}

	public void handleResidentApproved(ResidentApprovedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Welcome to Pravesh 🎉",
					EmailTemplates.residentApproved(contact.name(), req.flatNumber(), appLink));
		}
	}

	public void handleSocietyAdminApproved(SocietyAdminApprovedRequest req) {
		UserContactResponse contact = fetchContact(req.adminId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Welcome to Pravesh 🎉",
					EmailTemplates.societyAdminApproved(contact.name(), req.societyName(), appLink));
		}
	}
	
	public void handleGuardCredentials(GuardCredentialsRequest req) {
        String smsBody = "Pravesh: Your guard login for " + req.gateName()
                + " — username: guard." + req.phone() + "@pravesh.local, temp password: "
                + req.tempPassword() + ". Please log in and note this down.";

        safeSendSms(req.phone(), smsBody);
    }
	
	public void handleGateEntryRequest(GateEntryNotifyRequest req) {
        String smsBody = "Pravesh: " + req.visitorName() + " is at the gate for flat "
                + req.flatNumber() + ". Open the Pravesh app to approve or deny entry.";

        safeSendSms(req.residentPhone(), smsBody);
    }

	public void handleRelocationApproved(RelocationApprovedRequest req) {
		UserContactResponse contact = fetchContact(req.residentId());
		if (contact != null) {
			safeSendHtmlEmail(contact.email(), "Your Relocation Was Approved ✅",
					EmailTemplates.relocationApproved(contact.name(),
							req.oldFlatNumber(), req.oldSocietyName(),
							req.newFlatNumber(), req.newTower(), req.newSocietyName(), appLink));
		}
	}

	private static final int OTP_EXPIRY_MINUTES = 10;
	private static final String REGISTRATION_VERIFICATION = "REGISTRATION_VERIFICATION";

	/**
	 * Sends an OTP by email/SMS. Called directly (no queue, no persistence)
	 * from AuthService (forgot-password) and RegistrationVerificationService
	 * (registration OTP) — both used to write an outbox row that a background
	 * poller published to RabbitMQ for this same logic to run a few seconds later;
	 * now it just runs immediately, in the same transaction as the request.
	 */
	public void handleOtpRequested(String correlationId, Long userId, String email, String phone,
			String otp, String channel, String purpose) {

		boolean isRegistration = REGISTRATION_VERIFICATION.equals(purpose);

		String subject = isRegistration
				? "Verify Your Details — Pravesh"
				: "Your Pravesh Password Reset Code";
		String emailHtml = isRegistration
				? EmailTemplates.otpRegistrationVerification(otp, OTP_EXPIRY_MINUTES)
				: EmailTemplates.otpPasswordReset(otp, OTP_EXPIRY_MINUTES);
		String smsText = isRegistration
				? SmsTemplates.registrationOtp(otp)
				: SmsTemplates.otp(otp);

		boolean sendEmail = "EMAIL".equals(channel) || "BOTH".equals(channel);
		boolean sendSms = "SMS".equals(channel) || "BOTH".equals(channel);

		if (sendEmail) {
			safeSendHtmlEmail(email, subject, emailHtml);
		}
		if (sendSms && phone != null && !phone.isBlank()) {
			safeSendSms(phone, smsText);
		}

		log.info("OTP {} ({}) dispatched — email/SMS sent directly, no queue", correlationId,
				isRegistration ? REGISTRATION_VERIFICATION : "PASSWORD_RESET");
	}

	/**
	 * Pushes an SOS alert over WebSocket and SMS's the on-duty guard/admin.
	 * Called directly from SosService — used to go through an outbox row +
	 * RabbitMQ queue + this same logic in a separate listener; now it's just
	 * one synchronous call.
	 */
	public void handleSosEvent(String eventType, Long alertId, Long residentUserId, String residentName,
			String residentPhone, String flatNumber, String category, String description,
			String status, Long societyId) {

		var payload = new java.util.HashMap<String, Object>();
		payload.put("eventType", eventType);
		payload.put("id", alertId);
		payload.put("residentUserId", residentUserId);
		payload.put("residentName", residentName);
		payload.put("residentPhone", residentPhone);
		payload.put("flatNumber", flatNumber);
		payload.put("category", category);
		payload.put("description", description);
		payload.put("status", status);
		payload.put("societyId", societyId);

		// Guard/admin live banner, scoped to the whole society.
		messagingTemplate.convertAndSend("/topic/sos/" + societyId, (Object) payload);

		// The raising resident's own private live-status topic.
		if (residentUserId != null) {
			messagingTemplate.convertAndSend("/topic/sos-status/" + residentUserId, (Object) payload);
		}

		if (!"SOS_RAISED".equals(eventType)) {
			return; // status updates don't need a fresh SMS dispatch
		}

		var contact = userDirectoryService.getEmergencyContact(societyId);
		if (contact == null || "NONE".equals(contact.role()) || contact.phone() == null) {
			log.warn("No guard or admin available to notify for SOS alert — society {}, resident {}",
					societyId, residentName);
			return;
		}

		String body = "SOS ALERT (" + category + ") from " + residentName
				+ ", Flat " + flatNumber + ". Respond immediately via Pravesh.";
		safeSendSms(contact.phone(), body);
		log.info("SOS SMS sent to {} ({}) for alert from {} at flat {}",
				contact.name(), contact.role(), residentName, flatNumber);
	}

	/**
	 * Emails a payment receipt. Called directly from PaymentService right
	 * after a webhook confirms payment — used to go through an outbox row +
	 * RabbitMQ queue + this same logic in a separate listener; now it's just
	 * one synchronous call in the webhook request.
	 */
	public void handlePaymentReceipt(Long paymentOrderId, Long residentId, double amount,
			String purpose, java.time.LocalDateTime paidAt) {

		UserContactResponse contact = fetchContact(residentId);
		if (contact == null) {
			log.warn("Could not resolve resident {} for payment receipt (order {}) — notification skipped",
					residentId, paymentOrderId);
			return;
		}

		String title = "Payment Receipt";
		String formattedDate = paidAt.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"));

		if (contact.email() != null) {
			safeSendHtmlEmail(contact.email(), title,
					com.pravesh.util.PaymentReceiptEmailBuilder.build(contact.name(), purpose, amount, formattedDate, paymentOrderId));
		} else {
			log.warn("Resident {} has no email on file — payment receipt for order {} not sent anywhere",
					residentId, paymentOrderId);
		}

		log.info("Payment receipt delivered to {} for order {} ({} Rs.{})", contact.name(), paymentOrderId, purpose, amount);
	}
}
