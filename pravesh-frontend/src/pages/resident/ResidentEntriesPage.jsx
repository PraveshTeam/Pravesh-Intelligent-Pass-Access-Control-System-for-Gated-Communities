import { useState, useEffect } from 'react'
import { getFlatEntries } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

export default function ResidentEntriesPage() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getFlatEntries(user.userId)
      .then(res => setEntries(res.data.data))
      .catch(() => showToast('Failed to load entries.', 'error'))
      .finally(() => setLoading(false))
  }, [user.userId])

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/resident" label="Back to Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-list-check me-2"></i>My Entry Log</h4>
        </div>

        {loading ? <LoadingSpinner />
          : entries.length === 0 ? <div className="card p-5 text-center text-muted">No entries yet.</div>
          : (
            <div className="card p-0 overflow-hidden">
              <table className="table table-hover mb-0">
                <thead className="table-dark">
                  <tr><th>#</th><th>Visitor</th><th>Type</th><th>Result</th><th>Reason</th><th>Time</th></tr>
                </thead>
                <tbody>
                  {entries.map((e, i) => (
                    <tr key={e.id}>
                      <td data-label="#" className="text-muted">{i + 1}</td>
                      <td data-label="Visitor" className="fw-semibold">{e.visitorName || '—'}</td>
                      <td data-label="Type">
                        <span className={`badge ${e.entryType === 'WALK_IN' ? 'bg-info text-dark' : 'bg-secondary'}`}>
                          {e.entryType === 'WALK_IN' ? 'Walk-in' : 'QR'}
                        </span>
                      </td>
                      <td data-label="Result">
                        <span className={`badge ${e.scanResult === 'GRANTED' ? 'bg-success' :
                            e.scanResult === 'NO_RESPONSE' ? 'bg-secondary' : 'bg-danger'
                          }`}>
                          {e.scanResult === 'NO_RESPONSE' ? 'NO RESPONSE' : e.scanResult}
                        </span>
                      </td>
                      <td data-label="Reason"><small className="text-muted">{e.denyReason || '—'}</small></td>
                      <td data-label="Time"><small>{new Date(e.scannedAt).toLocaleString('en-IN')}</small></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
      </div>
    </>
  )
}
