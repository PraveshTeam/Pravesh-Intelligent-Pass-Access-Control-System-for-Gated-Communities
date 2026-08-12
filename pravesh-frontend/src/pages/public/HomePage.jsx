import { useRef, useCallback, useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import logoMark from '../../assets/logo.png'
import Navbar from '../../components/common/Navbar'
import AnimatedCounter from '../../components/common/AnimatedCounter'
import { useScrollReveal } from '../../hooks/useScrollReveal'
import './HomePage.css'

// Types out each line, pauses, deletes, then moves to the next — loops forever.
function useTypewriter(lines, { typingSpeed = 55, deletingSpeed = 28, pauseTime = 1100 } = {}) {
  const [index, setIndex] = useState(0)
  const [subIndex, setSubIndex] = useState(0)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    const current = lines[index]

    if (!deleting && subIndex === current.length) {
      const t = setTimeout(() => setDeleting(true), pauseTime)
      return () => clearTimeout(t)
    }
    if (deleting && subIndex === 0) {
      setDeleting(false)
      setIndex((i) => (i + 1) % lines.length)
      return
    }
    const t = setTimeout(() => {
      setSubIndex((s) => s + (deleting ? -1 : 1))
    }, deleting ? deletingSpeed : typingSpeed)
    return () => clearTimeout(t)
  }, [subIndex, deleting, index, lines, typingSpeed, deletingSpeed, pauseTime])

  return lines[index].slice(0, subIndex)
}

