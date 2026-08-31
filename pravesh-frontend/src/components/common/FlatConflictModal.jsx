export default function FlatConflictModal({ show, conflict, onCancel, onConfirm, confirming }) {
  if (!show || !conflict) return null

  return (
    <div className="modal d-block" style={{ background: 'rgba(16,40,65,0.55)' }} tabIndex="-1" onClick={onCancel}>
      <div className="modal-dialog modal-dialog-centered" onClick={e => e.stopPropagation()}>
        <div className="modal-content">
          <div className="modal-header">
            <h6 className="modal-title fw-bold">
              <i className="bi bi-exclamation-triangle-fill text-warning me-2"></i>Flat Already Occupied
            </h6>
            <button type="button" className="btn-close" onClick={onCancel}></button>
          </div>
          <div className="modal-body">
            <p className="mb-3">
              Flat <strong>{conflict.flatNumber}</strong> is currently assigned to:
            </p>
            <div className="d-flex align-items-center gap-3 p-3 rounded mb-3" style={{ background: 'var(--p-bg)' }}>
              <div className="rounded-circle d-flex align-items-center justify-content-center"
                style={{ width: 42, height: 42, background: 'rgba(220,38,38,0.1)', color: 'var(--p-danger)' }}>
                <i className="bi bi-person-fill"></i>
              </div>
              <div>
                <div className="fw-semibold">{conflict.occupantName}</div>
                <small className="text-muted">Current occupant · Flat {conflict.flatNumber}</small>
              </div>
            </div>
            <p className="text-muted small mb-0">
              Approving this relocation will <strong>remove {conflict.occupantName} from this flat</strong> and
              assign it to the new resident instead. {conflict.occupantName} will need to be reassigned to a
              flat separately. This action will be recorded in the admin notes.
            </p>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-outline-secondary" onClick={onCancel} disabled={confirming}>
              Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={confirming}>
              {confirming
                ? <><span className="spinner-border spinner-border-sm me-2"></span>Reassigning...</>
                : <><i className="bi bi-arrow-left-right me-1"></i>Reassign Anyway</>}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}