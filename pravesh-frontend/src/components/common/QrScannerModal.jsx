import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'

export default function QrScannerModal({ show, onClose, onScan }) {
  const scannerRef = useRef(null)
  const containerId = 'qr-scanner-region'
  const [error, setError] = useState('')

  useEffect(() => {
    if (!show) return

    const scanner = new Html5Qrcode(containerId)
    scannerRef.current = scanner

    scanner.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 250, height: 250 } },
      (decodedText) => {
        onScan(decodedText)
        stopScanner()
      },
      () => { /* ignore per-frame decode failures */ }
    ).catch((err) => {
      setError('Could not access camera: ' + err)
    })

    return () => { stopScanner() }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [show])

  const stopScanner = () => {
    if (scannerRef.current) {
      scannerRef.current.stop().then(() => scannerRef.current.clear()).catch(() => {})
      scannerRef.current = null
    }
  }

  const handleClose = () => {
    stopScanner()
    onClose()
  }

  if (!show) return null

  return (
    <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.6)' }} tabIndex="-1">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">
          <div className="modal-header">
            <h6 className="modal-title fw-bold">Scan Visitor QR Code</h6>
            <button type="button" className="btn-close" onClick={handleClose}></button>
          </div>
          <div className="modal-body text-center">
            {error && <div className="alert alert-danger small">{error}</div>}
            <div id={containerId} style={{ width: '100%' }}></div>
            <p className="text-muted small mt-2">Point the camera at the visitor's QR code</p>
          </div>
        </div>
      </div>
    </div>
  )
}