import React from 'react';
import { CheckCircle2, XCircle, Clock, ShieldCheck, Database, Link2 } from 'lucide-react';

const StatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = status.toUpperCase();

  if (normalized === 'VERIFIED' || normalized === 'CONFIRMED' || normalized === 'EVIDENCE_VERIFIED') {
    return (
      <span className="badge badge-success">
        <CheckCircle2 size={13} />
        {status}
      </span>
    );
  }

  if (normalized === 'NOT VERIFIED' || normalized === 'FAILED' || normalized === 'EVIDENCE_VERIFICATION_FAILED') {
    return (
      <span className="badge badge-danger">
        <XCircle size={13} />
        {status}
      </span>
    );
  }

  if (normalized === 'EVIDENCE_UPLOADED') {
    return (
      <span className="badge badge-info">
        <Database size={13} />
        {status}
      </span>
    );
  }

  if (normalized === 'BLOCKCHAIN_ANCHORED') {
    return (
      <span className="badge badge-warning">
        <Link2 size={13} />
        {status}
      </span>
    );
  }

  return (
    <span className="badge badge-info">
      <Clock size={13} />
      {status}
    </span>
  );
};

export default StatusBadge;
