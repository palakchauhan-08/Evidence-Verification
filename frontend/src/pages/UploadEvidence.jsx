import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import { Upload, File, CheckCircle2, ShieldCheck, Link2, ArrowRight } from 'lucide-react';

const UploadEvidence = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [uploadResult, setUploadResult] = useState(null);

  const navigate = useNavigate();

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setError('');
      setUploadResult(null);
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      setError('Please select a file to upload');
      return;
    }

    setLoading(true);
    setError('');
    setUploadResult(null);

    try {
      const data = await evidenceAPI.upload(selectedFile);
      setUploadResult(data);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to upload evidence file');
    } finally {
      setLoading(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Upload Digital Evidence</h3>
        </div>

        <AlertMessage type="danger" message={error} onClose={() => setError('')} />

        {!uploadResult ? (
          <form onSubmit={handleUpload}>
            <div className="dropzone" onClick={() => document.getElementById('fileInput').click()}>
              <input
                type="file"
                id="fileInput"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              <Upload size={48} style={{ color: 'var(--primary)', marginBottom: '12px' }} />
              <h4 style={{ marginBottom: '6px' }}>Click to browse or drop file here</h4>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Supports documents, images, video, audio, and forensic artifacts
              </p>
            </div>

            {selectedFile && (
              <div style={{ marginTop: '20px', padding: '16px', background: 'var(--bg-main)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', gap: '12px' }}>
                <File size={28} color="var(--secondary)" />
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: '600', fontSize: '0.95rem' }}>{selectedFile.name}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    Type: {selectedFile.type || 'Unknown'} | Size: {formatFileSize(selectedFile.size)}
                  </div>
                </div>
              </div>
            )}

            <button
              type="submit"
              className="btn btn-primary btn-block"
              disabled={loading || !selectedFile}
              style={{ marginTop: '24px', padding: '12px' }}
            >
              {loading ? (
                <div className="spinner"></div>
              ) : (
                <>
                  <ShieldCheck size={18} />
                  <span>Compute SHA-256 & Anchor to Blockchain</span>
                </>
              )}
            </button>
          </form>
        ) : (
          <div>
            <div style={{ textAlign: 'center', marginBottom: '24px' }}>
              <CheckCircle2 size={56} color="var(--success)" style={{ marginBottom: '8px' }} />
              <h3 style={{ color: 'var(--success)' }}>Evidence Successfully Uploaded & Anchored!</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '4px' }}>
                Cryptographic hash generated and anchored to mock blockchain ledger.
              </p>
            </div>

            <div style={{ background: 'var(--bg-main)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '20px', marginBottom: '24px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Evidence ID</div>
                  <div style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary)' }}>{uploadResult.evidenceId}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Name</div>
                  <div style={{ fontWeight: '600' }}>{uploadResult.fileName}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Uploaded By</div>
                  <div>{uploadResult.uploadedBy}</div>
                </div>
                <div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Blockchain Status</div>
                  <StatusBadge status="CONFIRMED" />
                </div>
              </div>

              <div style={{ marginTop: '16px' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>SHA-256 Hash</div>
                <div className="hash-code">{uploadResult.fileHash}</div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '12px' }}>
              <button
                onClick={() => {
                  setSelectedFile(null);
                  setUploadResult(null);
                }}
                className="btn btn-outline"
              >
                Upload Another File
              </button>
              <Link to={`/evidence/${uploadResult.evidenceId}`} className="btn btn-primary">
                View Evidence Details <ArrowRight size={16} />
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default UploadEvidence;
