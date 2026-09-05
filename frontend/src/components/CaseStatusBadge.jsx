import React from 'react';
import { FolderOpen, Clock, PauseCircle, CheckCircle2 } from 'lucide-react';

const CaseStatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = status.toUpperCase();

  if (normalized === 'OPEN') {
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
          backgroundColor: 'rgba(59, 130, 246, 0.15)',
          color: '#3b82f6',
          border: '1px solid rgba(59, 130, 246, 0.3)',
        }}
      >
        <FolderOpen size={13} />
        OPEN
      </span>
    );
  }

  if (normalized === 'IN_PROGRESS') {
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
        <Clock size={13} />
        IN PROGRESS
      </span>
    );
  }

  if (normalized === 'ON_HOLD') {
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
          backgroundColor: 'rgba(168, 85, 247, 0.15)',
          color: '#a855f7',
          border: '1px solid rgba(168, 85, 247, 0.3)',
        }}
      >
        <PauseCircle size={13} />
        ON HOLD
      </span>
    );
  }

  if (normalized === 'CLOSED') {
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
          backgroundColor: 'rgba(16, 185, 129, 0.15)',
          color: '#10b981',
          border: '1px solid rgba(16, 185, 129, 0.3)',
        }}
      >
        <CheckCircle2 size={13} />
        CLOSED
      </span>
    );
  }

  return (
    <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
      {status}
    </span>
  );
};

export default CaseStatusBadge;
