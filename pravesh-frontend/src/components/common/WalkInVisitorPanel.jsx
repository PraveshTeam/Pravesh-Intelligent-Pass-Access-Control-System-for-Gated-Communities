import { useState, useRef, useEffect } from 'react'
import { createGateEntryRequest, getGateEntryRequestStatus, getSocietyResidents } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import { sanitizePhoneInput, isValidPhone, PHONE_HINT } from '../../utils/inputValidation'

const POLL_INTERVAL_MS = 4000

const STATUS_META = {
  PENDING:  { label: 'Waiting for resident...', cls: 'walkin-pending',  icon: 'bi-hourglass-split' },
  APPROVED: { label: 'Approved — let them in',  cls: 'walkin-approved', icon: 'bi-check-circle-fill' },
  DENIED:   { label: 'Denied by resident',      cls: 'walkin-denied',   icon: 'bi-x-circle-fill' },
  EXPIRED:  { label: 'No response in time',     cls: 'walkin-expired',  icon: 'bi-clock-history' },
}

export default function WalkInVisitorPanel() {
  const { showToast } = useToast()
  const [form, setForm] = useState({ visitorName: '', visitorPhone: '', reason: '' })
  const [submitting, setSubmitting] = useState(false)
  const [activeRequest, setActiveRequest] = useState(null)
  const timerRef = useRef(null)

  const [residents, setResidents] = useState([])
  const [residentsLoading, setResidentsLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState(null)
  const [showList, setShowList] = useState(false)
  const wrapRef = useRef(null)

  useEffect(() => () => clearInterval(timerRef.current), [])

  useEffect(() => {
    getSocietyResidents()
      .then(res => setResidents(res.data.data || []))
      .catch(() => showToast('Could not load resident directory.', 'error'))
      .finally(() => setResidentsLoading(false))
  }, [])

  // Close the dropdown when clicking outside
  useEffect(() => {
    const onDocClick = (e) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) setShowList(false)
    }
    document.addEventListener('mousedown', onDocClick)
    return () => document.removeEventListener('mousedown', onDocClick)
  }, [])

  const q = search.trim().toLowerCase()
  const filtered = q
    ? residents.filter(r =>
        (r.name || '').toLowerCase().includes(q) ||
        (r.phone || '').includes(q) ||
        (r.flatNumber || '').toLowerCase().includes(q) ||
        (r.tower || '').toLowerCase().includes(q))
    : residents

  const startPolling = (id) => {
    clearInterval(timerRef.current)
    timerRef.current = setInterval(async () => {
      try {
        const res = await getGateEntryRequestStatus(id)
        const data = res.data.data
        setActiveRequest(data)
        if (data.status !== 'PENDING') clearInterval(timerRef.current)
      } catch {
        clearInterval(timerRef.current)
      }
    }, POLL_INTERVAL_MS)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.visitorName || !selected) {
      showToast('Enter the visitor name and select the resident.', 'warning'); return
    }
    if (form.visitorPhone && !isValidPhone(form.visitorPhone)) {
      showToast('Enter a valid 10-digit phone number starting with 6-9.', 'warning'); return
    }
    setSubmitting(true)
    try {
      const res = await createGateEntryRequest({
        visitorName: form.visitorName,
        visitorPhone: form.visitorPhone,
        claimedFlatNumber: selected.flatNumber,
        reason: form.reason,
      })
      const data = res.data.data
      setActiveRequest(data)
      showToast('Request sent to resident.', 'success')
      startPolling(data.id)
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to send request.', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const reset = () => {
    clearInterval(timerRef.current)
    setActiveRequest(null)
    setForm({ visitorName: '', visitorPhone: '', reason: '' })
    setSelected(null)
    setSearch('')
  }

  const meta = activeRequest ? STATUS_META[activeRequest.status] : null

  return (
    <div className="card p-4">
      <h6 className="fw-bold mb-3">
        <i className="bi bi-person-plus me-2 text-warning"></i>Register Walk-in Visitor
      </h6>
      <p className="text-muted small mb-3">
        For a visitor without a pre-created pass. The resident will be asked to approve or deny.
      </p>

      {!activeRequest ? (
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label fw-semibold">Visitor Name</label>
            <input className="form-control" value={form.visitorName}
              onChange={e => setForm({ ...form, visitorName: e.target.value })}
              disabled={submitting} required />
          </div>

          <div className="mb-3">
            <label className="form-label fw-semibold">Visitor Phone <span className="text-muted fw-normal">(optional)</span></label>
            <input className="form-control" placeholder="9876543210" value={form.visitorPhone}
              inputMode="numeric" maxLength={10}
              onChange={e => setForm({ ...form, visitorPhone: sanitizePhoneInput(e.target.value) })}
              disabled={submitting} />
            {form.visitorPhone && !isValidPhone(form.visitorPhone) && (
              <div className="form-text text-danger">{PHONE_HINT}</div>
            )}
          </div>

          <div className="mb-3 position-relative" ref={wrapRef}>
            <label className="form-label fw-semibold">Visiting Which Resident?</label>

            {selected ? (
              <div className="resident-selected">
                <div>
                  <div className="fw-semibold">{selected.name}</div>
                  <small className="text-muted">
                    Flat {selected.flatNumber}{selected.tower ? ` · Tower ${selected.tower}` : ''}
                    {selected.phone ? ` · ${selected.phone}` : ''}
                  </small>
                </div>
                <button type="button" className="btn btn-sm btn-outline-secondary"
                  onClick={() => { setSelected(null); setSearch(''); setShowList(true) }}
                  disabled={submitting}>
                  Change
                </button>
              </div>
            ) : (
              <>
                <input
                  className="form-control"
                  placeholder={residentsLoading ? 'Loading residents...' : 'Search name, flat, tower or phone'}
                  value={search}
                  onChange={e => { setSearch(e.target.value); setShowList(true) }}
                  onFocus={() => setShowList(true)}
                  disabled={submitting || residentsLoading}
                />
                {showList && (
                  <div className="resident-dropdown">
                    {filtered.length === 0 && (
                      <div className="p-2 text-muted small">No matching resident.</div>
                    )}
                    {filtered.map(r => (
                      <button
                        type="button"
                        key={r.residentId}
                        className="resident-option"
                        onClick={() => { setSelected(r); setShowList(false); setSearch('') }}
                      >
                        <span className="resident-flat">{r.flatNumber}</span>
                        <span className="resident-meta">
                          <span className="fw-semibold text-dark">{r.name}</span>
                          <small className="text-muted d-block">
                            {r.tower ? `Tower ${r.tower} · ` : ''}{r.phone || 'No phone'}
                          </small>
                        </span>
                      </button>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>

          <div className="mb-4">
            <label className="form-label fw-semibold">Reason <span className="text-muted fw-normal">(optional)</span></label>
            <input className="form-control" placeholder="Delivery, guest, cab, etc." value={form.reason}
              onChange={e => setForm({ ...form, reason: e.target.value })}
              disabled={submitting} />
          </div>

          <button type="submit" className="btn btn-pravesh w-100 py-2" disabled={submitting}>
            {submitting
              ? <><span className="spinner-border spinner-border-sm me-2"></span>Sending...</>
              : <><i className="bi bi-send me-2"></i>Send Request to Resident</>}
          </button>
        </form>
      ) : (
        <div className={`walkin-status ${meta.cls}`}>
          <div className="walkin-status-icon">
            <i className={`bi ${meta.icon}`}></i>
          </div>
          <div className="walkin-status-label">{meta.label}</div>
          <div className="walkin-status-visitor">
            <strong>{activeRequest.visitorName}</strong> → Flat {activeRequest.claimedFlatNumber}
          </div>

          {activeRequest.status === 'PENDING' && (
            <div className="walkin-status-pulse">
              <span className="pravesh-dots"><span></span><span></span><span></span></span>
            </div>
          )}

          {activeRequest.status !== 'PENDING' && (
            <button className="btn btn-outline-secondary w-100 mt-3" onClick={reset}>
              Register Another Visitor
            </button>
          )}
        </div>
      )}
    </div>
  )
}