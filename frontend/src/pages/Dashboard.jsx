import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import {
  FileText,
  ShieldCheck,
  Link2,
  Upload,
  FileCheck2,
  FolderLock,
  ArrowRight,
  Database
} from 'lucide-react';

const Dashboard = () => {
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

  return (
    <div>
      {/* Stat Metric Cards Grid */}
      <div className="grid-stats">
        <StatCard
          icon={FileText}
          value={loading ? '-' : totalCount}
          label="Total Evidence Uploaded"
          color="var(--primary)"
          bg="var(--primary-light)"
        />
        <StatCard
          icon={ShieldCheck}
          value={loading ? '-' : totalCount}
          label="SHA-256 Hashed Records"
          color="var(--success)"
          bg="var(--success-light)"
        />
        <StatCard
          icon={Link2}
          value={loading ? '-' : totalCount}
          label="Mock Blockchain Anchored"
          color="var(--warning)"
          bg="var(--warning-light)"
        />
        <StatCard
          icon={Database}
          value="PostgreSQL"
          label="Database Status"
          color="var(--secondary)"
          bg="var(--secondary-light)"
        />
      </div>

      <AlertMessage type="danger" message={error} onClose={() => setError('')} />

      {/* Quick Action Navigation Grid */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Quick Actions</h3>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
          <Link to="/evidence/upload" className="btn btn-primary" style={{ padding: '16px', justifyContent: 'center' }}>
            <Upload size={20} />
            <span>Upload New Evidence</span>
          </Link>
          <Link to="/evidence/verify" className="btn btn-secondary" style={{ padding: '16px', justifyContent: 'center' }}>
            <FileCheck2 size={20} />
            <span>Verify Evidence File</span>
          </Link>
          <Link to="/evidence" className="btn btn-outline" style={{ padding: '16px', justifyContent: 'center' }}>
            <FolderLock size={20} />
            <span>View All My Evidence</span>
          </Link>
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
            <p>No evidence records uploaded yet.</p>
            <Link to="/evidence/upload" className="btn btn-primary" style={{ marginTop: '12px' }}>
              Upload First Evidence
            </Link>
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Evidence ID</th>
                  <th>File Name</th>
                  <th>Type</th>
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
                    <td><span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{item.fileType || 'binary'}</span></td>
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
