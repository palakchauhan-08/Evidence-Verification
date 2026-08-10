import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, User, ShieldCheck } from 'lucide-react';

const Navbar = ({ title = 'Evidence Verification System' }) => {
  const { userEmail, logout } = useAuth();

  return (
    <header className="header">
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <h2 className="header-title">{title}</h2>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.88rem', color: 'var(--text-muted)' }}>
          <User size={16} color="var(--primary)" />
          <span>{userEmail || 'Authenticated User'}</span>
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
