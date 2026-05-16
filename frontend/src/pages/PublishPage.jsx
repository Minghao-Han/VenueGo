import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from '../components/Toast.jsx';

const EMPTY = {
  name: '',
  address: '',
  cityCode: 'SH',
  latitude: 31.2304,
  longitude: 121.4737,
  description: '',
  capacity: 5000,
  startTime: '2026-08-01T19:30:00+08:00',
  endTime: '2026-08-01T22:00:00+08:00',
  posterUrl: 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=1200&q=80',
  status: 'UPCOMING',
  tierName: 'Standard',
  price: 299,
  totalCapacity: 1000,
  purchaseLimit: 2,
  saleStartTime: '2026-07-01T12:00:00+08:00',
  saleEndTime: '2026-08-01T18:00:00+08:00',
};

export default function PublishPage() {
  const { token, isAuthed } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [form, setForm] = useState(EMPTY);
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (!isAuthed) navigate('/auth');
  }, [isAuthed, navigate]);

  function set(key, val) {
    setForm((f) => ({ ...f, [key]: val }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      const payload = {
        name: form.name, address: form.address, cityCode: form.cityCode,
        latitude: Number(form.latitude), longitude: Number(form.longitude),
        description: form.description, capacity: Number(form.capacity),
        startTime: form.startTime, endTime: form.endTime,
        posterUrl: form.posterUrl, status: form.status,
        ticketTiers: [{
          tierName: form.tierName, price: Number(form.price),
          totalCapacity: Number(form.totalCapacity),
          purchaseLimit: Number(form.purchaseLimit),
          saleStartTime: form.saleStartTime, saleEndTime: form.saleEndTime,
        }],
      };
      const res = await api.createVenue(payload, token);
      setResult(res);
      showToast(t('publish_success'), 'success');
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      setBusy(false);
    }
  }

  const Field = ({ label, name, type = 'text', rows }) => (
    <div className="field">
      <label>{label}</label>
      {rows ? (
        <textarea rows={rows} value={form[name]} onChange={(e) => set(name, e.target.value)} />
      ) : (
        <input type={type} value={form[name]} onChange={(e) => set(name, e.target.value)} />
      )}
    </div>
  );

  return (
    <div className="page">
      <div className="page-header">
        <h1>{t('publish')}</h1>
        <p className="muted">{t('publish_desc')}</p>
      </div>

      <div className="publish-layout">
        <form className="card publish-form" onSubmit={handleSubmit}>
          <div className="form-section">
            <h3>{t('publish_basic_info')}</h3>
            <div className="form-grid">
              <Field label={t('publish_event_name')} name="name" />
              <Field label={t('publish_city_code')} name="cityCode" />
              <Field label={t('publish_address')} name="address" />
              <div className="field">
                <label>{t('publish_status')}</label>
                <select value={form.status} onChange={(e) => set('status', e.target.value)}>
                  {['UPCOMING', 'SALE_ON', 'SOLD_OUT', 'FINISHED', 'CANCELLED'].map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
              <Field label={t('publish_capacity')} name="capacity" type="number" />
              <Field label={t('publish_poster_url')} name="posterUrl" />
            </div>
            <Field label={t('publish_description')} name="description" rows={3} />
          </div>

          <div className="form-section">
            <h3>{t('publish_time_settings')}</h3>
            <div className="form-grid">
              <Field label={t('publish_start_time')} name="startTime" />
              <Field label={t('publish_end_time')} name="endTime" />
              <Field label={t('publish_latitude')} name="latitude" type="number" />
              <Field label={t('publish_longitude')} name="longitude" type="number" />
            </div>
          </div>

          <div className="form-section">
            <h3>{t('publish_ticket_tier')}</h3>
            <div className="form-grid">
              <Field label={t('publish_tier_name')} name="tierName" />
              <Field label={t('publish_price')} name="price" type="number" />
              <Field label={t('publish_tier_capacity')} name="totalCapacity" type="number" />
              <Field label={t('publish_purchase_limit')} name="purchaseLimit" type="number" />
              <Field label={t('publish_sale_start')} name="saleStartTime" />
              <Field label={t('publish_sale_end')} name="saleEndTime" />
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn-ghost" onClick={() => setForm(EMPTY)}>{t('reset')}</button>
            <button type="submit" className="btn-primary" disabled={busy}>
              {busy ? t('publishing') : t('publish')}
            </button>
          </div>
        </form>

        <div className="publish-side">
          {form.posterUrl && (
            <div className="card preview-card">
              <h4>{t('publish_poster_preview')}</h4>
              <div className="preview-poster">
                <img src={form.posterUrl} alt="poster preview" onError={(e) => { e.target.style.display = 'none'; }} />
              </div>
              {form.name && <strong className="preview-name">{form.name}</strong>}
              {form.cityCode && <span className="muted">📍 {form.cityCode}</span>}
            </div>
          )}

          {result && (
            <div className="card result-preview">
              <h4>{t('publish_result')}</h4>
              <pre className="result-json">{JSON.stringify(result, null, 2)}</pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
