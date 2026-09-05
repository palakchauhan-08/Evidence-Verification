import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { publicAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import {
  ShieldCheck,
  ShieldOff,
  AlertTriangle,
  ExternalLink,
  Search,
  CheckCircle2,
  FileText,
  Clock,
  Lock,
  ArrowRight
} from 'lucide-react';

const PublicEvidenceVerification = () => {
  const { evidenceId } = useParams();
  const navigate = useNavigate();

  const [searchInput, setSearchInput] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (evidenceId) {
      fetchVerification(evidenceId);
    }
  }, [evidenceId]);

  const fetchVerification = async (targetId) => {
    setLoading(true);
    setError('');
    setData(null);
    try {
      const res = await publicAPI.verifyEvidence(targetId);
      setData(res);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Evidence verification record not found.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (!searchInput.trim()) return;
    navigate(`/verify/evidence/${searchInput.trim()}`);
  };

  const formatFileSize = (bytes) => {
    if (bytes == null || bytes === 0) return 'N/A';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return 'N/A';
    try {
      return new Date(isoStr).toLocaleString();
    } catch {
      return isoStr;
    }
  };

  const isVerified = data?.verificationStatus === 'VERIFIED';
  const isTampered = data?.verificationStatus === 'TAMPERED' || data?.verificationStatus === 'COMPROMISED';

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--bg-main, #0f172a)', color: 'var(--text-main, #f8fafc)', padding: '30px 16px' }}>
      <div style={{ maxWidth: '850px', margin: '0 auto' }}>
        
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '10px', background: 'rgba(37, 99, 235, 0.12)', border: '1px solid rgba(37, 99, 235, 0.3)', padding: '8px 16px', borderRadius: '30px', color: 'var(--primary, #3b82f6)', fontSize: '0.88rem', fontWeight: '600', marginBottom: '16px' }}>
            <ShieldCheck size={18} /> Public Evidence Authenticity Portal
          </div>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', color: 'var(--text-main, #ffffff)', marginBottom: '8px' }}>
            Digital Evidence Verification
          </h1>
          <p style={{ color: 'var(--text-muted, #94a3b8)', fontSize: '0.95rem' }}>
            Public read-only verification powered by SHA-256 Cryptographic Hashing and Polygon Amoy Blockchain
          </p>
        </div>

        {/* Quick Lookup Bar */}
        <div className="card" style={{ padding: '16px', marginBottom: '24px', backgroundColor: 'var(--bg-card, #1e293b)' }}>
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: '10px' }}>
            <div style={{ position: 'relative', flex: 1 }}>
              <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                type="text"
                className="form-control"
                placeholder="Enter Evidence Reference ID (e.g. EVI-1001)..."
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                style={{ paddingLeft: '38px', width: '100%' }}
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              Verify ID <ArrowRight size={16} />
            </button>
          </form>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="card" style={{ padding: '50px 20px', textAlign: 'center', backgroundColor: 'var(--bg-card, #1e293b)' }}>
            <LoadingSpinner text="Retrieving Evidence Verification Record..." />
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <div className="card" style={{ padding: '30px', backgroundColor: 'var(--bg-card, #1e293b)', textAlign: 'center' }}>
            <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px auto' }}>
              <AlertTriangle size={28} />
            </div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '8px', color: '#ef4444' }}>Verification Record Not Found</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '20px' }}>{error}</p>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Please check the Evidence ID and ensure you have scanned a valid verification reference.
            </p>
          </div>
        )}

        {/* Main Verification Display */}
        {data && !loading && (
          <div>
            {/* Status Banner */}
            <div
              style={{
                borderRadius: '12px',
                padding: '24px',
                marginBottom: '24px',
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                backgroundColor: isVerified
                  ? 'rgba(16, 185, 129, 0.12)'
                  : isTampered
                  ? 'rgba(239, 68, 68, 0.12)'
                  : 'rgba(59, 130, 246, 0.12)',
                border: `1px solid ${
                  isVerified ? '#10b981' : isTampered ? '#ef4444' : '#3b82f6'
                }`,
              }}
            >
              <div
                style={{
                  padding: '12px',
                  borderRadius: '50%',
                  backgroundColor: isVerified ? '#10b981' : isTampered ? '#ef4444' : '#3b82f6',
                  color: '#ffffff',
                }}
              >
                {isVerified ? (
                  <CheckCircle2 size={32} />
                ) : isTampered ? (
                  <ShieldOff size={32} />
                ) : (
                  <FileText size={32} />
                )}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '0.8rem', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em', color: isVerified ? '#10b981' : isTampered ? '#ef4444' : '#3b82f6', marginBottom: '2px' }}>
                  Verification Status
                </div>
                <h2 style={{ fontSize: '1.4rem', fontWeight: '800', margin: 0 }}>
                  {isVerified ? 'VERIFIED — AUTHENTIC' : isTampered ? 'TAMPERED / COMPROMISED' : data.verificationStatus}
                </h2>
                <p style={{ fontSize: '0.88rem', margin: '4px 0 0 0', opacity: 0.9 }}>{data.message}</p>
              </div>
            </div>

            {/* Details Card */}
            <div className="card" style={{ padding: '24px', marginBottom: '24px', backgroundColor: 'var(--bg-card, #1e293b)' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
                <FileText size={18} color="var(--primary)" /> Evidence Specifications
              </h3>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '24px' }}>
                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Evidence ID</div>
                  <div style={{ fontWeight: '700', fontSize: '1rem', color: 'var(--primary)' }}>{data.evidenceId}</div>
                </div>

                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>File Name</div>
                  <div style={{ fontWeight: '600', fontSize: '0.95rem' }}>{data.fileName || 'N/A'}</div>
                </div>

                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>File Type / MIME</div>
                  <div style={{ fontSize: '0.9rem' }}>{data.fileType || 'N/A'}</div>
                </div>

                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>File Size</div>
                  <div style={{ fontSize: '0.9rem' }}>{formatFileSize(data.fileSize)}</div>
                </div>

                {data.caseId && (
                  <div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Associated Case ID</div>
                    <div style={{ fontWeight: '600', fontSize: '0.9rem', color: 'var(--secondary)' }}>{data.caseId}</div>
                  </div>
                )}

                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Verified At</div>
                  <div style={{ fontSize: '0.88rem' }}>{formatDate(data.verificationTimestamp)}</div>
                </div>
              </div>

              {/* SHA-256 Hash Box */}
              <div style={{ background: 'var(--bg-main, #0f172a)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)', marginBottom: '24px' }}>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <ShieldCheck size={14} color="#10b981" /> SHA-256 Cryptographic Hash Digest
                </div>
                <code style={{ fontSize: '0.85rem', wordBreak: 'break-all', color: '#38bdf8', fontWeight: '600', fontFamily: 'monospace' }}>
                  {data.fileHash}
                </code>
              </div>

              {/* Blockchain Proof Section */}
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
                <CheckCircle2 size={18} color="#10b981" /> Polygon Amoy Blockchain Proof
              </h3>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Blockchain Network</div>
                  <div style={{ fontWeight: '600', fontSize: '0.95rem' }}>{data.blockchainNetwork || 'Polygon Amoy (Testnet)'}</div>
                </div>

                <div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Transaction Hash</div>
                  {data.blockchainTransactionHash ? (
                    <code style={{ fontSize: '0.82rem', wordBreak: 'break-all', color: 'var(--primary)', fontFamily: 'monospace' }}>
                      {data.blockchainTransactionHash}
                    </code>
                  ) : (
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Transaction hash pending</span>
                  )}
                </div>
              </div>

              {data.polygonExplorerUrl && (
                <a
                  href={data.polygonExplorerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-outline"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', marginTop: '8px', backgroundColor: 'rgba(59, 130, 246, 0.08)', borderColor: 'var(--primary)', color: 'var(--primary)' }}
                >
                  <ExternalLink size={16} /> View Transaction on Polygon Explorer
                </a>
              )}
            </div>

            {/* Privacy & Security Notice */}
            <div className="card" style={{ padding: '16px 20px', backgroundColor: 'var(--bg-card, #1e293b)', borderLeft: '4px solid var(--primary)', display: 'flex', gap: '14px', alignItems: 'flex-start' }}>
              <Lock size={20} color="var(--primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
              <div style={{ fontSize: '0.83rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
                <strong style={{ color: 'var(--text-main)' }}>Privacy & Confidentiality Notice:</strong> This public verification portal provides read-only validation of cryptographic SHA-256 evidence signatures stored on the Polygon Amoy blockchain. Original digital evidence file bytes, personal user identities, investigator details, and internal audit logs remain strictly confidential and protected under system RBAC controls.
              </div>
            </div>

            {/* Login Link for Authorized Users */}
            <div style={{ textAlign: 'center', marginTop: '24px' }}>
              <Link to="/login" style={{ fontSize: '0.85rem', color: 'var(--text-muted)', textDecoration: 'none' }}>
                Are you an authorized investigator? <span style={{ color: 'var(--primary)', fontWeight: '600' }}>Log in to view full evidence details →</span>
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PublicEvidenceVerification;
