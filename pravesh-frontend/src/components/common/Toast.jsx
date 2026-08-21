import { useEffect } from 'react'

export default function Toast({ toasts, removeToast }) {
  return (
    <div className="pravesh-toast-container">
      {toasts.map(t => (
        <div
          key={t.id}
          className={`toast show align-items-center border-0 mb-2 pravesh-toast
            ${t.type === 'success' ? 'bg-success text-white' :
              t.type === 'error'   ? 'bg-danger text-white'  :
              t.type === 'warning' ? 'bg-warning text-dark'  :
              'bg-primary text-white'}`}
          role="alert"
        >
          <div className="d-flex">
            <div className="toast-body d-flex align-items-center gap-2">
              <i className={`bi ${
                t.type === 'success' ? 'bi-check-circle-fill' :
                t.type === 'error'   ? 'bi-x-circle-fill'     :
                t.type === 'warning' ? 'bi-exclamation-triangle-fill' :
                'bi-info-circle-fill'
              } fs-5`}></i>
              <span>{t.message}</span>
            </div>
            <button
              type="button"
              className={`btn-close ${t.type === 'warning' ? '' : 'btn-close-white'} me-2 m-auto`}
              onClick={() => removeToast(t.id)}
            />
          </div>
        </div>
      ))}
    </div>
  )
}