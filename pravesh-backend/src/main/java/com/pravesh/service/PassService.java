package com.pravesh.service;

import com.pravesh.dto.request.CreatePassRequest;
import com.pravesh.dto.response.PassLockResponse;
import com.pravesh.dto.response.PassResponse;
import com.pravesh.dto.response.PassValidationResponse;
import com.pravesh.entity.Resident;
import com.pravesh.entity.Society;
import com.pravesh.entity.VisitorPass;
import com.pravesh.entity.enums.PassStatus;
import com.pravesh.entity.enums.PassType;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.request.PassCreatedRequest;
import com.pravesh.dto.request.PassRevokedRequest;
import com.pravesh.repository.VisitorPassRepository;
import com.pravesh.util.EntityRefs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassService {

	private final VisitorPassRepository passRepository;
	private final QRCodeService qrCodeService;
	private final com.pravesh.service.NotificationService notificationService;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PassService.class);
	private final com.pravesh.service.UserDirectoryService userDirectoryService;
	private final EntityRefs refs;

	@Transactional
	public PassResponse createPass(Long residentId, Long societyId, CreatePassRequest req) {
		if (req.validFrom().isBefore(LocalDateTime.now())) {
			throw new InvalidStateException("validFrom cannot be in the past");
		}
		if (!req.validFrom().isBefore(req.validUntil())) {
			throw new InvalidStateException("validFrom must be before validUntil");
		}
		if (req.validUntil().isBefore(LocalDateTime.now())) {
			throw new InvalidStateException("validUntil must be in the future");
		}
		if (req.passType() == PassType.MULTI_USE && (req.usesAllowed() == null || req.usesAllowed() < 1)) {
			throw new InvalidStateException("usesAllowed is required for MULTI_USE passes");
		}

		String uuid = UUID.randomUUID().toString();

		VisitorPass pass = VisitorPass.builder().resident(refs.ref(Resident.class, residentId))
				.society(refs.ref(Society.class, societyId)).uuid(uuid)
				.visitorName(req.visitorName()).visitorPhone(req.visitorPhone()).passType(req.passType())
				.usesAllowed(req.passType() == PassType.MULTI_USE ? req.usesAllowed() : null)
				.usesRemaining(req.passType() == PassType.MULTI_USE ? req.usesAllowed() : null)
				.validFrom(req.validFrom()).validUntil(req.validUntil()).status(PassStatus.ACTIVE).build();

		pass = passRepository.save(pass);

		String qrBase64 = qrCodeService.generate(uuid);

		String flatNumber = "N/A";
		try {
			flatNumber = userDirectoryService.getFlatNumber(residentId);
		} catch (Exception e) {
			log.warn("Failed to fetch flat number for resident {}: {}", residentId, e.getMessage());
		}

		try {
			notificationService.handlePassCreated(new PassCreatedRequest(residentId, flatNumber, req.visitorName(),
					uuid, req.validFrom().toString(), req.validUntil().toString(), qrBase64));
		} catch (Exception e) {
			log.warn("Failed to notify Notification-Service of pass creation for resident {}: {}", residentId,
					e.getMessage());
		}

		return toResponse(pass, qrBase64);
	}

	public List<PassResponse> getMyActivePasses(Long residentId, Long societyId) {
		return passRepository.findByResident_UserIdAndStatusAndSocietyId(residentId, PassStatus.ACTIVE, societyId).stream()
				.map(p -> toResponse(p, null)).toList();
	}

	public List<PassResponse> getMyPassHistory(Long residentId, Long societyId) {
		return passRepository.findByResident_UserIdAndSocietyId(residentId, societyId).stream().map(p -> toResponse(p, null))
				.toList();
	}

	public PassResponse getPassDetail(Long passId, Long residentId) {
		VisitorPass pass = passRepository.findById(passId)
				.orElseThrow(() -> new ResourceNotFoundException("Pass not found"));
		if (!pass.getResident().getUserId().equals(residentId)) {
			throw new org.springframework.security.access.AccessDeniedException("This pass does not belong to you");
		}
		return toResponse(pass, null);
	}

	public String regenerateQr(Long passId, Long residentId) {
		VisitorPass pass = passRepository.findById(passId)
				.orElseThrow(() -> new ResourceNotFoundException("Pass not found"));
		if (!pass.getResident().getUserId().equals(residentId)) {
			throw new org.springframework.security.access.AccessDeniedException("This pass does not belong to you");
		}
		if (pass.getStatus() != PassStatus.ACTIVE) {
			throw new InvalidStateException("Only ACTIVE passes have a valid QR code");
		}
		return qrCodeService.generate(pass.getUuid());
	}

	private PassResponse toResponse(VisitorPass pass, String qrBase64) {
		return new PassResponse(pass.getId(), pass.getUuid(), pass.getVisitorName(), pass.getVisitorPhone(),
				pass.getPassType().name(), pass.getUsesAllowed(), pass.getUsesRemaining(), pass.getValidFrom(),
				pass.getValidUntil(), pass.getStatus().name(), qrBase64);
	}

	@Transactional
	public void revokePass(Long passId, Long residentId) {
		VisitorPass pass = passRepository.findById(passId)
				.orElseThrow(() -> new ResourceNotFoundException("Pass not found"));

		if (!pass.getResident().getUserId().equals(residentId)) {
			throw new org.springframework.security.access.AccessDeniedException("This pass does not belong to you");
		}
		if (pass.getStatus() != PassStatus.ACTIVE) {
			throw new InvalidStateException(
					"Only ACTIVE passes can be revoked (current status: " + pass.getStatus() + ")");
		}

		pass.setStatus(PassStatus.REVOKED);
		passRepository.save(pass);

		try {
			notificationService.handlePassRevoked(
					new PassRevokedRequest(pass.getResident().getUserId(), pass.getVisitorName(), pass.getUuid()));
		} catch (Exception e) {
			// Non-fatal: the revocation itself already committed.
			log.warn("Failed to notify Notification-Service of pass revocation for pass {}: {}", pass.getId(),
					e.getMessage());
		}
	}

	@Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
	public PassValidationResponse validateAndConsume(String uuid, Long callerSocietyId) {
		VisitorPass pass = passRepository.findByUuidForUpdate(uuid).orElse(null);

		if (pass == null) {
			return PassValidationResponse.denied("QR_INVALID");
		}

		if (!pass.getSociety().getId().equals(callerSocietyId)) {
			return PassValidationResponse.denied("WRONG_SOCIETY");
		}

		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(pass.getValidFrom())) {
			return PassValidationResponse.denied("QR_NOT_YET_ACTIVE");
		}
		if (now.isAfter(pass.getValidUntil()) || pass.getStatus() == PassStatus.EXPIRED) {
			return PassValidationResponse.denied("QR_EXPIRED");
		}
		if (pass.getStatus() == PassStatus.REVOKED) {
			return PassValidationResponse.denied("REVOKED");
		}
		if (pass.getStatus() == PassStatus.CONSUMED) {
			return PassValidationResponse.denied("ALREADY_USED");
		}

		if (pass.getPassType() == PassType.ONE_TIME) {
			pass.setStatus(PassStatus.CONSUMED);

		} else if (pass.getPassType() == PassType.RECURRING_DAILY) {
			// One entry per calendar day; the pass itself stays ACTIVE.
			LocalDate today = LocalDate.now();
			if (today.equals(pass.getLastUsedDate())) {
				return PassValidationResponse.denied("ALREADY_USED_TODAY");
			}
			pass.setLastUsedDate(today);

		} else if (pass.getPassType() == PassType.MULTI_USE) {
			pass.setUsesRemaining(pass.getUsesRemaining() - 1);
			if (pass.getUsesRemaining() <= 0) {
				pass.setStatus(PassStatus.CONSUMED);
			}
		}
		passRepository.save(pass);
		return PassValidationResponse.granted(pass.getId(), pass.getResident().getUserId(), pass.getVisitorName(),
				pass.getPassType().name());
	}

	public List<PassResponse> getAllPassesInSociety(Long societyId) {
		return passRepository.findBySocietyId(societyId).stream().map(p -> toResponse(p, null)).toList();
	}
}