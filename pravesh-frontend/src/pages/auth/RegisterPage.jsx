import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register, sendRegistrationOtp, verifyRegistrationOtp } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { INDIAN_STATES } from '../../utils/indianStates'
import PasswordInput from '../../components/common/PasswordInput'
import { sanitizePhoneInput, isValidPhone, isValidEmail, PHONE_HINT } from '../../utils/inputValidation'
import logoMark from '../../assets/logo.png'

export default function RegisterPage() {
  const { loginUser } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    name: '', email: '', phone: '', password: '', state: '', role: 'RESIDENT'
  })
  const [loading, setLoading] = useState(false)

  // ── Email verification state ──
  const [emailOtp, setEmailOtp] = useState('')
  const [emailOtpSent, setEmailOtpSent] = useState(false)
  const [emailVerified, setEmailVerified] = useState(false)
  const [emailSending, setEmailSending] = useState(false)
  const [emailVerifying, setEmailVerifying] = useState(false)
  const [emailCooldown, setEmailCooldown] = useState(0)

  // ── Phone verification state ──
  const [phoneOtp, setPhoneOtp] = useState('')
  const [phoneOtpSent, setPhoneOtpSent] = useState(false)
  const [phoneVerified, setPhoneVerified] = useState(false)
  const [phoneSending, setPhoneSending] = useState(false)
  const [phoneVerifying, setPhoneVerifying] = useState(false)
  const [phoneCooldown, setPhoneCooldown] = useState(0)

  useEffect(() => {
    if (emailCooldown <= 0) return
    const t = setTimeout(() => setEmailCooldown(c => c - 1), 1000)
    return () => clearTimeout(t)
  }, [emailCooldown])

  useEffect(() => {
    if (phoneCooldown <= 0) return
    const t = setTimeout(() => setPhoneCooldown(c => c - 1), 1000)
    return () => clearTimeout(t)
  }, [phoneCooldown])

  const strength = (pw) => {
    if (!pw) return null
    if (pw.length < 8) return { label: 'Too short (min 8 characters)', cls: 'bg-danger', pct: 33 }
    if (pw.length < 12) return { label: 'Good', cls: 'bg-warning', pct: 66 }
    return { label: 'Strong', cls: 'bg-success', pct: 100 }
  }

  const handleEmailChange = (val) => {
    setForm(f => ({ ...f, email: val }))
    if (emailVerified || emailOtpSent) {
      setEmailVerified(false); setEmailOtpSent(false); setEmailOtp('')
    }
  }

  const handlePhoneChange = (val) => {
    setForm(f => ({ ...f, phone: sanitizePhoneInput(val) }))
    if (phoneVerified || phoneOtpSent) {
      setPhoneVerified(false); setPhoneOtpSent(false); setPhoneOtp('')
    }
  }

  const sendEmailOtp = async () => {
    if (!form.email) { showToast('Please enter your email first.', 'warning'); return }
    if (!isValidEmail(form.email)) { showToast('Enter a valid email address.', 'warning'); return }
    setEmailSending(true)
    try {
      await sendRegistrationOtp({ contactType: 'EMAIL', value: form.email })
      showToast('OTP sent to your email.', 'success')
      setEmailOtpSent(true); setEmailCooldown(90)
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to send OTP.', 'error')
    } finally { setEmailSending(false) }
  }

  const verifyEmailOtp = async () => {
    if (emailOtp.length !== 6) { showToast('Enter the full 6-digit code.', 'warning'); return }
    setEmailVerifying(true)
    try {
      await verifyRegistrationOtp({ contactType: 'EMAIL', value: form.email, otp: emailOtp })
      setEmailVerified(true); setEmailOtpSent(false); setEmailOtp('')
      showToast('Email verified.', 'success')
    } catch (err) {
      showToast(err.response?.data?.message || 'Invalid or expired code.', 'error')
    } finally { setEmailVerifying(false) }
  }

  const sendPhoneOtp = async () => {
    if (!form.phone) { showToast('Please enter your phone number first.', 'warning'); return }
    if (!isValidPhone(form.phone)) { showToast('Enter a valid 10-digit phone number starting with 6-9.', 'warning'); return }
    setPhoneSending(true)
    try {
      await sendRegistrationOtp({ contactType: 'PHONE', value: form.phone })
      showToast('OTP sent to your phone.', 'success')
      setPhoneOtpSent(true); setPhoneCooldown(90)
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to send OTP.', 'error')
    } finally { setPhoneSending(false) }
  }

  const verifyPhoneOtp = async () => {
    if (phoneOtp.length !== 6) { showToast('Enter the full 6-digit code.', 'warning'); return }
    setPhoneVerifying(true)
    try {
      await verifyRegistrationOtp({ contactType: 'PHONE', value: form.phone, otp: phoneOtp })
      setPhoneVerified(true); setPhoneOtpSent(false); setPhoneOtp('')
      showToast('Phone number verified.', 'success')
    } catch (err) {
      showToast(err.response?.data?.message || 'Invalid or expired code.', 'error')
    } finally { setPhoneVerifying(false) }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.state) { showToast('Please select your state.', 'warning'); return }
    if (!emailVerified) { showToast('Please verify your email first.', 'warning'); return }
    if (!phoneVerified) { showToast('Please verify your phone number first.', 'warning'); return }
    setLoading(true)
    try {
      const res = await register(form)
      loginUser(res.data.data)
      showToast(`Account created! Welcome, ${res.data.data.name}!`, 'success')
      navigate('/access-pending')
    } catch (err) {
      const data = err.response?.data
      const msg = data?.errors ? Object.values(data.errors).flat()[0] : (data?.message || 'Registration failed.')
      showToast(msg, 'error')
    } finally {
      setLoading(false)
    }
  }

  const pwStrength = strength(form.password)
  const canRegister = emailVerified && phoneVerified

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center py-4">
      <div className="card auth-card p-4" style={{ width: '100%', maxWidth: 480 }}>
        <div className="text-center mb-4">
          <Link to="/"><img src={logoMark} alt="Pravesh" style={{ width: 56 }} /></Link>
          <h4 className="fw-bold mt-2">Create Account</h4>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label fw-semibold">I am a</label>
            <select className="form-select" value={form.role}
              onChange={e => setForm({ ...form, role: e.target.value })}>
              <option value="RESIDENT">Resident</option>
              <option value="SOCIETY_ADMIN">Society Admin</option>
            </select>
          </div>
          <div className="mb-3">
            <label className="form-label fw-semibold">Full Name</label>
            <input className="form-control" value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })} required />
          </div>

          {/* ── Email + inline verification ── */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Email</label>
            <div className="d-flex gap-2">
              <input
                type="email"
                className="form-control"
                value={form.email}
                onChange={e => handleEmailChange(e.target.value)}
                readOnly={emailVerified}
                required
              />
              {emailVerified ? (
                <button type="button" className="btn btn-outline-success flex-shrink-0"
                  style={{ whiteSpace: 'nowrap' }}
                  onClick={() => { setEmailVerified(false); setEmailOtpSent(false); setEmailOtp('') }}>
                  <i className="bi bi-check-circle-fill me-1"></i>Verified
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-outline-primary flex-shrink-0"
                  style={{ whiteSpace: 'nowrap' }}
                  onClick={sendEmailOtp}
                  disabled={emailSending || !isValidEmail(form.email) || emailCooldown > 0}
                >
                  {emailSending
                    ? <span className="spinner-border spinner-border-sm"></span>
                    : emailCooldown > 0
                      ? `Resend ${emailCooldown}s`
                      : (emailOtpSent ? 'Resend OTP' : 'Send OTP')}
                </button>
              )}
            </div>

            {form.email && !isValidEmail(form.email) && !emailVerified && (
              <div className="form-text text-danger">Enter a valid email address.</div>
            )}

            {emailOtpSent && !emailVerified && (
              <div className="mt-2 p-2 border rounded bg-light">
                <label className="form-label small fw-semibold mb-1">
                  Enter the 6-digit code sent to {form.email}
                </label>
                <div className="d-flex gap-2">
                  <input
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    className="form-control"
                    placeholder="6-digit OTP"
                    value={emailOtp}
                    onChange={e => setEmailOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  />
                  <button type="button" className="btn btn-pravesh flex-shrink-0"
                    onClick={verifyEmailOtp} disabled={emailVerifying || emailOtp.length !== 6}>
                    {emailVerifying ? <span className="spinner-border spinner-border-sm"></span> : 'Verify'}
                  </button>
                </div>
              </div>
            )}

            {emailVerified && (
              <div className="small text-success mt-1">
                <i className="bi bi-patch-check-fill me-1"></i>Email verified
              </div>
            )}
          </div>

          {/* ── Phone + inline verification ── */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Phone</label>
            <div className="d-flex gap-2">
              <input
                className="form-control"
                value={form.phone}
                onChange={e => handlePhoneChange(e.target.value)}
                readOnly={phoneVerified}
                inputMode="numeric"
                maxLength={10}
                required
              />
              {phoneVerified ? (
                <button type="button" className="btn btn-outline-success flex-shrink-0"
                  style={{ whiteSpace: 'nowrap' }}
                  onClick={() => { setPhoneVerified(false); setPhoneOtpSent(false); setPhoneOtp('') }}>
                  <i className="bi bi-check-circle-fill me-1"></i>Verified
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-outline-primary flex-shrink-0"
                  style={{ whiteSpace: 'nowrap' }}
                  onClick={sendPhoneOtp}
                  disabled={phoneSending || !isValidPhone(form.phone) || phoneCooldown > 0}
                >
                  {phoneSending
                    ? <span className="spinner-border spinner-border-sm"></span>
                    : phoneCooldown > 0
                      ? `Resend ${phoneCooldown}s`
                      : (phoneOtpSent ? 'Resend OTP' : 'Send OTP')}
                </button>
              )}
            </div>

            {form.phone && !isValidPhone(form.phone) && !phoneVerified && (
              <div className="form-text text-danger">{PHONE_HINT}</div>
            )}

            {phoneOtpSent && !phoneVerified && (
              <div className="mt-2 p-2 border rounded bg-light">
                <label className="form-label small fw-semibold mb-1">
                  Enter the 6-digit code sent to {form.phone}
                </label>
                <div className="d-flex gap-2">
                  <input
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    className="form-control"
                    placeholder="6-digit OTP"
                    value={phoneOtp}
                    onChange={e => setPhoneOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  />
                  <button type="button" className="btn btn-pravesh flex-shrink-0"
                    onClick={verifyPhoneOtp} disabled={phoneVerifying || phoneOtp.length !== 6}>
                    {phoneVerifying ? <span className="spinner-border spinner-border-sm"></span> : 'Verify'}
                  </button>
                </div>
              </div>
            )}

            {phoneVerified && (
              <div className="small text-success mt-1">
                <i className="bi bi-patch-check-fill me-1"></i>Phone number verified
              </div>
            )}
          </div>

          <div className="mb-3">
            <label className="form-label fw-semibold">Password</label>
            <PasswordInput
              value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              placeholder="Min 8 characters"
              autoComplete="new-password"
              required
            />
            {pwStrength && (
              <div className="mt-1">
                <div className="progress" style={{ height: 4 }}>
                  <div className={`progress-bar ${pwStrength.cls}`} style={{ width: `${pwStrength.pct}%` }}></div>
                </div>
                <small className="text-muted">{pwStrength.label}</small>
              </div>
            )}
          </div>
          <div className="mb-3">
            <label className="form-label fw-semibold">State</label>
            <select className="form-select" value={form.state}
              onChange={e => setForm({ ...form, state: e.target.value })} required>
              <option value="">Select your state</option>
              {INDIAN_STATES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>

          <button type="submit" className="btn btn-pravesh w-100 py-2" disabled={loading || !canRegister}>
            {loading ? <span className="spinner-border spinner-border-sm me-2"></span>
              : <i className="bi bi-person-check me-2"></i>}
            {loading ? 'Registering...' : 'Register'}
          </button>
          {!canRegister && (
            <div className="form-text text-center mt-2">
              Verify your email and phone number to enable registration.
            </div>
          )}
        </form>

        <p className="text-center text-muted small mt-3">
          Already have an account? <Link to="/login" className="text-decoration-none">Login</Link>
        </p>
      </div>
    </div>
  )
}
