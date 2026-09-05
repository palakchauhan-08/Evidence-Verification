import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import { Search, FolderLock, Eye, FileCheck2, Plus } from 'lucide-react';

const MyEvidence = () => {
  const { userRole, hasAnyRole } = useAuth();
  const [evidenceList, setEvidenceList] = useState([]);
  const [filteredList, setFilteredList] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchEvidence();
  }, []);

  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredList(evidenceList);
    } else {
      const term = searchTerm.toLowerCase();
      setFilteredList(
        evidenceList.filter(
          (item) =>
            item.evidenceId?.toLowerCase().includes(term) ||
            item.fileName?.toLowerCase().includes(term) ||
            item.fileHash?.toLowerCase().includes(term) ||
            item.uploadedBy?.toLowerCase().includes(term) ||
            item.status?.toLowerCase().includes(term)
        )
      );
    }
  }, [searchTerm, evidenceList]);

  const fetchEvidence = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await evidenceAPI.getUserEvidence();
      setEvidenceList(data || []);
      setFilteredList(data || []);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to load evidence records');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">
            {userRole === 'INVESTIGATOR' ? 'My Evidence Repository' : 'Digital Evidence Repository'}
          </h3>
          {hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
            <Link to="/evidence/upload" className="btn btn-primary" style={{ padding: '8px 14px', fontSize: '0.85rem' }}>
              <Plus size={16} /> Upload Evidence
            </Link>
          )}
        </div>

        {/* Search filter */}
        <div style={{ marginBottom: '20px', position: 'relative' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-muted)' }} />
          <input
            type="text"
            className="form-control"
            placeholder="Search by Evidence ID, file name, status, SHA-256 hash, or uploader..."
            style={{ paddingLeft: '40px' }}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <AlertMessage type="danger" message={error} onClose={() => setError('')} />

        {loading ? (
          <LoadingSpinner text="Loading evidence list..." />
        ) : filteredList.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 16px', color: 'var(--text-muted)' }}>
            <FolderLock size={48} style={{ opacity: 0.4, marginBottom: '12px' }} />
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>
              {searchTerm ? 'No matching evidence records found' : 'No evidence records available in repository'}
            </p>
            {!searchTerm && hasAnyRole(['ADMIN', 'INVESTIGATOR']) && (
              <Link to="/evidence/upload" className="btn btn-primary" style={{ marginTop: '16px' }}>
                Upload First Evidence
              </Link>
            )}
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Evidence ID</th>
                  <th>File Name</th>
                  <th>Status</th>
                  <th>SHA-256 Hash</th>
                  <th>Uploaded By</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredList.map((item) => (
                  <tr key={item.id}>
                    <td>
                      <span style={{ fontWeight: '700', color: 'var(--primary)' }}>{item.evidenceId}</span>
                    </td>
                    <td>
                      <div style={{ fontWeight: '500' }}>{item.fileName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{item.fileType || 'binary'}</div>
                    </td>
                    <td>
                      <StatusBadge status={item.status || 'UPLOADED'} />
                    </td>
                    <td>
                      <span className="hash-code">
                        {item.fileHash ? `${item.fileHash.substring(0, 12)}...${item.fileHash.substring(item.fileHash.length - 8)}` : 'N/A'}
                      </span>
                    </td>
                    <td>{item.uploadedBy}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <Link
                          to={`/evidence/${item.evidenceId}`}
                          className="btn btn-outline"
                          style={{ padding: '4px 10px', fontSize: '0.78rem' }}
                          title="View Metadata & Audit Trail"
                        >
                          <Eye size={14} /> Details
                        </Link>
                        {hasAnyRole(['ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST']) && (
                          <Link
                            to="/evidence/verify"
                            className="btn btn-secondary"
                            style={{ padding: '4px 10px', fontSize: '0.78rem' }}
                            title="Verify File Integrity"
                          >
                            <FileCheck2 size={14} /> Verify
                          </Link>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyEvidence;
