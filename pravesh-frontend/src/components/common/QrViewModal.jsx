export default function QrViewModal({ show, onClose, qrBase64, visitorName, loading, error }) {
  if (!show) return null

  return (
    <div className="modal d-block" style={{ background: 'rgba(16,40,65,0.55)' }} tabIndex="-1" onClick={onClose}>
      <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
        <div className="modal-content">

          <div className="modal-header">
            <h6 className="modal-title fw-bold">
              <i className="bi bi-qr-code me-2"></i>Visitor Pass QR
            </h6>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>

          <div className="modal-body text-center py-4">

            {loading && (
              <div className="py-4">
                <div className="pravesh-spinner mx-auto mb-3"></div>
                <p className="text-muted small mb-0">Fetching QR code...</p>
              </div>
            )}

            {!loading && error && (
              <div className="alert alert-danger mb-0">{error}</div>
            )}

            {!loading && !error && qrBase64 && (
              <div>
                {visitorName && (
                  <p className="text-muted small mb-3">For: <strong>{visitorName}</strong></p>
                )}

                <img src={`data:image/png;base64,${qrBase64}`} alt="Visitor pass QR code" className="mb-3 border rounded p-2" style={{ width: 240, height: 240 }} />

                <div className="d-flex gap-2">
                  <a href={`data:image/png;base64,${qrBase64}`} download={`pravesh-pass-qr.png`} className="btn btn-pravesh w-100">
                    <i className="bi bi-download me-1"></i>Download QR
                  </a>
                  <button type="button" className="btn btn-outline-secondary w-100" onClick={onClose}>
                    Close
                  </button>
                </div>
              </div>
            )}

          </div>

        </div>
      </div>
    </div>
  )
}