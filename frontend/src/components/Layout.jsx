import { useNavigate, useLocation } from 'react-router-dom';
import Navbar from './Navbar.jsx';
import { Toast } from './Toast.jsx';
import { useAuth } from '../context/AuthContext.jsx';

const MOBILE_TABS = [
  { to: '/', label: '发现', icon: '✦' },
  { to: '/orders', label: '订单', icon: '◈', auth: true },
  { to: '/profile', label: '账户', icon: '◉', auth: true },
  { to: '/publish', label: '发布', icon: '⊕', auth: true },
  { to: '/checkin', label: '核销', icon: '⊘', auth: true },
];

export default function Layout({ children }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { isAuthed } = useAuth();

  function handleTabClick(tab) {
    if (tab.auth && !isAuthed) {
      navigate('/auth');
    } else {
      navigate(tab.to);
    }
  }

  return (
    <div className="app-shell">
      <Navbar />
      <div className="main-area">
        <main className="page-content">{children}</main>
      </div>
      <nav className="mobile-tabs">
        {MOBILE_TABS.map((tab) => {
          const isActive = pathname === tab.to || (tab.to !== '/' && pathname.startsWith(tab.to))
            || (tab.auth && !isAuthed && pathname === '/auth' && tab.to === '/profile');
          return (
            <button
              key={tab.to}
              type="button"
              className={`mobile-tab${isActive ? ' active' : ''}`}
              onClick={() => handleTabClick(tab)}
            >
              <span>{tab.icon}</span>
              <span>{tab.auth && !isAuthed && tab.to === '/profile' ? '登录' : tab.label}</span>
            </button>
          );
        })}
      </nav>
      <Toast />
    </div>
  );
}
