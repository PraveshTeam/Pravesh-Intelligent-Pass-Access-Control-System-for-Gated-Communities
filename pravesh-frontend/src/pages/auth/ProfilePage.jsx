import { useState, useEffect } from 'react'
import { getMe, updateMe } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'
import { sanitizePhoneInput, isValidPhone, PHONE_HINT } from '../../utils/inputValidation'

export default function ProfilePage() {
  const { user, loginUser } = useAuth()
  const { showToast } = useToast()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({ name: '', phone: '' })

  useEffect(() => {
    getMe()
      .then(res => {
        const p = res.data.data
        setProfile(p)
        setForm({ name: p.name, phone: p.phone || '' })
      })
      .catch(() => showToast('Failed to load profile.', 'error'))
      .finally(() => setLoading(false))
  }, [])

  const handleUpdate = async (e) => {
    e.preventDefault()
    if (form.phone && !isValidPhone(form.phone)) {
      showToast('Enter a valid 10-digit phone number starting with 6-9.', 'warning'); return
    }
    setSaving(true)
    try {
      await updateMe(form)
      showToast('Profile updated successfully!', 'success')
      loginUser({ ...user, name: form.name, token: localStorage.getItem('token') })
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to update profile.', 'error')
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4" style={{ maxWidth: 520 }}>
        <BackButton label="Back" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-person-circle me-2"></i>My Profile</h4>
        </div>

        {loading ? <LoadingSpinner text="Loading your profile..." /> : profile && (
          <div className="card p-4">
            <div className="mb-4 text-center">
              <span className="badge bg-primary fs-6">{profile.role}</span>

              {(profile.societyName || profile.flatNumber) && (
                <div className="mt-3">
                  <div className="d-inline-flex align-items-center gap-2 border rounded-pill px-3 py-2"
                    style={{ background: 'var(--p-bg)' }}>
                    <i className="bi bi-building text-primary"></i>
                    <span className="small fw-semibold">
                      {profile.societyName || 'Society —'}
                      {profile.societyId && <span className="text-muted fw-normal"> (ID: {profile.societyId})</span>}
                      {profile.flatNumber && (
                        <>
                          {' · '}Flat {profile.flatNumber}
                          {profile.tower ? ` — Tower ${profile.tower}` : ''}
                          {profile.flatId && <span className="text-muted fw-normal"> (ID: {profile.flatId})</span>}
                        </>
                      )}
                    </span>
                  </div>
                </div>
              )}

              {profile.gateId && (
                <div className="text-muted small mt-2">Gate ID: {profile.gateId}</div>
              )}
              {profile.verificationStatus && (
                <div className="mt-2">
                  <span className={`badge ${profile.verificationStatus === 'VERIFIED' ? 'bg-success' : 'bg-warning text-dark'}`}>
                    {profile.verificationStatus}
                  </span>
                </div>
              )}
            </div>

            <form onSubmit={handleUpdate}>
              <div className="mb-3">
                <label className="form-label fw-semibold">Full Name</label>
                <input className="form-control" value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })} required />
              </div>
              <div className="mb-3">
                <label className="form-label fw-semibold">Email</label>
                <input className="form-control" value={profile.email} disabled />
                <div className="form-text">Email cannot be changed.</div>
              </div>
              <div className="mb-4">
                <label className="form-label fw-semibold">Phone</label>
                <input className="form-control" value={form.phone} inputMode="numeric" maxLength={10}
                  onChange={e => setForm({ ...form, phone: sanitizePhoneInput(e.target.value) })} />
                {form.phone && !isValidPhone(form.phone) && (
                  <div className="form-text text-danger">{PHONE_HINT}</div>
                )}
              </div>
              <button type="submit" className="btn btn-pravesh w-100" disabled={saving}>
                {saving
                  ? <><span className="spinner-border spinner-border-sm me-2"></span>Saving...</>
                  : <><i className="bi bi-save me-2"></i>Save Changes</>}
              </button>
            </form>
          </div>
        )}
      </div>
    </>
  )
}