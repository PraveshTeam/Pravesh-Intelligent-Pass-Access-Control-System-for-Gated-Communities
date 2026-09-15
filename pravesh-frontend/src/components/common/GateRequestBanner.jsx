import { useState, useEffect, useRef } from 'react'
import { getPendingGateEntryRequests, approveGateEntryRequest, denyGateEntryRequest } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'

const POLL_INTERVAL_MS = 5000
const TICK_INTERVAL_MS = 1000
const GRACE_MS = 30000   // how long a resolved/expired card stays visible

const RESOLVED_META = {
  APPROVED: { label: 'You approved this visitor', icon: 'bi-check-circle-fill', cls: 'gate-resolved-approved' },
  DENIED:   { label: 'You denied this visitor',   icon: 'bi-x-circle-fill',     cls: 'gate-resolved-denied' },
  EXPIRED:  { label: 'No response in time',       icon: 'bi-clock-history',     cls: 'gate-resolved-expired' },
  RESOLVED: { label: 'Handled elsewhere',         icon: 'bi-check2',            cls: 'gate-resolved-expired' },
}

export default function GateRequestBanner() {
  const { showToast } = useToast()
  const [requests, setRequests] = useState([])
  const [respondingId, setRespondingId] = useState(null)
  const [, forceTick] = useState(0)
  const knownIdsRef = useRef(new Set())

  useEffect(() => {
    let active = true
    let timeoutId = null

    const runPoll = async () => {
      try {
        const res = await getPendingGateEntryRequests()
        if (!active) return
        const fresh = res.data.data || []
        const now = Date.now()

        fresh.forEach(r => {
          if (!knownIdsRef.current.has(r.id)) {
            showToast(`${r.visitorName} is at the gate for you`, 'warning')
            knownIdsRef.current.add(r.id)
          }
        })

        setRequests(prev => {
          const merged = new Map(prev.map(r => [r.id, r]))

          fresh.forEach(r => {
            const existing = merged.get(r.id)
            if (existing && existing.resolvedState) return
            merged.set(r.id, { ...r, missCount: 0 })
          })

          // A single dropped/empty response must NOT resolve a card that still
          // has real time left — require two consecutive misses.
          const freshIds = new Set(fresh.map(r => r.id))
          for (const [id, r] of merged) {
            if (r.resolvedState) continue
            if (freshIds.has(id)) continue

            if (new Date(r.expiresAt).getTime() <= now) {
              merged.set(id, { ...r, resolvedState: 'EXPIRED', resolvedAt: now })
              continue
            }

            const misses = (r.missCount || 0) + 1
            if (misses >= 2) {
              merged.set(id, { ...r, resolvedState: 'RESOLVED', resolvedAt: now })
            } else {
              merged.set(id, { ...r, missCount: misses })
            }
          }

          return Array.from(merged.values())
        })
      } catch {
        // Silent — a failed poll leaves current state untouched
      } finally {
        if (active) timeoutId = setTimeout(runPoll, POLL_INTERVAL_MS)
      }
    }

    runPoll()
    return () => {
      active = false
      clearTimeout(timeoutId)
    }
  }, [showToast])

  useEffect(() => {
    const tick = setInterval(() => {
      forceTick(t => t + 1)
      const now = Date.now()

      setRequests(prev => {
        let changed = false
        const next = []

        for (const r of prev) {
          if (r.resolvedState) {
            if (now - r.resolvedAt < GRACE_MS) {
              next.push(r)
            } else {
              changed = true
              knownIdsRef.current.delete(r.id)
            }
            continue
          }

          if (new Date(r.expiresAt).getTime() <= now) {
            next.push({ ...r, resolvedState: 'EXPIRED', resolvedAt: now })
            changed = true
            continue
          }

          next.push(r)
        }

        return changed ? next : prev
      })
    }, TICK_INTERVAL_MS)

    return () => clearInterval(tick)
  }, [])

  const respond = async (id, approve) => {
    setRespondingId(id)
    try {
      if (approve) {
        await approveGateEntryRequest(id)
        showToast('Visitor approved — they can enter now.', 'success')
      } else {
        await denyGateEntryRequest(id)
        showToast('Visitor entry denied.', 'warning')
      }
      const now = Date.now()
      setRequests(prev => prev.map(r =>
        r.id === id
          ? { ...r, resolvedState: approve ? 'APPROVED' : 'DENIED', resolvedAt: now }
          : r
      ))
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to respond.', 'error')
    } finally {
      setRespondingId(null)
    }
  }

  const timeLeft = (expiresAt) => {
    const ms = new Date(expiresAt).getTime() - Date.now()
    if (ms <= 0) return '0:00'
    const mins = Math.floor(ms / 60000)
    const secs = Math.floor((ms % 60000) / 1000)
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  if (requests.length === 0) return null

  return (
    <div className="gate-request-stack mb-4">
      {requests.map(r => {
        const meta = r.resolvedState ? RESOLVED_META[r.resolvedState] : null

        if (meta) {
          return (
            <div key={r.id} className={`gate-request-card gate-resolved ${meta.cls}`}>
              <div className="gate-request-content d-flex align-items-center gap-3">
                <span className="gate-resolved-icon"><i className={`bi ${meta.icon}`}></i></span>
                <div className="flex-grow-1">
                  <div className="gate-request-title">{meta.label}</div>
                  <div className="gate-request-sub">
                    {r.visitorName}{r.claimedFlatNumber ? ` · Flat ${r.claimedFlatNumber}` : ''}
                  </div>
                </div>
              </div>
            </div>
          )
        }

        return (
          <div key={r.id} className="gate-request-card">
            <div className="gate-request-glow"></div>
            <div className="gate-request-content">
              <div className="d-flex align-items-start justify-content-between">
                <div className="d-flex align-items-center gap-2">
                  <span className="gate-request-icon">
                    <i className="bi bi-person-walking"></i>
                  </span>
                  <div>
                    <div className="gate-request-title">Someone's at the gate</div>
                    <div className="gate-request-sub">Waiting for your response</div>
                  </div>
                </div>
                <span className="gate-request-timer">
                  <i className="bi bi-clock-history me-1"></i>{timeLeft(r.expiresAt)}
                </span>
              </div>

              <div className="gate-request-details">
                <div><strong>{r.visitorName}</strong>{r.visitorPhone ? ` · ${r.visitorPhone}` : ''}</div>
                {r.reason && <div className="text-muted small">Reason: {r.reason}</div>}
              </div>

              <div className="d-flex gap-2 mt-3">
                <button
                  className="btn btn-success flex-fill"
                  onClick={() => respond(r.id, true)}
                  disabled={respondingId === r.id}
                >
                  {respondingId === r.id
                    ? <span className="spinner-border spinner-border-sm"></span>
                    : <><i className="bi bi-check-lg me-1"></i>Approve</>}
                </button>
                <button
                  className="btn btn-outline-danger flex-fill"
                  onClick={() => respond(r.id, false)}
                  disabled={respondingId === r.id}
                >
                  <i className="bi bi-x-lg me-1"></i>Deny
                </button>
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}