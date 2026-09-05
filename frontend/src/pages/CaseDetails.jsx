import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { caseAPI, evidenceAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import CaseStatusBadge from '../components/CaseStatusBadge';
import CasePriorityBadge from '../components/CasePriorityBadge';
import StatusBadge from '../components/StatusBadge';
import StatCard from '../components/StatCard';
import {
  Briefcase,
  ArrowLeft,
  User,
  Clock,
  Plus,
  Trash2,
  Eye,
  History,
  FileCheck2,
  CheckCircle2,
  Search,
  ShieldOff,
  AlertTriangle,
  UserCheck,
  RotateCcw,
  X,
  ArrowRight,
  FileText
} from 'lucide-react';

const CaseDetails = () => {
  const { caseId } = useParams();
  const { hasAnyRole } = useAuth();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // All user evidence list for "Add Evidence to Case" modal selector
  const [availableEvidence, setAvailableEvidence] = useState([]);
  const [showAddEvidenceModal, setShowAddEvidenceModal] = useState(false);
  const [selectedEvidenceId, setSelectedEvidenceId] = useState('');

  // Modals for Update Status & Assign Investigator
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [targetStatus, setTargetStatus] = useState('IN_PROGRESS');
  const [statusReason, setStatusReason] = useState('');

  const [showAssignModal, setShowAssignModal] = useState(false);
  const [assignedInvestigatorInput, setAssignedInvestigatorInput] = useState('');

  useEffect(() => {
    fetchCaseDetail();
  }, [caseId]);

  const fetchCaseDetail = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await caseAPI.getCaseDetail(caseId);
      setDetail(data);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to load case details');
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableEvidence = async () => {
    try {
      const list = await evidenceAPI.getUserEvidence();
      // Filter out evidence items already in this case
      const existingIds = new Set((detail?.evidenceList || []).map((e) => e.evidenceId));
      setAvailableEvidence((list || []).filter((e) => !existingIds.has(e.evidenceId)));
    } catch {
      setAvailableEvidence([]);
    }
  };

  const handleOpenAddEvidenceModal = () => {
    fetchAvailableEvidence();
    setShowAddEvidenceModal(true);
  };

  const handleAddEvidenceSubmit = async (e) => {
    e.preventDefault();
    if (!selectedEvidenceId) return;

    setActionLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      const updated = await caseAPI.addEvidence(caseId, selectedEvidenceId);
      setDetail(updated);
      setSuccessMsg(`Evidence ${selectedEvidenceId} successfully associated with case ${caseId}.`);
      setShowAddEvidenceModal(false);
      setSelectedEvidenceId('');
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to add evidence to case');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRemoveEvidence = async (evidenceId) => {
    if (!window.confirm(`Are you sure you want to remove evidence ${evidenceId} from this case? (The evidence file, SHA-256 hash, and blockchain records will NOT be deleted).`)) {
      return;
    }

    setActionLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      const updated = await caseAPI.removeEvidence(caseId, evidenceId);
      setDetail(updated);
      setSuccessMsg(`Evidence ${evidenceId} disassociated from case ${caseId}.`);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to remove evidence from case');
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpdateStatusSubmit = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      const updatedCase = await caseAPI.updateStatus(caseId, targetStatus, statusReason.trim());
      setSuccessMsg(`Case status updated to ${targetStatus}.`);
      setShowStatusModal(false);
      setStatusReason('');
      await fetchCaseDetail();
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to update case status');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      await caseAPI.assignInvestigator(caseId, assignedInvestigatorInput.trim());
      setSuccessMsg(`Investigator assigned: ${assignedInvestigatorInput.trim() || 'Unassigned'}`);
      setShowAssignModal(false);
      await fetchCaseDetail();
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to assign investigator');
    } finally {
      setActionLoading(false);
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

  if (loading) {
    return <LoadingSpinner text={`Loading case details for ${caseId}...`} />;
  }

  if (error && !detail) {
    return (
      <div>
        <Link to="/cases" className="btn btn-outline" style={{ marginBottom: '20px' }}>
          <ArrowLeft size={16} /> Back to Cases
        </Link>
        <AlertMessage type="danger" message={error} />
      </div>
    );
  }

  if (!detail) return null;

  const { caseDetails, evidenceList, evidenceSummary, auditLogs } = detail;
  const isClosed = (caseDetails.status || '').toUpperCase() === 'CLOSED';

  return (
    <div>
      {/* Navigation & Action Bar */}
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Link to="/cases" className="btn btn-outline">
            <ArrowLeft size={16} /> Back to Cases
          </Link>
          <CaseStatusBadge status={caseDetails.status} />
          <CasePriorityBadge priority={caseDetails.priority} />
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && !isClosed && (
            <button
              onClick={handleOpenAddEvidenceModal}
              disabled={actionLoading}
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
            >
              <Plus size={16} /> Add Evidence
            </button>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST']) && (
            <button
              onClick={() => {
                setShowStatusModal(true);
                setTargetStatus(isClosed ? 'IN_PROGRESS' : 'CLOSED');
              }}
              disabled={actionLoading}
              className="btn btn-secondary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
            >
              <RotateCcw size={16} /> {isClosed ? 'Re-Open Case' : 'Update Status'}
            </button>
          )}

          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
            <button
              onClick={() => {
                setAssignedInvestigatorInput(caseDetails.assignedInvestigator || '');
                setShowAssignModal(true);
              }}
              disabled={actionLoading}
              className="btn btn-outline"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
            >
              <UserCheck size={16} /> Assign Investigator
            </button>
          )}
        </div>
      </div>

      <AlertMessage type="success" message={successMsg} onClose={() => setSuccessMsg('')} />
      <AlertMessage type="danger" message={error} onClose={() => setError('')} />

      {/* Case Metadata Card */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Briefcase color="var(--primary)" size={20} />
            {caseDetails.title}
          </h3>
          <span style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: '700' }}>
            ID: {caseDetails.caseId}
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '20px' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Assigned Investigator</div>
            <div style={{ fontWeight: '600', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <User size={14} color="var(--secondary)" />
              {caseDetails.assignedInvestigator || 'Unassigned'}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Created By</div>
            <div style={{ fontWeight: '600' }}>{caseDetails.createdBy}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Created Date</div>
            <div style={{ fontSize: '0.88rem' }}>{formatDate(caseDetails.createdAt)}</div>
          </div>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Last Updated</div>
            <div style={{ fontSize: '0.88rem' }}>{formatDate(caseDetails.updatedAt)}</div>
          </div>
          {caseDetails.closedAt && (
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Case Closed Date</div>
              <div style={{ fontSize: '0.88rem', color: '#10b981', fontWeight: '600' }}>{formatDate(caseDetails.closedAt)}</div>
            </div>
          )}
        </div>

        {caseDetails.description && (
          <div style={{ backgroundColor: 'rgba(255, 255, 255, 0.02)', padding: '12px 16px', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Investigation Summary / Description</div>
            <div style={{ fontSize: '0.9rem', lineHeight: '1.5' }}>{caseDetails.description}</div>
          </div>
        )}
      </div>

      {/* Case Evidence Summary Metrics */}
      <div className="grid-stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))' }}>
        <StatCard
          icon={FileText}
          value={evidenceSummary?.total || 0}
          label="Associated Evidence"
          color="var(--primary)"
          bg="var(--primary-light)"
        />
        <StatCard
          icon={CheckCircle2}
          value={evidenceSummary?.verified || 0}
          label="Verified"
          color="#10b981"
          bg="rgba(16, 185, 129, 0.1)"
        />
        <StatCard
          icon={Search}
          value={evidenceSummary?.underReview || 0}
          label="Under Review"
          color="#f59e0b"
          bg="rgba(245, 158, 11, 0.1)"
        />
        <StatCard
          icon={ShieldOff}
          value={evidenceSummary?.rejected || 0}
          label="Rejected"
          color="#6b7280"
          bg="rgba(107, 114, 128, 0.1)"
        />
        <StatCard
          icon={AlertTriangle}
          value={evidenceSummary?.tampered || 0}
          label="Tampered Alert"
          color="#ef4444"
          bg="rgba(239, 68, 68, 0.1)"
        />
      </div>

      {/* Section 2: Case Evidence Items */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <FileCheck2 color="var(--primary)" size={20} />
            Digital Evidence Items ({evidenceList.length})
          </h3>
          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && !isClosed && (
            <button
              onClick={handleOpenAddEvidenceModal}
              className="btn btn-outline"
              style={{ fontSize: '0.8rem', padding: '6px 12px' }}
            >
              <Plus size={14} /> Associate Evidence
            </button>
          )}
        </div>

        {evidenceList.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-muted)' }}>
            <FileText size={40} style={{ opacity: 0.4, marginBottom: '12px' }} />
            <p>No evidence items associated with this case yet.</p>
            {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && !isClosed && (
              <button onClick={handleOpenAddEvidenceModal} className="btn btn-primary" style={{ marginTop: '12px' }}>
                Add First Evidence Item
              </button>
            )}
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Evidence ID</th>
                  <th>File Name</th>
                  <th>File Type</th>
                  <th>Workflow Status</th>
                  <th>SHA-256 Hash</th>
                  <th>Uploaded By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {evidenceList.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <span style={{ fontWeight: '700', color: 'var(--primary)' }}>{item.evidenceId}</span>
                    </td>
                    <td>
                      <div style={{ fontWeight: '600' }}>{item.fileName}</div>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{item.fileType || 'binary'}</span>
                    </td>
                    <td>
                      <StatusBadge status={item.status || 'UPLOADED'} />
                    </td>
                    <td>
                      <span className="hash-code">
                        {item.fileHash ? `${item.fileHash.substring(0, 10)}...${item.fileHash.substring(item.fileHash.length - 6)}` : 'N/A'}
                      </span>
                    </td>
                    <td>{item.uploadedBy}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <Link
                          to={`/evidence/${item.evidenceId}`}
                          className="btn btn-outline"
                          style={{ padding: '4px 10px', fontSize: '0.78rem' }}
                          title="View Details"
                        >
                          <Eye size={14} /> Details
                        </Link>
                        {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && !isClosed && (
                          <button
                            onClick={() => handleRemoveEvidence(item.evidenceId)}
                            className="btn btn-outline"
                            style={{ padding: '4px 10px', fontSize: '0.78rem', color: '#ef4444', borderColor: '#ef4444' }}
                            title="Disassociate evidence from case"
                          >
                            <Trash2 size={14} /> Remove
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Section 3: Investigation Timeline (Chain of Custody) */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <History color="var(--secondary)" size={20} />
            Section 3: Integrated Investigation & Custody Timeline
          </h3>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {auditLogs ? `${auditLogs.length} historical events` : '0 events'}
          </span>
        </div>

        {!auditLogs || auditLogs.length === 0 ? (
          <div style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No audit history recorded yet.</div>
        ) : (
          <div className="timeline">
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
        )}
      </div>

      {/* Modal 1: Add Evidence to Case */}
      {showAddEvidenceModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.65)',
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
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Plus size={20} />
                Associate Evidence with Case ({caseId})
              </h3>
              <button
                onClick={() => setShowAddEvidenceModal(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleAddEvidenceSubmit}>
              <div style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                  Select Unassigned Evidence Item
                </label>
                {availableEvidence.length === 0 ? (
                  <div style={{ padding: '12px', fontSize: '0.88rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                    No unassigned evidence records available to add.
                  </div>
                ) : (
                  <select
                    className="form-control"
                    value={selectedEvidenceId}
                    onChange={(e) => setSelectedEvidenceId(e.target.value)}
                    required
                  >
                    <option value="">-- Choose Evidence Item --</option>
                    {availableEvidence.map((ev) => (
                      <option key={ev.evidenceId} value={ev.evidenceId}>
                        {ev.evidenceId} - {ev.fileName} ({ev.status})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={() => setShowAddEvidenceModal(false)} className="btn btn-outline">
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading || !selectedEvidenceId}
                  className="btn btn-primary"
                >
                  {actionLoading ? 'Associating...' : 'Associate Evidence'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 2: Update Case Status */}
      {showStatusModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.65)',
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
              maxWidth: '480px',
              width: '100%',
              margin: 0,
              backgroundColor: 'var(--bg-card, #1e293b)',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
            }}
          >
            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <RotateCcw size={20} />
                Update Case Status ({caseId})
              </h3>
              <button
                onClick={() => setShowStatusModal(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleUpdateStatusSubmit}>
              <div style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                  Target Case Status
                </label>
                <select
                  className="form-control"
                  value={targetStatus}
                  onChange={(e) => setTargetStatus(e.target.value)}
                >
                  <option value="OPEN">OPEN</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="ON_HOLD">ON_HOLD</option>
                  <option value="CLOSED">CLOSED</option>
                </select>
              </div>

              <div style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                  Status Change Rationale / Reason
                </label>
                <textarea
                  className="form-control"
                  rows={3}
                  placeholder="Provide context for updating case status..."
                  value={statusReason}
                  onChange={(e) => setStatusReason(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={() => setShowStatusModal(false)} className="btn btn-outline">
                  Cancel
                </button>
                <button type="submit" disabled={actionLoading} className="btn btn-primary">
                  {actionLoading ? 'Updating...' : 'Update Status'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 3: Assign Investigator */}
      {showAssignModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.65)',
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
              maxWidth: '480px',
              width: '100%',
              margin: 0,
              backgroundColor: 'var(--bg-card, #1e293b)',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
            }}
          >
            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <UserCheck size={20} />
                Assign Case Investigator ({caseId})
              </h3>
              <button
                onClick={() => setShowAssignModal(false)}
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleAssignSubmit}>
              <div style={{ marginBottom: '16px' }}>
                <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                  Investigator User Email
                </label>
                <input
                  type="email"
                  className="form-control"
                  placeholder="investigator@example.com"
                  value={assignedInvestigatorInput}
                  onChange={(e) => setAssignedInvestigatorInput(e.target.value)}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={() => setShowAssignModal(false)} className="btn btn-outline">
                  Cancel
                </button>
                <button type="submit" disabled={actionLoading} className="btn btn-primary">
                  {actionLoading ? 'Assigning...' : 'Assign Investigator'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CaseDetails;
