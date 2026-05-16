import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from '../components/Toast.jsx';
import { useTranslation } from 'react-i18next';

export default function ProfilePage() {
  const { token, isAuthed, claims } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [profileForm, setProfileForm] = useState({ username: '', avatarUrl: '', bio: '' });
  const [streak, setStreak] = useState(null);
  const [monthRecord, setMonthRecord] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [signing, setSigning] = useState(false);

  useEffect(() => {
    if (!isAuthed) { navigate('/auth'); return; }
    loadAll();
  }, [isAuthed]); // eslint-disable-line react-hooks/exhaustive-deps

  const { t } = useTranslation();

  async function loadAll() {
    setLoading(true);
    try {
      const [pRes, sRes, mRes] = await Promise.all([
        api.getProfile(token),
        api.getStreak(token),
        api.getMonthRecord(token),
      ]);
      const p = pRes.data;
      setProfile(p);
      setProfileForm({ username: p?.username || '', avatarUrl: p?.avatarUrl || '', bio: p?.bio || '' });
      setStreak(sRes.data);
      setMonthRecord(mRes.data || []);
    } catch (e) {
      showToast(e.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await api.updateProfile(profileForm, token);
      setProfile(res.data);
      showToast('资料已更新', 'success');
    } catch (e) {
      showToast(e.message, 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleSign() {
    setSigning(true);
    try {
      await api.signDaily(token);
      showToast('签到成功 🎉', 'success');
      await loadAll();
    } catch (e) {
      showToast(e.message, 'error');
    } finally {
      setSigning(false);
    }
  }

  const today = new Date().getDate();
  const daysInMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate();

  return (
    <div className="page">
      <div className="page-header">
        <h1>我的账户</h1>
        <button type="button" className="btn-secondary" onClick={loadAll} disabled={loading}>
          {loading ? '同步中…' : '刷新数据'}
        </button>
      </div>

      <div className="profile-grid">
        <section className="card">
          <div className="profile-top">
            <div className="profile-avatar">
              {profile?.avatarUrl ? (
                <img src={profile.avatarUrl} alt="avatar" onError={(e) => { e.target.style.display='none'; }} />
              ) : (
                <span>{(claims?.username || 'U')[0].toUpperCase()}</span>
              )}
            </div>
            <div>
              <h2>{profile?.username || claims?.username || '—'}</h2>
              <p className="muted">{profile?.bio || '还没有个人简介'}</p>
            </div>
          </div>

          <div className="stat-row">
            <div className="stat-item">
              <strong>{profile?.points ?? '-'}</strong>
              <span>积分</span>
            </div>
            <div className="stat-item">
              <strong>{streak ?? '-'}</strong>
              <span>连续签到</span>
            </div>
            <div className="stat-item">
              <strong>{monthRecord.length}</strong>
              <span>本月签到</span>
            </div>
          </div>

          <button
            type="button"
            className="btn-primary full-width sign-btn"
            onClick={handleSign}
            disabled={signing || monthRecord.includes(today)}
          >
            {monthRecord.includes(today) ? '✓ 今日已签到' : signing ? '签到中…' : '今日签到'}
          </button>
        </section>

        <section className="card">
          <h3>编辑资料</h3>
          <form className="auth-form" onSubmit={handleSave}>
            <div className="field">
              <label>用户名</label>
              <input
                value={profileForm.username}
                onChange={(e) => setProfileForm((f) => ({ ...f, username: e.target.value }))}
                placeholder="用户名"
              />
            </div>
            <div className="field">
              <label>头像 URL</label>
              <input
                value={profileForm.avatarUrl}
                onChange={(e) => setProfileForm((f) => ({ ...f, avatarUrl: e.target.value }))}
                placeholder="https://..."
              />
            </div>
            <div className="field">
              <label>个人简介</label>
              <textarea
                value={profileForm.bio}
                onChange={(e) => setProfileForm((f) => ({ ...f, bio: e.target.value }))}
                placeholder="介绍一下自己"
                rows="3"
              />
            </div>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? '保存中…' : '保存修改'}
            </button>
          </form>
        </section>

        <section className="card card-wide">
          <h3>本月签到日历</h3>
          <div className="calendar-grid">
            {Array.from({ length: daysInMonth }, (_, i) => i + 1).map((day) => (
              <div
                key={day}
                className={`cal-day${monthRecord.includes(day) ? ' signed' : ''}${day === today ? ' today' : ''}`}
              >
                {day}
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}
