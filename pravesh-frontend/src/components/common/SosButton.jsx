import { useState } from 'react'
import { raiseSos } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'

const CATEGORIES = [
  { value: 'MEDICAL',  label: 'Medical',  icon: 'bi-heart-pulse-fill' },
  { value: 'FIRE',     label: 'Fire',     icon: 'bi-fire' },
  { value: 'SECURITY', label: 'Security', icon: 'bi-shield-exclamation' },
  { value: 'OTHER',    label: 'Other',    icon: 'bi-exclamation-octagon-fill' },
]

export default function SosButton() {
  const { showToast } = useToast()
  const [open, setOpen] = useState(false)
  const [category, setCategory] = useState(null)
  const [description, setDescription] = useState('')
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)

  const reset = () => {
    setOpen(false)
    setCategory(null)
    setDescription('')
    setSent(false)
  }

  const handleSend = async () => {
    if (!category) return
    setSending(true)
    try {
      await raiseSos(category, description)
      setSent(true)
      showToast('SOS alert sent — help is being notified.', 'success')
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to send SOS alert.', 'error')
    } finally {
      setSending(false)
    }
  }

  return (
    <>
      <button className="sos-fab" onClick={() => setOpen(true)} aria-label="Emergency SOS">
        <i className="bi bi-exclamation-triangle-fill"></i>
        <span className="sos-fab-label">SOS</span>
      </button>

      {open && (
        <div className="modal d-block" style={{ background: 'rgba(3,6,12,0.7)' }}
          tabIndex="-1" onClick={reset}>
          <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
            <div className="modal-content sos-modal">
              {!sent ? (
                <>
                  <div className="modal-header border-0">
                    <h5 className="modal-title fw-bold text-danger">
                      <i className="bi bi-exclamation-triangle-fill me-2"></i>Emergency SOS
                    </h5>
                    <button className="btn-close" onClick={reset}></button>
                  </div>
                  <div className="modal-body">
                    <p className="text-muted small mb-3">
                      This immediately alerts your society's on-duty guard or admin. Use only for genuine emergencies.
                    </p>

                    <div className="sos-category-grid mb-3">
                      {CATEGORIES.map(c => (
                        <button
                          key={c.value}
                          type="button"
                          className={`sos-category-btn ${category === c.value ? 'active' : ''}`}
                          onClick={() => setCategory(c.value)}
                        >
                          <i className={`bi ${c.icon}`}></i>
                          <span>{c.label}</span>
                        </button>
                      ))}
                    </div>

                    <label className="form-label fw-semibold small">Details (optional)</label>
                    <textarea
                      className="form-control"
                      rows={2}
                      placeholder="Brief details to help responders..."
                      value={description}
                      onChange={e => setDescription(e.target.value)}
                      maxLength={500}
                    />
                  </div>
                  <div className="modal-footer border-0">
                    <button className="btn btn-outline-secondary" onClick={reset} disabled={sending}>
                      Cancel
                    </button>
                    <button
                      className="btn btn-danger fw-bold"
                      onClick={handleSend}
                      disabled={!category || sending}
                    >
                      {sending
                        ? <><span className="spinner-border spinner-border-sm me-2"></span>Sending...</>
                        : <><i className="bi bi-send-fill me-2"></i>Send SOS Alert</>}
                    </button>
                  </div>
                </>
              ) : (
                <div className="modal-body text-center py-5">
                  <i className="bi bi-check-circle-fill text-success" style={{ fontSize: '3rem' }}></i>
                  <h5 className="fw-bold mt-3">Alert Sent</h5>
                  <p className="text-muted small mb-4">
                    Your society's guard or admin has been notified and will respond shortly.
                  </p>
                  <button className="btn btn-pravesh w-100" onClick={reset}>Close</button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  )
}