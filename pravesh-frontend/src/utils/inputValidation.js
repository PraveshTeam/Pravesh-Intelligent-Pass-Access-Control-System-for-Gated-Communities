// Shared input validation/sanitization helpers.
// Patterns intentionally mirror the backend's own validation regex
// (RegisterRequest, RegistrationVerificationService, OnboardingService,
// ResidentRelocationService) so a value accepted here is guaranteed to
// also be accepted server-side.

export const PHONE_PATTERN = /^[6-9]\d{9}$/
export const EMAIL_PATTERN = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/
export const FLAT_NUMBER_PATTERN = /^[A-Z]-\d{1,5}$/
export const OTP_PATTERN = /^\d{6}$/

/** Strips everything but digits and caps at 10 characters — for phone number inputs. */
export function sanitizePhoneInput(value) {
  return (value || '').replace(/\D/g, '').slice(0, 10)
}

/** Uppercases and strips anything except A-Z, 0-9, and hyphen — for flat number inputs. */
export function sanitizeFlatNumberInput(value) {
  return (value || '').toUpperCase().replace(/[^A-Z0-9-]/g, '').slice(0, 7)
}

/** Strips everything but digits — for OTP inputs. */
export function sanitizeDigitsInput(value, maxLength = 6) {
  return (value || '').replace(/\D/g, '').slice(0, maxLength)
}

export function isValidPhone(value) {
  return PHONE_PATTERN.test(value || '')
}

export function isValidEmail(value) {
  return EMAIL_PATTERN.test(value || '')
}

export function isValidFlatNumber(value) {
  return FLAT_NUMBER_PATTERN.test(value || '')
}

export const PHONE_HINT = '10-digit mobile number starting with 6-9'
export const FLAT_NUMBER_HINT = 'Format: one capital letter, a hyphen, then up to 5 digits (e.g. A-101)'
