import { useState, useEffect } from 'react'
import { getGates, addGate } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

export default function AdminGatesPage() {
  const { showToast } = useToast()
  const [gates, setGates] = useState([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ name: '', location: '' })

  const load = () => {
    setLoading(true)
    getGates()
      .then(res => setGates(res.data.data))
      .catch(() => showToast('Failed to load gates.', 'error'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!form.name) { showToast('Gate name is required.', 'warning'); return }
    try {
      await addGate(form)
      showToast(`Gate "${form.name}" added.`, 'success')
      setForm({ name: '', location: '' })
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to add gate.', 'error')
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-building-lock me-2"></i>Manage Gates</h4>
        </div>

        <div className="card p-3 mb-4">
          <h6 className="fw-bold mb-3"><i className="bi bi-plus-circle me-2 text-warning"></i>Add New Gate</h6>
          <form onSubmit={handleAdd} className="row g-2">
            <div className="col-md-5">
              <input className="form-control" placeholder="Gate Name (e.g. Main Gate) *"
                value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="col-md-4">
              <input className="form-control" placeholder="Location"
                value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
            </div>
            <div className="col-md-3">
              <button type="submit" className="btn btn-warning w-100">Add Gate</button>
            </div>
          </form>
          <div className="form-text mt-2">Assign a guard to a gate from the Guards page when creating the guard.</div>
        </div>

        {loading ? <LoadingSpinner />
          : (
            <div className="card p-0 overflow-hidden">
              <table className="table table-hover mb-0">
                <thead className="table-dark">
                  <tr><th>ID</th><th>Gate Name</th><th>Location</th><th>Guard</th></tr>
                </thead>
                <tbody>
                  {gates.map(g => (
                    <tr key={g.id}>
                      <td data-label="ID" className="text-muted">{g.id}</td>
                      <td data-label="Gate Name" className="fw-semibold">{g.name}</td>
                      <td data-label="Location">{g.location || '—'}</td>
                      <td data-label="Guard">{g.hasAssignedGuard ? <span className="badge bg-success">Assigned</span>
                        : <span className="badge bg-secondary">Unassigned</span>}</td>
                    </tr>
                  ))}
                  {gates.length === 0 && (
                    <tr><td colSpan={4} className="text-center text-muted py-3">No gates yet.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
      </div>
    </>
  )
}
