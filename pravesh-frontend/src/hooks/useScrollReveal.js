import { useEffect } from 'react'

/**
 * Adds .is-visible to any [data-reveal] element as it scrolls into view.
 * Pure IntersectionObserver — no library, no bundle cost, GPU-friendly.
 */
export function useScrollReveal() {
  useEffect(() => {
    const els = document.querySelectorAll('[data-reveal]')
    if (!els.length) return

    const io = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            io.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.12, rootMargin: '0px 0px -60px 0px' }
    )

    els.forEach(el => io.observe(el))
    return () => io.disconnect()
  }, [])
}
