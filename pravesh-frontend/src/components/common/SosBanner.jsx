import { useState, useCallback, useEffect } from 'react'
import { getActiveSosAlerts, updateSosStatus, getSosHistory } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useSosSocket } from '../../hooks/useSosSocket'

const NEXT_STATUS = {
  ACTIVE: 'ACKNOWLEDGED',
  ACKNOWLEDGED: 'HELP_ON_THE_WAY',
  HELP_ON_THE_WAY: 'RESOLVED',
}
const STATUS_LABEL = {
  ACTIVE: 'Acknowledge',
  ACKNOWLEDGED: 'Mark Help On The Way',
  HELP_ON_THE_WAY: 'Mark Resolved',
}
const HISTORY_LABEL = {
  ACTIVE: 'Alert raised',
  ACKNOWLEDGED: 'Acknowledged',
  HELP_ON_THE_WAY: 'Help on the way',
  RESOLVED: 'Resolved',
}

export default function SosBanner() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [alerts, setAlerts] = useState([])
  const [updatingId, setUpdatingId] = useState(null)

  const [historyFor, setHistoryFor] = useState(null) // the alert object, or null
  const [history, setHistory] = useState([])
  const [loadingHistory, setLoadingHistory] = useState(false)

  const isResponder = user && (user.role === 'GUARD' || user.role === 'SOCIETY_ADMIN')

  useEffect(() => {
    if (!isResponder) return
    getActiveSosAlerts().then(res => setAlerts(res.data.data || [])).catch(() => {})
  }, [isResponder])

  const onLiveAlert = useCallback((alert) => {
    let isNew = false
    setAlerts(prev => {
      if (alert.status === 'RESOLVED') {
        return prev.filter(a => a.id !== alert.id)
      }
      const exists = prev.some(a => a.id === alert.id)
      if (!exists) {
        isNew = true
        return [alert, ...prev]
      }
      return prev.map(a => a.id === alert.id ? alert : a)
    })
    if (isNew) {
      showToast(`🚨 New SOS: ${alert.category} — ${alert.residentName}, Flat ${alert.flatNumber}`, 'error')
    }
  }, [showToast])

  useSosSocket(isResponder ? user.societyId : null, onLiveAlert)

  const advance = async (alert) => {
    const target = NEXT_STATUS[alert.status]
    if (!target) return
    setUpdatingId(alert.id)
    try {
      const res = await updateSosStatus(alert.id, target)
      const updated = res.data.data
      if (updated.status === 'RESOLVED') {
        setAlerts(prev => prev.filter(a => a.id !== alert.id))
      } else {
        setAlerts(prev => prev.map(a => a.id === alert.id ? updated : a))
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update status.', 'error')
    } finally {
      setUpdatingId(null)
    }
  }

  const openHistory = async (alert) => {
    setHistoryFor(alert)
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

  if (!isResponder || alerts.length === 0) return null

  return (
    <>
      <div className="sos-banner-stack">
        {alerts.map(a => (
          <div key={a.id} className={`sos-banner sos-banner-${(a.status || 'active').toLowerCase()}`}>
            <div className="sos-banner-icon">
              <i className="bi bi-exclamation-triangle-fill"></i>
            </div>
            <div className="sos-banner-body">
              <div className="sos-banner-title">
                {a.category} SOS — {a.residentName}, Flat {a.flatNumber}
              </div>
              <div className="sos-banner-sub">
                {a.description || 'No additional details provided.'} · {a.phone}
              </div>
            </div>
            <div className="sos-banner-status">{a.status.replace(/_/g, ' ')}</div>
            <button
              className="btn btn-sm btn-outline-light me-2"
              onClick={() => openHistory(a)}
              title="View acknowledgment history"
            >
              <i className="bi bi-clock-history"></i>
            </button>
            {NEXT_STATUS[a.status] && (
              <button
                className="btn btn-sm sos-banner-action"
                onClick={() => advance(a)}
                disabled={updatingId === a.id}
              >
                {updatingId === a.id
                  ? <span className="spinner-border spinner-border-sm"></span>
                  : STATUS_LABEL[a.status]}
              </button>
            )}
          </div>
        ))}
      </div>

      {historyFor && (
        <div className="modal d-block" style={{ background: 'rgba(3,6,12,0.7)' }}
          tabIndex="-1" onClick={() => setHistoryFor(null)}>
          <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
            <div className="modal-content sos-modal">
              <div className="modal-header border-0">
                <h5 className="modal-title fw-bold">
                  <i className="bi bi-clock-history me-2"></i>
                  Alert Timeline — {historyFor.residentName}, Flat {historyFor.flatNumber}
                </h5>
                <button className="btn-close" onClick={() => setHistoryFor(null)}></button>
              </div>
              <div className="modal-body">
                {loadingHistory ? (
                  <p className="text-muted small text-center py-3">Loading...</p>
                ) : history.length === 0 ? (
                  <p className="text-muted small text-center py-3">No history available.</p>
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
                            {h.changedByName || `User #${h.changedByUserId}`}
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
                <button className="btn btn-outline-secondary" onClick={() => setHistoryFor(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
