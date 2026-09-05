import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import {
  FileText,
  Link2,
  History,
  ArrowLeft,
  ShieldCheck,
  Clock,
  User,
  Search,
  ShieldOff,
  AlertTriangle,
  RotateCcw,
  X,
  ArrowRight,
  ShieldAlert,
  ExternalLink,
  QrCode,
  Download,
  MessageSquare,
  Plus,
  Trash2,
  Edit3,
  GitCommit,
  UploadCloud,
  Layers
} from 'lucide-react';

const EvidenceDetails = () => {
  const { evidenceId } = useParams();
  const { userEmail, userRole, hasAnyRole } = useAuth();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectError, setRejectError] = useState('');
  const [reportLoading, setReportLoading] = useState(false);
  const [showQrModal, setShowQrModal] = useState(false);
  const [qrLoading, setQrLoading] = useState(false);
  const [qrImageUrl, setQrImageUrl] = useState('');
  const [qrError, setQrError] = useState('');

  // Investigator Notes State
  const [notes, setNotes] = useState([]);
  const [notesLoading, setNotesLoading] = useState(false);
  const [newNoteContent, setNewNoteContent] = useState('');
  const [noteError, setNoteError] = useState('');
  const [noteSuccess, setNoteSuccess] = useState('');
  const [editingNoteId, setEditingNoteId] = useState(null);
  const [editNoteContent, setEditNoteContent] = useState('');

  const fetchNotes = async () => {
    setNotesLoading(true);
    try {
      const data = await evidenceAPI.getNotes(evidenceId);
      setNotes(data || []);
    } catch (err) {
      console.error('Failed to fetch notes:', err);
    } finally {
      setNotesLoading(false);
    }
  };

  // Versioning State
  const [versions, setVersions] = useState([]);
  const [selectedVersionNumber, setSelectedVersionNumber] = useState(null);
  const [showUploadVersionModal, setShowUploadVersionModal] = useState(false);
  const [versionFile, setVersionFile] = useState(null);
  const [versionUploadLoading, setVersionUploadLoading] = useState(false);
  const [versionUploadError, setVersionUploadError] = useState('');

  const fetchVersions = async () => {
    try {
      const data = await evidenceAPI.getVersions(evidenceId);
      setVersions(data || []);
      if (data && data.length > 0 && selectedVersionNumber === null) {
        setSelectedVersionNumber(data[0].versionNumber);
      }
    } catch (err) {
      console.error('Failed to fetch versions:', err);
    }
  };

  useEffect(() => {
    fetchDetail();
    fetchNotes();
    fetchVersions();
  }, [evidenceId]);

  const handleUploadVersionSubmit = async (e) => {
    e.preventDefault();
    if (!versionFile) return;
    setVersionUploadLoading(true);
    setVersionUploadError('');
    try {
      const newVersion = await evidenceAPI.uploadNewVersion(evidenceId, versionFile);
      setSuccessMsg(`New Evidence Version ${newVersion.versionNumber} uploaded successfully.`);
      setShowUploadVersionModal(false);
      setVersionFile(null);
      await fetchDetail();
      await fetchVersions();
      setSelectedVersionNumber(newVersion.versionNumber);
    } catch (err) {
      setVersionUploadError(err.response?.data?.error || err.message || 'Failed to upload new evidence version');
    } finally {
      setVersionUploadLoading(false);
    }
  };

  const handleAddNote = async (e) => {
    e.preventDefault();
    if (!newNoteContent.trim()) return;
    setNoteError('');
    setNoteSuccess('');
    try {
      await evidenceAPI.addNote(evidenceId, newNoteContent.trim());
      setNewNoteContent('');
      setNoteSuccess('Investigator note added successfully.');
      fetchNotes();
    } catch (err) {
      setNoteError(err.response?.data?.error || err.message || 'Failed to add note');
    }
  };

  const handleUpdateNote = async (noteId) => {
    if (!editNoteContent.trim()) return;
    setNoteError('');
    setNoteSuccess('');
    try {
      await evidenceAPI.updateNote(evidenceId, noteId, editNoteContent.trim());
      setEditingNoteId(null);
      setEditNoteContent('');
      setNoteSuccess('Investigator note updated successfully.');
      fetchNotes();
    } catch (err) {
      setNoteError(err.response?.data?.error || err.message || 'Failed to update note');
    }
  };

  const handleDeleteNote = async (noteId) => {
    if (!window.confirm('Are you sure you want to delete this investigator note?')) return;
    setNoteError('');
    setNoteSuccess('');
    try {
      await evidenceAPI.deleteNote(evidenceId, noteId);
      setNoteSuccess('Investigator note deleted successfully.');
      fetchNotes();
    } catch (err) {
      setNoteError(err.response?.data?.error || err.message || 'Failed to delete note');
    }
  };

  const handleOpenQrModal = async () => {
    setShowQrModal(true);
    setQrLoading(true);
    setQrError('');
    try {
      const blob = await evidenceAPI.downloadEvidenceQrCode(evidenceId);
      const objectUrl = URL.createObjectURL(blob);
      setQrImageUrl(objectUrl);
    } catch (err) {
      setQrError(err.response?.data?.error || err.message || 'Failed to load Evidence QR code');
    } finally {
      setQrLoading(false);
    }
  };

  const handleDownloadQrImage = () => {
    if (!qrImageUrl) return;
    const link = document.createElement('a');
    link.href = qrImageUrl;
    link.setAttribute('download', `Evidence_QR_${evidenceId}.png`);
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
  };

  const handleDownloadReport = async () => {
    setReportLoading(true);
    setError('');
    setSuccessMsg('');
    try {
      const blobData = await evidenceAPI.downloadVerificationReport(evidenceId);
      const url = window.URL.createObjectURL(new Blob([blobData], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Verification_Report_${evidenceId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
      setSuccessMsg(`Verification report for ${evidenceId} downloaded successfully.`);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to generate PDF verification report');
    } finally {
      setReportLoading(false);
    }
  };

  const handleDownloadVersionReport = async (vNum) => {
    setReportLoading(true);
    setError('');
    setSuccessMsg('');
    try {
      const blobData = await evidenceAPI.downloadVersionVerificationReport(evidenceId, vNum);
      const url = window.URL.createObjectURL(new Blob([blobData], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Verification_Report_${evidenceId}_v${vNum}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
      setSuccessMsg(`Version ${vNum} legal verification report downloaded successfully.`);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to generate version verification report');
    } finally {
      setReportLoading(false);
    }
  };

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

  const handleStartReview = async () => {
    setActionLoading(true);
    setError('');
    setSuccessMsg('');
    try {
      await evidenceAPI.startReview(evidenceId);
      setSuccessMsg('Evidence status updated to UNDER_REVIEW. Forensic examination initiated.');
      await fetchDetail();
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to start review');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectSubmit = async (e) => {
    e.preventDefault();
    if (!rejectReason.trim() || rejectReason.trim().length < 5) {
      setRejectError('Rejection reason must be at least 5 characters long.');
      return;
    }

    setActionLoading(true);
    setRejectError('');
    setError('');
    setSuccessMsg('');

    try {
      await evidenceAPI.reject(evidenceId, rejectReason.trim());
      setSuccessMsg('Evidence status updated to REJECTED with logged audit reason.');
      setShowRejectModal(false);
      setRejectReason('');
      await fetchDetail();
    } catch (err) {
      setRejectError(err.response?.data?.error || err.message || 'Failed to reject evidence');
    } finally {
      setActionLoading(false);
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

  const formatFileSize = (bytes) => {
    if (bytes === null || bytes === undefined) return 'Unavailable';
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i] + ` (${bytes.toLocaleString()} bytes)`;
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

  if (loading) {
    return <LoadingSpinner text={`Loading details for ${evidenceId}...`} />;
  }

  if (error && !detail) {
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

  const currentStatus = (detail.status || 'UPLOADED').toUpperCase();

  return (
    <div>
      {/* Top Header Navigation & Action Bar */}
      <div
        style={{
          marginBottom: '20px',
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '12px',
        }}
      >
        <Link to="/evidence" className="btn btn-outline">
          <ArrowLeft size={16} /> Back to Repository
        </Link>

        {/* Workflow Action Buttons for ADMIN and FORENSIC_ANALYST */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {hasAnyRole(['ADMIN', 'FORENSIC_ANALYST']) && (currentStatus === 'UPLOADED' || currentStatus === 'REJECTED' || currentStatus === 'TAMPERED') && (
            <button
              onClick={handleStartReview}
              disabled={actionLoading}
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
            >
              {currentStatus === 'REJECTED' || currentStatus === 'TAMPERED' ? (
                <>
                  <RotateCcw size={16} /> Re-Open Review
                </>
              ) : (
                <>
                  <Search size={16} /> Start Review
                </>
              )}
            </button>
          )}

          {hasAnyRole(['ADMIN', 'FORENSIC_ANALYST']) && (currentStatus === 'UPLOADED' || currentStatus === 'UNDER_REVIEW') && (
            <button
              onClick={() => {
                setShowRejectModal(true);
                setRejectError('');
              }}
              disabled={actionLoading}
              className="btn btn-outline"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: '#ef4444', borderColor: '#ef4444' }}
            >
              <ShieldOff size={16} /> Reject Evidence
            </button>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST']) && (
            <Link to="/evidence/verify" className="btn btn-secondary">
              <ShieldCheck size={16} /> Verify File Integrity
            </Link>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
            <button
              onClick={() => {
                setShowUploadVersionModal(true);
                setVersionUploadError('');
              }}
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', backgroundColor: '#8b5cf6', borderColor: '#8b5cf6' }}
              title="Upload a new version for this evidence record"
            >
              <UploadCloud size={16} /> Upload New Version
            </button>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER']) && (
            <button
              onClick={handleDownloadReport}
              disabled={reportLoading || actionLoading}
              className="btn btn-outline"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', backgroundColor: 'rgba(37, 99, 235, 0.08)', borderColor: 'var(--primary)', color: 'var(--primary)' }}
              title="Generate and download forensic PDF verification report"
            >
              {reportLoading ? (
                <>
                  <div className="spinner" style={{ width: '14px', height: '14px' }}></div>
                  <span>Generating PDF...</span>
                </>
              ) : (
                <>
                  <FileText size={16} /> Generate Verification Report
                </>
              )}
            </button>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER']) && (
            <button
              onClick={handleOpenQrModal}
              disabled={actionLoading}
              className="btn btn-outline"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
              title="Generate QR code linking to evidence verification details"
            >
              <QrCode size={16} /> Evidence QR Code
            </button>
          )}
        </div>
      </div>

      <AlertMessage type="success" message={successMsg} onClose={() => setSuccessMsg('')} />
      <AlertMessage type="danger" message={error} onClose={() => setError('')} />

      {/* Critical Status Alerts */}
      {(currentStatus === 'TAMPERED' || currentStatus === 'COMPROMISED') && (
        <div
          style={{
            marginBottom: '20px',
            padding: '20px',
            borderRadius: '10px',
            backgroundColor: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.4)',
            color: '#ef4444',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '14px', marginBottom: '16px' }}>
            <AlertTriangle size={28} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <div style={{ fontWeight: '800', fontSize: '1.1rem', marginBottom: '4px' }}>
                🚨 CRITICAL FORENSIC WARNING: EVIDENCE INTEGRITY COMPROMISED
              </div>
              <div style={{ fontSize: '0.88rem', lineHeight: '1.5', opacity: 0.9 }}>
                A cryptographic SHA-256 hash mismatch was detected during evidence verification between the uploaded file, database record, or Polygon blockchain anchor. Evidence status is marked as <strong>TAMPERED</strong>. Original stored hashes and blockchain transaction references remain strictly preserved for audit integrity.
              </div>
            </div>
          </div>

          <div style={{ background: 'var(--bg-main, #0f172a)', padding: '14px', borderRadius: '8px', border: '1px solid rgba(239, 68, 68, 0.3)' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: '700', textTransform: 'uppercase', marginBottom: '10px', color: '#f87171' }}>
              3-Way Cryptographic Hash Comparison
            </div>
            <div style={{ display: 'grid', gap: '10px', fontSize: '0.82rem' }}>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Database SHA-256 Hash: </span>
                <code style={{ wordBreak: 'break-all', color: '#38bdf8' }}>{detail.fileHash || 'N/A'}</code>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Blockchain SHA-256 Hash: </span>
                <code style={{ wordBreak: 'break-all', color: '#38bdf8' }}>{detail.blockchainRecord?.fileHash || 'N/A'}</code>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Verification Result: </span>
                <span style={{ fontWeight: '700', color: '#ef4444' }}>🚨 TAMPERED / INTEGRITY COMPROMISED</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {currentStatus === 'REJECTED' && detail.rejectionReason && (
        <div
          style={{
            marginBottom: '20px',
            padding: '16px 20px',
            borderRadius: '8px',
            backgroundColor: 'rgba(107, 114, 128, 0.15)',
            border: '1px solid rgba(107, 114, 128, 0.3)',
            color: 'var(--text-main)',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '14px',
          }}
        >
          <ShieldOff size={22} color="#6b7280" style={{ flexShrink: 0, marginTop: '2px' }} />
          <div>
            <div style={{ fontWeight: '700', fontSize: '0.95rem', marginBottom: '4px', color: '#9ca3af' }}>
              REJECTION RATIONALE
            </div>
            <div style={{ fontSize: '0.9rem', fontStyle: 'italic' }}>
              "{detail.rejectionReason}"
            </div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '6px' }}>
              Rejected by: {detail.reviewedBy || 'Authorized Examiner'} • {formatDate(detail.reviewedAt)}
            </div>
          </div>
        </div>
      )}



      {/* Section 1: Evidence Metadata & Workflow */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <FileText color="var(--primary)" size={20} />
            Section 1: Evidence Metadata & Workflow State
          </h3>
          <StatusBadge status={detail.status} />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '20px' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Evidence ID</div>
            <div style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary)' }}>{detail.evidenceId}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Name</div>
            <div style={{ fontWeight: '600' }}>{detail.fileName}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Extension</div>
            <div style={{ fontWeight: '600', textTransform: 'uppercase', color: 'var(--secondary)' }}>
              {detail.fileExtension || 'N/A'}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>MIME / Content Type</div>
            <div>{detail.fileType || 'application/octet-stream'}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>File Size</div>
            <div style={{ fontWeight: '600' }}>{formatFileSize(detail.fileSize)}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Uploaded By</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <User size={14} color="var(--secondary)" />
              {detail.uploadedBy}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Upload Timestamp</div>
            <div style={{ fontSize: '0.88rem' }}>{formatDate(detail.uploadedAt)}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Original Created Date</div>
            <div style={{ fontSize: '0.88rem', color: detail.createdTimestamp ? 'var(--text-main)' : 'var(--text-muted)', fontStyle: detail.createdTimestamp ? 'normal' : 'italic' }}>
              {detail.createdTimestamp ? formatDate(detail.createdTimestamp) : 'Unavailable'}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Original Modified Date</div>
            <div style={{ fontSize: '0.88rem', color: detail.modifiedTimestamp ? 'var(--text-main)' : 'var(--text-muted)', fontStyle: detail.modifiedTimestamp ? 'normal' : 'italic' }}>
              {detail.modifiedTimestamp ? formatDate(detail.modifiedTimestamp) : 'Unavailable'}
            </div>
          </div>
          {detail.reviewStartedAt && (
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Review Started At</div>
              <div style={{ fontSize: '0.88rem' }}>{formatDate(detail.reviewStartedAt)}</div>
            </div>
          )}
          {detail.reviewedAt && (
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Reviewed / Verified At</div>
              <div style={{ fontSize: '0.88rem' }}>{formatDate(detail.reviewedAt)}</div>
            </div>
          )}
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
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Blockchain Network</div>
                <div style={{ fontWeight: '600', color: 'var(--secondary)', fontSize: '0.9rem' }}>
                  Polygon Amoy Testnet (Chain ID 80002)
                </div>
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
              {detail.blockchainRecord.transactionHash && detail.blockchainRecord.transactionHash.trim().length > 0 ? (
                <>
                  <div className="hash-code" style={{ color: 'var(--warning)', wordBreak: 'break-all' }}>
                    {detail.blockchainRecord.transactionHash}
                  </div>
                  <div style={{ marginTop: '10px' }}>
                    <a
                      href={`https://amoy.polygonscan.com/tx/${detail.blockchainRecord.transactionHash.trim()}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-outline"
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '6px',
                        fontSize: '0.82rem',
                        padding: '6px 14px',
                        borderColor: 'var(--primary)',
                        color: 'var(--primary)'
                      }}
                      title="Open actual transaction ledger on Polygon Amoy Explorer"
                    >
                      <ExternalLink size={14} /> View on Polygon Explorer ↗
                    </a>
                  </div>
                </>
              ) : (
                <div style={{ fontSize: '0.88rem', color: 'var(--text-muted)', fontStyle: 'italic', padding: '8px 0' }}>
                  Blockchain transaction unavailable
                </div>
              )}
            </div>

            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>On-Chain Anchored File Hash</div>
              <div className="hash-code">{detail.blockchainRecord.fileHash}</div>
            </div>
          </div>
        ) : (
          <div style={{ padding: '12px 0', color: 'var(--text-muted)', fontStyle: 'italic', fontSize: '0.9rem' }}>
            Blockchain transaction unavailable
          </div>
        )}
      </div>

      {/* Section 3: Digital Evidence Chain of Custody Timeline */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <History color="var(--secondary)" size={20} />
            Section 3: Digital Evidence Chain of Custody
          </h3>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {detail.auditLogs ? `${detail.auditLogs.length} custodial events` : '0 events'}
          </span>
        </div>

        {!detail.auditLogs || detail.auditLogs.length === 0 ? (
          <div style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No custody history recorded yet.</div>
        ) : (
          <div className="timeline">
            {detail.auditLogs.map((log) => (
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
        )}
      </div>

      {/* Section 4: Investigator Notes & Observations */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <MessageSquare color="var(--primary)" size={20} />
            Section 4: Investigator Notes & Observations
          </h3>
          <span className="badge badge-secondary" style={{ fontSize: '0.75rem' }}>
            {notes.length} Note{notes.length === 1 ? '' : 's'}
          </span>
        </div>

        <AlertMessage type="danger" message={noteError} onClose={() => setNoteError('')} />
        <AlertMessage type="success" message={noteSuccess} onClose={() => setNoteSuccess('')} />

        {/* Add Note Form (INVESTIGATOR, FORENSIC_ANALYST, ADMIN) */}
        {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST']) && (
          <form onSubmit={handleAddNote} style={{ marginBottom: '24px', background: 'var(--bg-main)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
            <label className="form-label" style={{ fontWeight: '600', marginBottom: '8px', display: 'block', fontSize: '0.88rem' }}>
              Add Investigation Finding / Observation Note
            </label>
            <textarea
              className="form-control"
              rows={3}
              placeholder="Record forensic observation, verification notes, or case findings for this evidence..."
              value={newNoteContent}
              onChange={(e) => setNewNoteContent(e.target.value)}
              style={{ width: '100%', marginBottom: '12px', fontSize: '0.88rem' }}
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={!newNoteContent.trim()}
                style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
              >
                <Plus size={16} /> Add Note
              </button>
            </div>
          </form>
        )}

        {/* Notes List */}
        {notesLoading ? (
          <LoadingSpinner text="Loading investigator notes..." />
        ) : notes.length === 0 ? (
          <div style={{ color: 'var(--text-muted)', fontStyle: 'italic', padding: '20px', textAlign: 'center' }}>
            No investigator notes have been recorded for this evidence yet.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {notes.map((note) => {
              const isEditing = editingNoteId === note.noteId;
              const isAuthor = userEmail && userEmail.toLowerCase() === (note.author || '').toLowerCase();
              const isAdmin = userRole === 'ADMIN';
              const canModify = (isAuthor || isAdmin) && !hasAnyRole(['VIEWER']);

              return (
                <div
                  key={note.noteId}
                  style={{
                    padding: '16px',
                    borderRadius: '8px',
                    backgroundColor: 'var(--bg-main)',
                    border: '1px solid var(--border-color)',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <User size={14} color="var(--primary)" />
                      <span style={{ fontWeight: '700', fontSize: '0.9rem' }}>{note.author}</span>
                      <span className="badge badge-secondary" style={{ fontSize: '0.7rem' }}>
                        {note.authorRole || 'INVESTIGATOR'}
                      </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Clock size={12} />
                        {formatDate(note.createdAt)}
                        {note.updatedAt && note.updatedAt !== note.createdAt && (
                          <span style={{ fontStyle: 'italic', marginLeft: '4px' }}>(edited)</span>
                        )}
                      </div>

                      {canModify && !isEditing && (
                        <div style={{ display: 'flex', gap: '6px' }}>
                          <button
                            onClick={() => {
                              setEditingNoteId(note.noteId);
                              setEditNoteContent(note.content);
                            }}
                            className="btn btn-outline"
                            style={{ padding: '4px 8px', fontSize: '0.75rem' }}
                            title="Edit Note"
                          >
                            <Edit3 size={12} /> Edit
                          </button>
                          <button
                            onClick={() => handleDeleteNote(note.noteId)}
                            className="btn btn-outline"
                            style={{ padding: '4px 8px', fontSize: '0.75rem', color: '#ef4444', borderColor: 'rgba(239, 68, 68, 0.4)' }}
                            title="Delete Note"
                          >
                            <Trash2 size={12} /> Delete
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                  {isEditing ? (
                    <div>
                      <textarea
                        className="form-control"
                        rows={3}
                        value={editNoteContent}
                        onChange={(e) => setEditNoteContent(e.target.value)}
                        style={{ width: '100%', marginBottom: '10px', fontSize: '0.88rem' }}
                      />
                      <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                        <button
                          onClick={() => setEditingNoteId(null)}
                          className="btn btn-outline"
                          style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                        >
                          Cancel
                        </button>
                        <button
                          onClick={() => handleUpdateNote(note.noteId)}
                          className="btn btn-primary"
                          disabled={!editNoteContent.trim()}
                          style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                        >
                          Save Changes
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div style={{ fontSize: '0.9rem', lineHeight: '1.5', whiteSpace: 'pre-wrap' }}>
                      {note.content}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Section 5: Evidence Version History */}
      <div className="card" style={{ borderLeft: '4px solid #8b5cf6' }}>
        <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Layers color="#8b5cf6" size={20} />
            Section 5: Evidence Version History
          </h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span className="badge badge-primary" style={{ backgroundColor: '#8b5cf6', borderColor: '#8b5cf6', fontSize: '0.75rem' }}>
              {versions.length} Version{versions.length === 1 ? '' : 's'}
            </span>
            {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
              <button
                onClick={() => {
                  setShowUploadVersionModal(true);
                  setVersionUploadError('');
                }}
                className="btn btn-primary"
                style={{ fontSize: '0.8rem', padding: '6px 12px', display: 'inline-flex', alignItems: 'center', gap: '6px', backgroundColor: '#8b5cf6', borderColor: '#8b5cf6' }}
                title="Upload a new version for this evidence record"
              >
                <UploadCloud size={14} /> Upload New Version
              </button>
            )}
          </div>
        </div>

        {versions.length === 0 ? (
          <div style={{ color: 'var(--text-muted)', fontStyle: 'italic', padding: '20px', textAlign: 'center' }}>
            No version history recorded yet. Initial Version 1 will automatically initialize.
          </div>
        ) : (
          <div>
            {/* Version Pills Horizontal Bar */}
            <div style={{ display: 'flex', gap: '10px', overflowX: 'auto', paddingBottom: '12px', marginBottom: '20px', borderBottom: '1px solid var(--border-color)' }}>
              {versions.map((ver) => {
                const isSelected = (selectedVersionNumber || versions[0]?.versionNumber) === ver.versionNumber;
                const isLatest = ver.versionNumber === versions[0]?.versionNumber;

                return (
                  <button
                    key={ver.versionId}
                    onClick={() => setSelectedVersionNumber(ver.versionNumber)}
                    style={{
                      padding: '10px 16px',
                      borderRadius: '8px',
                      border: isSelected ? '2px solid #8b5cf6' : '1px solid var(--border-color)',
                      backgroundColor: isSelected ? 'rgba(139, 92, 246, 0.12)' : 'var(--bg-main)',
                      color: isSelected ? '#a78bfa' : 'var(--text-main)',
                      cursor: 'pointer',
                      minWidth: '160px',
                      textAlign: 'left',
                      transition: 'all 0.2s ease',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                      <span style={{ fontWeight: '700', fontSize: '0.95rem' }}>Version {ver.versionNumber}</span>
                      {isLatest && (
                        <span style={{ fontSize: '0.65rem', padding: '1px 6px', borderRadius: '4px', background: '#8b5cf6', color: '#fff', fontWeight: '700' }}>
                          LATEST
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {ver.fileName}
                    </div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {formatDate(ver.uploadedAt)}
                    </div>
                  </button>
                );
              })}
            </div>

            {/* Selected Version Detailed Breakdown */}
            {(() => {
              const activeVer = versions.find((v) => v.versionNumber === (selectedVersionNumber || versions[0]?.versionNumber)) || versions[0];
              if (!activeVer) return null;

              return (
                <div style={{ background: 'var(--bg-main)', padding: '20px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
                    <div style={{ fontWeight: '700', fontSize: '1.05rem', color: '#a78bfa', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <GitCommit size={18} />
                      Version {activeVer.versionNumber} Details ({activeVer.versionId})
                    </div>
                    <button
                      onClick={() => handleDownloadVersionReport(activeVer.versionNumber)}
                      disabled={reportLoading}
                      className="btn btn-outline"
                      style={{ fontSize: '0.8rem', padding: '6px 12px', display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--primary)', borderColor: 'var(--primary)' }}
                    >
                      <FileText size={14} /> Export v{activeVer.versionNumber} Verification Report (PDF)
                    </button>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '16px' }}>
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>File Name</div>
                      <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>{activeVer.fileName}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>File Extension</div>
                      <div style={{ fontWeight: '600', textTransform: 'uppercase', color: 'var(--secondary)', fontSize: '0.9rem' }}>
                        {activeVer.fileExtension || 'N/A'}
                      </div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>File Size</div>
                      <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>{formatFileSize(activeVer.fileSize)}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Uploaded By</div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.9rem' }}>
                        <User size={13} color="var(--secondary)" />
                        {activeVer.uploadedBy}
                      </div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Upload Timestamp</div>
                      <div style={{ fontSize: '0.85rem' }}>{formatDate(activeVer.uploadedAt)}</div>
                    </div>
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Workflow Status</div>
                      <StatusBadge status={activeVer.status} />
                    </div>
                  </div>

                  <div style={{ marginBottom: '16px' }}>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
                      Cryptographic SHA-256 Fingerprint (Version {activeVer.versionNumber})
                    </div>
                    <div className="hash-code" style={{ padding: '8px 12px', fontSize: '0.85rem' }}>
                      {activeVer.fileHash}
                    </div>
                  </div>

                  {activeVer.blockchainRecord && (
                    <div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
                        Polygon Amoy On-Chain Anchor (Version {activeVer.versionNumber})
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                        <div className="hash-code" style={{ padding: '8px 12px', fontSize: '0.82rem', color: 'var(--warning)' }}>
                          {activeVer.blockchainRecord.transactionHash || 'Anchored on-chain'}
                        </div>
                        {activeVer.blockchainRecord.transactionHash && (
                          <a
                            href={`https://amoy.polygonscan.com/tx/${activeVer.blockchainRecord.transactionHash}`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn btn-outline"
                            style={{ padding: '4px 10px', fontSize: '0.78rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                          >
                            <ExternalLink size={12} /> Explorer ↗
                          </a>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              );
            })()}
          </div>
        )}
      </div>

      {/* Rejection Modal Dialog */}
      {showRejectModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '20px',
          }}
        >
          <div
            className="card"
            style={{
              maxWidth: '500px',
              width: '100%',
              margin: 0,
              backgroundColor: 'var(--bg-card, #1e293b)',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
            }}
          >
            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#ef4444' }}>
                <ShieldOff size={20} />
                Reject Evidence ({detail.evidenceId})
              </h3>
              <button
                onClick={() => setShowRejectModal(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleRejectSubmit}>
              <AlertMessage type="danger" message={rejectError} onClose={() => setRejectError('')} />

              <div style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                  Rejection Reason / Rationale (Required)
                </label>
                <textarea
                  className="form-control"
                  rows={4}
                  placeholder="Provide a clear, detailed reason for rejecting this evidence (e.g. Duplicate submission, invalid metadata, insufficient chain of custody documentation)..."
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  style={{ width: '100%', padding: '10px', fontSize: '0.88rem' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowRejectModal(false)}
                  className="btn btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading || !rejectReason.trim()}
                  className="btn btn-primary"
                  style={{ backgroundColor: '#ef4444', borderColor: '#ef4444' }}
                >
                  {actionLoading ? 'Submitting...' : 'Confirm Rejection'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Evidence QR Code Modal */}
      {showQrModal && (
        <div className="modal-overlay" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0, 0, 0, 0.6)', position: 'fixed', inset: 0, zIndex: 1000, padding: '20px' }}>
          <div className="card" style={{ maxWidth: '420px', width: '100%', padding: '24px', position: 'relative', textAlign: 'center', backgroundColor: 'var(--bg-card, #1e293b)' }}>
            <button
              onClick={() => setShowQrModal(false)}
              style={{ position: 'absolute', top: '16px', right: '16px', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}
            >
              <X size={20} />
            </button>

            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '12px' }}>
              <div style={{ padding: '10px', background: 'rgba(37, 99, 235, 0.1)', borderRadius: '50%', color: 'var(--primary)' }}>
                <QrCode size={32} />
              </div>
            </div>

            <h3 style={{ marginBottom: '4px', fontSize: '1.2rem' }}>Evidence Verification QR Code</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
              Scan QR code to access evidence verification record.
            </p>

            <AlertMessage type="danger" message={qrError} onClose={() => setQrError('')} />

            {qrLoading ? (
              <div style={{ padding: '40px 0' }}>
                <LoadingSpinner text="Generating Evidence QR Code..." />
              </div>
            ) : qrImageUrl ? (
              <div>
                <div style={{ background: '#ffffff', padding: '16px', borderRadius: '12px', display: 'inline-block', border: '1px solid var(--border-color)', marginBottom: '16px' }}>
                  <img src={qrImageUrl} alt={`QR Code for ${evidenceId}`} style={{ width: '220px', height: '220px', display: 'block' }} />
                </div>

                <div style={{ background: 'var(--bg-main)', padding: '12px', borderRadius: '8px', border: '1px solid var(--border-color)', marginBottom: '20px', textAlign: 'left' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Evidence Reference ID</div>
                  <div style={{ fontWeight: '700', fontSize: '0.95rem', color: 'var(--primary)', marginBottom: '6px' }}>{evidenceId}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Verification URL</div>
                  <div style={{ fontSize: '0.8rem', wordBreak: 'break-all', color: 'var(--secondary)' }}>
                    {`${window.location.origin}/verify/evidence/${evidenceId}`}
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
                  <button onClick={handleDownloadQrImage} className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                    <Download size={16} /> Download QR Image
                  </button>
                  <button onClick={() => setShowQrModal(false)} className="btn btn-outline">
                    Close
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Upload New Version Modal Dialog */}
      {showUploadVersionModal && (
        <div className="modal-overlay" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(0, 0, 0, 0.6)', position: 'fixed', inset: 0, zIndex: 1000, padding: '20px' }}>
          <div className="card" style={{ maxWidth: '500px', width: '100%', padding: '24px', position: 'relative', backgroundColor: 'var(--bg-card, #1e293b)' }}>
            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#8b5cf6' }}>
                <UploadCloud size={20} />
                Upload New Evidence Version ({evidenceId})
              </h3>
              <button
                onClick={() => setShowUploadVersionModal(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleUploadVersionSubmit}>
              <AlertMessage type="danger" message={versionUploadError} onClose={() => setVersionUploadError('')} />

              <div style={{ marginBottom: '20px' }}>
                <label className="form-label" style={{ marginBottom: '8px', display: 'block', fontSize: '0.88rem', fontWeight: '600' }}>
                  Select Updated Evidence File (Max 50MB)
                </label>
                <input
                  type="file"
                  className="form-control"
                  required
                  onChange={(e) => setVersionFile(e.target.files[0])}
                  style={{ width: '100%', padding: '10px', fontSize: '0.88rem' }}
                />
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '6px' }}>
                  Next automatic version: <strong>Version {(versions.length > 0 ? versions[0].versionNumber : 1) + 1}</strong>. Historical versions remain strictly preserved.
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => setShowUploadVersionModal(false)}
                  className="btn btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={versionUploadLoading || !versionFile}
                  className="btn btn-primary"
                  style={{ backgroundColor: '#8b5cf6', borderColor: '#8b5cf6' }}
                >
                  {versionUploadLoading ? 'Uploading & Anchoring...' : 'Upload & Anchor Version'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default EvidenceDetails;
