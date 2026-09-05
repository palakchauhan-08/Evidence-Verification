import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, User, ShieldCheck } from 'lucide-react';

const Navbar = ({ title = 'Evidence Verification System' }) => {
  const { userEmail, userRole, logout } = useAuth();

  const getRoleBadgeStyle = (role) => {
    switch (role) {
      case 'ADMIN':
        return { backgroundColor: 'rgba(239, 68, 68, 0.15)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.3)' };
      case 'INVESTIGATOR':
        return { backgroundColor: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.3)' };
      case 'FORENSIC_ANALYST':
        return { backgroundColor: 'rgba(168, 85, 247, 0.15)', color: '#a855f7', border: '1px solid rgba(168, 85, 247, 0.3)' };
      case 'VIEWER':
        return { backgroundColor: 'rgba(107, 114, 128, 0.15)', color: '#9ca3af', border: '1px solid rgba(107, 114, 128, 0.3)' };
      default:
        return { backgroundColor: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6', border: '1px solid rgba(59, 130, 246, 0.3)' };
    }
  };

  return (
    <header className="header">
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <h2 className="header-title">{title}</h2>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
          <User size={16} color="var(--primary)" />
          <span style={{ fontWeight: '500' }}>{userEmail || 'Authenticated User'}</span>
          {userRole && (
            <span
              style={{
                display: 'inline-block',
                padding: '2px 8px',
                borderRadius: '10px',
                fontSize: '0.72rem',
                fontWeight: '700',
                letterSpacing: '0.04em',
                ...getRoleBadgeStyle(userRole),
              }}
            >
              {userRole}
            </span>
          )}
        </div>

        <button
          onClick={logout}
          className="btn btn-outline"
          style={{ padding: '6px 12px', fontSize: '0.82rem' }}
          title="Logout"
        >
          <LogOut size={14} />
          <span>Logout</span>
        </button>
      </div>
    </header>
  );
};

export default Navbar;
