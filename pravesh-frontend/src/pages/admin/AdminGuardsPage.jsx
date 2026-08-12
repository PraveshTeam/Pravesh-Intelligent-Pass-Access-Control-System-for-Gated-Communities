import { useState, useEffect, Fragment } from 'react'
import { getGuards, getGates, createGuard, reassignGuardGate, getGuardShiftHistory } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'
import { sanitizePhoneInput, isValidPhone, PHONE_HINT } from '../../utils/inputValidation'

export default function AdminGuardsPage() {
  const { showToast } = useToast()
  const [guards, setGuards] = useState([])
  const [allGates, setAllGates] = useState([])
  const [form, setForm] = useState({ name: '', phone: '', newGateName: '', newGateLocation: '' })
  const [expanded, setExpanded] = useState(null)
  const [shiftHistory, setShiftHistory] = useState([])

  const [pageLoading, setPageLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [historyLoadingId, setHistoryLoadingId] = useState(null)
  const [reassigningSubmitId, setReassigningSubmitId] = useState(null)

  const [reassigningId, setReassigningId] = useState(null)
  const [reassignGateId, setReassignGateId] = useState('')

  const loadData = async (showFullLoader = false) => {
    if (showFullLoader) setPageLoading(true)
    try {
      const [g, gt] = await Promise.all([getGuards(), getGates(false)])
      setGuards(g.data.data)
      setAllGates(gt.data.data)
    } catch {
      showToast('Failed to load guards/gates.', 'error')
    } finally {
      if (showFullLoader) setPageLoading(false)
    }
  }

  useEffect(() => { loadData(true) }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!form.name || !form.phone || !form.newGateName) {
      showToast('Fill guard name, phone, and gate name.', 'warning'); return
    }
    if (!isValidPhone(form.phone)) {
      showToast('Enter a valid 10-digit phone number starting with 6-9.', 'warning'); return
    }
    setCreating(true)
    try {
      await createGuard({
        name: form.name,
        phone: form.phone,
        newGateName: form.newGateName,
        newGateLocation: form.newGateLocation
      })
      showToast('Guard created with a new gate. Credentials sent via SMS.', 'success')
      setForm({ name: '', phone: '', newGateName: '', newGateLocation: '' })
      loadData()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to create guard.', 'error')
    } finally {
      setCreating(false)
    }
  }

  const toggleHistory = async (id) => {
    if (expanded === id) { setExpanded(null); return }
    setHistoryLoadingId(id)
    try {
      const res = await getGuardShiftHistory(id)
      setShiftHistory(res.data.data)
      setExpanded(id)
    } catch {
      showToast('Failed to load shift history.', 'error')
    } finally {
      setHistoryLoadingId(null)
    }
  }

  const startReassign = (guard) => {
    setReassigningId(guard.userId)
    setReassignGateId('')
  }

  const confirmReassign = async (guard) => {
    if (!reassignGateId) { showToast('Select a gate.', 'warning'); return }
    setReassigningSubmitId(guard.userId)
    try {
      await reassignGuardGate(guard.userId, Number(reassignGateId))
      showToast(`${guard.name} reassigned successfully.`, 'success')
      setReassigningId(null)
      loadData()
    } catch (err) {
      showToast(err.response?.data?.message || 'Reassignment failed.', 'error')
    } finally {
      setReassigningSubmitId(null)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-shield-lock me-2"></i>Guard Management</h4>
        </div>

        <div className="card p-4 mb-4">
          <h6 className="fw-bold mb-3">Create Guard Account</h6>
          <p className="text-muted small mb-3">
            A new gate is created automatically for this guard.
          </p>
          <form onSubmit={handleCreate} className="row g-3">
            <div className="col-md-6">
              <label className="form-label fw-semibold">Guard Name</label>
              <input className="form-control" value={form.name}
                onChange={e => setForm({ ...form, name: e.target.value })} disabled={creating} />
            </div>
            <div className="col-md-6">
              <label className="form-label fw-semibold">Phone (tablet SIM)</label>
              <input className="form-control" placeholder="9876543210" value={form.phone}
                inputMode="numeric" maxLength={10}
                onChange={e => setForm({ ...form, phone: sanitizePhoneInput(e.target.value) })} disabled={creating} />
              {form.phone && !isValidPhone(form.phone) && (
                <div className="form-text text-danger">{PHONE_HINT}</div>
              )}
            </div>
            <div className="col-md-6">
              <label className="form-label fw-semibold">New Gate Name</label>
              <input className="form-control" placeholder="Main Gate" value={form.newGateName}
                onChange={e => setForm({ ...form, newGateName: e.target.value })} disabled={creating} />
            </div>
            <div className="col-md-6">
              <label className="form-label fw-semibold">Gate Location <span className="text-muted fw-normal">(optional)</span></label>
              <input className="form-control" placeholder="Front Entrance" value={form.newGateLocation}
                onChange={e => setForm({ ...form, newGateLocation: e.target.value })} disabled={creating} />
            </div>
            <div className="col-12">
              <button className="btn btn-pravesh" disabled={creating}>
                {creating
                  ? <><span className="spinner-border spinner-border-sm me-2"></span>Creating...</>
                  : 'Create Guard + Gate'}
              </button>
            </div>
          </form>
        </div>

        <div className="card p-4">
          <h6 className="fw-bold mb-3">Guard Directory</h6>
          {pageLoading ? <LoadingSpinner text="Loading guards..." /> : (
          <table className="table">
            <thead>
              <tr><th>Name</th><th>Phone</th><th>Gate</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {guards.map(g => (
                <Fragment key={g.userId}>
                  <tr>
                    <td data-label="Name">{g.name}</td>
                    <td data-label="Phone">{g.phone}</td>
                    <td data-label="Gate">{g.gateName}</td>
                    <td data-label="Status">
                      <span className={`badge ${g.active ? 'bg-success' : 'bg-secondary'}`}>
                        {g.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td data-label="Actions" className="d-flex gap-2">
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        onClick={() => toggleHistory(g.userId)}
                        disabled={historyLoadingId === g.userId}
                      >
                        {historyLoadingId === g.userId
                          ? <span className="spinner-border spinner-border-sm"></span>
                          : (expanded === g.userId ? 'Hide' : 'Shifts')}
                      </button>
                      <button
                        className="btn btn-sm btn-outline-primary"
                        onClick={() => startReassign(g)}
                        disabled={reassigningSubmitId === g.userId}
                      >
                        Reassign Gate
                      </button>
                    </td>
                  </tr>

                  {reassigningId === g.userId && (
                    <tr>
                      <td colSpan={5}>
                        <div className="d-flex align-items-center gap-2 p-2 bg-light rounded flex-wrap">
                          <span className="small fw-semibold">Move to:</span>
                          <select className="form-select form-select-sm w-auto"
                            value={reassignGateId} onChange={e => setReassignGateId(e.target.value)}
                            disabled={reassigningSubmitId === g.userId}>
                            <option value="">Select a gate</option>
                            {allGates
                              .filter(gate => gate.id !== g.gateId)
                              .map(gate => (
                                <option key={gate.id} value={gate.id}>
                                  {gate.name}{gate.hasAssignedGuard ? ' (occupied)' : ''}
                                </option>
                              ))}
                          </select>
                          <button
                            className="btn btn-sm btn-success"
                            onClick={() => confirmReassign(g)}
                            disabled={reassigningSubmitId === g.userId}
                          >
                            {reassigningSubmitId === g.userId
                              ? <span className="spinner-border spinner-border-sm"></span>
                              : 'Confirm'}
                          </button>
                          <button
                            className="btn btn-sm btn-outline-secondary"
                            onClick={() => setReassigningId(null)}
                            disabled={reassigningSubmitId === g.userId}
                          >
                            Cancel
                          </button>
                        </div>
                      </td>
                    </tr>
                  )}

                  {expanded === g.userId && (
                    <tr>
                      <td colSpan={5}>
                        <table className="table table-sm mb-0">
                          <thead>
                            <tr><th>On-Duty Name</th><th>Employee ID</th><th>Start</th><th>End</th></tr>
                          </thead>
                          <tbody>
                            {shiftHistory.length === 0 && (
                              <tr><td colSpan={4} className="text-muted">No shifts logged.</td></tr>
                            )}
                            {shiftHistory.map(s => (
                              <tr key={s.shiftId}>
                                <td>{s.onDutyName}</td>
                                <td>{s.onDutyEmployeeId || '—'}</td>
                                <td>{new Date(s.shiftStart).toLocaleString('en-IN')}</td>
                                <td>{s.shiftEnd ? new Date(s.shiftEnd).toLocaleString('en-IN') : 'Ongoing'}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
              {guards.length === 0 && (
                <tr><td colSpan={5} className="text-center text-muted py-3">No guards yet.</td></tr>
              )}
            </tbody>
          </table>
          )}
        </div>
      </div>
    </>
  )
}