import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import UploadEvidence from './pages/UploadEvidence';
import MyEvidence from './pages/MyEvidence';
import EvidenceDetails from './pages/EvidenceDetails';
import VerifyEvidence from './pages/VerifyEvidence';
import AuditTrail from './pages/AuditTrail';
import BlockchainStatus from './pages/BlockchainStatus';

function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

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
          <Route path="evidence" element={<MyEvidence />} />
          <Route path="evidence/upload" element={<UploadEvidence />} />
          <Route path="evidence/verify" element={<VerifyEvidence />} />
          <Route path="evidence/:evidenceId" element={<EvidenceDetails />} />
          <Route path="audit-trail" element={<AuditTrail />} />
          <Route path="audit-trail/:evidenceId" element={<AuditTrail />} />
          <Route path="blockchain" element={<BlockchainStatus />} />
          <Route path="blockchain/:evidenceId" element={<BlockchainStatus />} />
        </Route>

        {/* Fallback redirect */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;
