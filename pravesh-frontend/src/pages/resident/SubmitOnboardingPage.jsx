import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { submitOnboardingRequest, getSocieties } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import BackButton from '../../components/common/BackButton'
import { sanitizeFlatNumberInput, isValidFlatNumber, FLAT_NUMBER_HINT } from '../../utils/inputValidation'

const DOCUMENT_TYPES = ['SALE_DEED', 'RENT_AGREEMENT', 'UTILITY_BILL', 'GOVT_ID', 'OTHER']
const MAX_SIZE = 5 * 1024 * 1024

export default function SubmitOnboardingPage() {
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({ societyId: '', claimedFlatNumber: '', tower: '', documentType: 'GOVT_ID' })
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)

  const [societies, setSocieties] = useState([])
  const [search, setSearch] = useState('')
  const [selectedSociety, setSelectedSociety] = useState(null)

  // Load societies once; filter client-side as the user types
  useEffect(() => {
    getSocieties().then(res => setSocieties(res.data.data)).catch(() => {})
  }, [])

  const filtered = search.trim()
    ? societies.filter(s =>
        s.name.toLowerCase().includes(search.toLowerCase()) ||
        (s.city || '').toLowerCase().includes(search.toLowerCase()) ||
        (s.address || '').toLowerCase().includes(search.toLowerCase()))
    : societies

  const pickSociety = (s) => {
    setSelectedSociety(s)
    setForm({ ...form, societyId: s.id })
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
    if (!form.societyId || !form.claimedFlatNumber || !file) {
      showToast('Select your society, enter your flat, and attach a document.', 'warning'); return
    }
    if (!isValidFlatNumber(form.claimedFlatNumber)) {
      showToast('Flat number must look like A-101 (one capital letter, a hyphen, then up to 5 digits).', 'warning'); return
    }
    const fd = new FormData()
    fd.append('societyId', form.societyId)
    fd.append('claimedFlatNumber', form.claimedFlatNumber)
    fd.append('tower', form.tower)
    fd.append('documentType', form.documentType)
    fd.append('documentFile', file)
    setLoading(true)
    try {
      await submitOnboardingRequest(fd)
      showToast('Request submitted. Awaiting admin review.', 'success')
      navigate('/access-pending')
    } catch (err) {
      showToast(err.response?.data?.message || 'Submission failed.', 'error')
    } finally { setLoading(false) }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4" style={{ maxWidth: 520 }}>
        <BackButton to="/access-pending" label="Back" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-file-earmark-arrow-up me-2"></i>Submit Onboarding Request</h4>
        </div>
        <div className="card auth-card p-4">
          <p className="text-muted small mb-4">Find your society, enter your flat, and upload proof of residency.</p>
          <form onSubmit={handleSubmit}>

            <div className="mb-3 position-relative">
              <label className="form-label fw-semibold">Your Society</label>
              {selectedSociety ? (
                <div className="d-flex justify-content-between align-items-center border rounded p-2 bg-light">
                  <div>
                    <div className="fw-semibold">{selectedSociety.name}</div>
                    <small className="text-muted">
                      {selectedSociety.address}{selectedSociety.city ? `, ${selectedSociety.city}` : ''}
                    </small>
                  </div>
                  <button type="button" className="btn btn-sm btn-outline-secondary"
                    onClick={() => { setSelectedSociety(null); setForm({ ...form, societyId: '' }) }}>
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
            </div>

            <div className="row mb-3">
              <div className="col-8">
                <label className="form-label fw-semibold">Flat Number</label>
                <input className="form-control" placeholder="A-101" value={form.claimedFlatNumber}
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
                <input className="form-control" placeholder="A" value={form.tower}
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
              {loading ? 'Submitting...' : 'Submit Request'}
            </button>
          </form>
        </div>
      </div>
    </>
  )
}