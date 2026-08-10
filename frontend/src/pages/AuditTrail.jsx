import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import { History, User, Clock, Search, ArrowLeft } from 'lucide-react';

const AuditTrail = () => {
  const { evidenceId: paramEvidenceId } = useParams();
  const [evidenceIdInput, setEvidenceIdInput] = useState(paramEvidenceId || '');
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (paramEvidenceId) {
      fetchAuditLogs(paramEvidenceId);
    }
  }, [paramEvidenceId]);

  const fetchAuditLogs = async (idToFetch) => {
    const id = idToFetch || evidenceIdInput;
    if (!id.trim()) {
      setError('Please enter an Evidence ID to query audit logs');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await evidenceAPI.getAuditLogs(id.trim());
      setAuditLogs(data || []);
      if (!data || data.length === 0) {
        setError(`No audit records found for Evidence ID: ${id}`);
      }
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to fetch audit logs');
      setAuditLogs([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return 'N/A';
    try {
      return new Date(isoStr).toLocaleString();
    } catch {
      return isoStr;
    }
  };

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <History color="var(--secondary)" size={20} />
            Chain of Custody Audit Trail Log
          </h3>
          <Link to="/evidence" className="btn btn-outline" style={{ fontSize: '0.82rem' }}>
            <ArrowLeft size={14} /> Back to Repository
          </Link>
        </div>

        <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Search size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-control"
              placeholder="Enter Evidence ID (e.g. EVI-8A2F9B1C)..."
              style={{ paddingLeft: '40px' }}
              value={evidenceIdInput}
              onChange={(e) => setEvidenceIdInput(e.target.value)}
            />
          </div>
          <button onClick={() => fetchAuditLogs()} className="btn btn-primary">
            Query Audit Logs
          </button>
        </div>

        <AlertMessage type="danger" message={error} onClose={() => setError('')} />

        {loading ? (
          <LoadingSpinner text="Fetching chain of custody history..." />
        ) : auditLogs.length > 0 ? (
          <div className="timeline" style={{ marginTop: '20px' }}>
            {auditLogs.map((log) => (
              <div key={log.id} className="timeline-item">
                <div className="timeline-dot"></div>
                <div className="timeline-content">
                  <div className="timeline-header">
                    <span className="timeline-title">
                      <StatusBadge status={log.action} />
                    </span>
                    <span className="timeline-time">{formatDate(log.timestamp)}</span>
                  </div>
                  <div className="timeline-user">
                    <User size={13} style={{ display: 'inline', marginRight: '4px' }} />
                    {log.performedBy}
                  </div>
                  <div className="timeline-detail">{log.details}</div>
                </div>
              </div>
            ))}
          </div>
        ) : !error ? (
          <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-muted)' }}>
            <History size={40} style={{ opacity: 0.4, marginBottom: '12px' }} />
            <p>Enter an Evidence ID above to inspect its complete chain-of-custody timeline.</p>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default AuditTrail;
