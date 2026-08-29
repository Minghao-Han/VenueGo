import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from '../components/Toast.jsx';

export default function AuthPage() {
  const { login, isAuthed, claims, logout, token } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('login');
  const [busy, setBusy] = useState(false);
  const [regForm, setRegForm] = useState({ username: '', email: '', password: '' });
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });

  async function handleRegister(e) {
    e.preventDefault();
    setBusy(true);
    try {
      await api.register(regForm);
      showToast('注册成功，请登录', 'success');
      setRegForm({ username: '', email: '', password: '' });
      setTab('login');
      setLoginForm({ username: regForm.username, password: '' });
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setBusy(false);
    }
  }

  async function handleLogin(e) {
    e.preventDefault();
    setBusy(true);
    try {
      const res = await api.login(loginForm);
      login(res.token || '');
      showToast('登录成功', 'success');
      navigate('/');
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setBusy(false);
    }
  }

  async function handleLogout() {
    try { await api.logout(token); } catch { /* ignore */ }
    logout();
    showToast('已退出登录', 'info');
  }

  if (isAuthed) {
    return (
      <div className="page auth-page">
        <div className="auth-card">
          <div className="auth-avatar">{(claims?.username || claims?.email || 'U')[0].toUpperCase()}</div>
          <h2>你好，{claims?.username || claims?.email}</h2>
          <p className="muted">当前持有有效的 JWT 令牌</p>
          <div className="auth-info">
            <div className="info-row"><span>用户 ID</span><code>{claims?.sub || '-'}</code></div>
            <div className="info-row"><span>邮箱</span><code>{claims?.email || '-'}</code></div>
          </div>
          <button type="button" className="btn-ghost full-width" onClick={handleLogout}>退出登录</button>
          <button type="button" className="btn-primary full-width mt-8" onClick={() => navigate('/')}>
            浏览活动
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page auth-page">
      <div className="auth-card">
        <div className="auth-brand">VenueGo</div>
        <div className="auth-tabs">
          <button
            type="button"
            className={`auth-tab${tab === 'login' ? ' active' : ''}`}
            onClick={() => setTab('login')}
          >
            登录
          </button>
          <button
            type="button"
            className={`auth-tab${tab === 'register' ? ' active' : ''}`}
            onClick={() => setTab('register')}
          >
            注册
          </button>
        </div>

        {tab === 'login' ? (
          <form className="auth-form" onSubmit={handleLogin}>
            <div className="field">
              <label>用户名</label>
              <input
                value={loginForm.username}
                onChange={(e) => setLoginForm((f) => ({ ...f, username: e.target.value }))}
                placeholder="输入用户名"
                required
                autoFocus
              />
            </div>
            <div className="field">
              <label>密码</label>
              <input
                type="password"
                value={loginForm.password}
                onChange={(e) => setLoginForm((f) => ({ ...f, password: e.target.value }))}
                placeholder="输入密码"
                required
              />
            </div>
            <button type="submit" className="btn-primary full-width" disabled={busy}>
              {busy ? '登录中…' : '登录'}
            </button>
          </form>
        ) : (
          <form className="auth-form" onSubmit={handleRegister}>
            <div className="field">
              <label>用户名</label>
              <input
                value={regForm.username}
                onChange={(e) => setRegForm((f) => ({ ...f, username: e.target.value }))}
                placeholder="设置用户名"
                required
                autoFocus
              />
            </div>
            <div className="field">
              <label>邮箱</label>
              <input
                type="email"
                value={regForm.email}
                onChange={(e) => setRegForm((f) => ({ ...f, email: e.target.value }))}
                placeholder="邮箱地址"
                required
              />
            </div>
            <div className="field">
              <label>密码</label>
              <input
                type="password"
                value={regForm.password}
                onChange={(e) => setRegForm((f) => ({ ...f, password: e.target.value }))}
                placeholder="设置密码"
                required
              />
            </div>
            <button type="submit" className="btn-primary full-width" disabled={busy}>
              {busy ? '注册中…' : '注册'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
