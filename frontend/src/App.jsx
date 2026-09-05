import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import VerifyEmail from './pages/VerifyEmail';
import Dashboard from './pages/Dashboard';
import CasesPage from './pages/CasesPage';
import CaseDetails from './pages/CaseDetails';
import UploadEvidence from './pages/UploadEvidence';
import MyEvidence from './pages/MyEvidence';
import EvidenceDetails from './pages/EvidenceDetails';
import VerifyEvidence from './pages/VerifyEvidence';
import AuditTrail from './pages/AuditTrail';
import BlockchainStatus from './pages/BlockchainStatus';
import UserManagement from './pages/UserManagement';
import PublicEvidenceVerification from './pages/PublicEvidenceVerification';

function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/verify/evidence/:evidenceId" element={<PublicEvidenceVerification />} />

        {/* Protected Application Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="cases" element={<CasesPage />} />
          <Route path="cases/:caseId" element={<CaseDetails />} />
          <Route path="evidence" element={<MyEvidence />} />
          <Route
            path="evidence/upload"
            element={
              <ProtectedRoute allowedRoles={['ADMIN', 'INVESTIGATOR']}>
                <UploadEvidence />
              </ProtectedRoute>
            }
          />
          <Route
            path="evidence/verify"
            element={
              <ProtectedRoute allowedRoles={['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST']}>
                <VerifyEvidence />
              </ProtectedRoute>
            }
          />
          <Route path="evidence/:evidenceId" element={<EvidenceDetails />} />
          <Route path="audit-trail" element={<AuditTrail />} />
          <Route path="audit-trail/:evidenceId" element={<AuditTrail />} />
          <Route path="blockchain" element={<BlockchainStatus />} />
          <Route path="blockchain/:evidenceId" element={<BlockchainStatus />} />
          <Route
            path="admin/users"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <UserManagement />
              </ProtectedRoute>
            }
          />
        </Route>

        {/* Fallback redirect */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;
