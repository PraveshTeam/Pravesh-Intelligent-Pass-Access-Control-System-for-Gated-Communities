import { useState, useEffect } from 'react'
import {
  getRelocationRequests, getRelocationDocument, approveRelocationRequest, rejectRelocationRequest
} from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import FlatConflictModal from '../../components/common/FlatConflictModal'
import BackButton from '../../components/common/BackButton'

export default function AdminRelocationPage() {
  const { showToast } = useToast()
  const [requests, setRequests] = useState([])
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [loading, setLoading] = useState(true)
  const [rejectingId, setRejectingId] = useState(null)
  const [reason, setReason] = useState('')

  const [approvingId, setApprovingId] = useState(null)
  const [submittingRejectId, setSubmittingRejectId] = useState(null)
  const [viewingDocId, setViewingDocId] = useState(null)

  const [conflict, setConflict] = useState(null)
  const [conflictRequestId, setConflictRequestId] = useState(null)
  const [forcing, setForcing] = useState(false)

  const load = () => {
    setLoading(true)
    getRelocationRequests(statusFilter)
      .then(res => setRequests(res.data.data))
      .catch(() => showToast('Failed to load relocation requests.', 'error'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [statusFilter])

  const viewDoc = async (id) => {
    setViewingDocId(id)
    try {
      const res = await getRelocationDocument(id)
      window.open(URL.createObjectURL(res.data), '_blank')
    } catch {
      showToast('Failed to load document.', 'error')
    } finally {
      setViewingDocId(null)
    }
  }

  const approve = async (id, force = false) => {
    setApprovingId(id)
    try {
      await approveRelocationRequest(id, force)
      showToast(force ? 'Approved — previous occupant reassigned.' : 'Relocation approved. Flat reassigned.', 'success')
      setConflict(null)
      setConflictRequestId(null)
      load()
    } catch (err) {
      const data = err.response?.data
      if (err.response?.status === 409 && data?.conflict) {
        setConflict(data)
        setConflictRequestId(id)
      } else {
        showToast(data?.message || 'Approval failed.', 'error')
      }
    } finally {
      setApprovingId(null)
      setForcing(false)
    }
  }

  const confirmForceApprove = async () => {
    setForcing(true)
    await approve(conflictRequestId, true)
  }

  const reject = async (id) => {
    if (!reason.trim()) { showToast('Provide a rejection reason.', 'warning'); return }
    setSubmittingRejectId(id)
    try {
      await rejectRelocationRequest(id, reason)
      showToast('Rejected.', 'success')
      setRejectingId(null); setReason(''); load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Rejection failed.', 'error')
    } finally {
      setSubmittingRejectId(null)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-1"><i className="bi bi-signpost-2 me-2"></i>Relocation Requests</h4>
            <p className="mb-0 opacity-75 small">
              Residents requesting to move flat or society. Historical passes and entries are never changed.
            </p>
          </div>
          <select className="form-select w-auto" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>

        <div className="card p-4">
          {loading ? <LoadingSpinner text="Loading requests..." /> : (
          <table className="table">
            <thead>
              <tr>
                <th>Resident</th><th>From</th><th>To</th><th>Document</th><th>Status</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {requests.map(r => (
                <tr key={r.id}>
                  <td data-label="Resident">{r.residentName}</td>
                  <td data-label="From">
                    <div className="small">{r.oldFlatNumber}</div>
                    <div className="text-muted" style={{ fontSize: '0.75rem' }}>{r.oldSocietyName}</div>
                  </td>
                  <td data-label="To">
                    <div className="small">{r.claimedFlatNumber}</div>
                    <div className="text-muted" style={{ fontSize: '0.75rem' }}>{r.targetSocietyName}</div>
                  </td>
                  <td data-label="Document">
                    <button
                      className="btn btn-sm btn-outline-primary"
                      onClick={() => viewDoc(r.id)}
                      disabled={viewingDocId === r.id}
                    >
                      {viewingDocId === r.id
                        ? <span className="spinner-border spinner-border-sm"></span>
                        : <><i className="bi bi-file-earmark-text me-1"></i>View</>}
                    </button>
                  </td>
                  <td data-label="Status"><span className="badge bg-secondary">{r.status}</span></td>
                  <td data-label="Actions">
                    {r.status === 'PENDING' && (
                      <>
                        <button
                          className="btn btn-sm btn-success me-2"
                          onClick={() => approve(r.id, false)}
                          disabled={approvingId === r.id || submittingRejectId === r.id}
                        >
                          {approvingId === r.id
                            ? <><span className="spinner-border spinner-border-sm me-1"></span>Approving...</>
                            : 'Approve'}
                        </button>
                        {rejectingId === r.id ? (
                          <div className="d-inline-flex gap-1">
                            <input className="form-control form-control-sm" placeholder="Reason"
                              value={reason} onChange={e => setReason(e.target.value)} style={{ width: 160 }}
                              disabled={submittingRejectId === r.id} />
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => reject(r.id)}
                              disabled={submittingRejectId === r.id}
                            >
                              {submittingRejectId === r.id
                                ? <span className="spinner-border spinner-border-sm"></span>
                                : 'Confirm'}
                            </button>
                            <button
                              className="btn btn-sm btn-outline-secondary"
                              onClick={() => { setRejectingId(null); setReason('') }}
                              disabled={submittingRejectId === r.id}
                            >
                              Cancel
                            </button>
                          </div>
                        ) : (
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => setRejectingId(r.id)}
                            disabled={approvingId === r.id}
                          >
                            Reject
                          </button>
                        )}
                      </>
                    )}
                    {r.status === 'REJECTED' && r.adminNotes && (
                      <span className="text-muted small">Reason: {r.adminNotes}</span>
                    )}
                    {r.status === 'APPROVED' && r.adminNotes && (
                      <span className="text-warning small"><i className="bi bi-info-circle me-1"></i>{r.adminNotes}</span>
                    )}
                  </td>
                </tr>
              ))}
              {requests.length === 0 && (
                <tr><td colSpan={6} className="text-center text-muted py-3">No requests found.</td></tr>
              )}
            </tbody>
          </table>
          )}
        </div>
      </div>

      <FlatConflictModal
        show={!!conflict}
        conflict={conflict}
        confirming={forcing}
        onCancel={() => { setConflict(null); setConflictRequestId(null) }}
        onConfirm={confirmForceApprove}
      />
    </>
  )
}