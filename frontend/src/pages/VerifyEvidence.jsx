import React, { useState } from 'react';
import { evidenceAPI } from '../services/api';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import { FileCheck2, File, CheckCircle2, XCircle, AlertTriangle, ShieldCheck, Database, Link2 } from 'lucide-react';

const VerifyEvidence = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [verificationResult, setVerificationResult] = useState(null);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setError('');
      setVerificationResult(null);
    }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      setError('Please select a file to verify');
      return;
    }

    setLoading(true);
    setError('');
    setVerificationResult(null);

    try {
      const data = await evidenceAPI.verify(selectedFile);
      setVerificationResult(data);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to verify evidence file');
    } finally {
      setLoading(false);
    }
  };

  const isVerified = verificationResult?.verificationStatus === 'VERIFIED';

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Dual-Layer Evidence Integrity Verification</h3>
        </div>

        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '20px' }}>
          Upload any evidence file to verify its cryptographic SHA-256 fingerprint against both the PostgreSQL database and the Blockchain record.
        </p>

        <AlertMessage type="danger" message={error} onClose={() => setError('')} />

        <form onSubmit={handleVerify} style={{ marginBottom: '24px' }}>
          <div className="dropzone" onClick={() => document.getElementById('verifyFileInput').click()}>
            <input
              type="file"
              id="verifyFileInput"
              style={{ display: 'none' }}
              onChange={handleFileChange}
            />
            <FileCheck2 size={48} style={{ color: 'var(--secondary)', marginBottom: '12px' }} />
            <h4 style={{ marginBottom: '6px' }}>Select File to Verify Integrity</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              Computes SHA-256 locally and verifies against dual ledger sources
            </p>
          </div>

          {selectedFile && (
            <div style={{ marginTop: '16px', padding: '12px 16px', background: 'var(--bg-main)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <File size={24} color="var(--primary)" />
              <div>
                <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>{selectedFile.name}</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{selectedFile.size} bytes</div>
              </div>
            </div>
          )}

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading || !selectedFile}
            style={{ marginTop: '20px', padding: '12px' }}
          >
            {loading ? (
              <div className="spinner"></div>
            ) : (
              <>
                <ShieldCheck size={18} />
                <span>Verify Against Database & Blockchain</span>
              </>
            )}
          </button>
        </form>

        {/* Verification Result Output Display */}
        {verificationResult && (
          <div style={{ marginTop: '32px' }}>
            {/* Visual Result Header Banner */}
            <div
              style={{
                backgroundColor: isVerified ? 'var(--success-light)' : 'var(--danger-light)',
                border: `2px solid ${isVerified ? 'var(--success)' : 'var(--danger)'}`,
                borderRadius: 'var(--radius-md)',
                padding: '24px',
                textAlign: 'center',
                marginBottom: '24px',
              }}
            >
              {isVerified ? (
                <CheckCircle2 size={64} color="var(--success)" style={{ marginBottom: '8px' }} />
              ) : (
                <XCircle size={64} color="var(--danger)" style={{ marginBottom: '8px' }} />
              )}
              <h2 style={{ color: isVerified ? 'var(--success)' : 'var(--danger)', fontSize: '1.75rem', fontWeight: '700' }}>
                {verificationResult.verificationStatus}
              </h2>
              <p style={{ marginTop: '8px', fontSize: '1rem', fontWeight: '500', color: 'var(--text-main)' }}>
                {verificationResult.verificationMessage}
              </p>
            </div>

            {/* Detailed Dual-Layer Hash Breakdown Table */}
            <div style={{ background: 'var(--bg-main)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '20px' }}>
              <h4 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <ShieldCheck size={18} color="var(--primary)" />
                Cryptographic Hash Comparison Results
              </h4>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '20px' }}>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Evidence ID</div>
                  <div style={{ fontWeight: '700', color: 'var(--primary)' }}>{verificationResult.evidenceId}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Name</div>
                  <div style={{ fontWeight: '600' }}>{verificationResult.fileName}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Verification Status</div>
                  <StatusBadge status={verificationResult.verificationStatus} />
                </div>
              </div>

              {/* Hash comparison details */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>1. Uploaded File Calculated Hash</div>
                  <div className="hash-code" style={{ color: 'var(--primary)' }}>{verificationResult.calculatedHash || 'N/A'}</div>
                </div>

                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Database size={14} color="var(--secondary)" />
                    2. PostgreSQL Stored Database Hash
                  </div>
                  <div className="hash-code">
                    {verificationResult.storedHash || 'No matching database hash found'}
                  </div>
                </div>

                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Link2 size={14} color="var(--warning)" />
                    3. Mock Blockchain Anchored Hash
                  </div>
                  <div className="hash-code" style={{ color: 'var(--warning)' }}>
                    {verificationResult.blockchainHash || 'No matching blockchain hash found'}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VerifyEvidence;
