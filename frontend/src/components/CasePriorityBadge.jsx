import React from 'react';
import { AlertTriangle, Shield, ArrowDown, ArrowUp } from 'lucide-react';

const CasePriorityBadge = ({ priority }) => {
  if (!priority) return null;

  const normalized = priority.toUpperCase();

  if (normalized === 'LOW') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '3px 8px',
          borderRadius: '10px',
          fontSize: '0.72rem',
          fontWeight: '600',
          backgroundColor: 'rgba(107, 114, 128, 0.15)',
          color: '#9ca3af',
          border: '1px solid rgba(107, 114, 128, 0.3)',
        }}
      >
        <ArrowDown size={12} />
        LOW
      </span>
    );
  }

  if (normalized === 'MEDIUM') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '3px 8px',
          borderRadius: '10px',
          fontSize: '0.72rem',
          fontWeight: '600',
          backgroundColor: 'rgba(59, 130, 246, 0.15)',
          color: '#3b82f6',
          border: '1px solid rgba(59, 130, 246, 0.3)',
        }}
      >
        <Shield size={12} />
        MEDIUM
      </span>
    );
  }

  if (normalized === 'HIGH') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '3px 8px',
          borderRadius: '10px',
          fontSize: '0.72rem',
          fontWeight: '700',
          backgroundColor: 'rgba(245, 158, 11, 0.15)',
          color: '#f59e0b',
          border: '1px solid rgba(245, 158, 11, 0.3)',
        }}
      >
        <ArrowUp size={12} />
        HIGH
      </span>
    );
  }

  if (normalized === 'CRITICAL') {
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '3px 8px',
          borderRadius: '10px',
          fontSize: '0.72rem',
          fontWeight: '800',
          letterSpacing: '0.03em',
          backgroundColor: 'rgba(239, 68, 68, 0.2)',
          color: '#ef4444',
          border: '1px solid rgba(239, 68, 68, 0.5)',
        }}
      >
        <AlertTriangle size={12} />
        CRITICAL
      </span>
    );
  }

  return (
    <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
      {priority}
    </span>
  );
};

export default CasePriorityBadge;
