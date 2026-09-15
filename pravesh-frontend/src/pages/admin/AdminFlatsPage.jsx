import { useState, useEffect } from 'react'
import { getFlats } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

export default function AdminFlatsPage() {
  const { showToast } = useToast()
  const [flats, setFlats] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getFlats()
      .then(res => setFlats(res.data.data))
      .catch(() => showToast('Failed to load flats.', 'error'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-door-open me-2"></i>Flats</h4>
          <p className="mb-0 opacity-75 small">Flats are created automatically when you approve a resident's onboarding request.</p>
        </div>

        {loading ? <LoadingSpinner />
          : (
            <div className="card p-0 overflow-hidden">
              <table className="table table-hover mb-0">
                <thead className="table-dark">
                  <tr><th>ID</th><th>Flat No.</th><th>Tower</th><th>Occupancy</th></tr>
                </thead>
                <tbody>
                  {flats.map(f => (
                    <tr key={f.id}>
                      <td data-label="ID" className="text-muted">{f.id}</td>
                      <td data-label="Flat No." className="fw-semibold">{f.flatNumber}</td>
                      <td data-label="Tower">{f.tower || '—'}</td>
                      <td data-label="Occupancy">{f.residentId ? <span className="badge bg-success">Occupied</span>
                        : <span className="badge bg-secondary">Vacant</span>}</td>
                    </tr>
                  ))}
                  {flats.length === 0 && (
                    <tr><td colSpan={4} className="text-center text-muted py-3">No flats yet.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
      </div>
    </>
  )
}
