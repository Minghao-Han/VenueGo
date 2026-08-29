import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from '../components/Toast.jsx';

export default function CheckinPage() {
  const { token, isAuthed } = useAuth();
  const navigate = useNavigate();
  const [ticketCode, setTicketCode] = useState('');
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const inputRef = useRef(null);
  const { t } = useTranslation();

  useEffect(() => {
    if (!isAuthed) { navigate('/auth'); return; }
    inputRef.current?.focus();
  }, [isAuthed, navigate]);

  async function handleScan(e) {
    e.preventDefault();
    if (!ticketCode.trim()) { showToast('请输入核销码', 'error'); return; }
    setBusy(true);
    setResult(null);
    try {
      const res = await api.scanTicket(ticketCode.trim(), token);
      setResult(res);
      if (res?.success) {
        showToast('核销成功 ✓', 'success');
        setTicketCode('');
      } else {
        showToast('核销失败', 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
      setResult({ success: false, message: err.message });
    } finally {
      setBusy(false);
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('checkin')}</h1>
        <p className="muted">{t('checkin_desc')}</p>
      </div>

      <div className="checkin-layout">
        <div className="card checkin-card">
          <div className="checkin-icon">⊘</div>
          <h2>扫码 / 输入核销码</h2>
          <p className="muted">将光标置于此处，使用扫码枪扫描二维码，或手动输入核销码。</p>

          <form onSubmit={handleScan} className="checkin-form">
            <input
              ref={inputRef}
              value={ticketCode}
              onChange={(e) => setTicketCode(e.target.value)}
              placeholder="核销码 / 二维码解析内容"
              className="checkin-input"
              disabled={busy}
            />
            <button type="submit" className="btn-primary checkin-btn" disabled={busy}>
              {busy ? '核销中…' : '执行核销'}
            </button>
          </form>
        </div>

        {result && (
          <div className={`card checkin-result ${result.success ? 'result-ok' : 'result-fail'}`}>
            <div className="result-icon">{result.success ? '✓' : '✕'}</div>
            <h3>{result.success ? '核销成功' : '核销失败'}</h3>
            <pre className="result-json">{JSON.stringify(result, null, 2)}</pre>
          </div>
        )}
      </div>
    </div>
  );
}
