import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { listTrips, proposeTrip, requestToJoinTrip } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

const STATUS_BADGE = {
  OPEN: 'success',
  FULL: 'warning',
  CLOSED: 'secondary',
}

export default function TripsPage() {
  const { user } = useAuth()
  const { showToast } = useToast()

  const [trips, setTrips] = useState([])
  const [loading, setLoading] = useState(true)
  const [joiningId, setJoiningId] = useState(null)

  const [showProposeModal, setShowProposeModal] = useState(false)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [capacity, setCapacity] = useState(4)
  const [submitting, setSubmitting] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await listTrips()
      setTrips(res.data.data || [])
    } catch {
      setTrips([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handlePropose = async (e) => {
    e.preventDefault()
    if (!title.trim() || !capacity || capacity < 1) return
    setSubmitting(true)
    try {
      await proposeTrip(title.trim(), description.trim(), Number(capacity))
      showToast('Trip proposed!', 'success')
      setShowProposeModal(false)
      setTitle(''); setDescription(''); setCapacity(4)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not propose trip.', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const handleJoin = async (tripId) => {
    setJoiningId(tripId)
    try {
      await requestToJoinTrip(tripId)
      showToast('Join request sent!', 'success')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not send join request.', 'error')
    } finally {
      setJoiningId(null)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/dashboard" label="Back to Society Dashboard" />
        <div className="page-header d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-1"><i className="bi bi-signpost-split me-2"></i>Trip Buddy</h4>
            <p className="mb-0 opacity-75">Propose a trip, or join one already open</p>
          </div>
          <button className="btn btn-pravesh" onClick={() => setShowProposeModal(true)}>
            <i className="bi bi-plus-circle me-1"></i>Propose Trip
          </button>
        </div>

        {loading ? (
          <LoadingSpinner text="Loading trips..." />
        ) : trips.length === 0 ? (
          <div className="card p-4 text-center text-muted mt-3">
            No trips proposed yet. Be the first!
          </div>
        ) : (
          <div className="row g-3 mt-1">
            {trips.map(t => {
              const isCreator = t.creatorId === user?.userId
              const progressPct = Math.min(100, (t.acceptedCount / t.capacity) * 100)
              return (
                <div className="col-md-4 col-sm-6" key={t.id}>
                  <div className="card p-3 h-100 d-flex flex-column">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <h6 className="fw-bold mb-0">{t.title}</h6>
                      <span className={`badge bg-${STATUS_BADGE[t.status] || 'secondary'}`}>{t.status}</span>
                    </div>
                    <p className="text-muted small flex-grow-1">{t.description || 'No description provided.'}</p>
                    <div className="small text-muted mb-1">
                      Proposed by {t.creatorName || `User #${t.creatorId}`}
                    </div>
                    <div className="mb-2">
                      <div className="d-flex justify-content-between small text-muted mb-1">
                        <span>{t.acceptedCount} / {t.capacity} joined</span>
                      </div>
                      <div className="progress" style={{ height: 6 }}>
                        <div className="progress-bar bg-warning" style={{ width: `${progressPct}%` }}></div>
                      </div>
                    </div>

                    {isCreator ? (
                      <Link to={`/resident/trips/${t.id}`} className="btn btn-sm btn-outline-light w-100">
                        <i className="bi bi-people me-1"></i>Manage Requests
                      </Link>
                    ) : t.myRequestStatus === 'ACCEPTED' ? (
                      <div className="d-flex gap-2">
                        <button className="btn btn-sm btn-success flex-grow-1" disabled>
                          <i className="bi bi-check-circle me-1"></i>Joined
                        </button>
                        <Link to={`/resident/trips/${t.id}`} className="btn btn-sm btn-outline-light">
                          <i className="bi bi-chat-dots"></i>
                        </Link>
                      </div>
                    ) : t.myRequestStatus === 'PENDING' ? (
                      <div className="d-flex gap-2">
                        <button className="btn btn-sm btn-outline-warning flex-grow-1" disabled>
                          Request Pending
                        </button>
                        <Link to={`/resident/trips/${t.id}`} className="btn btn-sm btn-outline-light">
                          <i className="bi bi-chat-dots"></i>
                        </Link>
                      </div>
                    ) : t.myRequestStatus === 'REJECTED' ? (
                      <div className="d-flex gap-2">
                        <button className="btn btn-sm btn-outline-secondary flex-grow-1" disabled>
                          Request Declined
                        </button>
                        <Link to={`/resident/trips/${t.id}`} className="btn btn-sm btn-outline-light">
                          <i className="bi bi-chat-dots"></i>
                        </Link>
                      </div>
                    ) : (
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-sm btn-pravesh flex-grow-1"
                          disabled={t.status !== 'OPEN' || joiningId === t.id}
                          onClick={() => handleJoin(t.id)}
                        >
                          {joiningId === t.id
                            ? <span className="spinner-border spinner-border-sm"></span>
                            : t.status === 'OPEN' ? 'Request to Join' : t.status}
                        </button>
                        <Link to={`/resident/trips/${t.id}`} className="btn btn-sm btn-outline-light">
                          <i className="bi bi-chat-dots"></i>
                        </Link>
                      </div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {showProposeModal && (
        <div className="modal d-block" style={{ background: 'rgba(3,6,12,0.7)' }}
          tabIndex="-1" onClick={() => setShowProposeModal(false)}>
          <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
            <div className="modal-content sos-modal">
              <form onSubmit={handlePropose}>
                <div className="modal-header border-0">
                  <h5 className="modal-title fw-bold">
                    <i className="bi bi-signpost-split me-2"></i>Propose a Trip
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setShowProposeModal(false)}></button>
                </div>
                <div className="modal-body">
                  <label className="form-label small fw-semibold">Title</label>
                  <input
                    className="form-control mb-3"
                    value={title}
                    onChange={e => setTitle(e.target.value)}
                    placeholder="e.g. Weekend Trek to Lonavala"
                    maxLength={150}
                    required
                  />
                  <label className="form-label small fw-semibold">Description</label>
                  <textarea
                    className="form-control mb-3"
                    rows={3}
                    value={description}
                    onChange={e => setDescription(e.target.value)}
                    placeholder="Dates, meeting point, what to bring..."
                  />
                  <label className="form-label small fw-semibold">Capacity (including you)</label>
                  <input
                    type="number"
                    min={1}
                    className="form-control"
                    value={capacity}
                    onChange={e => setCapacity(e.target.value)}
                    required
                  />
                </div>
                <div className="modal-footer border-0">
                  <button type="button" className="btn btn-outline-secondary" onClick={() => setShowProposeModal(false)} disabled={submitting}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-pravesh fw-bold" disabled={submitting}>
                    {submitting
                      ? <><span className="spinner-border spinner-border-sm me-2"></span>Proposing...</>
                      : 'Propose Trip'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </>
  )
}