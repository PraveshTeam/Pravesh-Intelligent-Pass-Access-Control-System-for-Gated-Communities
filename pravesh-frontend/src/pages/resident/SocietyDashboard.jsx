import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  getActivePasses, getActiveSosAlerts,
  getMyPaymentHistory, getAdminPayments,
  listForumPosts, listTrips,
  getOnboardingRequests, getRelocationRequests
} from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import Navbar from '../../components/common/Navbar'

const NOTE_COLORS = ['#f4d35e', '#f79256', '#8fd694', '#7ec8e3', '#e07a9e']
const NOTE_ROTATIONS = [-2.5, 1.5, -1, 2, -1.8, 1.2]

function useSection(fetcher, extract, deps) {
  const [data, setData] = useState(null)
  const [error, setError] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    fetcher()
      .then(res => { if (!cancelled) { setData(extract(res)); setError(false) } })
      .catch(() => { if (!cancelled) setError(true) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  return { data, error, loading }
}

export default function SocietyDashboard() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'SOCIETY_ADMIN'
  const isResident = user?.role === 'RESIDENT'
  const myDashboardPath = isAdmin ? '/admin' : '/resident'

  const passes = useSection(
    () => isResident ? getActivePasses() : Promise.resolve({ data: { data: [] } }),
    res => res.data.data, [isResident])

  const sosAlerts = useSection(
    () => getActiveSosAlerts(),
    res => res.data.data, [])

  const payments = useSection(
    () => isAdmin ? getAdminPayments() : getMyPaymentHistory(),
    res => res.data.data || [], [isAdmin])

  const posts = useSection(
    () => listForumPosts(),
    res => (res.data.data || []).slice(0, 6), [])

  const trips = useSection(
    () => isResident ? listTrips() : Promise.resolve({ data: { data: [] } }),
    res => (res.data.data || []).filter(t => t.status === 'OPEN'), [isResident])

  const onboardingRequests = useSection(
    () => isAdmin ? getOnboardingRequests('PENDING') : Promise.resolve({ data: { data: [] } }),
    res => res.data.data || [], [isAdmin])

  const relocationRequests = useSection(
    () => isAdmin ? getRelocationRequests('PENDING') : Promise.resolve({ data: { data: [] } }),
    res => res.data.data || [], [isAdmin])

  const pendingPaymentsCount = (payments.data || []).filter(p => p.status === 'PENDING').length
  const hasActiveSos = (sosAlerts.data?.length ?? 0) > 0

  return (
    <>
      <Navbar />
      <div className="container py-4">

        {/* ---------- Header + quick actions ---------- */}
        <div className="page-header d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h4 className="mb-1"><i className="bi bi-grid-1x2-fill me-2"></i>Society Dashboard</h4>
            <p className="mb-0 opacity-75">Welcome, {user?.name}</p>
          </div>
          <div className="d-flex gap-2">
            {isResident && (
              <Link to="/resident/create-pass" className="btn btn-sm btn-pravesh">
                <i className="bi bi-plus-circle me-1"></i>Create Pass
              </Link>
            )}
            {isResident && (
              <Link to="/resident/trips" className="btn btn-sm btn-outline-light">
                <i className="bi bi-signpost-split me-1"></i>Propose Trip
              </Link>
            )}
          </div>
        </div>

        {/* ---------- SOS banner — only takes space if something's actually active ---------- */}
        {hasActiveSos && (
          <div className="alert alert-danger d-flex align-items-center gap-2 mt-3 mb-0">
            <i className="bi bi-exclamation-triangle-fill fs-5"></i>
            <div className="flex-grow-1">
              <strong>{sosAlerts.data.length} active SOS alert{sosAlerts.data.length > 1 ? 's' : ''}</strong>
              {sosAlerts.data.map(a => (
                <div key={a.id} className="small">
                  {a.category} — {a.residentName || `Flat ${a.flatNumber}`}
                  <span className="badge bg-dark ms-2">{a.status}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ---------- Key counts only -- no lists here, that's what "Go to my dashboard" is for ---------- */}
        <div className="row g-3 mt-3">
          {isResident && (
            <StatTile icon="bi-ticket-perforated" color="#7ec8e3"
              value={passes.data?.length ?? '—'} label="Active Passes" to="/resident/passes" />
          )}
          <StatTile icon="bi-cash-coin" color="#f4d35e"
            value={pendingPaymentsCount} label="Payments Pending" to={isAdmin ? '/admin/payments' : '/resident/payments/history'}
            highlight={pendingPaymentsCount > 0} />
          {isResident && (
            <StatTile icon="bi-signpost-split" color="#f79256"
              value={trips.data?.length ?? '—'} label="Open Trips" to="/resident/trips" />
          )}
          {isAdmin && (
            <StatTile icon="bi-person-check" color="#7ec8e3"
              value={onboardingRequests.data?.length ?? '—'} label="Onboarding Pending" to="/admin/onboarding"
              highlight={(onboardingRequests.data?.length ?? 0) > 0} />
          )}
          {isAdmin && (
            <StatTile icon="bi-signpost-2" color="#8fd694"
              value={relocationRequests.data?.length ?? '—'} label="Relocation Pending" to="/admin/relocation"
              highlight={(relocationRequests.data?.length ?? 0) > 0} />
          )}
          {hasActiveSos && (
            <StatTile icon="bi-exclamation-triangle-fill" color="#e35d5d"
              value={sosAlerts.data.length} label="Active SOS" to="#" highlight />
          )}
        </div>

        {/* ---------- Community Noticeboard (Forum) ---------- */}
        <div className="mt-4">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="fw-bold mb-0"><i className="bi bi-pin-angle-fill me-2"></i>Community Noticeboard</h5>
            <Link to="/forum" className="small">See all posts &rarr;</Link>
          </div>

          <div
            className="p-4 rounded-3"
            style={{
              background: 'radial-gradient(circle at top left, #f6f1e7 0%, #efe7d8 100%)',
              border: '1px solid rgba(180,140,90,0.30)',
            }}
          >
            {posts.loading ? (
              <p className="text-muted small mb-0">Loading notices...</p>
            ) : posts.error ? (
              <p className="text-muted small mb-0"><i className="bi bi-exclamation-circle me-1"></i>Couldn't load the noticeboard.</p>
            ) : (posts.data?.length ?? 0) === 0 ? (
              <p className="text-muted small mb-0 text-center py-3">The noticeboard is empty. Be the first to post!</p>
            ) : (
              <div className="d-flex flex-wrap gap-3">
                {posts.data.map((p, i) => (
                  <div
                    key={p.id}
                    style={{
                      background: NOTE_COLORS[i % NOTE_COLORS.length],
                      color: '#2a2118',
                      width: 200,
                      minHeight: 130,
                      padding: '14px 14px 10px',
                      borderRadius: 3,
                      transform: `rotate(${NOTE_ROTATIONS[i % NOTE_ROTATIONS.length]}deg)`,
                      boxShadow: '0 6px 14px rgba(80,60,30,0.20)',
                      position: 'relative',
                    }}
                  >
                    <i
                      className="bi bi-pin-fill"
                      style={{ position: 'absolute', top: -10, left: '50%', transform: 'translateX(-50%)', fontSize: 20, color: '#c0392b' }}
                    ></i>
                    {p.pinned && (
                      <span className="badge bg-dark position-absolute" style={{ top: 6, right: 6, fontSize: 9 }}>PINNED</span>
                    )}
                    <div className="fw-bold small mb-1" style={{ lineHeight: 1.2 }}>{p.title}</div>
                    <div className="small" style={{ fontSize: 12, opacity: 0.85, lineHeight: 1.3 }}>
                      {(p.body || '').slice(0, 70)}{(p.body || '').length > 70 ? '…' : ''}
                    </div>
                    <div className="small mt-2" style={{ fontSize: 10.5, opacity: 0.7 }}>
                      — {p.authorName || 'Unknown'}, {p.category}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* ---------- Single link out to the full, detailed role dashboard ---------- */}
        <Link
          to={myDashboardPath}
          className="card p-4 text-decoration-none mt-4 d-flex flex-row align-items-center justify-content-between"
        >
          <div>
            <h6 className="fw-bold mb-1">
              <i className="bi bi-speedometer2 me-2"></i>
              {isAdmin ? 'Go to Admin Dashboard' : 'Go to My Dashboard'}
            </h6>
            <p className="text-muted small mb-0">
              {isAdmin
                ? 'Users, flats, gates, guards, onboarding, analytics, and more.'
                : 'Full entry log, passes, and account details.'}
            </p>
          </div>
          <i className="bi bi-arrow-right fs-3"></i>
        </Link>

      </div>
    </>
  )
}

function StatTile({ icon, color, value, label, to, highlight }) {
  return (
    <div className="col-md-3 col-6">
      <Link
        to={to}
        className="card p-3 text-decoration-none h-100 d-block"
        style={highlight ? { borderColor: color, borderWidth: 2 } : {}}
      >
        <div className="d-flex align-items-center gap-3">
          <div
            className="d-flex align-items-center justify-content-center rounded-circle flex-shrink-0"
            style={{ width: 44, height: 44, background: `${color}22` }}
          >
            <i className={`bi ${icon}`} style={{ color, fontSize: 20 }}></i>
          </div>
          <div>
            <div className="fs-4 fw-bold lh-1">{value}</div>
            <div className="text-muted small">{label}</div>
          </div>
        </div>
      </Link>
    </div>
  )
}