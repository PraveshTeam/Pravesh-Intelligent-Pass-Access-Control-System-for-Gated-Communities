import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPass } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import BackButton from '../../components/common/BackButton'
import { sanitizePhoneInput, isValidPhone, PHONE_HINT } from '../../utils/inputValidation'

const PASS_TYPES = ['ONE_TIME', 'MULTI_USE', 'RECURRING_DAILY']

export default function CreatePassPage() {
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    visitorName: '', visitorPhone: '', passType: 'ONE_TIME',
    usesAllowed: 1, validFrom: '', validUntil: ''
  })
  const [loading, setLoading] = useState(false)
  const [qr, setQr] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.visitorName || !form.validFrom || !form.validUntil) {
      showToast('Fill visitor name and validity window.', 'warning'); return
    }
    if (form.visitorPhone && !isValidPhone(form.visitorPhone)) {
      showToast('Enter a valid 10-digit phone number starting with 6-9.', 'warning'); return
    }
    const payload = {
      visitorName: form.visitorName,
      visitorPhone: form.visitorPhone,
      passType: form.passType,
      validFrom: form.validFrom,
      validUntil: form.validUntil,
    }
    if (form.passType === 'MULTI_USE') payload.usesAllowed = Number(form.usesAllowed)

    setLoading(true)
    try {
      const res = await createPass(payload)
      const data = res.data.data
      setQr(data.qrBase64 || null)
      showToast('Pass created successfully!', 'success')
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to create pass.', 'error')
    } finally { setLoading(false) }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4" style={{ maxWidth: 560 }}>
        <BackButton to="/resident" label="Back to Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-ticket-perforated me-2"></i>Create Visitor Pass</h4>
        </div>

        {!qr ? (
          <div className="card p-4">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label fw-semibold">Visitor Name</label>
                <input className="form-control" value={form.visitorName}
                  onChange={e => setForm({ ...form, visitorName: e.target.value })} required />
              </div>
              <div className="mb-3">
                <label className="form-label fw-semibold">Visitor Phone</label>
                <input className="form-control" placeholder="9876543210" value={form.visitorPhone}
                  inputMode="numeric" maxLength={10}
                  onChange={e => setForm({ ...form, visitorPhone: sanitizePhoneInput(e.target.value) })} />
                {form.visitorPhone && !isValidPhone(form.visitorPhone) && (
                  <div className="form-text text-danger">{PHONE_HINT}</div>
                )}
              </div>
              <div className="mb-3">
                <label className="form-label fw-semibold">Pass Type</label>
                <select className="form-select" value={form.passType}
                  onChange={e => setForm({ ...form, passType: e.target.value })}>
                  {PASS_TYPES.map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
                </select>
              </div>
              {form.passType === 'MULTI_USE' && (
                <div className="mb-3">
                  <label className="form-label fw-semibold">Uses Allowed</label>
                  <input type="number" min="1" className="form-control" value={form.usesAllowed}
                    onChange={e => setForm({ ...form, usesAllowed: e.target.value })} />
                </div>
              )}
              <div className="row mb-4">
                <div className="col-6">
                  <label className="form-label fw-semibold">Valid From</label>
                  <input type="datetime-local" className="form-control" value={form.validFrom}
                    onChange={e => setForm({ ...form, validFrom: e.target.value })} required />
                </div>
                <div className="col-6">
                  <label className="form-label fw-semibold">Valid Until</label>
                  <input type="datetime-local" className="form-control" value={form.validUntil}
                    onChange={e => setForm({ ...form, validUntil: e.target.value })} required />
                </div>
              </div>
              <button type="submit" className="btn btn-pravesh w-100 py-2" disabled={loading}>
                {loading ? 'Creating...' : 'Create Pass'}
              </button>
            </form>
          </div>
        ) : (
          <div className="card p-4 text-center">
            <h5 className="fw-bold mb-3">Pass Created!</h5>
            <p className="text-muted small">Share this QR code with your visitor.</p>
            <img src={`data:image/png;base64,${qr}`} alt="Pass QR"
              style={{ width: 240, height: 240, margin: '0 auto' }} />
            <div className="mt-4 d-flex gap-2">
              <a href={`data:image/png;base64,${qr}`} download="pravesh-pass-qr.png"
                className="btn btn-outline-secondary w-100">
                <i className="bi bi-download me-1"></i>Download QR
              </a>
              <button className="btn btn-pravesh w-100" onClick={() => navigate('/resident/passes')}>
                View My Passes
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}
