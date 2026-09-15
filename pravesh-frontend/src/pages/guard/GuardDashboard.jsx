import { useState, useEffect } from 'react'
import { scanPass, getGateEntries, shiftCheckin, shiftCheckout, getShiftStatus } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import QrScannerModal from '../../components/common/QrScannerModal'
import WalkInVisitorPanel from '../../components/common/WalkInVisitorPanel'

export default function GuardDashboard() {
  const { user } = useAuth()
  const { showToast } = useToast()

  const [shift, setShift] = useState(null)
  const [checkingStatus, setCheckingStatus] = useState(true)

  const [checkinForm, setCheckinForm] = useState({ onDutyName: '', onDutyEmployeeId: '' })
  const [checkinLoading, setCheckinLoading] = useState(false)
  const [checkoutLoading, setCheckoutLoading] = useState(false)

  const [uuid, setUuid] = useState('')
  const [result, setResult] = useState(null)
  const [entries, setEntries] = useState([])
  const [entriesLoading, setEntriesLoading] = useState(false)
  const [loading, setLoading] = useState(false)
  const [showScanner, setShowScanner] = useState(false)

  // On mount, verify the REAL shift state with the backend — never trust
  // localStorage alone, since it can be stale (cleared tab, different device,
  // or a genuinely still-open shift from a previous session).
  useEffect(() => {
    getShiftStatus()
      .then(res => {
        const status = res.data.data
        if (status.hasActiveShift) {
          const s = {
            shiftId: status.shiftId,
            gateId: status.gateId,
            onDutyName: status.onDutyName
          }
          localStorage.setItem('guard_shift', JSON.stringify(s))
          setShift(s)
        } else {
          localStorage.removeItem('guard_shift')
          setShift(null)
        }
      })
      .catch(() => {
        const stored = localStorage.getItem('guard_shift')
        setShift(stored ? JSON.parse(stored) : null)
      })
      .finally(() => setCheckingStatus(false))
  }, [])

  const loadEntries = (gateId) => {
    setEntriesLoading(true)
    getGateEntries(gateId)
      .then(res => setEntries(res.data.data))
      .catch(() => { })
      .finally(() => setEntriesLoading(false))
  }

  useEffect(() => { if (shift) loadEntries(shift.gateId) }, [shift])

  const handleCheckin = async (e) => {
    e.preventDefault()
    if (!checkinForm.onDutyName) { showToast('Please enter your name.', 'warning'); return }
    setCheckinLoading(true)
    try {
      const res = await shiftCheckin(checkinForm)
      const s = res.data.data
      localStorage.setItem('guard_shift', JSON.stringify(s))
      setShift(s)
      showToast('Shift started.', 'success')
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to start shift.', 'error')
    } finally {
      setCheckinLoading(false)
    }
  }

  const handleCheckout = async () => {
    setCheckoutLoading(true)
    try {
      await shiftCheckout()
      localStorage.removeItem('guard_shift')
      setShift(null); setEntries([]); setResult(null)
      showToast('Shift ended.', 'success')
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to end shift.', 'error')
    } finally {
      setCheckoutLoading(false)
    }
  }

  const submitScan = async (scannedUuid) => {
    setResult(null); setLoading(true)
    try {
      const res = await scanPass(scannedUuid, shift.gateId)
      const data = res.data.data
      setResult(data); setUuid('')
      loadEntries(shift.gateId)
      showToast(data.granted ? `GRANTED for ${data.visitorName}` : `DENIED — ${data.reason}`,
        data.granted ? 'success' : 'error')
    } catch (err) {
      if (err.response?.status === 428) {
        showToast('Shift session expired. Please check in again.', 'warning')
        localStorage.removeItem('guard_shift'); setShift(null)
      } else {
        showToast(err.response?.data?.message || 'Something went wrong.', 'error')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleScan = async (e) => {
    e.preventDefault()
    await submitScan(uuid)
  }

  const handleCameraScan = (decodedText) => {
    setShowScanner(false)
    submitScan(decodedText)
  }

  if (checkingStatus) {
    return (
      <>
        <Navbar />
        <LoadingSpinner text="Checking your shift status..." />
      </>
    )
  }

  if (!shift) {
    return (
      <>
        <Navbar />
        <div className="container py-5 d-flex justify-content-center">
          <div className="card auth-card p-4" style={{ maxWidth: 420, width: '100%' }}>
            <h5 className="fw-bold mb-3 text-center">Start Your Shift</h5>
            <p className="text-muted small text-center mb-3">
              Confirm your identity before scanning — this ties every entry to you.
            </p>
            <form onSubmit={handleCheckin}>
              <div className="mb-3">
                <label className="form-label fw-semibold">Your Name</label>
                <input className="form-control" value={checkinForm.onDutyName}
                  onChange={e => setCheckinForm({ ...checkinForm, onDutyName: e.target.value })}
                  disabled={checkinLoading} required />
              </div>
              <div className="mb-4">
                <label className="form-label fw-semibold">Employee ID <span className="text-muted fw-normal">(optional)</span></label>
                <input className="form-control" value={checkinForm.onDutyEmployeeId}
                  onChange={e => setCheckinForm({ ...checkinForm, onDutyEmployeeId: e.target.value })}
                  disabled={checkinLoading} />
              </div>
              <button type="submit" className="btn btn-pravesh w-100 py-2" disabled={checkinLoading}>
                {checkinLoading
                  ? <><span className="spinner-border spinner-border-sm me-2"></span>Starting...</>
                  : 'Start Shift'}
              </button>
            </form>
          </div>
        </div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <div className="page-header d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-1"><i className="bi bi-shield-check me-2"></i>Guard Dashboard</h4>
            <p className="mb-0 opacity-75 d-flex align-items-center gap-2">
              <span className="live-dot"></span>On duty: {shift.onDutyName}
            </p>
          </div>
          <button
            className="btn btn-outline-light btn-sm"
            onClick={handleCheckout}
            disabled={checkoutLoading}
          >
            {checkoutLoading
              ? <><span className="spinner-border spinner-border-sm me-1"></span>Ending...</>
              : 'End Shift'}
          </button>
        </div>

        <div className="row g-4">
          <div className="col-lg-4">
            <div className="card p-4">
              <h6 className="fw-bold mb-3"><i className="bi bi-qr-code-scan me-2 text-primary"></i>Scan Visitor Pass</h6>

              <button
                type="button"
                className="btn btn-pravesh w-100 py-2 mb-3"
                onClick={() => setShowScanner(true)}
                disabled={loading}
              >
                <i className="bi bi-camera me-2"></i>Scan with Camera
              </button>

              <div className="text-center text-muted small mb-3">— or enter manually —</div>

              <form onSubmit={handleScan}>
                <div className="mb-3">
                  <label className="form-label fw-semibold">Visitor Pass UUID</label>
                  <input className="form-control form-control-lg" placeholder="Paste UUID..."
                    value={uuid} onChange={e => setUuid(e.target.value)} disabled={loading} required />
                </div>
                <button type="submit" className="btn btn-outline-secondary w-100 py-2" disabled={loading}>
                  {loading
                    ? <><span className="spinner-border spinner-border-sm me-2"></span>Validating...</>
                    : 'Validate Manually'}
                </button>
              </form>

              {result && (
                <div className={`alert scan-result mt-3 ${result.granted ? "alert-success" : "alert-danger"}`}>
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <i className={`bi ${result.granted ? 'bi-check-circle-fill' : 'bi-x-circle-fill'} fs-4`}></i>
                    <strong className="fs-5">{result.granted ? 'GRANTED' : 'DENIED'}</strong>
                  </div>
                  {result.visitorName && <div>Visitor: <strong>{result.visitorName}</strong></div>}
                  {result.passType && <div>Type: <strong>{result.passType}</strong></div>}
                  {result.reason && (
                    <div>Reason: <strong>{
                      {
                        QR_INVALID: 'Invalid QR code',
                        QR_NOT_YET_ACTIVE: 'Pass is not active yet',
                        QR_EXPIRED: 'Pass has expired',
                        REVOKED: 'Pass was revoked by the resident',
                        ALREADY_USED: 'Pass already used',
                        ALREADY_USED_TODAY: 'Already entered today — try again tomorrow',
                        WRONG_SOCIETY: 'Pass belongs to a different society',
                      }[result.reason] || result.reason
                    }</strong></div>
                  )}
                </div>
              )}
            </div>
          </div>

          <div className="col-lg-4">
            <WalkInVisitorPanel />
          </div>

          <div className="col-lg-4">
            <div className="card p-3">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h6 className="fw-bold mb-0"><i className="bi bi-list-check me-2 text-success"></i>Today's Entries</h6>
                <button
                  className="btn btn-sm btn-outline-secondary"
                  onClick={() => loadEntries(shift.gateId)}
                  disabled={entriesLoading}
                >
                  <i className={`bi bi-arrow-clockwise ${entriesLoading ? 'spin-icon' : ''}`}></i>
                </button>
              </div>
              {entriesLoading ? <LoadingSpinner text="Loading entries..." />
                : entries.length === 0 ? <p className="text-muted text-center py-4">No entries yet</p>
                  : (
                    <div style={{ maxHeight: 420, overflowY: 'auto' }}>
                      {entries.map(e => (
                        <div key={e.id} className="border rounded p-2 mb-2">
                          <div className="fw-semibold">{e.visitorName || 'Unknown'}</div>
                          <div className="d-flex justify-content-between align-items-center">
                            <span className={`badge ${e.entryType === 'WALK_IN' ? 'bg-info text-dark' : 'bg-secondary'}`}>
                              {e.entryType === 'WALK_IN' ? 'Walk-in' : 'QR'}
                            </span>
                            <span className={`badge ${e.scanResult === 'GRANTED' ? 'bg-success' :
                                e.scanResult === 'NO_RESPONSE' ? 'bg-secondary' : 'bg-danger'
                              }`}>
                              {e.scanResult === 'NO_RESPONSE' ? 'NO RESPONSE' : e.scanResult}
                            </span>
                          </div>
                          <small className="text-muted">
                            {new Date(e.scannedAt).toLocaleTimeString('en-IN')}
                            {e.denyReason && <span className="text-danger ms-2">({e.denyReason})</span>}
                          </small>
                        </div>
                      ))}
                    </div>
                  )}
            </div>
          </div>
        </div>

        <QrScannerModal
          show={showScanner}
          onClose={() => setShowScanner(false)}
          onScan={handleCameraScan}
        />
      </div>
    </>
  )
}