import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Navbar from '../../components/common/Navbar'

const tiles = [
  { to: '/admin/onboarding', icon: 'bi-person-check', label: 'Onboarding Requests', color: 'text-warning' },
  { to: '/admin/users', icon: 'bi-people', label: 'Manage Users', color: 'text-primary' },
  { to: '/admin/flats', icon: 'bi-door-open', label: 'Flats', color: 'text-success' },
  { to: '/admin/gates', icon: 'bi-building-lock', label: 'Gates', color: 'text-danger' },
  { to: '/admin/guards', icon: 'bi-shield-lock', label: 'Guards', color: 'text-info' },
  { to: '/admin/entries', icon: 'bi-list-check', label: 'Entry Logs', color: 'text-secondary' },
  { to: '/admin/analytics', icon: 'bi-graph-up', label: 'Analytics', color: 'text-warning' },
  { to: '/admin/relocation', icon: 'bi-signpost-2', label: 'Relocation Requests', color: 'text-danger' },
  { to: '/admin/payments', icon: 'bi-cash-stack', label: 'Payments', color: 'text-success' },
  { to: '/dashboard', icon: 'bi-grid-1x2-fill', label: 'Society Dashboard', color: 'text-info' },
  { to: '/admin/sos-log', icon: 'bi-shield-exclamation', label: 'SOS Incident Log', color: 'text-danger' },
]

export default function AdminDashboard() {
  const { user } = useAuth()
  return (
    <>
      <Navbar />
      <div className="container py-4">
        <div className="page-header">
          <h4 className="mb-1"><i className="bi bi-building me-2"></i>Admin Dashboard</h4>
          <p className="mb-0 opacity-75">Welcome, {user.name}</p>
        </div>
        <div className="row g-3 stagger-in">
          {tiles.map(t => (
            <div className="col-md-3 col-6" key={t.to}>
              <Link to={t.to} className="card card-hover p-4 text-center text-decoration-none h-100 d-flex justify-content-center">
                <i className={`bi ${t.icon} fs-1 ${t.color}`}></i>
                <div className="fw-semibold mt-2 text-dark">{t.label}</div>
              </Link>
            </div>
          ))}
        </div>
      </div>
    </>
  )
}
