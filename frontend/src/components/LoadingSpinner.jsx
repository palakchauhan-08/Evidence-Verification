import React from 'react';

const LoadingSpinner = ({ text = 'Loading...' }) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px' }}>
      <div className="spinner" style={{ width: '32px', height: '32px', borderWidth: '3px', marginBottom: '12px' }}></div>
      <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{text}</span>
    </div>
  );
};

export default LoadingSpinner;
