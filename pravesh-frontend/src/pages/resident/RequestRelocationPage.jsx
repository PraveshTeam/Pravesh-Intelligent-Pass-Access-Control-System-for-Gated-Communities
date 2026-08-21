import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  submitRelocationRequest, getSocieties, getMe, getMyRelocationRequest, revokeRelocationRequest
} from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'
import { sanitizeFlatNumberInput, isValidFlatNumber, FLAT_NUMBER_HINT } from '../../utils/inputValidation'

const DOCUMENT_TYPES = ['SALE_DEED', 'RENT_AGREEMENT', 'UTILITY_BILL', 'GOVT_ID', 'OTHER']
const MAX_SIZE = 5 * 1024 * 1024

export default function RequestRelocationPage() {
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({ targetSocietyId: '', claimedFlatNumber: '', tower: '', documentType: 'GOVT_ID' })
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)

  const [societies, setSocieties] = useState([])
  const [search, setSearch] = useState('')
  const [selectedSociety, setSelectedSociety] = useState(null)

  const [checkingStatus, setCheckingStatus] = useState(true)
  const [hasFlat, setHasFlat] = useState(true)
  const [pendingRequest, setPendingRequest] = useState(null)
  const [revoking, setRevoking] = useState(false)

  useEffect(() => {
    Promise.all([getMe(), getMyRelocationRequest()])
      .then(([meRes, reqRes]) => {
        setHasFlat(!!meRes.data.data.flatId)
        setPendingRequest(reqRes.data.data)
      })
      .catch(() => setHasFlat(true))
      .finally(() => setCheckingStatus(false))
  }, [])

  useEffect(() => {
    if (!hasFlat || pendingRequest) return
    getSocieties().then(res => setSocieties(res.data.data)).catch(() => {})
  }, [hasFlat, pendingRequest])

  const filtered = search.trim()
    ? societies.filter(s =>
        s.name.toLowerCase().includes(search.toLowerCase()) ||
        (s.city || '').toLowerCase().includes(search.toLowerCase()) ||
        (s.address || '').toLowerCase().includes(search.toLowerCase()))
    : societies

  const pickSociety = (s) => {
    setSelectedSociety(s)
    setForm({ ...form, targetSocietyId: s.id })
    setSearch('')
  }

  const handleFileChange = (e) => {
    const f = e.target.files[0]
    if (!f) return
    const ext = f.name.split('.').pop().toLowerCase()
    if (!['pdf', 'jpg', 'jpeg', 'png'].includes(ext)) { showToast('Only PDF, JPG, PNG allowed.', 'warning'); return }
    if (f.size > MAX_SIZE) { showToast('File must be under 5MB.', 'warning'); return }
    setFile(f)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.targetSocietyId || !form.claimedFlatNumber || !file) {
      showToast('Select a society, enter the new flat number, and attach proof.', 'warning'); return
    }
    if (!isValidFlatNumber(form.claimedFlatNumber)) {
      showToast('Flat number must look like A-101 (one capital letter, a hyphen, then up to 5 digits).', 'warning'); return
    }
    const fd = new FormData()
    fd.append('targetSocietyId', form.targetSocietyId)
    fd.append('claimedFlatNumber', form.claimedFlatNumber)
    fd.append('tower', form.tower)
    fd.append('documentType', form.documentType)
    fd.append('documentFile', file)
    setLoading(true)
    try {
      await submitRelocationRequest(fd)
      showToast('Relocation request submitted. Awaiting admin approval.', 'success')
      navigate('/resident')
    } catch (err) {
      showToast(err.response?.data?.message || 'Submission failed.', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleRevoke = async () => {
    if (!pendingRequest) return
    setRevoking(true)
    try {
      await revokeRelocationRequest(pendingRequest.id)
      showToast('Request withdrawn.', 'warning')
      setPendingRequest(null)
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to withdraw request.', 'error')
    } finally {
      setRevoking(false)
    }
  }

  if (checkingStatus) {
    return (
      <>
        <Navbar />
        <LoadingSpinner text="Checking your account..." />
      </>
    )
  }

  if (!hasFlat) {
    return (
      <>
        <Navbar />
        <div className="container py-4" style={{ maxWidth: 520 }}>
          <BackButton to="/resident" label="Back to Dashboard" />
          <div className="page-header">
            <h4 className="mb-0"><i className="bi bi-signpost-2 me-2"></i>Request Flat / Society Change</h4>
          </div>
          <div className="card p-4 text-center">
            <i className="bi bi-house-x text-warning" style={{ fontSize: '2.5rem' }}></i>
            <h6 className="fw-bold mt-3">You don't have a flat assigned right now</h6>
            <p className="text-muted small mb-4">
              Relocation moves you from an existing flat to a new one. Since your account isn't currently
              linked to a flat, please submit a fresh onboarding request instead to claim one.
            </p>
            <button className="btn btn-pravesh" onClick={() => navigate('/onboarding/submit')}>
              Go to Onboarding Request
            </button>
          </div>
        </div>
      </>
    )
  }

  if (pendingRequest) {
    return (
      <>
        <Navbar />
        <div className="container py-4" style={{ maxWidth: 520 }}>
          <BackButton to="/resident" label="Back to Dashboard" />
          <div className="page-header">
            <h4 className="mb-0"><i className="bi bi-signpost-2 me-2"></i>Request Flat / Society Change</h4>
          </div>
          <div className="card p-4">
            <div className="text-center mb-3">
              <i className="bi bi-hourglass-split text-warning" style={{ fontSize: '2.5rem' }}></i>
              <h6 className="fw-bold mt-3 mb-0">Your request is under review</h6>
            </div>
            <div className="p-3 rounded mb-3" style={{ background: 'var(--p-bg)' }}>
              <div className="d-flex justify-content-between small mb-1">
                <span className="text-muted">Moving to</span>
                <strong>{pendingRequest.claimedFlatNumber} · {pendingRequest.targetSocietyName}</strong>
              </div>
              <div className="d-flex justify-content-between small">
                <span className="text-muted">Submitted</span>
                <span>{new Date(pendingRequest.createdAt).toLocaleString('en-IN')}</span>
              </div>
            </div>
            <p className="text-muted small mb-3">
              You'll be notified once the target society's admin reviews this. You can withdraw it below
              if you change your mind.
            </p>
            <button className="btn btn-outline-danger w-100" onClick={handleRevoke} disabled={revoking}>
              {revoking
                ? <><span className="spinner-border spinner-border-sm me-2"></span>Withdrawing...</>
                : <><i className="bi bi-x-circle me-1"></i>Withdraw Request</>}
            </button>
          </div>
        </div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div className="container py-4" style={{ maxWidth: 520 }}>
        <BackButton to="/resident" label="Back to Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-signpost-2 me-2"></i>Request Flat / Society Change</h4>
        </div>
        <div className="card p-4">
          <p className="text-muted small mb-4">
            Moving to a new flat, same society or a different one? Submit proof and your new society's
            admin will review it. Your past passes and entry history stay exactly as they are.
          </p>
          <form onSubmit={handleSubmit}>
            <div className="mb-3 position-relative">
              <label className="form-label fw-semibold">New Society</label>
              {selectedSociety ? (
                <div className="d-flex justify-content-between align-items-center border rounded p-2 bg-light">
                  <div>
                    <div className="fw-semibold">{selectedSociety.name}</div>
                    <small className="text-muted">
                      {selectedSociety.address}{selectedSociety.city ? `, ${selectedSociety.city}` : ''}
                    </small>
                  </div>
                  <button type="button" className="btn btn-sm btn-outline-secondary"
                    onClick={() => { setSelectedSociety(null); setForm({ ...form, targetSocietyId: '' }) }}>
                    Change
                  </button>
                </div>
              ) : (
                <>
                  <input className="form-control" placeholder="Search by society name, city, or address..."
                    value={search} onChange={e => setSearch(e.target.value)} />
                  {search.trim() && (
                    <div className="border rounded mt-1" style={{ maxHeight: 200, overflowY: 'auto' }}>
                      {filtered.length === 0 && (
                        <div className="p-2 text-muted small">No matching societies found.</div>
                      )}
                      {filtered.map(s => (
                        <button type="button" key={s.id}
                          className="btn btn-link text-start w-100 text-decoration-none p-2 border-bottom"
                          onClick={() => pickSociety(s)}>
                          <div className="fw-semibold text-dark">{s.name}</div>
                          <small className="text-muted">
                            {s.address}{s.city ? `, ${s.city}` : ''}
                          </small>
                        </button>
                      ))}
                    </div>
                  )}
                </>
              )}
              <div className="form-text">Moving within your current society? Search and select it again below.</div>
            </div>

            <div className="row mb-3">
              <div className="col-8">
                <label className="form-label fw-semibold">New Flat Number</label>
                <input className="form-control" placeholder="B-205" value={form.claimedFlatNumber}
                  onChange={e => setForm({ ...form, claimedFlatNumber: sanitizeFlatNumberInput(e.target.value) })}
                  required />
                {form.claimedFlatNumber && !isValidFlatNumber(form.claimedFlatNumber) ? (
                  <div className="form-text text-danger">{FLAT_NUMBER_HINT}</div>
                ) : (
                  <div className="form-text">{FLAT_NUMBER_HINT}</div>
                )}
              </div>
              <div className="col-4">
                <label className="form-label fw-semibold">Tower</label>
                <input className="form-control" placeholder="B" value={form.tower}
                  onChange={e => setForm({ ...form, tower: e.target.value })} />
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label fw-semibold">Document Type</label>
              <select className="form-select" value={form.documentType}
                onChange={e => setForm({ ...form, documentType: e.target.value })}>
                {DOCUMENT_TYPES.map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
              </select>
            </div>

            <div className="mb-4">
              <label className="form-label fw-semibold">Proof Document</label>
              <input type="file" className="form-control" accept=".pdf,.jpg,.jpeg,.png" onChange={handleFileChange} required />
              <div className="form-text">PDF, JPG, or PNG — max 5MB.</div>
            </div>

            <button type="submit" className="btn btn-pravesh w-100 py-2" disabled={loading}>
              {loading
                ? <><span className="spinner-border spinner-border-sm me-2"></span>Submitting...</>
                : 'Submit Request'}
            </button>
          </form>
        </div>
      </div>
    </>
  )
}