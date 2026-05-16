import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from './Toast.jsx';
import { useTranslation } from 'react-i18next';

const NAV_ITEMS = [
  { to: '/', labelKey: 'discover', icon: '✦' },
  { to: '/orders', labelKey: 'orders', icon: '◈', auth: true },
  { to: '/profile', labelKey: 'profile', icon: '◉', auth: true },
  { to: '/publish', labelKey: 'publish', icon: '⊕', auth: true },
  { to: '/checkin', labelKey: 'checkin', icon: '⊘', auth: true },
];

export default function Navbar() {
  const { token, claims, logout, isAuthed } = useAuth();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();

  async function handleLogout() {
    try { await api.logout(token); } catch { /* ignore */ }
    logout();
    showToast('已退出登录', 'info');
    navigate('/auth');
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-icon">V</span>
        <span className="brand-name">VenueGo</span>
      </div>

      {/* Language Switcher */}
      <div style={{ textAlign: 'center', margin: '8px 0' }}>
        <span>{t('language')}: </span>
        <button onClick={() => i18n.changeLanguage('en')} style={{ marginRight: 4 }}>{t('english')}</button>
        <button onClick={() => i18n.changeLanguage('zh')}>{t('chinese')}</button>
      </div>

      <nav className="sidebar-nav">
        {NAV_ITEMS.map(({ to, labelKey, icon, auth }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `nav-item${isActive ? ' active' : ''}${auth && !isAuthed ? ' nav-locked' : ''}`}
            onClick={(e) => {
              if (auth && !isAuthed) {
                e.preventDefault();
                showToast(t('login'), 'error');
                navigate('/auth');
              }
            }}
          >
            <span className="nav-icon">{icon}</span>
            <span className="nav-label">{t(labelKey)}</span>
            {auth && !isAuthed && <span className="nav-lock">🔒</span>}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        {isAuthed ? (
          <>
            <div className="user-pill">
              <div className="user-avatar">{(claims?.username || claims?.email || 'U')[0].toUpperCase()}</div>
              <div className="user-info">
                <span className="user-name">{claims?.username || claims?.email || t('profile')}</span>
                <span className="user-sub">{t('login')}</span>
              </div>
            </div>
            <button type="button" className="logout-btn" onClick={handleLogout}>{t('logout')}</button>
          </>
        ) : (
          <NavLink to="/auth" className="login-cta">{t('login')}</NavLink>
        )}
      </div>
    </aside>
  );
}
