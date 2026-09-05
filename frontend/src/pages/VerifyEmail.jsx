import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { authAPI } from '../services/api';
import AlertMessage from '../components/AlertMessage';
import { ShieldCheck, CheckCircle, AlertTriangle, Send } from 'lucide-react';

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [verifying, setVerifying] = useState(true);
  const [status, setStatus] = useState('pending'); // 'pending' | 'success' | 'error'
  const [message, setMessage] = useState('');

  const [resendEmail, setResendEmail] = useState('');
  const [resendMessage, setResendMessage] = useState('');
  const [resendError, setResendError] = useState('');
  const [resendLoading, setResendLoading] = useState(false);

  useEffect(() => {
    if (!token) {
      setVerifying(false);
      setStatus('error');
      setMessage('Invalid verification link. No verification token was provided.');
      return;
    }

    const doVerify = async () => {
      try {
        const response = await authAPI.verifyEmail(token);
        setStatus('success');
        setMessage(response.message || 'Email verified successfully. You can now login.');
      } catch (err) {
        setStatus('error');
        const errMsg = err.response?.data?.message || err.message || 'Verification failed.';
        setMessage(errMsg);
      } finally {
        setVerifying(false);
      }
    };

    doVerify();
  }, [token]);

  const handleResend = async (e) => {
    e.preventDefault();
    setResendError('');
    setResendMessage('');

    if (!resendEmail) {
      setResendError('Please enter your email address');
      return;
    }

    setResendLoading(true);
    try {
      const response = await authAPI.resendVerification(resendEmail);
      setResendMessage(response.message || 'Verification email resent successfully. Please check your inbox.');
    } catch (err) {
      const errMsg = err.response?.data?.message || err.message || 'Failed to resend verification email.';
      setResendError(errMsg);
    } finally {
      setResendLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">
            <ShieldCheck size={28} />
          </div>
          <h2>Email Verification</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', marginTop: '6px' }}>
            Blockchain Digital Evidence Verification System
          </p>
        </div>

        {verifying && (
          <div style={{ textAlign: 'center', padding: '24px 0' }}>
            <div className="spinner" style={{ margin: '0 auto 16px auto', width: '32px', height: '32px' }}></div>
            <p style={{ color: 'var(--text-muted)' }}>Verifying your email address...</p>
          </div>
        )}

        {!verifying && status === 'success' && (
          <div style={{ textAlign: 'center' }}>
            <div style={{ color: 'var(--success, #10b981)', marginBottom: '16px' }}>
              <CheckCircle size={56} style={{ margin: '0 auto' }} />
            </div>
            <AlertMessage type="success" message={message} />
            <Link to="/login" className="btn btn-primary btn-block" style={{ marginTop: '20px', textDecoration: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              Proceed to Sign In
            </Link>
          </div>
        )}

        {!verifying && status === 'error' && (
          <div>
            <div style={{ color: 'var(--danger, #ef4444)', textAlign: 'center', marginBottom: '16px' }}>
              <AlertTriangle size={56} style={{ margin: '0 auto' }} />
            </div>
            <AlertMessage type="danger" message={message} />

            <div style={{ marginTop: '24px', paddingTop: '20px', borderTop: '1px solid var(--border-color, #e5e7eb)' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: '600', marginBottom: '8px' }}>Need a new verification link?</h4>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '16px' }}>
                Enter your email address below to receive a new verification email.
              </p>

              <AlertMessage type="danger" message={resendError} onClose={() => setResendError('')} />
              <AlertMessage type="success" message={resendMessage} />

              <form onSubmit={handleResend}>
                <div className="form-group">
                  <label className="form-label">Email Address</label>
                  <input
                    type="email"
                    className="form-control"
                    placeholder="officer@police.gov"
                    value={resendEmail}
                    onChange={(e) => setResendEmail(e.target.value)}
                    required
                  />
                </div>
                <button type="submit" className="btn btn-secondary btn-block" disabled={resendLoading} style={{ marginTop: '12px' }}>
                  {resendLoading ? (
                    <div className="spinner"></div>
                  ) : (
                    <>
                      <Send size={16} />
                      <span>Resend Verification Email</span>
                    </>
                  )}
                </button>
              </form>
            </div>

            <div style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.88rem' }}>
              <Link to="/login" style={{ fontWeight: '600', color: 'var(--primary)' }}>
                Back to Sign In
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VerifyEmail;
