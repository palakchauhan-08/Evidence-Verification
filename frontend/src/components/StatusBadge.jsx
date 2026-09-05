import React from 'react';
import {
  CheckCircle2,
  XCircle,
  Clock,
  Database,
  Link2,
  AlertTriangle,
  Search,
  ShieldOff,
  Upload,
  Eye,
  Hash,
  RotateCcw
} from 'lucide-react';

const StatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = status.toUpperCase();

  if (normalized === 'VERIFIED' || normalized === 'CONFIRMED' || normalized === 'EVIDENCE_VERIFIED') {
    return (
      <span className="badge badge-success" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
        <CheckCircle2 size={13} />
        EVIDENCE VERIFIED
      </span>
    );
  }

  if (normalized === 'RE_VERIFICATION_PERFORMED') {
    return (
      <span className="badge badge-success" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}>
        <RotateCcw size={13} />
        RE-VERIFICATION PERFORMED
      </span>
    );
  }

  if (normalized === 'UNDER_REVIEW' || normalized === 'STATUS_CHANGED_TO_UNDER_REVIEW' || normalized === 'REVIEW_STARTED') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '4px 10px',
          borderRadius: '12px',
          fontSize: '0.75rem',
          fontWeight: '700',
          backgroundColor: 'rgba(245, 158, 11, 0.15)',
          color: '#f59e0b',
          border: '1px solid rgba(245, 158, 11, 0.3)',
        }}
      >
        <Search size={13} />
        {normalized === 'REVIEW_STARTED' ? 'REVIEW STARTED' : 'UNDER REVIEW'}
      </span>
    );
  }

  if (normalized === 'UPLOADED' || normalized === 'EVIDENCE_UPLOADED') {
    return (
      <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
        <Upload size={13} />
        EVIDENCE UPLOADED
      </span>
    );
  }

  if (normalized === 'HASH_GENERATED') {
    return (
      <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', backgroundColor: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' }}>
        <Hash size={13} />
        HASH GENERATED
      </span>
    );
  }

  if (normalized === 'BLOCKCHAIN_ANCHORED') {
    return (
      <span className="badge badge-warning" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
        <Link2 size={13} />
        BLOCKCHAIN ANCHORED
      </span>
    );
  }

  if (normalized === 'EVIDENCE_ACCESSED') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '4px 10px',
          borderRadius: '12px',
          fontSize: '0.75rem',
          fontWeight: '600',
          backgroundColor: 'rgba(168, 85, 247, 0.15)',
          color: '#a855f7',
          border: '1px solid rgba(168, 85, 247, 0.3)',
        }}
      >
        <Eye size={13} />
        EVIDENCE ACCESSED
      </span>
    );
  }

  if (normalized === 'REJECTED' || normalized === 'EVIDENCE_REJECTED') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '4px 10px',
          borderRadius: '12px',
          fontSize: '0.75rem',
          fontWeight: '700',
          backgroundColor: 'rgba(107, 114, 128, 0.15)',
          color: '#9ca3af',
          border: '1px solid rgba(107, 114, 128, 0.3)',
        }}
      >
        <ShieldOff size={13} />
        EVIDENCE REJECTED
      </span>
    );
  }

  if (normalized === 'TAMPERED' || normalized === 'EVIDENCE_TAMPERED' || normalized === 'VERIFICATION_FAILED') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '4px 10px',
          borderRadius: '12px',
          fontSize: '0.75rem',
          fontWeight: '800',
          letterSpacing: '0.04em',
          backgroundColor: 'rgba(239, 68, 68, 0.2)',
          color: '#ef4444',
          border: '1px solid rgba(239, 68, 68, 0.5)',
        }}
      >
        <AlertTriangle size={13} />
        {normalized === 'VERIFICATION_FAILED' ? 'VERIFICATION FAILED' : 'TAMPERED'}
      </span>
    );
  }

  return (
    <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
      <Clock size={13} />
      {status}
    </span>
  );
};

export default StatusBadge;
