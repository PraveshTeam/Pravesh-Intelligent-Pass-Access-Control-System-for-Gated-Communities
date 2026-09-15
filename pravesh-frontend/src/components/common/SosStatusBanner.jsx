import { useState, useEffect, useCallback } from 'react'
import { getMySosAlerts, getSosHistory } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useSosStatusSocket } from '../../hooks/useSosStatusSocket'

const STEPS = ['ACTIVE', 'ACKNOWLEDGED', 'HELP_ON_THE_WAY', 'RESOLVED']
const STEP_LABEL = {
  ACTIVE: 'Alert Sent',
  ACKNOWLEDGED: 'Acknowledged',
  HELP_ON_THE_WAY: 'Help On The Way',
  RESOLVED: 'Resolved',
}
const HISTORY_LABEL = {
  ACTIVE: 'Alert raised',
  ACKNOWLEDGED: 'Acknowledged',
  HELP_ON_THE_WAY: 'Help on the way',
  RESOLVED: 'Resolved',
}

// Sits on every resident page (mount it near SosButton) -- shows a small,
// persistent status banner for the resident's OWN most recent unresolved
// SOS alert, updating live as a guard/admin progresses it. Auto-hides a
// few seconds after RESOLVED so it doesn't linger forever.
export default function SosStatusBanner() {
  const { user } = useAuth()
  const [alert, setAlert] = useState(null)
  const [justResolved, setJustResolved] = useState(false)
  const [showDetails, setShowDetails] = useState(false)
  const [history, setHistory] = useState([])
  const [loadingHistory, setLoadingHistory] = useState(false)

  const load = useCallback(() => {
    if (user?.role !== 'RESIDENT') return
    getMySosAlerts()
      .then(res => {
        const mine = res.data.data || []
        // Most recent non-RESOLVED alert, if any -- mySosAlerts is already
        // sorted newest-first by the backend.
        const active = mine.find(a => a.status !== 'RESOLVED')
        setAlert(active || null)
      })
      .catch(() => {})
  }, [user?.role])

  useEffect(() => { load() }, [load])

  const onStatusUpdate = useCallback((payload) => {
    if (payload.status === 'RESOLVED') {
      setJustResolved(true)
      setTimeout(() => { setAlert(null); setJustResolved(false) }, 6000)
    } else {
      setAlert(prev => (prev && prev.id === payload.id) ? { ...prev, ...payload } : payload)
    }
  }, [])

  useSosStatusSocket(user?.role === 'RESIDENT' ? user.userId : null, onStatusUpdate)

  const openDetails = async () => {
    if (!alert) return
    setShowDetails(true)
    setLoadingHistory(true)
    try {
      const res = await getSosHistory(alert.id)
      setHistory(res.data.data || [])
    } catch {
      setHistory([])
    } finally {
      setLoadingHistory(false)
    }
  }

  if (user?.role !== 'RESIDENT' || !alert) return null

  const currentIndex = STEPS.indexOf(alert.status)

  return (
    <div className="card p-3 mb-3" style={{ borderLeft: '4px solid #f97316' }}>
      <div className="d-flex justify-content-between align-items-start mb-2">
        <div>
          <div className="fw-bold small">
            <i className="bi bi-exclamation-triangle-fill text-danger me-2"></i>
            Your {alert.category} SOS Alert
          </div>
          <div className="text-muted small mt-1">
            {alert.description || 'No additional details provided.'}
          </div>
        </div>
        {justResolved && (
          <span className="badge bg-success">
            <i className="bi bi-check-circle-fill me-1"></i>Resolved
          </span>
        )}
      </div>

      {/* Progress stepper */}
      <div className="d-flex align-items-center mt-2">
        {STEPS.map((step, i) => (
          <div key={step} className="d-flex align-items-center flex-grow-1">
            <div className="d-flex flex-column align-items-center" style={{ minWidth: 70 }}>
              <div
                className="rounded-circle d-flex align-items-center justify-content-center"
                style={{
                  width: 26, height: 26,
                  background: i <= currentIndex ? '#f97316' : 'rgba(20,40,70,0.10)',
                  color: i <= currentIndex ? '#fff' : '#8794a6',
                  fontSize: 12, fontWeight: 700,
                  transition: 'background 0.3s',
                }}
              >
                {i < currentIndex || justResolved ? <i className="bi bi-check"></i> : i + 1}
              </div>
              <div className="text-center small mt-1" style={{ fontSize: 10.5, color: i <= currentIndex ? '#16233a' : '#8794a6' }}>
                {STEP_LABEL[step]}
              </div>
            </div>
            {i < STEPS.length - 1 && (
              <div
                style={{
                  flexGrow: 1, height: 2, marginBottom: 16,
                  background: i < currentIndex ? '#f97316' : 'rgba(20,40,70,0.12)',
                  transition: 'background 0.3s',
                }}
              ></div>
            )}
          </div>
        ))}
      </div>

      <button
        className="btn btn-sm btn-link text-decoration-none p-0 mt-2"
        onClick={openDetails}
      >
        <i className="bi bi-info-circle me-1"></i>
        {currentIndex >= 1 ? 'See who\'s handling this' : 'Details'}
      </button>

      {showDetails && (
        <div className="modal d-block" style={{ background: 'rgba(3,6,12,0.7)' }}
          tabIndex="-1" onClick={() => setShowDetails(false)}>
          <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
            <div className="modal-content sos-modal">
              <div className="modal-header border-0">
                <h5 className="modal-title fw-bold">
                  <i className="bi bi-clock-history me-2"></i>Alert Timeline
                </h5>
                <button className="btn-close" onClick={() => setShowDetails(false)}></button>
              </div>
              <div className="modal-body">
                {loadingHistory ? (
                  <p className="text-muted small text-center py-3">Loading...</p>
                ) : history.length === 0 ? (
                  <p className="text-muted small text-center py-3">No history available yet.</p>
                ) : (
                  <div className="d-flex flex-column gap-3">
                    {history.map((h, i) => (
                      <div key={i} className="d-flex gap-3">
                        <div className="d-flex flex-column align-items-center">
                          <div
                            className="rounded-circle"
                            style={{ width: 12, height: 12, background: '#f97316', flexShrink: 0 }}
                          ></div>
                          {i < history.length - 1 && (
                            <div style={{ width: 2, flexGrow: 1, background: 'rgba(20,40,70,0.15)', minHeight: 24 }}></div>
                          )}
                        </div>
                        <div className="pb-2">
                          <div className="fw-semibold small">
                            {HISTORY_LABEL[h.status] || h.status}
                          </div>
                          <div className="text-muted small">
                            {h.status === 'ACTIVE'
                              ? 'You raised this alert'
                              : (h.changedByName || `Responder #${h.changedByUserId}`)}
                            {' · '}
                            {new Date(h.changedAt).toLocaleString()}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div className="modal-footer border-0">
                <button className="btn btn-outline-secondary" onClick={() => setShowDetails(false)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}