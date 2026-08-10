import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import { FileText, Link2, History, ArrowLeft, ShieldCheck, Clock, User, CheckCircle2 } from 'lucide-react';

const EvidenceDetails = () => {
  const { evidenceId } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDetail();
  }, [evidenceId]);

  const fetchDetail = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await evidenceAPI.getEvidenceDetail(evidenceId);
      setDetail(data);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to load evidence details');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return 'N/A';
    try {
      const d = new Date(isoStr);
      return d.toLocaleString();
    } catch {
      return isoStr;
    }
  };

  if (loading) {
    return <LoadingSpinner text={`Loading details for ${evidenceId}...`} />;
  }

  if (error) {
    return (
      <div>
        <Link to="/evidence" className="btn btn-outline" style={{ marginBottom: '20px' }}>
          <ArrowLeft size={16} /> Back to Repository
        </Link>
        <AlertMessage type="danger" message={error} />
      </div>
    );
  }

  if (!detail) return null;

  return (
    <div>
      <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Link to="/evidence" className="btn btn-outline">
          <ArrowLeft size={16} /> Back to Repository
        </Link>
        <Link to="/evidence/verify" className="btn btn-primary">
          <ShieldCheck size={16} /> Verify File Integrity
        </Link>
      </div>

      {/* Section 1: Evidence Metadata */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <FileText color="var(--primary)" size={20} />
            Section 1: Evidence Metadata
          </h3>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>ID: {detail.evidenceId}</span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '20px' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Evidence ID</div>
            <div style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary)' }}>{detail.evidenceId}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Name</div>
            <div style={{ fontWeight: '600' }}>{detail.fileName}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Type</div>
            <div>{detail.fileType || 'binary'}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Uploaded By</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <User size={14} color="var(--secondary)" />
              {detail.uploadedBy}
            </div>
          </div>
        </div>

        <div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Cryptographic SHA-256 Fingerprint</div>
          <div className="hash-code" style={{ padding: '10px 14px', fontSize: '0.88rem' }}>{detail.fileHash}</div>
        </div>
      </div>

      {/* Section 2: Blockchain Information */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Link2 color="var(--primary)" size={20} />
            Section 2: Blockchain Record
          </h3>
          <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>
            Polygon Amoy Testnet
          </span>
        </div>

        {detail.blockchainRecord ? (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '20px' }}>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Status</div>
                <StatusBadge status={detail.blockchainRecord.status} />
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Blockchain Timestamp</div>
                <div style={{ fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Clock size={14} color="var(--text-muted)" />
                  {formatDate(detail.blockchainRecord.blockchainTimestamp)}
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Transaction Hash</div>
              <div className="hash-code" style={{ color: 'var(--warning)', wordBreak: 'break-all' }}>{detail.blockchainRecord.transactionHash}</div>
              {detail.blockchainRecord.transactionHash && detail.blockchainRecord.transactionHash.startsWith('0x') && (
                <div style={{ marginTop: '6px' }}>
                  <a
                    href={`https://amoy.polygonscan.com/tx/${detail.blockchainRecord.transactionHash}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{ fontSize: '0.8rem', color: 'var(--primary)', textDecoration: 'none' }}
                  >
                    View on PolygonScan Amoy Explorer ↗
                  </a>
                </div>
              )}
            </div>

            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>On-Chain Anchored File Hash</div>
              <div className="hash-code">{detail.blockchainRecord.fileHash}</div>
            </div>
          </div>
        ) : (
          <div style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No blockchain anchoring record found.</div>
        )}
      </div>

      {/* Section 3: Audit Trail */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <History color="var(--secondary)" size={20} />
            Section 3: Chain of Custody Audit Log
          </h3>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {detail.auditLogs ? `${detail.auditLogs.length} events logged` : '0 events'}
          </span>
        </div>

        {!detail.auditLogs || detail.auditLogs.length === 0 ? (
          <div style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No audit history recorded yet.</div>
        ) : (
          <div className="timeline">
            {detail.auditLogs.map((log) => (
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
        )}
      </div>
    </div>
  );
};

export default EvidenceDetails;
