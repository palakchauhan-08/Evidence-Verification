import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

const Layout = () => {
  const location = useLocation();

  const getPageTitle = (path) => {
    if (path === '/dashboard') return 'Dashboard Overview';
    if (path === '/evidence/upload') return 'Upload Digital Evidence';
    if (path === '/evidence') return 'Evidence Repository';
    if (path === '/evidence/verify') return 'Evidence Integrity Verification';
    if (path.startsWith('/evidence/')) return 'Evidence Metadata & Audit Trail';
    return 'Digital Evidence Verification System';
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Navbar title={getPageTitle(location.pathname)} />
        <main className="content-body">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;
