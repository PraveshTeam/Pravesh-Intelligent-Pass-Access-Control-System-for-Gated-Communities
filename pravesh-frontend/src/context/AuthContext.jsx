import { createContext, useContext, useState, useEffect } from 'react'
import { shiftCheckout } from '../api/endpoints'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) {
      try {
        setUser(JSON.parse(stored))
      } catch {
        localStorage.removeItem('user')
      }
    }
    setIsLoading(false)
  }, [])

  // auth: { token, userId, name, email, role, verificationStatus }
  const loginUser = (auth) => {
    localStorage.setItem('token', auth.token)
    localStorage.setItem('user', JSON.stringify(auth))
    setUser(auth)
  }

  const updateVerificationStatus = (status) => {
    setUser(prev => {
      if (!prev) return prev
      const updated = { ...prev, verificationStatus: status }
      localStorage.setItem('user', JSON.stringify(updated))
      return updated
    })
  }

  const logout = async () => {
    if (user?.role === 'GUARD') {
      try {
        await shiftCheckout()
      } catch {
        // No active shift, or the request failed for some other reason --
        // either way, logout must proceed regardless.
      }
    }
    localStorage.clear()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loginUser, logout, isLoading, updateVerificationStatus }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
