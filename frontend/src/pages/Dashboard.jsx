import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import {
  FileText,
  Briefcase,
  Upload,
  FileCheck2,
  FolderLock,
  ArrowRight,
  UserCog,
  Search,
  CheckCircle2,
  ShieldOff,
  AlertTriangle
} from 'lucide-react';

const Dashboard = () => {
  const { userRole, hasAnyRole } = useAuth();
  const [evidenceList, setEvidenceList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchEvidence();
  }, []);

  const fetchEvidence = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await evidenceAPI.getUserEvidence();
      setEvidenceList(data || []);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to load evidence metrics');
    } finally {
      setLoading(false);
    }
  };

  const totalCount = evidenceList.length;
  const uploadedCount = evidenceList.filter((e) => (e.status || 'UPLOADED').toUpperCase() === 'UPLOADED').length;
  const underReviewCount = evidenceList.filter((e) => (e.status || '').toUpperCase() === 'UNDER_REVIEW').length;
  const verifiedCount = evidenceList.filter((e) => (e.status || '').toUpperCase() === 'VERIFIED').length;
  const rejectedCount = evidenceList.filter((e) => (e.status || '').toUpperCase() === 'REJECTED').length;
  const tamperedCount = evidenceList.filter((e) => (e.status || '').toUpperCase() === 'TAMPERED').length;

  return (
    <div>
      {/* Workflow Stat Metric Cards Grid */}
      <div className="grid-stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))' }}>
        <StatCard
          icon={FileText}
          value={loading ? '-' : totalCount}
          label="Total Submissions"
          color="var(--primary)"
          bg="var(--primary-light)"
        />
        <StatCard
          icon={Upload}
          value={loading ? '-' : uploadedCount}
          label="Uploaded"
          color="#3b82f6"
          bg="rgba(59, 130, 246, 0.1)"
        />
        <StatCard
          icon={Search}
          value={loading ? '-' : underReviewCount}
          label="Under Review"
          color="#f59e0b"
          bg="rgba(245, 158, 11, 0.1)"
        />
        <StatCard
          icon={CheckCircle2}
          value={loading ? '-' : verifiedCount}
          label="Verified"
          color="#10b981"
          bg="rgba(16, 185, 129, 0.1)"
        />
        <StatCard
          icon={ShieldOff}
          value={loading ? '-' : rejectedCount}
          label="Rejected"
          color="#6b7280"
          bg="rgba(107, 114, 128, 0.1)"
        />
        <StatCard
          icon={AlertTriangle}
          value={loading ? '-' : tamperedCount}
          label="Tampered Alert"
          color="#ef4444"
          bg="rgba(239, 68, 68, 0.1)"
        />
      </div>

      <AlertMessage type="danger" message={error} onClose={() => setError('')} />

      {/* Quick Action Navigation Grid */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Quick Actions</h3>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
          <Link to="/cases" className="btn btn-primary" style={{ padding: '16px', justifyContent: 'center' }}>
            <Briefcase size={20} />
            <span>View Cases Repository</span>
          </Link>

          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
            <Link to="/evidence/upload" className="btn btn-secondary" style={{ padding: '16px', justifyContent: 'center' }}>
              <Upload size={20} />
              <span>Upload New Evidence</span>
            </Link>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST']) && (
            <Link to="/evidence/verify" className="btn btn-outline" style={{ padding: '16px', justifyContent: 'center' }}>
              <FileCheck2 size={20} />
              <span>Verify Evidence File</span>
            </Link>
          )}

          <Link to="/evidence" className="btn btn-outline" style={{ padding: '16px', justifyContent: 'center' }}>
            <FolderLock size={20} />
            <span>{userRole === 'INVESTIGATOR' ? 'View My Evidence' : 'View All Evidence'}</span>
          </Link>

          {hasAnyRole(['ADMIN']) && (
            <Link to="/admin/users" className="btn btn-outline" style={{ padding: '16px', justifyContent: 'center' }}>
              <UserCog size={20} />
              <span>Manage User Roles</span>
            </Link>
          )}
        </div>
      </div>

      {/* Recent Evidence Table */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Recent Evidence Submissions</h3>
          <Link to="/evidence" style={{ fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            View All <ArrowRight size={14} />
          </Link>
        </div>

        {loading ? (
          <LoadingSpinner text="Fetching evidence repository..." />
        ) : evidenceList.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-muted)' }}>
            <FileText size={40} style={{ opacity: 0.4, marginBottom: '12px' }} />
            <p>No evidence records available.</p>
            {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
              <Link to="/evidence/upload" className="btn btn-primary" style={{ marginTop: '12px' }}>
                Upload First Evidence
              </Link>
            )}
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Evidence ID</th>
                  <th>File Name</th>
                  <th>Workflow Status</th>
                  <th>SHA-256 Hash</th>
                  <th>Uploaded By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {evidenceList.slice(0, 5).map((item) => (
                  <tr key={item.id}>
                    <td>
                      <span style={{ fontWeight: '600', color: 'var(--primary)' }}>{item.evidenceId}</span>
                    </td>
                    <td>{item.fileName}</td>
                    <td>
                      <StatusBadge status={item.status || 'UPLOADED'} />
                    </td>
                    <td>
                      <span className="hash-code">
                        {item.fileHash ? `${item.fileHash.substring(0, 12)}...${item.fileHash.substring(item.fileHash.length - 8)}` : 'N/A'}
                      </span>
                    </td>
                    <td>{item.uploadedBy}</td>
                    <td>
                      <Link to={`/evidence/${item.evidenceId}`} className="btn btn-outline" style={{ padding: '4px 10px', fontSize: '0.78rem' }}>
                        Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
