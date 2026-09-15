import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function ProtectedRoute({ children, roles }) {
  const { user, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) return null
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/unauthorized" replace />

  // Residents and society admins stuck PENDING can only reach Access Pending / Profile.
  const isPendingRole = user.role === 'RESIDENT' || user.role === 'SOCIETY_ADMIN'
  const allowedWhilePending = ['/access-pending', '/profile', '/onboarding/submit', '/society-onboarding/submit']
  const onAllowedPath = allowedWhilePending.includes(location.pathname)

  if (isPendingRole && user.verificationStatus === 'PENDING' && !onAllowedPath) {
    return <Navigate to="/access-pending" replace />
  }

  return children
}
