package com.pravesh.util;

public class SmsTemplates {

    public static String otp(String otp) {
        return "Pravesh: Your password reset code is " + otp + ". Valid for 10 min. Do not share this code.";
    }

    public static String registrationOtp(String otp) {
        return "Pravesh: Your verification code is " + otp + ". Valid for 10 min. Do not share this code.";
    }

    public static String passCreated(String visitorName, String validFrom, String validUntil) {
        return "Pravesh: Pass created for " + visitorName + ". Valid " + validFrom + " to " + validUntil + ".";
    }

    public static String passRevoked(String visitorName) {
        return "Pravesh: Pass for " + visitorName + " has been revoked and is no longer valid.";
    }

    public static String visitorEntered(String visitorName, String gateName, String enteredAt) {
        return "Pravesh: " + visitorName + " entered via " + gateName + " at " + enteredAt + ".";
    }
}