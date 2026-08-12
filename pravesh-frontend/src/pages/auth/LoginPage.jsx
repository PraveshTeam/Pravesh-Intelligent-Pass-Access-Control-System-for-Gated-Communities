import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import logoMark from '../../assets/logo.png'
import PasswordInput from '../../components/common/PasswordInput'

export default function LoginPage() {
  const { loginUser } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)

  const routeByRole = (auth) => {
  const { role, verificationStatus } = auth
  if ((role === 'RESIDENT' || role === 'SOCIETY_ADMIN') && verificationStatus === 'PENDING') {
    navigate('/access-pending'); return
  }
  if (role === 'SUPER_ADMIN') navigate('/super-admin')
  else if (role === 'SOCIETY_ADMIN') navigate('/dashboard')
  else if (role === 'RESIDENT') navigate('/dashboard')
  else if (role === 'GUARD') navigate('/guard')
  else navigate('/')
}

  const decodeJwtPayload = (token) => {
    try {
      return JSON.parse(atob(token.split('.')[1]))
    } catch {
      return {}
    }
  }

  const handleLogin = async () => {
    if (!form.email || !form.password) {
      showToast('Please enter email and password.', 'warning'); return
    }
    setLoading(true)
    try {
      const res = await login(form)
      const auth = res.data.data
      const claims = decodeJwtPayload(auth.token)
      const authWithSociety = { ...auth, societyId: claims.societyId }

      loginUser(authWithSociety)
      showToast(`Welcome back, ${auth.name}!`, 'success')
      routeByRole(authWithSociety)
    } catch (err) {
      showToast(err.response?.data?.message || 'Login failed. Check credentials.', 'error')
    } finally {
      setLoading(false)
    }
  }

  const onKey = (e) => { if (e.key === 'Enter') handleLogin() }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center">
      <div className="card auth-card p-4" style={{ width: '100%', maxWidth: 420 }}>
        <div className="text-center mb-4">
          <Link to="/"><img src={logoMark} alt="Pravesh" style={{ width: 64 }} /></Link>
          <h3 className="fw-bold mt-2">Pravesh</h3>
          <p className="text-muted small">Society Visitor Management</p>
        </div>

        <div className="mb-3">
          <label className="form-label fw-semibold">Email</label>
          <input type="email" className="form-control"
            value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}
            onKeyDown={onKey} autoComplete="username" />
        </div>
        <div className="mb-2">
          <label className="form-label fw-semibold">Password</label>
          <PasswordInput
            value={form.password}
            onChange={e => setForm({ ...form, password: e.target.value })}
            placeholder="••••••••"
            autoComplete="current-password"
            required
          />
        </div>
        <div className="text-end mb-3">
          <Link to="/forgot-password" className="small text-decoration-none">Forgot password?</Link>
        </div>

        <button className="btn btn-pravesh w-100 py-2" onClick={handleLogin} disabled={loading}>
          {loading
            ? <span className="spinner-border spinner-border-sm me-2"></span>
            : <i className="bi bi-box-arrow-in-right me-2"></i>}
          {loading ? 'Logging in...' : 'Login'}
        </button>

        <p className="text-center text-muted small mt-3">
          Don't have an account? <Link to="/register" className="text-decoration-none">Register</Link>
        </p>
      </div>
    </div>
  )
}