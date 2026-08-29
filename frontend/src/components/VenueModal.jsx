import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from './Toast.jsx';

function fmt(val) {
  if (!val) return '-';
  const d = new Date(val);
  return isNaN(d) ? val : d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

const VENUE_DETAIL_QUERY = `
  query VenueDetail($id: ID!) {
    venueById(id: $id) {
      id name cityCode address description capacity
      latitude longitude posterUrl startTime endTime status
      ticketTiers {
        id tierName price totalCapacity purchaseLimit saleStartTime saleEndTime
      }
    }
  }
`;

export default function VenueModal({ venueId, onClose, onPurchased }) {
  const { token, isAuthed } = useAuth();
  const [venue, setVenue] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedTierId, setSelectedTierId] = useState('');
  const [count, setCount] = useState(1);
  const [buying, setBuying] = useState(false);

  useEffect(() => {
    if (!venueId) return;
    setLoading(true);
    setVenue(null);
    api.queryGraphQL(VENUE_DETAIL_QUERY, { id: venueId }, token)
      .then((res) => {
        const v = res?.data?.venueById;
        setVenue(v);
        setSelectedTierId(v?.ticketTiers?.[0]?.id || '');
      })
      .catch((e) => showToast(e.message, 'error'))
      .finally(() => setLoading(false));
  }, [venueId, token]);

  if (!venueId) return null;

  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') onClose(); }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  async function handlePurchase() {
    if (!selectedTierId) { showToast('请先选择票档', 'error'); return; }
    if (!isAuthed) { showToast('请先登录', 'error'); return; }
    setBuying(true);
    try {
      const res = await api.purchaseTicket({ ticketTierId: selectedTierId, purchaseCount: Number(count) }, token);
      showToast('购票成功！订单已创建', 'success');
      onPurchased?.(res?.orderId);
      onClose();
    } catch (e) {
      showToast(e.message, 'error');
    } finally {
      setBuying(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <button type="button" className="modal-close" onClick={onClose}>✕</button>

        {loading ? (
          <div className="modal-loading">
            <div className="spinner" />
          </div>
        ) : venue ? (
          <div className="modal-inner">
            <div className="modal-poster">
              <img src={venue.posterUrl} alt={venue.name} onError={(e) => { e.target.style.display = 'none'; }} />
            </div>
            <div className="modal-body">
              <div className="modal-header">
                <h2>{venue.name}</h2>
                <div className="modal-meta">
                  <span>📍 {venue.cityCode} · {venue.address}</span>
                  <span>📅 {fmt(venue.startTime)} — {fmt(venue.endTime)}</span>
                  {venue.capacity && <span>👥 容量 {venue.capacity.toLocaleString()}</span>}
                </div>
              </div>

              {venue.description && (
                <p className="modal-desc">{venue.description}</p>
              )}

              <div className="tier-section">
                <h4>选择票档</h4>
                <div className="tier-list">
                  {venue.ticketTiers?.map((tier) => (
                    <label
                      key={tier.id}
                      className={`tier-card${selectedTierId === tier.id ? ' selected' : ''}`}
                    >
                      <input
                        type="radio"
                        name="tier"
                        checked={selectedTierId === tier.id}
                        onChange={() => setSelectedTierId(tier.id)}
                      />
                      <div className="tier-info">
                        <strong>{tier.tierName}</strong>
                        <span className="tier-price">¥{tier.price}</span>
                        <small>库存 {tier.totalCapacity} · 限购 {tier.purchaseLimit}</small>
                        <small>售票 {fmt(tier.saleStartTime)} 至 {fmt(tier.saleEndTime)}</small>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              <div className="purchase-row">
                <div className="count-input">
                  <button type="button" onClick={() => setCount((c) => Math.max(1, Number(c) - 1))}>−</button>
                  <input
                    type="number"
                    min="1"
                    value={count}
                    onChange={(e) => setCount(e.target.value)}
                  />
                  <button type="button" onClick={() => setCount((c) => Number(c) + 1)}>+</button>
                </div>
                <button
                  type="button"
                  className="btn-primary"
                  onClick={handlePurchase}
                  disabled={buying || !isAuthed}
                >
                  {buying ? '处理中…' : isAuthed ? '立即购票' : '登录后购票'}
                </button>
              </div>
            </div>
          </div>
        ) : (
          <p className="modal-empty">活动数据加载失败</p>
        )}
      </div>
    </div>
  );
}
