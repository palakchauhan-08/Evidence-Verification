import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Upload,
  FileCheck2,
  ShieldCheck,
  LogOut,
  FolderLock
} from 'lucide-react';

const Sidebar = () => {
  const { userEmail, logout } = useAuth();

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/evidence/upload', label: 'Upload Evidence', icon: Upload },
    { path: '/evidence', label: 'My Evidence', icon: FolderLock },
    { path: '/evidence/verify', label: 'Verify Evidence', icon: FileCheck2 },
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="brand-icon">
          <ShieldCheck size={22} />
        </div>
        <div>
          <div className="brand-title">EvidenceVerify</div>
          <div className="brand-subtitle">Blockchain Ledger</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              <Icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="user-info">
          <div className="user-avatar">
            {userEmail ? userEmail.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="user-details">
            <div className="user-name">{userEmail || 'User'}</div>
            <div className="user-email">Active Session</div>
          </div>
        </div>

        <button onClick={logout} className="btn btn-outline btn-block" style={{ fontSize: '0.85rem' }}>
          <LogOut size={16} />
          Sign Out
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
