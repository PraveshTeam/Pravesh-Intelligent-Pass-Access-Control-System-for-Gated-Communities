import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { submitSocietyRequest } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import BackButton from '../../components/common/BackButton'

const MAX_SIZE = 5 * 1024 * 1024

export default function SubmitSocietyRequestPage() {
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({ societyName: '', address: '', city: '' })
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)

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
    if (!form.societyName || !file) { showToast('Provide a society name and a document.', 'warning'); return }
    const fd = new FormData()
    fd.append('societyName', form.societyName)
    fd.append('address', form.address)
    fd.append('city', form.city)
    fd.append('documentFile', file)
    setLoading(true)
    try {
      await submitSocietyRequest(fd)
      showToast('Society request submitted. Awaiting super admin review.', 'success')
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
          <h4 className="mb-0"><i className="bi bi-building-add me-2"></i>Register Your Society</h4>
        </div>
        <div className="card auth-card p-4">
          <p className="text-muted small mb-4">Upload a registration certificate or other proof for super admin verification.</p>
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label fw-semibold">Society Name</label>
              <input className="form-control" placeholder="Green Valley Residency"
                value={form.societyName} onChange={e => setForm({ ...form, societyName: e.target.value })} required />
            </div>
            <div className="mb-3">
              <label className="form-label fw-semibold">Address</label>
              <input className="form-control" value={form.address}
                onChange={e => setForm({ ...form, address: e.target.value })} />
            </div>
            <div className="mb-3">
              <label className="form-label fw-semibold">City</label>
              <input className="form-control" value={form.city}
                onChange={e => setForm({ ...form, city: e.target.value })} />
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
