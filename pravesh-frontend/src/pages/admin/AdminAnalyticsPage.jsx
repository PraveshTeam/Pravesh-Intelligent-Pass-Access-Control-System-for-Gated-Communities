import { useState, useEffect } from 'react'
import {
  getAnalyticsSummary, getHourlyHeatmap, getDeniedBreakdown,
  getFrequentVisitors, getGateStats, getWeeklyTrend
} from '../../api/endpoints'
import { useToast } from '../../context/ToastContext'
import Navbar from '../../components/common/Navbar'
import LoadingSpinner from '../../components/common/LoadingSpinner'
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid
} from 'recharts'
import BackButton from '../../components/common/BackButton'

const COLORS = ['#1a3c5e', '#e8871a', '#2c5d8c', '#f5a83f', '#dc3545', '#198754']

export default function AdminAnalyticsPage() {
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [summary, setSummary] = useState(null)
  const [hourly, setHourly] = useState([])
  const [denied, setDenied] = useState([])
  const [visitors, setVisitors] = useState([])
  const [gates, setGates] = useState([])
  const [weekly, setWeekly] = useState([])

  useEffect(() => {
    Promise.all([
      getAnalyticsSummary(), getHourlyHeatmap(), getDeniedBreakdown(),
      getFrequentVisitors(), getGateStats(), getWeeklyTrend()
    ]).then(([s, h, d, v, g, w]) => {
      setSummary(s.data.data)
      setHourly(h.data.data)
      setDenied(d.data.data)
      setVisitors(v.data.data)
      setGates(g.data.data)
      setWeekly(w.data.data)
    })
    .catch(() => showToast('Failed to load analytics.', 'error'))
    .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <Navbar />
      <div className="container py-4">
        <BackButton to="/admin" label="Back to Admin Dashboard" />
        <div className="page-header"><h4 className="mb-0"><i className="bi bi-graph-up me-2"></i>Analytics Dashboard</h4></div>

        {loading && <LoadingSpinner text="Crunching the numbers..." />}

        {summary && (
          <div className="row g-3 mb-4 stagger-in">
            {[
              { label: 'Total Entries Today', value: summary.totalEntries },
              { label: 'Granted', value: summary.totalGranted },
              { label: 'Denied', value: summary.totalDenied },
              { label: 'Unique Visitors', value: summary.uniqueVisitors },
            ].map(c => (
              <div className="col-md-3 col-6" key={c.label}>
                <div className="card stat-card p-3 text-center">
                  <div className="text-muted small">{c.label}</div>
                  <div className="fs-3 fw-bold stat-value">{c.value}</div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="row g-3 stagger-in">
          <div className="col-md-6">
            <div className="card p-3">
              <h6 className="fw-bold mb-3">Hourly Entries (Last 7 Days)</h6>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={hourly}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="hour" tickFormatter={h => `${h}:00`} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#1a3c5e" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="col-md-6">
            <div className="card p-3">
              <h6 className="fw-bold mb-3">Denied Entries by Reason</h6>
              {denied.length === 0 ? <p className="text-muted text-center py-5">No denied entries.</p>
                : (
                  <ResponsiveContainer width="100%" height={260}>
                    <PieChart>
                      <Pie data={denied} dataKey="count" nameKey="reason" cx="50%" cy="50%" outerRadius={90} label>
                        {denied.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                )}
            </div>
          </div>

          <div className="col-md-6">
            <div className="card p-3">
              <h6 className="fw-bold mb-3">Daily Trend (Last 30 Days)</h6>
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={[...weekly].reverse()}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Line type="monotone" dataKey="count" stroke="#e8871a" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card p-3 h-100">
              <h6 className="fw-bold mb-3">Top Visitors</h6>
              <table className="table table-sm table-kv">
                <tbody>
                  {visitors.map(v => <tr key={v.visitorName}><td>{v.visitorName}</td><td className="kv-val">{v.count}</td></tr>)}
                  {visitors.length === 0 && <tr><td className="text-muted">No data.</td></tr>}
                </tbody>
              </table>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card p-3 h-100">
              <h6 className="fw-bold mb-3">Entries per Gate</h6>
              <table className="table table-sm table-kv">
                <tbody>
                  {gates.map(g => <tr key={g.gateId}><td>Gate {g.gateId}</td><td className="kv-val">{g.count}</td></tr>)}
                  {gates.length === 0 && <tr><td className="text-muted">No data.</td></tr>}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
