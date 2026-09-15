import { createContext, useContext, useState, useCallback, useMemo, useRef } from 'react'
import Toast from '../components/common/Toast'

const ToastContext = createContext(null)

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const idCounterRef = useRef(0)

  const showToast = useCallback((message, type = 'success', duration = 3000) => {
    idCounterRef.current += 1
    const id = `${Date.now()}-${idCounterRef.current}`
    setToasts(prev => [...prev, { id, message, type }])
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id))
    }, duration)
  }, [])

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  // Memoize the context value so components using useToast() only
  // re-render when showToast/removeToast themselves actually change
  // (which, being useCallback with empty deps, is never) — not on
  // every single toast add/remove happening anywhere in the app.
  const value = useMemo(() => ({ showToast, removeToast }), [showToast, removeToast])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Toast toasts={toasts} removeToast={removeToast} />
    </ToastContext.Provider>
  )
}

export const useToast = () => useContext(ToastContext)