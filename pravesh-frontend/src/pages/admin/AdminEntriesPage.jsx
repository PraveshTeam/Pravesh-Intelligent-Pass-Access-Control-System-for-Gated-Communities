import { useState, useEffect } from 'react'
import { getAllEntries } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

export default function AdminEntriesPage() {
  const { showToast } = useToast()
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL')

  useEffect(() => {
    getAllEntries()
      .then(res => setEntries(res.data.data))
      .catch(() => showToast('Failed to load entries.', 'error'))
      .finally(() => setLoading(false))
  }, [])

  const filtered = filter === 'ALL' ? entries : entries.filter(e => e.scanResult === filter)

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-list-check me-2"></i>All Entry Logs</h4>
        </div>

        <div className="mb-3">
          {['ALL', 'GRANTED', 'DENIED'].map(f => (
            <button key={f} className={`btn btn-sm me-2 ${filter === f ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setFilter(f)}>
              {f} {f !== 'ALL' && `(${entries.filter(e => e.scanResult === f).length})`}
            </button>
          ))}
          <span className="text-muted small ms-2">Total: {filtered.length}</span>
        </div>

        {loading ? <LoadingSpinner />
          : filtered.length === 0 ? <div className="card p-5 text-center text-muted">No entries found.</div>
          : (
            <div className="card p-0 overflow-hidden">
              <table className="table table-hover table-sm mb-0">
                <thead className="table-dark">
                  <tr><th>#</th><th>Visitor</th><th>Resident ID</th><th>Type</th><th>Result</th><th>Reason</th><th>Time</th></tr>
                </thead>
                <tbody>
                  {filtered.map((e, i) => (
                    <tr key={e.id}>
                      <td data-label="#" className="text-muted small">{i + 1}</td>
                      <td data-label="Visitor" className="fw-semibold">{e.visitorName || '—'}</td>
                      <td data-label="Resident ID">{e.residentId || '—'}</td>
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