export default function HomePage() {
  useScrollReveal()
  const heroRef = useRef(null)

  // Cursor spotlight — writes CSS vars, no re-render
  const handleMouseMove = useCallback((e) => {
    const el = heroRef.current
    if (!el) return
    const r = el.getBoundingClientRect()
    el.style.setProperty('--mx', `${e.clientX - r.left}px`)
    el.style.setProperty('--my', `${e.clientY - r.top}px`)
  }, [])

  const steps = [
    { n: '01', icon: 'bi-pencil-square', title: 'Create a pass', text: 'Enter visitor details and a validity window. A unique QR is generated instantly.' },
    { n: '02', icon: 'bi-send',          title: 'Share the QR',  text: 'Send it over WhatsApp, SMS or email — no app install for the visitor.' },
    { n: '03', icon: 'bi-upc-scan',      title: 'Guard scans',   text: 'Validity, society and usage are checked live before entry is granted.' },
    { n: '04', icon: 'bi-bell',          title: 'You get alerted', text: 'Entry is logged against your flat with an instant notification.' },
  ]

  const roles = [
    { id: 1, name: 'Residents',       desc: 'Create passes, track visitors, view history', icon: 'bi-house-heart' },
    { id: 2, name: 'Security Guards', desc: 'Scan passes, grant or deny entry',            icon: 'bi-person-badge' },
    { id: 3, name: 'Society Admins',  desc: 'Manage flats, gates, residents, records',     icon: 'bi-building' },
    { id: 4, name: 'Super Admins',    desc: 'Oversee societies and platform config',       icon: 'bi-shield-lock' },
  ]

  const stats = [
    { num: '100', suffix: '%', label: 'Digital Passes' },
    { num: '4',   suffix: '',  label: 'Role Dashboards' },
    { num: '24/7', suffix: '', label: 'Gate Monitoring' },
    { num: '<5s',  suffix: '', label: 'Verification' },
  ]

  const marquee = [
    'QR Passes', 'Live Entry Logs', 'Guard Shift Audit', 'Multi-Society',
    'Instant Alerts', 'Walk-in Approval', 'Role Dashboards', 'Analytics',
  ]

  const acronymLines = [
    'P Premises',
    'R Registration',
    'A And',
    'V Visitor',
    'E Entry',
    'S Security',
    'H Hub',
    '  Premises · Registration · And · Visitor · Entry · Security · Hub',
  ]
  const typed = useTypewriter(acronymLines, { typingSpeed: 48, deletingSpeed: 22, pauseTime: 1000 })
  const gapIdx = typed.indexOf(' ')
  const typedLetters = gapIdx === -1 ? typed : typed.slice(0, gapIdx)
  const typedWord = gapIdx === -1 ? '' : typed.slice(gapIdx + 1)

  return (
    <>
      <Navbar />
      <div className="home-page">

        {/* ══ HERO ══════════════════════════════════════ */}
        <section className="hero" ref={heroRef} onMouseMove={handleMouseMove}>
          <div className="hero-aurora" aria-hidden="true">
            <span className="aur aur-1"></span>
            <span className="aur aur-2"></span>
            <span className="aur aur-3"></span>
          </div>
          <div className="hero-noise" aria-hidden="true"></div>
          <div className="hero-dots" aria-hidden="true"></div>
          <div className="hero-spotlight" aria-hidden="true"></div>

          <div className="hero-inner">
            <div className="hero-copy">
              <div className="pill">
                <span className="pill-dot"></span>
                Intelligent Access Control
              </div>

              <div className="brand-reveal">
                <div className="brand-name">PRAVESH</div>
                <div className="brand-type" aria-label="PRAVESH stands for Premises Registration And Visitor Entry Security Hub">
                  <span className="bt-letters">{typedLetters}</span>
                  {typedWord && <span className="bt-sep">·</span>}
                  <span className="bt-word">{typedWord}</span>
                  <span className="bt-cursor">|</span>
                </div>
              </div>

              <h1 className="hero-h1">
                Every visitor <span className="glow-text">verified</span>.<br />
                Every entry <span className="glow-text">recorded</span>.
              </h1>

              <p className="hero-sub">
                Secure, paperless visitor management for gated communities —
                from QR pass creation to gate verification, in one platform.
              </p>

              <div className="hero-cta">
                <Link to="/register" className="btn-glow">
                  <span>Get Started</span>
                  <i className="bi bi-arrow-right"></i>
                </Link>
                <Link to="/about" className="btn-ghost">
                  <i className="bi bi-play-circle"></i> Learn More
                </Link>
              </div>

              <ul className="hero-ticks">
                <li><i className="bi bi-check-lg"></i> No app install for visitors</li>
                <li><i className="bi bi-check-lg"></i> Every entry auditable</li>
                <li><i className="bi bi-check-lg"></i> One-time &amp; multi-use</li>
              </ul>
            </div>

            {/* Floating product mockup */}
            <div className="hero-art" aria-hidden="true">
              <div className="art-ring art-ring-1"></div>
              <div className="art-ring art-ring-2"></div>

              <div className="glass-card pass-card">
                <div className="card-sheen"></div>
                <div className="pass-head">
                  <span className="tag">ONE-TIME PASS</span>
                  <span className="live"><span className="live-dot-sm"></span>Active</span>
                </div>
                <div className="qr">
                  {Array.from({ length: 36 }).map((_, i) => (
                    <span key={i} className={(i * 7) % 3 === 0 ? 'on' : ''}></span>
                  ))}
                </div>
                <div className="pass-meta">
                  <div><strong>Rohit Sharma</strong><small>Visitor</small></div>
                  <div className="text-end"><strong>A-264</strong><small>Flat</small></div>
                </div>
              </div>

              <div className="glass-card chip chip-scan">
                <span className="chip-ico ok"><i className="bi bi-check-lg"></i></span>
                <div>
                  <div className="chip-t">Entry Granted</div>
                  <div className="chip-s">Main Gate · just now</div>
                </div>
              </div>

              <div className="glass-card chip chip-bell">
                <span className="chip-ico gold"><i className="bi bi-bell-fill"></i></span>
                <div className="chip-t">Visitor entered</div>
              </div>
            </div>
          </div>

          {/* Stats sit inside the dark hero */}
          <div className="stats" data-reveal>
            {stats.map(s => (
              <div className="stat" key={s.label}>
                <div className="stat-n"><AnimatedCounter value={s.num} />{s.suffix}</div>
                <div className="stat-l">{s.label}</div>
              </div>
            ))}
          </div>
        </section>

        {/* ══ MARQUEE ═══════════════════════════════════ */}
        <div className="marquee" aria-hidden="true">
          <div className="marquee-track">
            {[...marquee, ...marquee].map((m, i) => (
              <span className="marquee-item" key={i}><i className="bi bi-dot"></i>{m}</span>
            ))}
          </div>
        </div>

        {/* ══ BENTO FEATURES ════════════════════════════ */}
        <section className="section">
          <div className="sec-head" data-reveal>
            <span className="eyebrow">What it does</span>
            <h2 className="sec-title">Why <span className="grad">Pravesh</span>?</h2>
          </div>

          <div className="bento">
            <div className="bento-cell bento-lg" data-reveal>
              <div className="cell-glow"></div>
              <div className="cell-ico"><i className="bi bi-qr-code-scan"></i></div>
              <h5>QR Gate Passes</h5>
              <p>
                Residents generate time-bound QR passes for visitors, delivery staff or cabs —
                scanned and verified at the gate in under five seconds, with exactly-once
                consumption enforced at the database level.
              </p>
              <div className="cell-visual">
                <div className="mini-qr">
                  {Array.from({ length: 25 }).map((_, i) => (
                    <span key={i} className={(i * 5) % 3 === 0 ? 'on' : ''}></span>
                  ))}
                </div>
              </div>
            </div>

            <div className="bento-cell" data-reveal style={{ transitionDelay: '80ms' }}>
              <div className="cell-glow"></div>
              <div className="cell-ico"><i className="bi bi-clock-history"></i></div>
              <h5>Real-Time Logs</h5>
              <p>Every scan logged instantly with gate, time and outcome.</p>
            </div>

            <div className="bento-cell" data-reveal style={{ transitionDelay: '160ms' }}>
              <div className="cell-glow"></div>
              <div className="cell-ico"><i className="bi bi-diagram-3"></i></div>
              <h5>Role-Based Access</h5>
              <p>Four dashboards, each built for exactly what that role does.</p>
            </div>

            <div className="bento-cell bento-wide" data-reveal style={{ transitionDelay: '240ms' }}>
              <div className="cell-glow"></div>
              <div className="cell-ico"><i className="bi bi-shield-check"></i></div>
              <h5>Multi-Society Isolation</h5>
              <p>
                Every pass, entry and user is scoped to its own society. A guard at one gate
                can never validate another community's pass — enforced end-to-end.
              </p>
            </div>
          </div>
        </section>

        {/* ══ HOW IT WORKS ══════════════════════════════ */}
        <section className="section">
          <div className="sec-head" data-reveal>
            <span className="eyebrow">The flow</span>
            <h2 className="sec-title">How It <span className="grad">Works</span></h2>
          </div>

          <div className="steps">
            {steps.map((s, i) => (
              <div className="step" key={s.n} data-reveal style={{ transitionDelay: `${i * 90}ms` }}>
                <div className="step-badge">
                  <i className={`bi ${s.icon}`}></i>
                  <span className="step-n">{s.n}</span>
                </div>
                <h6>{s.title}</h6>
                <p>{s.text}</p>
              </div>
            ))}
          </div>
        </section>

        {/* ══ ROLES ═════════════════════════════════════ */}
        <section className="section">
          <div className="sec-head" data-reveal>
            <span className="eyebrow">Who it's for</span>
            <h2 className="sec-title">Built For <span className="grad">Every Role</span></h2>
          </div>

          <div className="roles">
            {roles.map((r, i) => (
              <div className="role" key={r.id} data-reveal style={{ transitionDelay: `${i * 80}ms` }}>
                <div className="role-ico"><i className={`bi ${r.icon}`}></i></div>
                <h6>{r.name}</h6>
                <p>{r.desc}</p>
              </div>
            ))}
          </div>
        </section>

        {/* ══ CTA ═══════════════════════════════════════ */}
        <section className="cta" data-reveal>
          <div className="cta-aurora" aria-hidden="true"></div>
          <div className="cta-inner">
            <img src={logoMark} alt="Pravesh" className="cta-mark" />
            <h3>Ready to secure your community?</h3>
            <p>Bring every gate, guard and visitor pass onto one trusted platform.</p>
            <div className="hero-cta justify-content-center">
              <Link to="/register" className="btn-glow">
                <span>Create an Account</span>
                <i className="bi bi-arrow-right"></i>
              </Link>
              <Link to="/contact" className="btn-ghost">Talk to Us</Link>
            </div>
          </div>
        </section>

      </div>
    </>
  )
}
