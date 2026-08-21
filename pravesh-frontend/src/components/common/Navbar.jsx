import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import fullLogo from '../../assets/full_logo.png'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = () => {
    setMenuOpen(false)
    logout()
    navigate('/')
  }

  const brandLink = () => {
  if (!user) return '/'
  switch (user.role) {
    case 'SUPER_ADMIN':    return '/super-admin'
    case 'SOCIETY_ADMIN':  return '/dashboard'
    case 'RESIDENT':       return '/dashboard'
    case 'GUARD':          return '/guard'
    default:               return '/'
  }
}

  return (
    <nav className="navbar navbar-pravesh navbar-expand-lg px-3 px-md-4 py-2">
      <div className="d-flex align-items-center justify-content-between w-100">
        <Link
          className="navbar-brand d-flex align-items-center"
          to={brandLink()}
          onClick={() => setMenuOpen(false)}
        >
          <img src={fullLogo} alt="Pravesh — Visitor Access Control" className="navbar-full-logo" />
        </Link>

        {/* Hamburger — mobile only */}
        <button
          className="btn btn-outline-light btn-sm d-lg-none"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle navigation"
        >
          <i className={`bi ${menuOpen ? 'bi-x-lg' : 'bi-list'} fs-5`}></i>
        </button>

        {/* Desktop — public links */}
        {!user && (
          <div className="navbar-links d-none d-lg-flex align-items-center gap-1 ms-4 me-auto">
            <Link to="/" className="nav-link-pravesh">Home</Link>
            <Link to="/about" className="nav-link-pravesh">About</Link>
            <Link to="/contact" className="nav-link-pravesh">Contact</Link>
          </div>
        )}

        {/* Desktop — logged in */}
        {user && (
          <div className="d-none d-lg-flex align-items-center gap-3 ms-auto">
            <span className="text-white-50 small">
              <i className="bi bi-person-circle me-1"></i>
              {user.name}
              <span className="badge bg-warning text-dark ms-2">{user.role}</span>
            </span>
            <Link to="/profile" className="btn btn-outline-light btn-sm">
              <i className="bi bi-gear"></i>
            </Link>
            <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>
              <i className="bi bi-box-arrow-right me-1"></i>Logout
            </button>
          </div>
        )}

        {/* Desktop — logged out */}
        {!user && (
          <div className="d-none d-lg-flex align-items-center gap-2 ms-auto">
            <Link to="/login" className="btn btn-outline-light btn-sm">Login</Link>
            <Link to="/register" className="btn btn-pravesh btn-sm">Register</Link>
          </div>
        )}
      </div>

      {/* Mobile dropdown menu */}
      {menuOpen && (
        <div className="w-100 d-lg-none mt-3 pb-2 border-top border-light border-opacity-25 pt-3">
          {!user && (
            <div className="d-flex flex-column gap-2">
              <Link to="/" className="nav-link-pravesh" onClick={() => setMenuOpen(false)}>Home</Link>
              <Link to="/about" className="nav-link-pravesh" onClick={() => setMenuOpen(false)}>About</Link>
              <Link to="/contact" className="nav-link-pravesh" onClick={() => setMenuOpen(false)}>Contact</Link>
              <hr className="border-light border-opacity-25 my-2" />
              <Link to="/login" className="btn btn-outline-light btn-sm w-100" onClick={() => setMenuOpen(false)}>
                Login
              </Link>
              <Link to="/register" className="btn btn-pravesh btn-sm w-100" onClick={() => setMenuOpen(false)}>
                Register
              </Link>
            </div>
          )}

          {user && (
            <div className="d-flex flex-column gap-2">
              <div className="text-white-50 small mb-1">
                <i className="bi bi-person-circle me-1"></i>
                {user.name}
                <span className="badge bg-warning text-dark ms-2">{user.role}</span>
              </div>
              <Link
                to="/profile"
                className="btn btn-outline-light btn-sm w-100"
                onClick={() => setMenuOpen(false)}
              >
                <i className="bi bi-gear me-1"></i>Profile
              </Link>
              <button className="btn btn-outline-light btn-sm w-100" onClick={handleLogout}>
                <i className="bi bi-box-arrow-right me-1"></i>Logout
              </button>
            </div>
          )}
        </div>
      )}
    </nav>
  )
}