import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import VenueCard from '../components/VenueCard.jsx';
import VenueModal from '../components/VenueModal.jsx';
import { showToast } from '../components/Toast.jsx';

const VENUE_LIST_QUERY = `
  query VenueList($first: Int!, $filter: VenueFilter) {
    venuesConnection(first: $first, filter: $filter) {
      edges { cursor node {
        id name cityCode address description posterUrl status startTime endTime
        ticketTiers { id tierName price totalCapacity purchaseLimit saleStartTime saleEndTime }
      }}
      pageInfo { hasNextPage endCursor }
    }
  }
`;

const CITIES = ['', 'BJ', 'SH', 'GZ', 'SZ', 'CD', 'HZ'];

export default function DiscoverPage() {
  const { token } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [filter, setFilter] = useState({ cityCode: '', minPrice: '', maxPrice: '' });
  const [activeCity, setActiveCity] = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [recentOrders, setRecentOrders] = useState(() => {
    try { return JSON.parse(localStorage.getItem('venuego.console.orders') || '[]'); } catch { return []; }
  });

  const loadVenues = useCallback(async (f = filter) => {
    setLoading(true);
    setLoadError(null);
    try {
      const gqlFilter = {};
      if (f.cityCode) gqlFilter.cityCode = f.cityCode;
      if (f.minPrice) gqlFilter.minPrice = Number(f.minPrice);
      if (f.maxPrice) gqlFilter.maxPrice = Number(f.maxPrice);

      const res = await api.queryGraphQL(
        VENUE_LIST_QUERY,
        { first: 24, filter: Object.keys(gqlFilter).length ? gqlFilter : null },
        token,
      );
      setVenues(res?.data?.venuesConnection?.edges?.map((e) => e.node) || []);
    } catch (e) {
      setLoadError(e.message);
    } finally {
      setLoading(false);
    }
  }, [token, filter]);

  useEffect(() => { loadVenues(); }, [token]); // eslint-disable-line react-hooks/exhaustive-deps

  function handleCityFilter(city) {
    setActiveCity(city);
    const next = { ...filter, cityCode: city };
    setFilter(next);
    loadVenues(next);
  }

  function handlePurchased(orderId) {
    if (!orderId) return;
    const next = [orderId, ...recentOrders.filter((id) => id !== orderId)].slice(0, 8);
    setRecentOrders(next);
    localStorage.setItem('venuego.console.orders', JSON.stringify(next));
    showToast(t('order_created'), 'success');
  }

  return (
    <div className="page">
      <div className="page-hero">
        <div className="hero-text">
          <h1>{t('discover_hero_title')}</h1>
          <p>{t('discover_hero_desc')}</p>
        </div>
        <div className="hero-actions">
          <button type="button" className="btn-secondary" onClick={() => loadVenues()} disabled={loading}>
            {loading ? t('loading') : t('refresh')}
          </button>
        </div>
      </div>

      <div className="filter-bar">
        <div className="city-tabs">
          {CITIES.map((c) => (
            <button
              key={c || 'all'}
              className={`city-tab${activeCity === c ? ' active' : ''}`}
              onClick={() => handleCityFilter(c)}
            >
              {c || t('all_cities')}
            </button>
          ))}
        </div>
        <div className="price-filter">
          <input
            type="number"
            placeholder={t('filter_min_price')}
            value={filter.minPrice}
            onChange={(e) => setFilter((f) => ({ ...f, minPrice: e.target.value }))}
          />
          <span>—</span>
          <input
            type="number"
            placeholder={t('filter_max_price')}
            value={filter.maxPrice}
            onChange={(e) => setFilter((f) => ({ ...f, maxPrice: e.target.value }))}
          />
          <button type="button" className="btn-sm" onClick={() => loadVenues()}>{t('filter_apply')}</button>
        </div>
      </div>

      {loading ? (
        <div className="venue-grid">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="venue-card skeleton">
              <div className="venue-poster skeleton-block" />
              <div className="venue-info">
                <div className="skeleton-line" style={{ width: '70%' }} />
                <div className="skeleton-line" style={{ width: '40%' }} />
              </div>
            </div>
          ))}
        </div>
      ) : loadError ? (
        <div className="empty-state">
          <span className="empty-icon">⚠</span>
          <p>{loadError}</p>
          <button type="button" className="btn-secondary" style={{ marginTop: '1rem' }} onClick={() => loadVenues()}>
            {t('refresh')}
          </button>
        </div>
      ) : venues.length === 0 ? (
        <div className="empty-state">
          <span className="empty-icon">🎭</span>
          <p>{t('no_events')}</p>
        </div>
      ) : (
        <div className="venue-grid">
          {venues.map((venue) => (
            <VenueCard
              key={venue.id}
              venue={venue}
              selected={selectedId === venue.id}
              onClick={() => setSelectedId(venue.id)}
            />
          ))}
        </div>
      )}

      <VenueModal
        venueId={selectedId}
        onClose={() => setSelectedId(null)}
        onPurchased={handlePurchased}
      />
    </div>
  );
}
