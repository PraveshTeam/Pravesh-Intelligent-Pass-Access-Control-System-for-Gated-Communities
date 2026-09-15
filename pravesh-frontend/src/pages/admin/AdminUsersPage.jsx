import { useState, useEffect } from 'react'
import { getAllUsers, toggleUserStatus } from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import BackButton from '../../components/common/BackButton'

const roleBadge = (role) => {
  const map = { RESIDENT: 'primary', GUARD: 'warning', SOCIETY_ADMIN: 'success', SUPER_ADMIN: 'danger' }
  return <span className={`badge bg-${map[role] || 'secondary'}`}>{role}</span>
}

export default function AdminUsersPage() {
  const { showToast } = useToast()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [roleFilter, setRoleFilter] = useState('')
  const [togglingId, setTogglingId] = useState(null)

  const load = () => {
    setLoading(true)
    getAllUsers(roleFilter ? { role: roleFilter } : {})
      .then(res => setUsers(res.data.data))
      .catch(() => showToast('Failed to load users.', 'error'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [roleFilter])

  const handleToggle = async (u) => {
    setTogglingId(u.id)
    try {
      await toggleUserStatus(u.id, !u.active)
      showToast(`${u.name} ${!u.active ? 'activated' : 'deactivated'}.`, !u.active ? 'success' : 'warning')
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed.', 'error')
    } finally {
      setTogglingId(null)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header d-flex justify-content-between align-items-center">
          <h4 className="mb-0"><i className="bi bi-people me-2"></i>Manage Users</h4>
          <select className="form-select w-auto" value={roleFilter} onChange={e => setRoleFilter(e.target.value)}>
            <option value="">All Roles</option>
            <option value="RESIDENT">Resident</option>
            <option value="GUARD">Guard</option>
            <option value="SOCIETY_ADMIN">Society Admin</option>
          </select>
        </div>

        {loading ? <LoadingSpinner text="Loading users..." />
          : (
            <div className="card p-0 overflow-hidden">
              <table className="table table-hover mb-0">
                <thead className="table-dark">
                  <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Action</th></tr>
                </thead>
                <tbody>
                  {users.map(u => (
                    <tr key={u.id}>
                      <td data-label="ID" className="text-muted">{u.id}</td>
                      <td data-label="Name" className="fw-semibold">{u.name}</td>
                      <td data-label="Email" className="small">{u.email}</td>
                      <td data-label="Role">{roleBadge(u.role)}</td>
                      <td data-label="Status">
                        <span className={`badge ${u.active ? 'bg-success' : 'bg-secondary'}`}>
                          {u.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td data-label="Action">
                        <button
                          className={`btn btn-sm ${u.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                          onClick={() => handleToggle(u)}
                          disabled={togglingId === u.id}
                        >
                          {togglingId === u.id
                            ? <><span className="spinner-border spinner-border-sm me-1"></span>Saving...</>
                            : (u.active ? 'Deactivate' : 'Activate')}
                        </button>
                      </td>
                    </tr>
                  ))}
                  {users.length === 0 && (
                    <tr><td colSpan={6} className="text-center text-muted py-3">No users found.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
      </div>
    </>
  )
}