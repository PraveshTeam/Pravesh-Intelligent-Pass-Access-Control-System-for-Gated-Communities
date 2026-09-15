import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { getActivePasses, getFlatEntries } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useWebSocket } from '../../hooks/useWebSocket'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import GateRequestBanner from '../../components/common/GateRequestBanner'
import SosButton from '../../components/common/SosButton'
import SosStatusBanner from '../../components/common/SosStatusBanner'



export default function ResidentDashboard() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [activePasses, setActivePasses] = useState([])
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([getActivePasses(), getFlatEntries(user.userId)])
      .then(([p, e]) => {
        setActivePasses(p.data.data)
        setEntries(e.data.data)
      })
      .catch(() => { })
      .finally(() => setLoading(false))
  }, [user.userId])

  const onNotify = useCallback((payload) => {
    showToast(payload.message || `${payload.visitorName} entered`, 'success')
    getFlatEntries(user.userId).then(e => setEntries(e.data.data)).catch(() => { })
  }, [showToast, user.userId])

  // Subscribe to live entry notifications for this resident's flat
  //useWebSocket(user.userId, onNotify)

  const stats = [
    { label: 'Active Passes', value: activePasses.length, icon: 'bi-ticket-perforated' },
    { label: 'Recent Entries', value: entries.length, icon: 'bi-clock-history' },
  ]

  return (
    <>
      <Navbar />
      <SosButton />
      <SosStatusBanner />
      <div className="container py-4">
        <div className="page-header d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-1"><i className="bi bi-house-heart me-2"></i>Welcome, {user.name}</h4>
            <p className="mb-0 opacity-75">Resident Dashboard</p>
          </div>
          <Link to="/resident/create-pass" className="btn btn-pravesh">
            <i className="bi bi-plus-circle me-1"></i>Create Pass
          </Link>
        </div>

        <GateRequestBanner />

        <div className="row g-3 mb-4 stagger-in">
          {stats.map(s => (
            <div className="col-md-3 col-6" key={s.label}>
              <div
                className="card p-3 h-100"
                style={{
                  cursor: 'default',
                  border: '1px dashed rgba(20,40,70,0.15)',
                  background: '#ffffff',
                }}
              >
                <div className="d-flex align-items-center gap-3">
                  <div
                    className="d-flex align-items-center justify-content-center rounded-circle flex-shrink-0"
                    style={{ width: 44, height: 44, background: 'rgba(255,193,7,0.12)' }}
                  >
                    <i className={`bi ${s.icon}`} style={{ color: '#ffc107', fontSize: 20 }}></i>
                  </div>
                  <div>
                    <div className="fs-4 fw-bold lh-1">{s.value}</div>
                    <div className="text-muted small">{s.label}</div>
                  </div>
                </div>
              </div>
            </div>
          ))}

          <div className="col-md-3 col-6">
            <Link to="/resident/passes" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-ticket-detailed fs-3 text-primary"></i>
              <div className="small fw-semibold mt-1">My Passes</div>
            </Link>
          </div>

          <div className="col-md-3 col-6">
            <Link to="/resident/entries" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-list-check fs-3 text-success"></i>
              <div className="small fw-semibold mt-1">Entry Log</div>
            </Link>
          </div>

          <div className="col-md-3 col-6">
            <Link to="/resident/relocation" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-signpost-2 fs-3 text-warning"></i>
              <div className="small fw-semibold mt-1">Change Flat/Society</div>
            </Link>
          </div>

          <div className="col-md-3 col-6">
            <Link to="/resident/payments" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-cash-coin fs-3 text-warning"></i>
              <div className="small fw-semibold mt-1">Maintenance Payment</div>
            </Link>
          </div>

          <div className="col-md-3 col-6">
            <Link to="/resident/payments/history" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-receipt fs-3 text-primary"></i>
              <div className="small fw-semibold mt-1">Transaction History</div>
            </Link>
          </div>
          <div className="col-md-3 col-6">
            <Link to="/resident/trips" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-signpost-split fs-3 text-warning"></i>
              <div className="small fw-semibold mt-1">Trip Buddy</div>
            </Link>
          </div>
          <div className="col-md-3 col-6">
            <Link to="/dashboard" className="card card-hover p-3 text-center text-decoration-none h-100 d-flex justify-content-center">
              <i className="bi bi-grid-1x2-fill fs-3 text-info"></i>
              <div className="small fw-semibold mt-1">Society Dashboard</div>
            </Link>
          </div>
        </div>


        <div className="card p-3">
          <h6 className="fw-bold mb-3">Recent Entries</h6>
          {loading ? <LoadingSpinner text="Loading entries..." />
            : entries.length === 0 ? <p className="text-muted text-center py-3">No entries yet.</p>
              : (
                <div style={{ maxHeight: 360, overflowY: 'auto' }}>
                  {entries.slice(0, 15).map(e => (
                    <div key={e.id} className="border rounded p-2 mb-2 d-flex justify-content-between align-items-center">
                      <div>
                        <span className="fw-semibold">{e.visitorName || 'Unknown'}</span>
                        <small className="text-muted d-block">{new Date(e.scannedAt).toLocaleString('en-IN')}</small>
                      </div>
                      <span className={`badge ${e.scanResult === 'GRANTED' ? 'bg-success' : 'bg-danger'}`}>
                        {e.scanResult}{e.denyReason ? ` — ${e.denyReason}` : ''}
                      </span>
                    </div>
                  ))}
                </div>
              )}
        </div>
      </div>
    </>
  )
}