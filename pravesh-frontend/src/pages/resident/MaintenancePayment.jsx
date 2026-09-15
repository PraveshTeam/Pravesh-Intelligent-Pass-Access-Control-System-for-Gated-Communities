import { useState, useEffect } from 'react'
import { createPaymentOrder } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import { useAuth } from '../../context/AuthContext'
import Navbar from '../../components/common/Navbar'
import BackButton from '../../components/common/BackButton'

// Static demo due amount -- there's no maintenance-fee schedule in the backend
// yet (that would be its own admin-configurable feature), so this is a fixed
// placeholder amount to exercise the real payment flow end-to-end.
const MAINTENANCE_DUE = 500

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) return resolve(true)
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

export default function MaintenancePayment() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [paying, setPaying] = useState(false)
  const [status, setStatus] = useState(null) // null | 'success' | 'cancelled'

  useEffect(() => { loadRazorpayScript() }, [])

  const handlePayNow = async () => {
    setPaying(true)
    setStatus(null)
    try {
      const res = await createPaymentOrder('MAINTENANCE', MAINTENANCE_DUE)
      const { razorpayKeyId, razorpayOrderId, amount, currency } = res.data.data

      const ready = await loadRazorpayScript()
      if (!ready) {
        showToast('Could not load payment gateway. Check your connection.', 'error')
        setPaying(false)
        return
      }

      const rzp = new window.Razorpay({
        key: razorpayKeyId,
        amount: Math.round(amount * 100),
        currency,
        name: 'Pravesh Society',
        description: 'Maintenance Payment',
        order_id: razorpayOrderId,
        handler: function () {
          setStatus('success')
          showToast('Payment submitted. Confirming with the bank…', 'success')
        },
        modal: {
          ondismiss: function () {
            setStatus('cancelled')
            setPaying(false)
          },
        },
        theme: { color: '#f97316' },
      })

      rzp.on('payment.failed', function () {
        showToast('Payment failed. Please try again.', 'error')
        setPaying(false)
      })

      rzp.open()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not start payment.', 'error')
    } finally {
      setPaying(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/resident" label="Back to Dashboard" />
        <div className="page-header">
          <h4 className="mb-1">
            <i className="bi bi-cash-coin me-2"></i>Maintenance Payment
          </h4>
          <p className="mb-0 opacity-75">Welcome, {user?.name}</p>
        </div>

        <div className="row justify-content-center mt-4">
          <div className="col-md-5 col-10">
            <div className="card p-4 text-center">
              <i className="bi bi-cash-coin fs-1 text-warning mb-2"></i>
              <h5 className="fw-bold mb-1">Maintenance Due</h5>
              <div className="fs-2 fw-bold text-warning mb-1">₹{MAINTENANCE_DUE.toFixed(2)}</div>
              <p className="text-muted small mb-4">For {user?.flatNumber || 'your flat'} — this cycle</p>

              {status === 'success' ? (
                <div className="alert alert-success py-2 small mb-3">
                  <i className="bi bi-check-circle-fill me-2"></i>
                  Payment submitted — check Transaction History shortly for confirmation.
                </div>
              ) : status === 'cancelled' ? (
                <div className="alert alert-warning py-2 small mb-3">
                  Checkout closed before completing.
                </div>
              ) : null}

              <button
                className="btn btn-pravesh w-100 fw-bold"
                onClick={handlePayNow}
                disabled={paying}
              >
                {paying
                  ? <><span className="spinner-border spinner-border-sm me-2"></span>Opening checkout...</>
                  : <><i className="bi bi-credit-card-fill me-2"></i>Pay Now</>}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}