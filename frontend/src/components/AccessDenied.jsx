import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';

const AccessDenied = () => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        textAlign: 'center',
        padding: '2rem',
      }}
    >
      <div
        style={{
          width: '72px',
          height: '72px',
          borderRadius: '50%',
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          color: 'var(--danger, #ef4444)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1.5rem',
        }}
      >
        <ShieldAlert size={38} />
      </div>

      <h1
        style={{
          fontSize: '3rem',
          fontWeight: '800',
          color: 'var(--text-main)',
          margin: '0 0 0.5rem 0',
          letterSpacing: '-0.02em',
        }}
      >
        403
      </h1>

      <h2
        style={{
          fontSize: '1.5rem',
          fontWeight: '600',
          color: 'var(--text-main)',
          margin: '0 0 1rem 0',
        }}
      >
        Access Denied
      </h2>

      <p
        style={{
          color: 'var(--text-muted)',
          maxWidth: '420px',
          marginBottom: '2rem',
          lineHeight: '1.5',
        }}
      >
        You don't have permission to access this resource. If you believe this is an error, please contact your system administrator.
      </p>

      <Link
        to="/dashboard"
        className="btn btn-primary"
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '8px',
          padding: '10px 20px',
        }}
      >
        <ArrowLeft size={18} />
        Return to Dashboard
      </Link>
    </div>
  );
};

export default AccessDenied;
