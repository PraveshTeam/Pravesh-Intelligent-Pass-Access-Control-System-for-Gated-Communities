import { useEffect, useRef, useState } from 'react'

/**
 * Counts up to `value` once the element scrolls into view.
 * Accepts strings like "24/7" or "<5s" and renders them verbatim
 * (only pure numbers animate).
 */
export default function AnimatedCounter({ value, duration = 1400 }) {
  const [display, setDisplay] = useState(0)
  const ref = useRef(null)
  const startedRef = useRef(false)

  const numeric = typeof value === 'number' ? value : parseInt(String(value), 10)
  const isPureNumber = !Number.isNaN(numeric) && String(value).trim() === String(numeric)

  useEffect(() => {
    if (!isPureNumber || !ref.current) return

    const io = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !startedRef.current) {
          startedRef.current = true
          const start = performance.now()
          const step = (now) => {
            const p = Math.min((now - start) / duration, 1)
            const eased = 1 - Math.pow(1 - p, 3)   // easeOutCubic
            setDisplay(Math.round(numeric * eased))
            if (p < 1) requestAnimationFrame(step)
          }
          requestAnimationFrame(step)
        }
      })
    }, { threshold: 0.4 })

    io.observe(ref.current)
    return () => io.disconnect()
  }, [numeric, isPureNumber, duration])

  return <span ref={ref}>{isPureNumber ? display : value}</span>
}
