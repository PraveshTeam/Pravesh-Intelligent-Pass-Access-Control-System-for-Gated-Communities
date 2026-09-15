import { useState, useEffect } from 'react'
import {
  getSocietyRequests, getSocietyRequestDocument,
  approveSocietyRequest, rejectSocietyRequest
} from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'

export default function SuperAdminDashboard() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [requests, setRequests] = useState([])
  const [statusFilter, setStatusFilter] = useState('PENDING')
  const [loading, setLoading] = useState(true)
  const [rejectingId, setRejectingId] = useState(null)
  const [reason, setReason] = useState('')

  const [approvingId, setApprovingId] = useState(null)
  const [submittingRejectId, setSubmittingRejectId] = useState(null)
  const [viewingDocId, setViewingDocId] = useState(null)

  const load = () => {
    setLoading(true)
    getSocietyRequests(statusFilter)
      .then(res => setRequests(res.data.data))
      .catch(() => showToast('Failed to load society requests.', 'error'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [statusFilter])

  const viewDoc = async (id) => {
    setViewingDocId(id)
    try {
      const res = await getSocietyRequestDocument(id)
      window.open(URL.createObjectURL(res.data), '_blank')
    } catch {
      showToast('Failed to load document.', 'error')
    } finally {
      setViewingDocId(null)
    }
  }

  const approve = async (id) => {
    setApprovingId(id)
    try {
      await approveSocietyRequest(id)
      showToast('Society approved and created.', 'success')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Approval failed.', 'error')
    } finally {
      setApprovingId(null)
    }
  }

  const reject = async (id) => {
    if (!reason.trim()) { showToast('Provide a rejection reason.', 'warning'); return }
    setSubmittingRejectId(id)
    try {
      await rejectSocietyRequest(id, reason)
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
        <div className="page-header d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-1"><i className="bi bi-shield-lock me-2"></i>Super Admin Dashboard</h4>
            <p className="mb-0 opacity-75">Welcome, {user?.name}</p>
          </div>
          <select className="form-select w-auto" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>

        <div className="card p-4">
          <h6 className="fw-bold mb-3">Society Registration Requests</h6>
          {loading ? <LoadingSpinner text="Loading requests..." /> : (
          <table className="table">
            <thead>
              <tr><th>Admin</th><th>Society</th><th>City</th><th>Document</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {requests.map(r => (
                <tr key={r.id}>
                  <td data-label="Admin">{r.adminName}</td>
                  <td data-label="Society">{r.societyName}</td>
                  <td data-label="City">{r.city || '—'}</td>
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
                          onClick={() => approve(r.id)}
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
    </>
  )
}