import React, { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';
import AlertMessage from '../components/AlertMessage';
import LoadingSpinner from '../components/LoadingSpinner';
import { Users, ShieldCheck, UserCog, CheckCircle, XCircle } from 'lucide-react';

const ROLES = ['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER'];

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState({});
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminAPI.getUsers();
      setUsers(data);
      const roleMap = {};
      data.forEach((u) => {
        roleMap[u.id] = u.role;
      });
      setSelectedRoles(roleMap);
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.message || err.message || 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = (userId, newRole) => {
    setSelectedRoles((prev) => ({
      ...prev,
      [userId]: newRole,
    }));
  };

  const handleUpdateRole = async (user) => {
    const newRole = selectedRoles[user.id];
    if (!newRole || newRole === user.role) return;

    setUpdatingId(user.id);
    setMessage(null);
    setError(null);

    try {
      const updatedUser = await adminAPI.updateUserRole(user.id, newRole);
      setMessage(`Role for ${user.email} updated to ${updatedUser.role} successfully.`);
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, role: updatedUser.role } : u))
      );
    } catch (err) {
      setError(
        err.response?.data?.error ||
          err.response?.data?.message ||
          err.message ||
          'Failed to update user role'
      );
      // Reset selected role back to current user role
      setSelectedRoles((prev) => ({
        ...prev,
        [user.id]: user.role,
      }));
    } finally {
      setUpdatingId(null);
    }
  };

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
        return { backgroundColor: 'rgba(107, 114, 128, 0.15)', color: '#9ca3af', border: '1px solid rgba(107, 114, 128, 0.3)' };
    }
  };

  if (loading) {
    return <LoadingSpinner text="Loading users list..." />;
  }

  return (
    <div className="page-container">
      <div className="page-header" style={{ marginBottom: '1.5rem' }}>
        <div>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <UserCog size={28} color="var(--primary)" />
            User Management & Access Control
          </h1>
          <p className="page-subtitle">
            Administrative panel to view system users and assign role-based authorization permissions.
          </p>
        </div>
      </div>

      <AlertMessage message={message} type="success" onClose={() => setMessage(null)} />
      <AlertMessage message={error} type="error" onClose={() => setError(null)} />

      <div className="card">
        <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Users size={18} />
            System Accounts ({users.length})
          </h3>
        </div>

        <div className="table-responsive" style={{ overflowX: 'auto' }}>
          <table className="table" style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--border)' }}>
                <th style={{ padding: '12px' }}>ID</th>
                <th style={{ padding: '12px' }}>Name</th>
                <th style={{ padding: '12px' }}>Email</th>
                <th style={{ padding: '12px' }}>Current Role</th>
                <th style={{ padding: '12px' }}>Email Verified</th>
                <th style={{ padding: '12px' }}>Modify Role</th>
                <th style={{ padding: '12px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => {
                const isSelectedRoleChanged = selectedRoles[u.id] && selectedRoles[u.id] !== u.role;
                const isUpdating = updatingId === u.id;

                return (
                  <tr key={u.id} style={{ borderBottom: '1px solid var(--border-light)' }}>
                    <td style={{ padding: '12px', fontWeight: '600' }}>#{u.id}</td>
                    <td style={{ padding: '12px' }}>{u.name || 'N/A'}</td>
                    <td style={{ padding: '12px' }}>{u.email}</td>
                    <td style={{ padding: '12px' }}>
                      <span
                        style={{
                          display: 'inline-block',
                          padding: '4px 10px',
                          borderRadius: '12px',
                          fontSize: '0.75rem',
                          fontWeight: '700',
                          letterSpacing: '0.04em',
                          ...getRoleBadgeStyle(u.role),
                        }}
                      >
                        {u.role}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {u.emailVerified ? (
                        <span style={{ color: 'var(--success, #10b981)', display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '0.85rem' }}>
                          <CheckCircle size={15} /> Verified
                        </span>
                      ) : (
                        <span style={{ color: 'var(--warning, #f59e0b)', display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '0.85rem' }}>
                          <XCircle size={15} /> Pending
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <select
                        value={selectedRoles[u.id] || u.role}
                        onChange={(e) => handleRoleChange(u.id, e.target.value)}
                        className="form-input"
                        style={{ padding: '6px 10px', fontSize: '0.85rem', maxWidth: '180px' }}
                      >
                        {ROLES.map((r) => (
                          <option key={r} value={r}>
                            {r}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <button
                        onClick={() => handleUpdateRole(u)}
                        disabled={!isSelectedRoleChanged || isUpdating}
                        className="btn btn-primary"
                        style={{ padding: '6px 14px', fontSize: '0.82rem', opacity: !isSelectedRoleChanged || isUpdating ? 0.6 : 1 }}
                      >
                        {isUpdating ? 'Saving...' : 'Update Role'}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default UserManagement;
