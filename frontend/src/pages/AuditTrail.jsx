import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import { History, User, Clock, Search, ArrowLeft, ArrowRight } from 'lucide-react';

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
      const data = await evidenceAPI.getChainOfCustody(id.trim());
      setAuditLogs(data || []);
      if (!data || data.length === 0) {
        setError(`No chain of custody records found for Evidence ID: ${id}`);
      }
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to fetch chain of custody history');
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

  const renderRoleBadge = (role) => {
    if (!role) return null;
    const r = role.toUpperCase().replace('ROLE_', '');
    let bg = 'rgba(107, 114, 128, 0.15)';
    let color = '#9ca3af';
    let border = '1px solid rgba(107, 114, 128, 0.3)';

    if (r === 'ADMIN') {
      bg = 'rgba(239, 68, 68, 0.15)';
      color = '#ef4444';
      border = '1px solid rgba(239, 68, 68, 0.3)';
    } else if (r === 'INVESTIGATOR') {
      bg = 'rgba(59, 130, 246, 0.15)';
      color = '#3b82f6';
      border = '1px solid rgba(59, 130, 246, 0.3)';
    } else if (r === 'FORENSIC_ANALYST') {
      bg = 'rgba(168, 85, 247, 0.15)';
      color = '#a855f7';
      border = '1px solid rgba(168, 85, 247, 0.3)';
    } else if (r === 'VIEWER') {
      bg = 'rgba(16, 185, 129, 0.15)';
      color = '#10b981';
      border = '1px solid rgba(16, 185, 129, 0.3)';
    }

    return (
      <span
        style={{
          fontSize: '0.7rem',
          fontWeight: '700',
          padding: '2px 8px',
          borderRadius: '10px',
          backgroundColor: bg,
          color: color,
          border: border,
          marginLeft: '6px',
        }}
      >
        {r}
      </span>
    );
  };

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <History color="var(--secondary)" size={20} />
            Digital Evidence Chain of Custody Audit Trail
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
            Query Chain of Custody
          </button>
        </div>

        <AlertMessage type="danger" message={error} onClose={() => setError('')} />

        {loading ? (
          <LoadingSpinner text="Fetching chain of custody timeline..." />
        ) : auditLogs.length > 0 ? (
          <div className="timeline" style={{ marginTop: '20px' }}>
            {auditLogs.map((log) => (
              <div key={log.id} className="timeline-item">
                <div className="timeline-dot"></div>
                <div className="timeline-content">
                  <div className="timeline-header" style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }}>
                    <span className="timeline-title">
                      <StatusBadge status={log.action} />
                    </span>
                    <span className="timeline-time" style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                      <Clock size={12} style={{ display: 'inline', marginRight: '4px' }} />
                      {formatDate(log.timestamp)}
                    </span>
                  </div>

                  <div className="timeline-user" style={{ marginTop: '6px', fontSize: '0.82rem', display: 'flex', alignItems: 'center' }}>
                    <User size={13} style={{ display: 'inline', marginRight: '4px', color: 'var(--text-muted)' }} />
                    <span style={{ fontWeight: '600' }}>{log.performedBy}</span>
                    {renderRoleBadge(log.actorRole)}
                  </div>

                  {/* Status Transition Pill if present */}
                  {(log.previousStatus || log.newStatus) && (
                    <div style={{ marginTop: '8px', fontSize: '0.78rem', display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)' }}>
                      <span>Status Transition:</span>
                      <span className="badge badge-info" style={{ fontSize: '0.7rem', padding: '2px 6px' }}>
                        {log.previousStatus || '—'}
                      </span>
                      <ArrowRight size={12} />
                      <span className="badge badge-primary" style={{ fontSize: '0.7rem', padding: '2px 6px' }}>
                        {log.newStatus || '—'}
                      </span>
                    </div>
                  )}

                  {/* Reason rationale if present */}
                  {log.reason && (
                    <div
                      style={{
                        marginTop: '8px',
                        padding: '8px 12px',
                        borderRadius: '6px',
                        backgroundColor: 'rgba(245, 158, 11, 0.1)',
                        borderLeft: '3px solid #f59e0b',
                        fontSize: '0.82rem',
                        fontStyle: 'italic',
                        color: 'var(--text-main)',
                      }}
                    >
                      <strong>Reason / Rationale:</strong> {log.reason}
                    </div>
                  )}

                  <div className="timeline-detail" style={{ marginTop: '6px', fontSize: '0.85rem' }}>
                    {log.details}
                  </div>
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
