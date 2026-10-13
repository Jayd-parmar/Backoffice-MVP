import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth } from './useAuth'
import { AppShell } from './components/AppShell'
import { LoadingState } from './components/PageState'
import { ApplyPage } from './pages/ApplyPage'
import { ApprovalDetailPage } from './pages/ApprovalDetailPage'
import { ApprovalsPage } from './pages/ApprovalsPage'
import { AuthPage } from './pages/AuthPage'
import { CustomerGate } from './pages/CustomerGate'
import { EditPage } from './pages/EditPage'
import { ReviewPage } from './pages/ReviewPage'
import { SubmittedPage } from './pages/SubmittedPage'

function RequireAuth() {
  const { session, ready } = useAuth()
  if (!ready) return <LoadingState label="Checking your session" />
  if (!session) return <Navigate to="/" replace />
  return <Outlet />
}

function RequireCustomer() {
  const { session } = useAuth()
  if (session?.role !== 'CUSTOMER') return <Navigate to="/approvals" replace />
  return <Outlet />
}

function RequireApprover() {
  const { session } = useAuth()
  if (session?.role !== 'APPROVER_1' && session?.role !== 'APPROVER_2') {
    return <Navigate to="/home" replace />
  }
  return <Outlet />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AuthPage />} />
        <Route element={<RequireAuth />}>
          <Route element={<AppShell />}>
            <Route path="/home" element={<CustomerGate />} />
            <Route element={<RequireCustomer />}>
              <Route path="/apply" element={<ApplyPage />} />
              <Route path="/applications/:id" element={<ReviewPage />} />
              <Route path="/applications/:id/edit" element={<EditPage />} />
              <Route path="/applications/:id/submitted" element={<SubmittedPage />} />
            </Route>
            <Route element={<RequireApprover />}>
              <Route path="/approvals" element={<ApprovalsPage />} />
              <Route path="/approvals/:id" element={<ApprovalDetailPage />} />
            </Route>
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
