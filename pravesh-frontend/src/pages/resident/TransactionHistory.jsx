import { useState, useEffect, useCallback, useMemo } from 'react'
import { getMyPaymentHistory, getAdminPayments } from '../../api/endpoints'
import { useAuth } from '../../context/AuthContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

const STATUS_BADGE = {
  PAID: 'success',
  PENDING: 'secondary',
  FAILED: 'danger',
}

const PURPOSE_LABEL = {
  MAINTENANCE: 'Maintenance',
  EVENT: 'Event Fee',
  ACTIVITY: 'Activity Fee',
  TRIP: 'Trip Fee',
}

export default function TransactionHistory() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'SOCIETY_ADMIN'

  const [payments, setPayments] = useState([])
  const [loading, setLoading] = useState(true)
  const [purposeFilter, setPurposeFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [search, setSearch] = useState('')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = isAdmin
        ? await getAdminPayments(purposeFilter || undefined, statusFilter || undefined)
        : await getMyPaymentHistory()
      setPayments(res.data.data || [])
    } catch {
      setPayments([])
    } finally {
      setLoading(false)
    }
  }, [isAdmin, purposeFilter, statusFilter])

  useEffect(() => { load() }, [load])

  // Search + date range are applied client-side on the already-fetched list --
  // purpose/status stay server-side (admin only) since those go through the
  // backend filter params; search and dates don't need a round-trip for the
  // volumes this app deals with.
  const visiblePayments = useMemo(() => {
    let rows = payments

    if (search.trim()) {
      const q = search.trim().toLowerCase()
      rows = rows.filter(p =>
        String(p.id).includes(q) ||
        (p.residentName || '').toLowerCase().includes(q) ||
        (p.flatNumber || '').toLowerCase().includes(q) ||
        (PURPOSE_LABEL[p.purpose] || p.purpose || '').toLowerCase().includes(q)
      )
    }

    if (fromDate) {
      const from = new Date(fromDate + 'T00:00:00')
      rows = rows.filter(p => new Date(p.paidAt || p.createdAt) >= from)
    }
    if (toDate) {
      const to = new Date(toDate + 'T23:59:59')
      rows = rows.filter(p => new Date(p.paidAt || p.createdAt) <= to)
    }

    return rows
  }, [payments, search, fromDate, toDate])

  const clearFilters = () => {
    setSearch('')
    setFromDate('')
    setToDate('')
    if (isAdmin) {
      setPurposeFilter('')
      setStatusFilter('')
    }
  }

  const hasActiveFilters = search || fromDate || toDate || purposeFilter || statusFilter

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to={isAdmin ? '/admin' : '/resident'} label={isAdmin ? 'Back to Admin Dashboard' : 'Back to Dashboard'} />
        <div className="page-header d-flex justify-content-between align-items-center">
          <div>
            <h4 className="mb-1">
              <i className="bi bi-receipt me-2"></i>
              {isAdmin ? 'All Payments' : 'Transaction History'}
            </h4>
            <p className="mb-0 opacity-75">
              {isAdmin ? 'Society-wide payment records' : `Welcome, ${user?.name}`}
            </p>
          </div>
          <button className="btn btn-outline-light btn-sm" onClick={load}>
            <i className="bi bi-arrow-clockwise"></i>
          </button>
        </div>

        <div className="card p-3 mt-3 mb-3">
          <div className="d-flex gap-2">
            {isAdmin && (
              <select
                className="form-select form-select-sm flex-fill"
                style={{ minWidth: 160 }}
                value={purposeFilter}
                onChange={e => setPurposeFilter(e.target.value)}
              >
                <option value="">All Purposes</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="EVENT">Event Fee</option>
                <option value="ACTIVITY">Activity Fee</option>
                <option value="TRIP">Trip Fee</option>
              </select>
            )}
            {isAdmin && (
              <select
                className="form-select form-select-sm flex-fill"
                style={{ minWidth: 160 }}
                value={statusFilter}
                onChange={e => setStatusFilter(e.target.value)}
              >
                <option value="">All Statuses</option>
                <option value="PAID">Paid</option>
                <option value="PENDING">Pending</option>
                <option value="FAILED">Failed</option>
              </select>
            )}

            <div className="input-group input-group-sm flex-fill" style={{ minWidth: 160 }}>
              <span className="input-group-text bg-transparent">From</span>
              <input
                type="date"
                className="form-control"
                value={fromDate}
                onChange={e => setFromDate(e.target.value)}
              />
            </div>
            <div className="input-group input-group-sm flex-fill" style={{ minWidth: 160 }}>
              <span className="input-group-text bg-transparent">To</span>
              <input
                type="date"
                className="form-control"
                value={toDate}
                onChange={e => setToDate(e.target.value)}
              />
            </div>

            <div className="input-group input-group-sm flex-fill" style={{ minWidth: 160 }}>
              <span className="input-group-text bg-transparent">
                <i className="bi bi-search"></i>
              </span>
              <input
                type="text"
                className="form-control"
                placeholder={isAdmin ? 'Search resident, flat, receipt...' : 'Search receipt, purpose...'}
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
          </div>

          {hasActiveFilters && (
            <div className="mt-2">
              <button className="btn btn-sm btn-link text-muted p-0" onClick={clearFilters}>
                <i className="bi bi-x-circle me-1"></i>Clear filters
              </button>
            </div>
          )}
        </div>

        <div className="card p-3">
          {loading ? (
            <LoadingSpinner text="Loading payments..." />
          ) : visiblePayments.length === 0 ? (
            <p className="text-muted text-center py-3">
              {payments.length === 0 ? 'No transactions yet.' : 'No transactions match your filters.'}
            </p>
          ) : (
            <div className="table-responsive">
              <table className="table table-sm align-middle mb-0">
                <thead>
                  <tr>
                    <th>Receipt</th>
                    {isAdmin && <th>Resident</th>}
                    {isAdmin && <th>Flat</th>}
                    <th>Purpose</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {visiblePayments.map(p => (
                    <tr key={p.id}>
                      <td className="text-muted small">#PVS-{p.id}</td>
                      {isAdmin && (
                        <td className="fw-semibold">
                          {p.residentName || <span className="text-muted">Resident #{p.residentId}</span>}
                        </td>
                      )}
                      {isAdmin && (
                        <td className="text-muted">{p.flatNumber || '—'}</td>
                      )}
                      <td>{PURPOSE_LABEL[p.purpose] || p.purpose}</td>
                      <td className="fw-semibold">₹{Number(p.amount).toFixed(2)}</td>
                      <td>
                        <span className={`badge bg-${STATUS_BADGE[p.status] || 'secondary'}`}>
                          {p.status}
                        </span>
                      </td>
                      <td className="text-muted small">
                        {p.paidAt
                          ? new Date(p.paidAt).toLocaleString()
                          : new Date(p.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  )
}