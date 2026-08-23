import { useRef } from 'react'

export default function OtpInput({ value, onChange, length = 6 }) {
  const inputsRef = useRef([])

  const handleChange = (index, digit) => {
    if (!/^\d?$/.test(digit)) return
    const chars = value.split('')
    chars[index] = digit
    onChange(chars.join('').slice(0, length))
    if (digit && index < length - 1) inputsRef.current[index + 1]?.focus()
  }

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !value[index] && index > 0) {
      inputsRef.current[index - 1]?.focus()
    }
  }

  return (
    <div className="d-flex gap-2 justify-content-center">
      {Array.from({ length }).map((_, i) => (
        <input
          key={i}
          ref={el => (inputsRef.current[i] = el)}
          type="text"
          inputMode="numeric"
          maxLength={1}
          className="form-control text-center fw-bold fs-4"
          style={{ width: '48px', height: '56px' }}
          value={value[i] || ''}
          onChange={e => handleChange(i, e.target.value)}
          onKeyDown={e => handleKeyDown(i, e)}
        />
      ))}
    </div>
  )
}
