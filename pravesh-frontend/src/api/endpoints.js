import api from './axiosClient'

/*
 * Every backend response is wrapped in ApiResponse<T> = { success, message, data }.
 * Callers must therefore read res.data.data for the actual payload.
 * These helpers return the raw axios response; the .data.data unwrap happens in the pages.
 */

// ── AUTH ──────────────────────────────────────────────────────────────────
export const register = (data) => api.post('/api/auth/register', data)
export const login = (data) => api.post('/api/auth/login', data)
export const forgotPassword = (data) => api.post('/api/auth/forgot-password', data)
export const verifyOtp = (data) => api.post('/api/auth/verify-otp', data)
export const resetPassword = (data) => api.post('/api/auth/reset-password', data)

// ── AUTH — email/phone verification during registration ────────────────────
export const sendRegistrationOtp = (data) => api.post('/api/auth/register/send-otp', data)
export const verifyRegistrationOtp = (data) => api.post('/api/auth/register/verify-otp', data)

// ── PROFILE ───────────────────────────────────────────────────────────────
export const getMe = () => api.get('/api/users/me')
export const updateMe = (data) => api.put('/api/users/me', data)

// ── RESIDENT ONBOARDING ───────────────────────────────────────────────────
export const submitOnboardingRequest = (formData) =>
  api.post('/api/onboarding/request', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const getMyOnboardingRequest = () => api.get('/api/onboarding/my-request')

export const getSocieties = (search) =>
  api.get('/api/societies', { params: search ? { search } : {} })



// ── SOCIETY ADMIN ONBOARDING ──────────────────────────────────────────────
export const submitSocietyRequest = (formData) =>
  api.post('/api/society-onboarding/request', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const getMySocietyRequest = () => api.get('/api/society-onboarding/my-request')

// ── SUPER ADMIN ───────────────────────────────────────────────────────────
export const getSocietyRequests = (status = 'PENDING') =>
  api.get('/api/superadmin/society-requests', { params: { status } })
export const getSocietyRequestDocument = (id) =>
  api.get(`/api/superadmin/society-requests/${id}/document`, { responseType: 'blob' })
export const approveSocietyRequest = (id) =>
  api.put(`/api/superadmin/society-requests/${id}/approve`)
export const rejectSocietyRequest = (id, reason) =>
  api.put(`/api/superadmin/society-requests/${id}/reject`, { reason })

// ── ADMIN — ONBOARDING REVIEW ─────────────────────────────────────────────
export const getOnboardingRequests = (status = 'PENDING') =>
  api.get('/api/admin/onboarding/requests', { params: { status } })
export const getOnboardingDocument = (id) =>
  api.get(`/api/admin/onboarding/requests/${id}/document`, { responseType: 'blob' })
export const approveOnboardingRequest = (id, force = false) =>
  api.put(`/api/admin/onboarding/requests/${id}/approve`, null, { params: { force } })
export const rejectOnboardingRequest = (id, reason) =>
  api.put(`/api/admin/onboarding/requests/${id}/reject`, { reason })

// ── ADMIN — FLATS (read-only; created via onboarding approval) ─────────────
export const getFlats = () => api.get('/api/admin/flats')

// ── ADMIN — GATES ─────────────────────────────────────────────────────────
export const getGates = (unassignedOnly = false) =>
  api.get('/api/admin/gates', { params: { unassigned: unassignedOnly } })
export const addGate = (data) => api.post('/api/admin/gates', data)

// ── ADMIN — GUARDS ────────────────────────────────────────────────────────
export const getGuards = () => api.get('/api/admin/guards')
export const createGuard = (data) => api.post('/api/admin/guards', data)
export const reassignGuardGate = (id, newGateId) =>
  api.put(`/api/admin/guards/${id}/reassign-gate`, { newGateId })
export const getGuardShiftHistory = (id) => api.get(`/api/admin/guards/${id}/shifts`)

// ── ADMIN — USERS ─────────────────────────────────────────────────────────
export const getAllUsers = (params) => api.get('/api/admin/users', { params })
export const toggleUserStatus = (id, isActive) =>
  api.put(`/api/admin/users/${id}/status`, { isActive })

// ── ADMIN — PASSES / ENTRIES ──────────────────────────────────────────────
export const getAllPasses = () => api.get('/api/admin/passes')
export const getAllEntries = () => api.get('/api/admin/entries')

// ── ANALYTICS ─────────────────────────────────────────────────────────────
export const getAnalyticsSummary = () => api.get('/api/analytics/summary')
export const getHourlyHeatmap = () => api.get('/api/analytics/hourly')
export const getDeniedBreakdown = () => api.get('/api/analytics/denied-breakdown')
export const getFrequentVisitors = () => api.get('/api/analytics/frequent-visitors')
export const getGateStats = () => api.get('/api/analytics/gate-stats')
export const getWeeklyTrend = () => api.get('/api/analytics/weekly-trend')

// ── GUARD — SHIFT ─────────────────────────────────────────────────────────
export const shiftCheckin = (data) => api.post('/api/guard/shift-checkin', data)
export const shiftCheckout = () => api.post('/api/guard/shift-checkout')
export const getShiftStatus = () => api.get('/api/guard/shift-status')

// ── PASSES (resident) ─────────────────────────────────────────────────────
export const createPass = (data) => api.post('/api/passes', data)
export const getActivePasses = () => api.get('/api/passes')
export const getPassHistory = () => api.get('/api/passes/history')
export const getPassById = (id) => api.get(`/api/passes/${id}`)
export const regenerateQr = (id) => api.get(`/api/passes/${id}/qr`)
export const revokePass = (id) => api.delete(`/api/passes/${id}`)

// ── VALIDATION (guard scan) / ENTRIES ─────────────────────────────────────
export const scanPass = (uuid, gateId) =>
  api.post('/api/validate/scan', { uuid }, { params: { gateId } })
export const getGateEntries = (gateId, date) =>
  api.get('/api/entries', { params: { gateId, date } })
export const getFlatEntries = (flatId) => api.get(`/api/entries/flat/${flatId}`)


// ── GATE ENTRY REQUESTS (unannounced / walk-in visitors) ──────────────────
export const createGateEntryRequest = (data) => api.post('/api/gate-requests', data)
export const getGateEntryRequestStatus = (id) => api.get(`/api/gate-requests/${id}/status`)
export const getPendingGateEntryRequests = () => api.get('/api/gate-requests/pending')
export const approveGateEntryRequest = (id) => api.put(`/api/gate-requests/${id}/approve`)
export const denyGateEntryRequest = (id) => api.put(`/api/gate-requests/${id}/deny`)
export const getSocietyResidents = () => api.get('/api/gate-requests/residents')

// ── AI ASSISTANT ────────────────────────────────────────────────────────────
export const sendAssistantMessage = (message, history) =>
  api.post('/api/assistant/chat', { message, history })


// ── RESIDENT RELOCATION ──────────────────────────────────────────────────────
export const submitRelocationRequest = (formData) =>
  api.post('/api/relocation/request', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
export const getRelocationRequests = (status = 'PENDING') =>
  api.get('/api/admin/relocation-requests', { params: { status } })
export const approveRelocationRequest = (id, force = false) =>
  api.put(`/api/admin/relocation-requests/${id}/approve`, null, { params: { force } })
export const rejectRelocationRequest = (id, reason) =>
  api.put(`/api/admin/relocation-requests/${id}/reject`, { reason })
export const getMyRelocationRequest = () => api.get('/api/relocation/my-request')
export const revokeRelocationRequest = (id) => api.delete(`/api/relocation/${id}`)
export const getRelocationDocument = (id) =>
  api.get(`/api/admin/relocation-requests/${id}/document`, { responseType: 'blob' })


// ── SOS / EMERGENCY ALERTS ──────────────────────────────────────────────────
export const raiseSos = (category, description) =>
  api.post('/api/sos', { category, description })
export const getActiveSosAlerts = () => api.get('/api/sos')
export const updateSosStatus = (id, status) =>
  api.put(`/api/sos/${id}/status`, { status })
export const getMySosAlerts = () => api.get('/api/sos/mine')
export const getSosHistory = (id) => api.get(`/api/sos/${id}/history`)
export const getSosIncidentLog = () => api.get('/api/sos/log')

export const createPaymentOrder = (purpose, amount, referenceId = null) =>
  api.post('/api/payments/orders', { purpose, amount, referenceId })

export const getMyPaymentHistory = () =>
  api.get('/api/payments/history')

export const getAdminPayments = (purpose, status) =>
  api.get('/api/payments/admin/payments', { params: { purpose, status } })


export const listTrips = () =>
  api.get('/api/trips')

export const proposeTrip = (title, description, capacity) =>
  api.post('/api/trips', { title, description, capacity })

export const requestToJoinTrip = (tripId) =>
  api.post(`/api/trips/${tripId}/join`)

export const listTripRequests = (tripId) =>
  api.get(`/api/trips/${tripId}/requests`)

export const decideTripRequest = (tripId, requestId, status) =>
  api.put(`/api/trips/${tripId}/requests/${requestId}`, { status })

export const getTripDiscussion = (tripId) =>
  api.get(`/api/trips/${tripId}/discussion`)

export const getTripParticipants = (tripId) =>
  api.get(`/api/trips/${tripId}/participants`)

export const addTripComment = (tripId, body) =>
  api.post(`/api/trips/${tripId}/discussion`, { body })


export const listForumPosts = (category) =>
  api.get('/api/forum/posts', { params: category ? { category } : {} })
 
export const createForumPost = (category, title, body) =>
  api.post('/api/forum/posts', { category, title, body })
 
export const listForumComments = (postId) =>
  api.get(`/api/forum/posts/${postId}/comments`)
 
export const addForumComment = (postId, body) =>
  api.post(`/api/forum/posts/${postId}/comments`, { body })
 
export const toggleForumPin = (postId) =>
  api.put(`/api/forum/posts/${postId}/pin`)
 
export const deleteForumPost = (postId) =>
  api.delete(`/api/forum/posts/${postId}`)