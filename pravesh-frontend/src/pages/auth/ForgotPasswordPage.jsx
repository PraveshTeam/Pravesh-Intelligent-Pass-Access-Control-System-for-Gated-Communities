import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { forgotPassword, verifyOtp, resetPassword } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import OtpInput from '../../components/common/OtpInput'
import PasswordInput from '../../components/common/PasswordInput'
import { isValidEmail } from '../../utils/inputValidation'
import logoMark from '../../assets/logo.png'

export default function ForgotPasswordPage() {
  const { showToast } = useToast()
  const navigate = useNavigate()

  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [channel, setChannel] = useState('BOTH')
  const [otp, setOtp] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  useEffect(() => {
    if (cooldown <= 0) return
    const t = setTimeout(() => setCooldown(c => c - 1), 1000)
    return () => clearTimeout(t)
  }, [cooldown])

  const sendOtp = async () => {
    if (!email) { showToast('Please enter your email.', 'warning'); return }
    if (!isValidEmail(email)) { showToast('Enter a valid email address.', 'warning'); return }
    setLoading(true)
    try {
      await forgotPassword({ email, channel })
      showToast('Code sent to your email and/or phone.', 'success')
      setStep(2); setCooldown(90)
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to send code.', 'error')
    } finally { setLoading(false) }
  }

  const doVerify = async () => {
    if (otp.length !== 6) { showToast('Enter the full 6-digit code.', 'warning'); return }
    setLoading(true)
    try {
      const res = await verifyOtp({ email, otp })
      setResetToken(res.data.data.resetToken)
      showToast('Code verified.', 'success'); setStep(3)
    } catch (err) {
      showToast(err.response?.data?.message || 'Invalid or expired code.', 'error')
    } finally { setLoading(false) }
  }

  const doReset = async () => {
    if (newPassword.length < 8) { showToast('Password must be at least 8 characters.', 'warning'); return }
    if (newPassword !== confirmPassword) { showToast('Passwords do not match.', 'warning'); return }
    setLoading(true)
    try {
      await resetPassword({ resetToken, newPassword })
      showToast('Password reset successful. Please log in.', 'success')
      navigate('/login')
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to reset password.', 'error')
    } finally { setLoading(false) }
  }

  const steps = ['Email', 'Verify', 'New Password']

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center auth-bg py-4">
      <div className="card auth-card p-4" style={{ width: '100%', maxWidth: 440 }}>
        <div className="text-center mb-3">
          <Link to="/login"><img src={logoMark} alt="Pravesh" style={{ width: 56 }} /></Link>
          <h4 className="fw-bold mt-2">Reset Password</h4>
        </div>

        <div className="d-flex justify-content-center gap-2 mb-4">
          {steps.map((label, i) => {
            const n = i + 1
            const done = step > n
            const active = step === n
            return (
              <div key={label} className="text-center" style={{ flex: 1 }}>
                <div
                  className={`mx-auto d-flex align-items-center justify-content-center fw-bold
                    ${done ? 'bg-success text-white' : active ? 'bg-primary text-white' : 'bg-light text-muted'}`}
                  style={{ width: 30, height: 30, borderRadius: '50%', fontSize: 13, transition: 'all .25s ease' }}
                >
                  {done ? <i className="bi bi-check-lg"></i> : n}
                </div>
                <small className={`d-block mt-1 ${active ? 'fw-semibold' : 'text-muted'}`} style={{ fontSize: 11 }}>
                  {label}
                </small>
              </div>
            )
          })}
        </div>

        {step === 1 && (
          <div>
            <div className="mb-3">
              <label className="form-label fw-semibold">Email</label>
              <input type="email" className="form-control" 
                value={email} onChange={e => setEmail(e.target.value)} />
              {email && !isValidEmail(email) && (
                <div className="form-text text-danger">Enter a valid email address.</div>
              )}
            </div>
            <div className="mb-4">
              <label className="form-label fw-semibold">Send code via</label>
              <select className="form-select" value={channel} onChange={e => setChannel(e.target.value)}>
                <option value="BOTH">Email + SMS</option>
                <option value="EMAIL">Email only</option>
                <option value="SMS">SMS only</option>
              </select>
            </div>
            <button className="btn btn-pravesh w-100 py-2" onClick={sendOtp} disabled={loading || !isValidEmail(email)}>
              {loading && <span className="spinner-border spinner-border-sm me-2"></span>}
              {loading ? 'Sending...' : 'Send Code'}
            </button>
          </div>
        )}

        {step === 2 && (
          <div>
            <p className="text-muted small text-center mb-3">
              Enter the 6-digit code sent to <strong>{email}</strong>.
            </p>
            <div className="mb-4"><OtpInput value={otp} onChange={setOtp} /></div>
            <button className="btn btn-pravesh w-100 py-2 mb-2" onClick={doVerify} disabled={loading}>
              {loading && <span className="spinner-border spinner-border-sm me-2"></span>}
              {loading ? 'Verifying...' : 'Verify Code'}
            </button>
            <button className="btn btn-link w-100 small" disabled={cooldown > 0} onClick={sendOtp}>
              {cooldown > 0 ? `Resend in ${cooldown}s` : 'Resend code'}
            </button>
          </div>
        )}

        {step === 3 && (
          <div>
            <div className="mb-3">
              <label className="form-label fw-semibold">New Password</label>
              <PasswordInput
                value={newPassword}
                onChange={e => setNewPassword(e.target.value)}
                placeholder="Min 8 characters"
                autoComplete="new-password"
              />
            </div>
            <div className="mb-2">
              <label className="form-label fw-semibold">Confirm Password</label>
              <PasswordInput
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                placeholder="Re-enter password"
                autoComplete="new-password"
              />
            </div>
            {confirmPassword && (
              <div className={`small mb-3 ${newPassword === confirmPassword ? 'text-success' : 'text-danger'}`}>
                <i className={`bi ${newPassword === confirmPassword ? 'bi-check-circle' : 'bi-x-circle'} me-1`}></i>
                {newPassword === confirmPassword ? 'Passwords match' : 'Passwords do not match'}
              </div>
            )}
            <button className="btn btn-pravesh w-100 py-2" onClick={doReset} disabled={loading}>
              {loading && <span className="spinner-border spinner-border-sm me-2"></span>}
              {loading ? 'Resetting...' : 'Reset Password'}
            </button>
          </div>
        )}

        <p className="text-center text-muted small mt-3 mb-0">
          <Link to="/login" className="text-decoration-none">Back to Login</Link>
        </p>
      </div>
    </div>
  )
}
