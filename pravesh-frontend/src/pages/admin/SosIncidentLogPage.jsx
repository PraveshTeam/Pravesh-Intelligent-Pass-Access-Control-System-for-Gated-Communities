import { useState, useEffect } from 'react'
import { getSosIncidentLog, getSosHistory } from '../../api/endpoints'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

const STATUS_BADGE = {
  ACTIVE: 'danger',
  ACKNOWLEDGED: 'warning',
  HELP_ON_THE_WAY: 'warning',
  RESOLVED: 'success',
}
const HISTORY_LABEL = {
  ACTIVE: 'Alert raised',
  ACKNOWLEDGED: 'Acknowledged',
  HELP_ON_THE_WAY: 'Help on the way',
  RESOLVED: 'Resolved',
}

export default function SosIncidentLogPage() {
  const [incidents, setIncidents] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')

  const [historyFor, setHistoryFor] = useState(null)
  const [history, setHistory] = useState([])
  const [loadingHistory, setLoadingHistory] = useState(false)

  useEffect(() => {
    getSosIncidentLog()
      .then(res => setIncidents(res.data.data || []))
      .catch(() => setIncidents([]))
      .finally(() => setLoading(false))
  }, [])

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

  const visible = statusFilter ? incidents.filter(a => a.status === statusFilter) : incidents

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h4 className="mb-1"><i className="bi bi-shield-exclamation me-2"></i>SOS Incident Log</h4>
            <p className="mb-0 opacity-75">Full history of every emergency alert, including resolved ones</p>
          </div>
          <select
            className="form-select form-select-sm w-auto"
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="ACKNOWLEDGED">Acknowledged</option>
            <option value="HELP_ON_THE_WAY">Help On The Way</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>

        <div className="card p-3 mt-3">
          {loading ? (
            <LoadingSpinner text="Loading incident log..." />
          ) : visible.length === 0 ? (
            <p className="text-muted text-center py-4">No incidents recorded yet.</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-sm align-middle mb-0">
                <thead>
                  <tr>
                    <th>Resident</th>
                    <th>Flat</th>
                    <th>Category</th>
                    <th>Status</th>
                    <th>Raised</th>
                    <th>Resolved</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {visible.map(a => (
                    <tr key={a.id}>
                      <td className="fw-semibold">{a.residentName}</td>
                      <td className="text-muted">{a.flatNumber}</td>
                      <td>{a.category}</td>
                      <td>
                        <span className={`badge bg-${STATUS_BADGE[a.status] || 'secondary'}`}>
                          {a.status.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className="text-muted small">{new Date(a.createdAt).toLocaleString()}</td>
                      <td className="text-muted small">
                        {a.resolvedAt ? new Date(a.resolvedAt).toLocaleString() : '—'}
                      </td>
                      <td className="text-end">
                        <button
                          className="btn btn-sm btn-outline-light"
                          onClick={() => openHistory(a)}
                        >
                          <i className="bi bi-clock-history me-1"></i>Timeline
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {historyFor && (
        <div className="modal d-block" style={{ background: 'rgba(3,6,12,0.7)' }}
          tabIndex="-1" onClick={() => setHistoryFor(null)}>
          <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
            <div className="modal-content sos-modal">
              <div className="modal-header border-0">
                <h5 className="modal-title fw-bold">
                  <i className="bi bi-clock-history me-2"></i>
                  Timeline — {historyFor.residentName}, Flat {historyFor.flatNumber}
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
