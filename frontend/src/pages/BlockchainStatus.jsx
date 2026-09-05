import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { evidenceAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import AlertMessage from '../components/AlertMessage';
import StatusBadge from '../components/StatusBadge';
import { Link2, Search, Clock, ArrowLeft, ShieldCheck, ExternalLink } from 'lucide-react';

const BlockchainStatus = () => {
  const { evidenceId: paramEvidenceId } = useParams();
  const [evidenceIdInput, setEvidenceIdInput] = useState(paramEvidenceId || '');
  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (paramEvidenceId) {
      fetchBlockchainRecord(paramEvidenceId);
    }
  }, [paramEvidenceId]);

  const fetchBlockchainRecord = async (idToFetch) => {
    const id = idToFetch || evidenceIdInput;
    if (!id.trim()) {
      setError('Please enter an Evidence ID to query blockchain status');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await evidenceAPI.getBlockchainRecord(id.trim());
      setRecord(data);
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to fetch blockchain record');
      setRecord(null);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (isoStr) => {
    if (!isoStr) return 'N/A';
    try {
      return new Date(isoStr).toLocaleString();
    } catch {
      return isoStr;
    }
  };

  const isRealTx = record?.transactionHash && record.transactionHash.startsWith('0x') && !record.transactionHash.includes('mock');

  return (
    <div>
      <div className="card">
        <div className="card-header">
          <h3 className="card-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Link2 color="var(--primary)" size={20} />
            Polygon Amoy Blockchain Ledger Status
          </h3>
          <span className="badge badge-success" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <ShieldCheck size={13} /> Polygon Amoy Testnet (Chain ID 80002)
          </span>
        </div>

        <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', marginBottom: '20px' }}>
          Decentralized Polygon Amoy smart contract anchoring status for cryptographic evidence hashes.
        </p>

        <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Search size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-control"
              placeholder="Enter Evidence ID (e.g. EVI-8A2F9B1C)..."
              style={{ paddingLeft: '40px' }}
              value={evidenceIdInput}
              onChange={(e) => setEvidenceIdInput(e.target.value)}
            />
          </div>
          <button onClick={() => fetchBlockchainRecord()} className="btn btn-primary">
            Query Blockchain
          </button>
        </div>

        <AlertMessage type="danger" message={error} onClose={() => setError('')} />

        {loading ? (
          <LoadingSpinner text="Querying Polygon Amoy blockchain ledger..." />
        ) : record ? (
          <div style={{ background: 'var(--bg-main)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '24px' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '24px' }}>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Evidence ID</div>
                <div style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary)' }}>{record.evidenceId}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Status</div>
                <StatusBadge status={record.status} />
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Network</div>
                <div style={{ fontSize: '0.9rem', fontWeight: '600', color: 'var(--secondary)' }}>
                  Polygon Amoy Testnet
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Anchoring Timestamp</div>
                <div style={{ fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Clock size={14} color="var(--text-muted)" />
                  {formatDate(record.blockchainTimestamp)}
                </div>
              </div>
            </div>

            <div style={{ marginBottom: '20px' }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Transaction Hash</div>
              {record.transactionHash && record.transactionHash.trim().length > 0 ? (
                <>
                  <div className="hash-code" style={{ color: 'var(--warning)', fontSize: '0.9rem', padding: '10px 14px', wordBreak: 'break-all' }}>
                    {record.transactionHash}
                  </div>
                  <div style={{ marginTop: '10px' }}>
                    <a
                      href={`https://amoy.polygonscan.com/tx/${record.transactionHash.trim()}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-outline"
                      style={{ fontSize: '0.8rem', padding: '6px 12px', display: 'inline-flex', alignItems: 'center', gap: '6px', borderColor: 'var(--primary)', color: 'var(--primary)' }}
                    >
                      <ExternalLink size={14} /> View on Polygon Explorer ↗
                    </a>
                  </div>
                </>
              ) : (
                <div style={{ fontSize: '0.88rem', color: 'var(--text-muted)', fontStyle: 'italic', padding: '6px 0' }}>
                  Blockchain transaction unavailable
                </div>
              )}
            </div>

            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '6px' }}>Anchored File SHA-256 Hash</div>
              <div className="hash-code" style={{ padding: '10px 14px', fontSize: '0.9rem' }}>
                {record.fileHash}
              </div>
            </div>
          </div>
        ) : !error ? (
          <div style={{ textAlign: 'center', padding: '32px 16px', color: 'var(--text-muted)' }}>
            <Link2 size={40} style={{ opacity: 0.4, marginBottom: '12px' }} />
            <p>Enter an Evidence ID above to view its Polygon Amoy blockchain transaction commitment details.</p>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default BlockchainStatus;
