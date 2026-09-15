import { Link, useNavigate } from 'react-router-dom'

/**
 * Consistent "back" affordance for every sub-page in the app -- matches the
 * exact placement already proven in TripDetailPage ("← Back to Trips"),
 * but styled for visibility against this app's dark, dotted background
 * (Bootstrap's default text-muted was too low-contrast to notice at a glance).
 *
 * Usage:
 *   <BackButton to="/admin" label="Back to Admin Dashboard" />
 *     -> always goes to a fixed, predictable route
 *
 *   <BackButton label="Back" />
 *     -> no `to` prop: falls back to browser history (navigate(-1)).
 *        Use only for pages reachable from multiple different places.
 */
const linkStyle = {
  color: 'rgba(74,90,114,0.9)',
  fontSize: '0.85rem',
  fontWeight: 500,
  transition: 'color 0.15s ease',
}

export default function BackButton({ to, label = 'Back' }) {
  const navigate = useNavigate()

  const handlers = {
    onMouseEnter: (e) => { e.currentTarget.style.color = '#f97316' },
    onMouseLeave: (e) => { e.currentTarget.style.color = 'rgba(74,90,114,0.9)' },
  }

  if (to) {
    return (
      <Link
        to={to}
        className="d-inline-flex align-items-center mb-2 text-decoration-none"
        style={linkStyle}
        {...handlers}
      >
        <i className="bi bi-arrow-left me-1"></i>{label}
      </Link>
    )
  }

  return (
    <button
      onClick={() => navigate(-1)}
      className="btn btn-link d-inline-flex align-items-center mb-2 p-0 text-decoration-none border-0"
      style={linkStyle}
      {...handlers}
    >
      <i className="bi bi-arrow-left me-1"></i>{label}
    </button>
  )
}