package com.pravesh.service;

import com.pravesh.dto.request.DisplacementNotifyRequest;
import com.pravesh.dto.request.GateEntryNotifyRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String toPhone, String messageBody) {
        try {
            String formattedTo = toPhone.startsWith("+") ? toPhone : "+91" + toPhone;
            Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhone, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
    
    public void handleGateEntryRequest(GateEntryNotifyRequest req) {
        String smsBody = "Pravesh: " + req.visitorName() + " is at the gate for flat "
                + req.flatNumber() + ". Open the Pravesh app to approve or deny entry.";
        try {
            sendSms(req.residentPhone(), smsBody);
        } catch (Exception e) {
            log.warn("Failed to send gate-entry SMS to {}: {}", req.residentPhone(), e.getMessage());
        }
    }
    
    public void handleFlatDisplacement(DisplacementNotifyRequest req) {
        String smsBody = "Pravesh: You have been removed from flat " + req.oldFlatNumber()
                + " by your society admin. Please submit a new onboarding request if this was made in error, "
                + "or to claim a different flat.";
        try {
            sendSms(req.residentPhone(), smsBody);
        } catch (Exception e) {
            log.warn("Failed to send displacement SMS to {}: {}", req.residentPhone(), e.getMessage());
        }
    }
}