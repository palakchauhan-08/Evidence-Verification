import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Briefcase,
  Upload,
  FileCheck2,
  ShieldCheck,
  LogOut,
  FolderLock,
  History,
  Blocks,
  UserCog,
} from 'lucide-react';

const Sidebar = () => {
  const { userEmail, userRole, logout, hasAnyRole } = useAuth();

  const allNavItems = [
    {
      path: '/dashboard',
      label: 'Dashboard',
      icon: LayoutDashboard,
      roles: ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER'],
    },
    {
      path: '/cases',
      label: 'Cases',
      icon: Briefcase,
      roles: ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER'],
    },
    {
      path: '/evidence/upload',
      label: 'Upload Evidence',
      icon: Upload,
      roles: ['ADMIN', 'INVESTIGATOR'],
    },
    {
      path: '/evidence',
      label: userRole === 'INVESTIGATOR' ? 'My Evidence' : 'All Evidence',
      icon: FolderLock,
      roles: ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER'],
    },
    {
      path: '/evidence/verify',
      label: 'Verify Evidence',
      icon: FileCheck2,
      roles: ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST'],
    },
    {
      path: '/audit-trail',
      label: 'Audit Trail',
      icon: History,
      roles: ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER'],
    },
    {
      path: '/blockchain',
      label: 'Blockchain Status',
      icon: Blocks,
      roles: ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER'],
    },
    {
      path: '/admin/users',
      label: 'User Management',
      icon: UserCog,
      roles: ['ADMIN'],
    },
  ];

  const navItems = allNavItems.filter((item) => hasAnyRole(item.roles));

  const getRoleColor = (role) => {
    switch (role) {
      case 'ADMIN':
        return '#ef4444';
      case 'INVESTIGATOR':
        return '#3b82f6';
      case 'FORENSIC_ANALYST':
        return '#a855f7';
      case 'VIEWER':
        return '#6b7280';
      default:
        return '#3b82f6';
    }
  };

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
          <div className="user-avatar" style={{ border: `2px solid ${getRoleColor(userRole)}` }}>
            {userEmail ? userEmail.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="user-details" style={{ overflow: 'hidden' }}>
            <div className="user-name" title={userEmail || 'User'} style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
              {userEmail || 'User'}
            </div>
            <div
              className="user-role"
              style={{
                fontSize: '0.72rem',
                fontWeight: '700',
                color: getRoleColor(userRole),
                letterSpacing: '0.05em',
                textTransform: 'uppercase',
              }}
            >
              {userRole || 'INVESTIGATOR'}
            </div>
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
