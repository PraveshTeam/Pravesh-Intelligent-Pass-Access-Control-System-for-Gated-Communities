import { useState } from 'react'

export default function PasswordInput({ value, onChange, placeholder, autoComplete, required }) {
  const [visible, setVisible] = useState(false)

  return (
    <div className="position-relative">
      <input
        type={visible ? 'text' : 'password'}
        className="form-control pe-5"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
        required={required}
      />
      <button
        type="button"
        className="btn btn-sm position-absolute top-50 end-0 translate-middle-y me-2 p-0 border-0 bg-transparent text-muted"
        style={{ zIndex: 5 }}
        onClick={() => setVisible(v => !v)}
        tabIndex={-1}
      >
        <i className={`bi ${visible ? 'bi-eye-slash' : 'bi-eye'}`}></i>
      </button>
    </div>
  )
}