const STATUS_MAP = {
  UPCOMING: { label: '即将开售', cls: 'status-upcoming' },
  SALE_ON: { label: '热卖中', cls: 'status-sale' },
  SOLD_OUT: { label: '已售罄', cls: 'status-sold' },
  FINISHED: { label: '已结束', cls: 'status-finished' },
  CANCELLED: { label: '已取消', cls: 'status-cancelled' },
};

function formatDateShort(val) {
  if (!val) return '';
  const d = new Date(val);
  return isNaN(d) ? val : `${d.getMonth() + 1}/${d.getDate()}`;
}

function minPrice(tiers) {
  if (!tiers?.length) return null;
  return Math.min(...tiers.map((t) => t.price));
}

export default function VenueCard({ venue, onClick, selected }) {
  const status = STATUS_MAP[venue.status] || { label: venue.status, cls: '' };
  const lowest = minPrice(venue.ticketTiers);

  return (
    <button
      type="button"
      className={`venue-card${selected ? ' selected' : ''}`}
      onClick={onClick}
    >
      <div
        className="venue-poster"
        style={{
          backgroundImage: `linear-gradient(180deg, transparent 40%, rgba(20,14,12,0.85) 100%), url(${venue.posterUrl || ''})`,
        }}
      >
        <span className={`status-badge ${status.cls}`}>{status.label}</span>
      </div>
      <div className="venue-info">
        <h3 className="venue-name">{venue.name}</h3>
        <div className="venue-meta">
          <span className="venue-city">📍 {venue.cityCode}</span>
          {venue.startTime && <span className="venue-date">📅 {formatDateShort(venue.startTime)}</span>}
        </div>
        {lowest != null && <span className="venue-price">¥{lowest} 起</span>}
      </div>
    </button>
  );
}
