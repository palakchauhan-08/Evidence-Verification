import React from 'react';
import { AlertCircle, CheckCircle, Info, X } from 'lucide-react';

const AlertMessage = ({ type = 'info', message, onClose }) => {
  if (!message) return null;

  const typeStyles = {
    success: { bg: 'var(--success-light)', color: 'var(--success)', border: 'rgba(16, 185, 129, 0.4)', icon: CheckCircle },
    danger: { bg: 'var(--danger-light)', color: 'var(--danger)', border: 'rgba(244, 63, 94, 0.4)', icon: AlertCircle },
    warning: { bg: 'var(--warning-light)', color: 'var(--warning)', border: 'rgba(245, 158, 11, 0.4)', icon: AlertCircle },
    info: { bg: 'var(--secondary-light)', color: 'var(--secondary)', border: 'rgba(6, 182, 212, 0.4)', icon: Info },
  };

  const style = typeStyles[type] || typeStyles.info;
  const IconComponent = style.icon;

  return (
    <div
      style={{
        backgroundColor: style.bg,
        color: style.color,
        border: `1px solid ${style.border}`,
        borderRadius: 'var(--radius-sm)',
        padding: '12px 16px',
        marginBottom: '20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '0.9rem',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <IconComponent size={18} />
        <span>{message}</span>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', padding: 0 }}
        >
          <X size={16} />
        </button>
      )}
    </div>
  );
};

export default AlertMessage;
