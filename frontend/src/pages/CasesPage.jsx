import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { caseAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import StatCard from '../components/StatCard';
import CaseStatusBadge from '../components/CaseStatusBadge';
import CasePriorityBadge from '../components/CasePriorityBadge';
import CreateCaseForm from '../components/CreateCaseForm';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import {
  Briefcase,
  FolderOpen,
  Clock,
  PauseCircle,
  CheckCircle2,
  Plus,
  Search,
  Eye,
  Filter
} from 'lucide-react';

const CasesPage = () => {
  const { hasAnyRole } = useAuth();
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Filter States
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');
  const [investigatorFilter, setInvestigatorFilter] = useState('');

  useEffect(() => {
    fetchCases();
  }, [searchTerm, statusFilter, priorityFilter, investigatorFilter]);

  const fetchCases = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (searchTerm.trim()) params.search = searchTerm.trim();
      if (statusFilter) params.status = statusFilter;
      if (priorityFilter) params.priority = priorityFilter;
      if (investigatorFilter.trim()) params.investigator = investigatorFilter.trim();

      const data = await caseAPI.getCases(params);
      setCases(data || []);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to load investigation cases');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return 'N/A';
    try {
      return new Date(isoStr).toLocaleDateString();
    } catch {
      return isoStr;
    }
  };

  const totalCount = cases.length;
  const openCount = cases.filter((c) => (c.status || '').toUpperCase() === 'OPEN').length;
  const inProgressCount = cases.filter((c) => (c.status || '').toUpperCase() === 'IN_PROGRESS').length;
  const onHoldCount = cases.filter((c) => (c.status || '').toUpperCase() === 'ON_HOLD').length;
  const closedCount = cases.filter((c) => (c.status || '').toUpperCase() === 'CLOSED').length;

  return (
    <div>
      {/* Metric Cards Grid */}
      <div className="grid-stats" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))' }}>
        <StatCard
          icon={Briefcase}
          value={loading ? '-' : totalCount}
          label="Total Cases"
          color="var(--primary)"
          bg="var(--primary-light)"
        />
        <StatCard
          icon={FolderOpen}
          value={loading ? '-' : openCount}
          label="Open Cases"
          color="#3b82f6"
          bg="rgba(59, 130, 246, 0.1)"
        />
        <StatCard
          icon={Clock}
          value={loading ? '-' : inProgressCount}
          label="In Progress"
          color="#f59e0b"
          bg="rgba(245, 158, 11, 0.1)"
        />
        <StatCard
          icon={PauseCircle}
          value={loading ? '-' : onHoldCount}
          label="On Hold"
          color="#a855f7"
          bg="rgba(168, 85, 247, 0.1)"
        />
        <StatCard
          icon={CheckCircle2}
          value={loading ? '-' : closedCount}
          label="Closed Cases"
          color="#10b981"
          bg="rgba(16, 185, 129, 0.1)"
        />
      </div>

      <AlertMessage type="danger" message={error} onClose={() => setError('')} />

      {/* Main Cases Card */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Briefcase color="var(--primary)" size={20} />
            Investigation Cases Repository
          </h3>
          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
            <button
              onClick={() => setShowCreateModal(true)}
              className="btn btn-primary"
              style={{ padding: '8px 14px', fontSize: '0.85rem' }}
            >
              <Plus size={16} /> Create Case
            </button>
          )}
        </div>

        {/* Filter Bar */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '12px',
            marginBottom: '20px',
            backgroundColor: 'rgba(255, 255, 255, 0.02)',
            padding: '14px',
            borderRadius: '8px',
            border: '1px solid var(--border-color, rgba(255, 255, 255, 0.08))',
          }}
        >
          <div style={{ position: 'relative' }}>
            <Search size={16} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-control"
              placeholder="Search Case ID or title..."
              style={{ paddingLeft: '36px', fontSize: '0.88rem' }}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div>
            <select
              className="form-control"
              style={{ fontSize: '0.88rem' }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="OPEN">OPEN</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="ON_HOLD">ON_HOLD</option>
              <option value="CLOSED">CLOSED</option>
            </select>
          </div>

          <div>
            <select
              className="form-control"
              style={{ fontSize: '0.88rem' }}
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
            >
              <option value="">All Priorities</option>
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="CRITICAL">CRITICAL</option>
            </select>
          </div>

          <div>
            <input
              type="text"
              className="form-control"
              placeholder="Filter by Investigator..."
              style={{ fontSize: '0.88rem' }}
              value={investigatorFilter}
              onChange={(e) => setInvestigatorFilter(e.target.value)}
            />
          </div>
        </div>

        {loading ? (
          <LoadingSpinner text="Fetching investigation cases..." />
        ) : cases.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 16px', color: 'var(--text-muted)' }}>
            <Briefcase size={48} style={{ opacity: 0.4, marginBottom: '12px' }} />
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>No investigation cases found.</p>
            {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
              <button onClick={() => setShowCreateModal(true)} className="btn btn-primary" style={{ marginTop: '16px' }}>
                Create First Investigation Case
              </button>
            )}
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Case ID</th>
                  <th>Case Title</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Assigned Investigator</th>
                  <th>Created By</th>
                  <th>Evidence</th>
                  <th>Created Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {cases.map((c) => (
                  <tr key={c.id}>
                    <td>
                      <span style={{ fontWeight: '700', color: 'var(--primary)' }}>{c.caseId}</span>
                    </td>
                    <td>
                      <div style={{ fontWeight: '600', maxWidth: '240px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {c.title}
                      </div>
                    </td>
                    <td>
                      <CasePriorityBadge priority={c.priority} />
                    </td>
                    <td>
                      <CaseStatusBadge status={c.status} />
                    </td>
                    <td>
                      <span style={{ fontSize: '0.85rem' }}>{c.assignedInvestigator || 'Unassigned'}</span>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>{c.createdBy}</span>
                    </td>
                    <td>
                      <span className="badge badge-info" style={{ fontSize: '0.75rem' }}>
                        {c.evidenceCount} items
                      </span>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>{formatDate(c.createdAt)}</span>
                    </td>
                    <td>
                      <Link
                        to={`/cases/${c.caseId}`}
                        className="btn btn-outline"
                        style={{ padding: '4px 10px', fontSize: '0.78rem' }}
                      >
                        <Eye size={14} /> View Case
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create Case Form Modal */}
      <CreateCaseForm
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onCaseCreated={() => fetchCases()}
      />
    </div>
  );
};

export default CasesPage;
