import React, { useState } from 'react';
import { caseAPI } from '../services/api';
import AlertMessage from '../components/AlertMessage';
import { Briefcase, X } from 'lucide-react';

const CreateCaseForm = ({ isOpen, onClose, onCaseCreated }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('MEDIUM');
  const [assignedInvestigator, setAssignedInvestigator] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Please provide a case title');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const newCase = await caseAPI.createCase(
        title.trim(),
        description.trim(),
        priority,
        assignedInvestigator.trim()
      );
      if (onCaseCreated) {
        onCaseCreated(newCase);
      }
      onClose();
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to create investigation case');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.65)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
        padding: '20px',
      }}
    >
      <div
        className="card"
        style={{
          maxWidth: '540px',
          width: '100%',
          margin: 0,
          backgroundColor: 'var(--bg-card, #1e293b)',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5)',
        }}
      >
        <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary)' }}>
            <Briefcase size={20} />
            Create Investigation Case
          </h3>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <AlertMessage type="danger" message={error} onClose={() => setError('')} />

          <div style={{ marginBottom: '16px' }}>
            <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
              Case Title / Subject (Required)
            </label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Corporate Data Leak & Fraud Investigation"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div style={{ marginBottom: '16px' }}>
            <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
              Description / Investigation Summary
            </label>
            <textarea
              className="form-control"
              rows={3}
              placeholder="Detailed description of the incident, scope of examination, and target systems..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
            <div>
              <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                Case Priority
              </label>
              <select
                className="form-control"
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
              >
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>
            </div>

            <div>
              <label className="form-label" style={{ marginBottom: '6px', display: 'block', fontSize: '0.88rem' }}>
                Assigned Investigator (Optional)
              </label>
              <input
                type="email"
                className="form-control"
                placeholder="investigator@example.com"
                value={assignedInvestigator}
                onChange={(e) => setAssignedInvestigator(e.target.value)}
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <button type="button" onClick={onClose} className="btn btn-outline">
              Cancel
            </button>
            <button type="submit" disabled={loading || !title.trim()} className="btn btn-primary">
              {loading ? 'Creating...' : 'Create Case'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateCaseForm;
