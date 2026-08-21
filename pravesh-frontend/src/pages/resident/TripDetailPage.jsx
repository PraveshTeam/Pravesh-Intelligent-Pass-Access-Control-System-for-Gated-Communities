import { useState, useEffect, useCallback, useMemo } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  listTrips, listTripRequests, decideTripRequest,
  getTripDiscussion, addTripComment, getTripParticipants
} from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'

const STATUS_BADGE = {
  OPEN: 'success',
  FULL: 'warning',
  CLOSED: 'secondary',
  PENDING: 'secondary',
  ACCEPTED: 'success',
  REJECTED: 'danger',
}

export default function TripDetailPage() {
  const { id } = useParams()
  const tripId = Number(id)
  const { user } = useAuth()
  const { showToast } = useToast()

  const [trip, setTrip] = useState(null)
  const [requests, setRequests] = useState([])
  const [comments, setComments] = useState([])
  const [participants, setParticipants] = useState([])
  const [participantsError, setParticipantsError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [discussionError, setDiscussionError] = useState(null)
  const [decidingId, setDecidingId] = useState(null)
  const [commentBody, setCommentBody] = useState('')
  const [posting, setPosting] = useState(false)

  const [activeTab, setActiveTab] = useState('discussion') // 'requests' | 'participants' | 'discussion'
  const [requestStatusFilter, setRequestStatusFilter] = useState('PENDING') // defaults to Pending-only -- less noise for a big trip

  const isCreator = trip?.creatorId === user?.userId
  const myRequest = requests.find(r => r.requesterId === user?.userId)

  const load = useCallback(async () => {
    setLoading(true)
    setDiscussionError(null)
    try {
      const tripsRes = await listTrips()
      const found = (tripsRes.data.data || []).find(t => t.id === tripId)
      setTrip(found || null)

      if (found?.creatorId === user?.userId) {
        const reqRes = await listTripRequests(tripId)
        setRequests(reqRes.data.data || [])
        setActiveTab('requests') // organizer lands on Requests by default
      }

      try {
        const discRes = await getTripDiscussion(tripId)
        setComments(discRes.data.data || [])
      } catch (err) {
        setDiscussionError(err.response?.status === 403
          ? 'You need to be an accepted participant to view this discussion.'
          : 'Could not load discussion.')
      }

      try {
        const partRes = await getTripParticipants(tripId)
        setParticipants(partRes.data.data || [])
        setParticipantsError(null)
      } catch (err) {
        setParticipantsError(err.response?.status === 403
          ? 'You need to be an accepted participant to see who else is going.'
          : 'Could not load participants.')
      }
    } catch {
      setTrip(null)
    } finally {
      setLoading(false)
    }
  }, [tripId, user?.userId])

  useEffect(() => { load() }, [load])

  const handleDecide = async (requestId, status) => {
    setDecidingId(requestId)
    try {
      await decideTripRequest(tripId, requestId, status)
      showToast(`Request ${status.toLowerCase()}`, 'success')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not update request.', 'error')
    } finally {
      setDecidingId(null)
    }
  }

  const handlePostComment = async (e) => {
    e.preventDefault()
    if (!commentBody.trim()) return
    setPosting(true)
    try {
      await addTripComment(tripId, commentBody.trim())
      setCommentBody('')
      const discRes = await getTripDiscussion(tripId)
      setComments(discRes.data.data || [])
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not post comment.', 'error')
    } finally {
      setPosting(false)
    }
  }

  const filteredRequests = useMemo(() => {
    if (requestStatusFilter === 'ALL') return requests
    return requests.filter(r => r.status === requestStatusFilter)
  }, [requests, requestStatusFilter])

  const pendingCount = requests.filter(r => r.status === 'PENDING').length

  if (loading) {
    return (<><Navbar /><div className="container py-4"><LoadingSpinner text="Loading trip..." /></div></>)
  }

  if (!trip) {
    return (
      <>
        <Navbar />
        <div className="container py-4">
          <div className="card p-4 text-center text-muted">
            Trip not found, or it doesn't belong to your society.
          </div>
        </div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <div className="page-header d-flex justify-content-between align-items-start">
          <div>
            <Link to="/resident/trips" className="text-muted small d-block mb-2">
              <i className="bi bi-arrow-left me-1"></i>Back to Trips
            </Link>
            <h4 className="mb-1">
              <i className="bi bi-signpost-split me-2"></i>{trip.title}
              <span className={`badge bg-${STATUS_BADGE[trip.status]} ms-2`}>{trip.status}</span>
            </h4>
            <p className="mb-0 opacity-75">{trip.description || 'No description provided.'}</p>
          </div>
        </div>

        <div className="row g-3 mt-1">
          {/* Sidebar: static details, never scrolls with a big group */}
          <div className="col-md-3">
            <div className="card p-3">
              <h6 className="fw-bold mb-2">Details</h6>
              <div className="small text-muted mb-1">Proposed by</div>
              <div className="fw-semibold mb-2">{trip.creatorName || `User #${trip.creatorId}`}</div>
              <div className="small text-muted mb-1">Capacity</div>
              <div className="fw-semibold mb-2">{trip.acceptedCount} / {trip.capacity} joined</div>
              {!isCreator && myRequest && (
                <>
                  <div className="small text-muted mb-1">Your request</div>
                  <span className={`badge bg-${STATUS_BADGE[myRequest.status]}`}>{myRequest.status}</span>
                </>
              )}
            </div>
          </div>

          {/* Main panel: tabbed instead of stacked -- scales to any group size */}
          <div className="col-md-9">
            <div className="card p-0 overflow-hidden">
              <ul className="nav nav-tabs px-3 pt-2" style={{ borderBottom: '1px solid rgba(20,40,70,0.1)' }}>
                {isCreator && (
                  <li className="nav-item">
                    <button
                      className={`nav-link ${activeTab === 'requests' ? 'active' : ''}`}
                      onClick={() => setActiveTab('requests')}
                    >
                      <i className="bi bi-people me-1"></i>Requests
                      {pendingCount > 0 && <span className="badge bg-warning text-dark ms-1">{pendingCount}</span>}
                    </button>
                  </li>
                )}
                <li className="nav-item">
                  <button
                    className={`nav-link ${activeTab === 'participants' ? 'active' : ''}`}
                    onClick={() => setActiveTab('participants')}
                  >
                    <i className="bi bi-person-lines-fill me-1"></i>Who's Going
                    {participants.length > 0 && <span className="badge bg-secondary ms-1">{participants.length}</span>}
                  </button>
                </li>
                <li className="nav-item">
                  <button
                    className={`nav-link ${activeTab === 'discussion' ? 'active' : ''}`}
                    onClick={() => setActiveTab('discussion')}
                  >
                    <i className="bi bi-chat-dots me-1"></i>Discussion
                  </button>
                </li>
              </ul>

              <div className="p-3">
                {/* ---------- Requests tab ---------- */}
                {activeTab === 'requests' && isCreator && (
                  <div>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                      <span className="small text-muted">
                        Showing {requestStatusFilter === 'ALL' ? 'all' : requestStatusFilter.toLowerCase()} requests
                      </span>
                      <select
                        className="form-select form-select-sm w-auto"
                        value={requestStatusFilter}
                        onChange={e => setRequestStatusFilter(e.target.value)}
                      >
                        <option value="PENDING">Pending</option>
                        <option value="ACCEPTED">Accepted</option>
                        <option value="REJECTED">Rejected</option>
                        <option value="ALL">All Statuses</option>
                      </select>
                    </div>
                    {filteredRequests.length === 0 ? (
                      <p className="text-muted small text-center py-3">
                        No {requestStatusFilter !== 'ALL' ? requestStatusFilter.toLowerCase() : ''} requests.
                      </p>
                    ) : (
                      <div style={{ maxHeight: 420, overflowY: 'auto' }}>
                        <table className="table table-sm align-middle mb-0">
                          <thead>
                            <tr><th>Resident</th><th>Status</th><th></th></tr>
                          </thead>
                          <tbody>
                            {filteredRequests.map(r => (
                              <tr key={r.id}>
                                <td>{r.requesterName || `User #${r.requesterId}`}</td>
                                <td><span className={`badge bg-${STATUS_BADGE[r.status]}`}>{r.status}</span></td>
                                <td className="text-end">
                                  {r.status === 'PENDING' && (
                                    <div className="d-flex gap-1 justify-content-end">
                                      <button
                                        className="btn btn-sm btn-success"
                                        disabled={decidingId === r.id}
                                        onClick={() => handleDecide(r.id, 'ACCEPTED')}
                                      >Accept</button>
                                      <button
                                        className="btn btn-sm btn-outline-danger"
                                        disabled={decidingId === r.id}
                                        onClick={() => handleDecide(r.id, 'REJECTED')}
                                      >Reject</button>
                                    </div>
                                  )}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                )}

                {/* ---------- Who's Going tab ---------- */}
                {activeTab === 'participants' && (
                  participantsError ? (
                    <div className="alert alert-secondary small mb-0">{participantsError}</div>
                  ) : participants.length === 0 ? (
                    <p className="text-muted small text-center py-3">No participants yet.</p>
                  ) : (
                    <div style={{ maxHeight: 420, overflowY: 'auto' }}>
                      <table className="table table-sm align-middle mb-0">
                        <thead>
                          <tr><th>Name</th><th>Flat</th><th>Phone</th><th></th></tr>
                        </thead>
                        <tbody>
                          {participants.map(p => (
                            <tr key={p.userId}>
                              <td className="fw-semibold">{p.name || `User #${p.userId}`}</td>
                              <td className="text-muted">{p.flatNumber || '—'}</td>
                              <td className="text-muted">{p.phone || '—'}</td>
                              <td>{p.isCreator && <span className="badge bg-warning text-dark">Organizer</span>}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )
                )}

                {/* ---------- Discussion tab ---------- */}
                {activeTab === 'discussion' && (
                  discussionError ? (
                    <div className="alert alert-secondary small mb-0">{discussionError}</div>
                  ) : (
                    <>
                      <div style={{ maxHeight: 360, overflowY: 'auto' }} className="mb-3">
                        {comments.length === 0 ? (
                          <p className="text-muted small text-center py-3">No messages yet.</p>
                        ) : comments.map(c => (
                          <div key={c.id} className="border-bottom pb-2 mb-2">
                            <div className="d-flex justify-content-between">
                              <span className="fw-semibold small">{c.authorName || `User #${c.authorId}`}</span>
                              <span className="text-muted small">{new Date(c.createdAt).toLocaleString()}</span>
                            </div>
                            <div className="small">{c.body}</div>
                          </div>
                        ))}
                      </div>
                      <form onSubmit={handlePostComment} className="d-flex gap-2">
                        <input
                          className="form-control form-control-sm"
                          placeholder="Message the group..."
                          value={commentBody}
                          onChange={e => setCommentBody(e.target.value)}
                        />
                        <button
                          className="btn btn-sm btn-pravesh flex-shrink-0"
                          style={{ whiteSpace: 'nowrap' }}
                          disabled={posting || !commentBody.trim()}
                        >
                          {posting ? <span className="spinner-border spinner-border-sm"></span> : 'Send'}
                        </button>
                      </form>
                    </>
                  )
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}