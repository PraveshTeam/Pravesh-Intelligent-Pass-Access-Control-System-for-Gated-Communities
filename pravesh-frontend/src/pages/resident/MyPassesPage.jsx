import { useState, useEffect } from 'react'
import { getActivePasses, getPassHistory, revokePass, regenerateQr } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import QrViewModal from '../../components/common/QrViewModal'
import BackButton from '../../components/common/BackButton'

const badgeClass = (status) => ({
  ACTIVE: 'bg-primary', CONSUMED: 'bg-secondary',
  REVOKED: 'bg-danger', EXPIRED: 'bg-warning text-dark'
}[status] || 'bg-secondary')

export default function MyPassesPage() {
  const { showToast } = useToast()
  const [tab, setTab] = useState('ACTIVE')
  const [passes, setPasses] = useState([])
  const [loading, setLoading] = useState(true)
  const [revokingId, setRevokingId] = useState(null)

  // QR view modal state
  const [qrModalOpen, setQrModalOpen] = useState(false)
  const [qrLoading, setQrLoading] = useState(false)
  const [qrError, setQrError] = useState('')
  const [qrBase64, setQrBase64] = useState('')
  const [qrVisitorName, setQrVisitorName] = useState('')

  const load = () => {
    setLoading(true)
    const call = tab === 'ACTIVE' ? getActivePasses : getPassHistory
    call().then(res => setPasses(res.data.data))
      .catch(() => showToast('Failed to load passes.', 'error'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [tab])

  const handleRevoke = async (id) => {
    setRevokingId(id)
    try {
      await revokePass(id)
      showToast('Pass revoked.', 'warning')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to revoke.', 'error')
    } finally {
      setRevokingId(null)
    }
  }

  const handleViewQr = async (pass) => {
    setQrVisitorName(pass.visitorName)
    setQrModalOpen(true)
    setQrLoading(true)
    setQrError('')
    setQrBase64('')
    try {
      const res = await regenerateQr(pass.id)
      const data = res.data.data
      if (data?.qrBase64) {
        setQrBase64(data.qrBase64)
      } else {
        setQrError('QR code is unavailable for this pass.')
      }
    } catch (err) {
      setQrError(err.response?.data?.message || 'Failed to load QR code.')
    } finally {
      setQrLoading(false)
    }
  }

  const closeQrModal = () => {
    setQrModalOpen(false)
    setQrBase64('')
    setQrError('')
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/resident" label="Back to Dashboard" />
        <div className="page-header">
          <h4 className="mb-0"><i className="bi bi-ticket-detailed me-2"></i>My Passes</h4>
        </div>

        <ul className="nav nav-pills mb-3">
          {['ACTIVE', 'HISTORY'].map(t => (
            <li className="nav-item" key={t}>
              <button className={`nav-link ${tab === t ? 'active' : ''}`} onClick={() => setTab(t)}>
                {t === 'ACTIVE' ? 'Active' : 'History'}
              </button>
            </li>
          ))}
        </ul>

        {loading ? <LoadingSpinner text="Loading your passes..." />
          : passes.length === 0
            ? <div className="card p-5 text-center text-muted">No passes found.</div>
            : (
              <div className="row g-3 stagger-in">
                {passes.map(p => (
                  <div className="col-md-6" key={p.id}>
                    <div className="card p-3 h-100">
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <h6 className="fw-bold mb-1">{p.visitorName}</h6>
                          <small className="text-muted">{p.visitorPhone || 'No phone'}</small>
                        </div>
                        <span className={`badge ${badgeClass(p.status)}`}>{p.status}</span>
                      </div>
                      <hr className="my-2" />
                      <div className="small text-muted">
                        <div>Type: {p.passType}</div>
                        <div>
                          Valid: {new Date(p.validFrom).toLocaleString('en-IN')} →{' '}
                          {new Date(p.validUntil).toLocaleString('en-IN')}
                        </div>
                        <div className="text-break">UUID: {p.uuid}</div>
                      </div>
                      {p.status === 'ACTIVE' && (
                        <div className="d-flex gap-2 mt-2">
                          <button
                            className="btn btn-sm btn-outline-primary flex-fill"
                            onClick={() => handleViewQr(p)}
                          >
                            <i className="bi bi-qr-code me-1"></i>View / Download QR
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleRevoke(p.id)}
                            disabled={revokingId === p.id}
                          >
                            {revokingId === p.id
                              ? <span className="spinner-border spinner-border-sm"></span>
                              : <><i className="bi bi-x-circle me-1"></i>Revoke</>}
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
      </div>

      <QrViewModal
        show={qrModalOpen}
        onClose={closeQrModal}
        qrBase64={qrBase64}
        visitorName={qrVisitorName}
        loading={qrLoading}
        error={qrError}
      />
    </>
  )
}