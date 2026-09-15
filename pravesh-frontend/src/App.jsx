import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import ProtectedRoute from './components/common/ProtectedRoute'

import HomePage from './pages/public/HomePage'
import AboutPage from './pages/public/AboutPage'
import ContactPage from './pages/public/ContactPage'

import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import AccessPendingPage from './pages/auth/AccessPendingPage'
import ProfilePage from './pages/auth/ProfilePage'

import SubmitOnboardingPage from './pages/resident/SubmitOnboardingPage'
import SubmitSocietyRequestPage from './pages/admin/SubmitSocietyRequestPage'

import ResidentDashboard from './pages/resident/ResidentDashboard'
import CreatePassPage from './pages/resident/CreatePassPage'
import MyPassesPage from './pages/resident/MyPassesPage'
import ResidentEntriesPage from './pages/resident/ResidentEntriesPage'

import GuardDashboard from './pages/guard/GuardDashboard'

import SuperAdminDashboard from './pages/admin/SuperAdminDashboard'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminUsersPage from './pages/admin/AdminUsersPage'
import AdminFlatsPage from './pages/admin/AdminFlatsPage'
import AdminGatesPage from './pages/admin/AdminGatesPage'
import AdminGuardsPage from './pages/admin/AdminGuardsPage'
import AdminOnboardingPage from './pages/admin/AdminOnboardingPage'
import AdminEntriesPage from './pages/admin/AdminEntriesPage'
import AdminAnalyticsPage from './pages/admin/AdminAnalyticsPage'
import AssistantWidget from './components/common/AssistantWidget'
import BackgroundFX from './components/common/BackgroundFX'

import RequestRelocationPage from './pages/resident/RequestRelocationPage'
import AdminRelocationPage from './pages/admin/AdminRelocationPage'

import SosBanner from './components/common/SosBanner'
import SosButton from './components/common/SosButton'
import SosIncidentLogPage from './pages/admin/SosIncidentLogPage'

import MaintenancePayment from './pages/resident/MaintenancePayment'
import TransactionHistory from './pages/resident/TransactionHistory'
import AllPayments from './pages/admin/AllPayments'

import TripsPage from './pages/resident/TripsPage'
import TripDetailPage from './pages/resident/TripDetailPage'
import SocietyDashboard from './pages/resident/SocietyDashboard'
import ForumPage from './pages/forum/ForumPage'

const r = (roles, el) => <ProtectedRoute roles={roles}>{el}</ProtectedRoute>

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <BrowserRouter>
          <BackgroundFX />
          <Routes>
            {/* Public */}
            <Route path="/" element={<HomePage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />

            {/* Any authenticated */}
            <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
            <Route path="/access-pending" element={r(['RESIDENT', 'SOCIETY_ADMIN'], <AccessPendingPage />)} />
            <Route path="/dashboard" element={<ProtectedRoute><SocietyDashboard /></ProtectedRoute>} />
            <Route path="/forum" element={<ProtectedRoute><ForumPage /></ProtectedRoute>} />

            {/* Onboarding submissions */}
            <Route path="/onboarding/submit" element={r(['RESIDENT'], <SubmitOnboardingPage />)} />
            <Route path="/society-onboarding/submit" element={r(['SOCIETY_ADMIN'], <SubmitSocietyRequestPage />)} />

            {/* Super admin */}
            <Route path="/super-admin" element={r(['SUPER_ADMIN'], <SuperAdminDashboard />)} />

            {/* Society admin */}
            <Route path="/admin" element={r(['SOCIETY_ADMIN'], <AdminDashboard />)} />
            <Route path="/admin/users" element={r(['SOCIETY_ADMIN'], <AdminUsersPage />)} />
            <Route path="/admin/flats" element={r(['SOCIETY_ADMIN'], <AdminFlatsPage />)} />
            <Route path="/admin/gates" element={r(['SOCIETY_ADMIN'], <AdminGatesPage />)} />
            <Route path="/admin/guards" element={r(['SOCIETY_ADMIN'], <AdminGuardsPage />)} />
            <Route path="/admin/onboarding" element={r(['SOCIETY_ADMIN'], <AdminOnboardingPage />)} />
            <Route path="/admin/relocation" element={r(['SOCIETY_ADMIN'], <AdminRelocationPage />)} />
            <Route path="/admin/entries" element={r(['SOCIETY_ADMIN'], <AdminEntriesPage />)} />
            <Route path="/admin/analytics" element={r(['SOCIETY_ADMIN'], <AdminAnalyticsPage />)} />
            <Route path="/admin/payments" element={r(['SOCIETY_ADMIN'], <AllPayments />)} />
            <Route path="/admin/sos-log" element={r(['SOCIETY_ADMIN', 'GUARD'], <SosIncidentLogPage />)} />

            {/* Resident */}
            <Route path="/resident" element={r(['RESIDENT'], <ResidentDashboard />)} />
            <Route path="/resident/create-pass" element={r(['RESIDENT'], <CreatePassPage />)} />
            <Route path="/resident/passes" element={r(['RESIDENT'], <MyPassesPage />)} />
            <Route path="/resident/entries" element={r(['RESIDENT'], <ResidentEntriesPage />)} />
            <Route path="/resident/relocation" element={r(['RESIDENT'], <RequestRelocationPage />)} />
            <Route path="/resident/payments" element={r(['RESIDENT'], <MaintenancePayment />)} />
            <Route path="/resident/payments/history" element={r(['RESIDENT'], <TransactionHistory />)} />
            <Route path="/resident/trips" element={r(['RESIDENT'], <TripsPage />)} />
            <Route path="/resident/trips/:id" element={r(['RESIDENT'], <TripDetailPage />)} />

            {/* Guard */}
            <Route path="/guard" element={r(['GUARD'], <GuardDashboard />)} />

            {/* Fallbacks */}
            <Route path="/unauthorized" element={
              <div className="min-vh-100 d-flex align-items-center justify-content-center">
                <div className="text-center">
                  <h1 className="display-1 text-danger">403</h1>
                  <h4>Unauthorized</h4>
                  <a href="/login" className="btn btn-pravesh mt-3">Go to Login</a>
                </div>
              </div>
            } />
            <Route path="*" element={
              <div className="min-vh-100 d-flex align-items-center justify-content-center">
                <div className="text-center">
                  <h1 className="display-1 text-muted">404</h1>
                  <h4>Page Not Found</h4>
                  <a href="/" className="btn btn-pravesh mt-3">Go Home</a>
                </div>
              </div>
            } />
          </Routes>
          <AssistantWidget />
          <SosBanner />
        </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  )
}
