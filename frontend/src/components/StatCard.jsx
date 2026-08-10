import React from 'react';

const StatCard = ({ icon: Icon, value, label, color = 'var(--primary)', bg = 'var(--primary-light)' }) => {
  return (
    <div className="stat-card">
      <div className="stat-icon" style={{ backgroundColor: bg, color: color }}>
        <Icon size={24} />
      </div>
      <div>
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
      </div>
    </div>
  );
};

export default StatCard;
