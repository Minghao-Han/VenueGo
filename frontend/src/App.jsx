import React, { useEffect, useMemo, useState } from 'react';
import { api, decodeJwtClaims } from './api.js';

const VENUE_LIST_QUERY = `
  query VenueList($first: Int!, $filter: VenueFilter) {
    venuesConnection(first: $first, filter: $filter) {
      edges {
        cursor
        node {
          id
          name
          cityCode
          address
          description
          posterUrl
          status
          startTime
          endTime
          ticketTiers {
            id
            tierName
            price
            totalCapacity
            purchaseLimit
            saleStartTime
            saleEndTime
          }
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

const VENUE_DETAIL_QUERY = `
  query VenueDetail($id: ID!) {
    venueById(id: $id) {
      id
      name
      cityCode
      address
      description
      capacity
      latitude
      longitude
      posterUrl
      startTime
      endTime
      status
      ticketTiers {
        id
        tierName
        price
        totalCapacity
        purchaseLimit
        saleStartTime
        saleEndTime
      }
    }
  }
`;

const TOKEN_KEY = 'venuego.console.token';
const ORDER_KEY = 'venuego.console.orders';

function safeGetStorage(key, fallback = '') {
  try {
    const value = localStorage.getItem(key);
    return value ?? fallback;
  } catch {
    return fallback;
  }
}

function safeSetStorage(key, value) {
  try {
    localStorage.setItem(key, value);
  } catch {
    // Ignore storage write failures in restricted browser modes.
  }
}

const emptyVenueForm = {
  name: '',
  address: '',
  cityCode: 'SH',
  latitude: 31.2304,
  longitude: 121.4737,
  description: '',
  capacity: 5000,
  startTime: '2026-05-01T19:30:00+08:00',
  endTime: '2026-05-01T22:00:00+08:00',
  posterUrl: 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=1200&q=80',
  status: 'UPCOMING',
  tierName: 'Standard',
  price: 299,
  totalCapacity: 1000,
  purchaseLimit: 2,
  saleStartTime: '2026-04-01T12:00:00+08:00',
  saleEndTime: '2026-05-01T18:00:00+08:00',
};

function formatDate(value) {
  if (!value) {
    return '-';
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN');
}

function loadStoredOrders() {
  try {
    const raw = safeGetStorage(ORDER_KEY, '');
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function App() {
  const [token, setToken] = useState(() => safeGetStorage(TOKEN_KEY, ''));
  const [claims, setClaims] = useState(() => decodeJwtClaims(safeGetStorage(TOKEN_KEY, '')));
  const [activeTab, setActiveTab] = useState(() => (safeGetStorage(TOKEN_KEY, '') ? 'discover' : 'auth'));
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  const [registerForm, setRegisterForm] = useState({ username: '', email: '', password: '' });
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });

  const [venueFilter, setVenueFilter] = useState({ cityCode: '', minPrice: '', maxPrice: '' });
  const [venues, setVenues] = useState([]);
  const [selectedVenue, setSelectedVenue] = useState(null);
  const [selectedTierId, setSelectedTierId] = useState('');
  const [purchaseCount, setPurchaseCount] = useState(1);

  const [venueForm, setVenueForm] = useState(emptyVenueForm);
  const [createdVenue, setCreatedVenue] = useState(null);

  const [profile, setProfile] = useState(null);
  const [profileForm, setProfileForm] = useState({ username: '', avatarUrl: '', bio: '' });
  const [streak, setStreak] = useState(null);
  const [monthRecord, setMonthRecord] = useState([]);

  const [recentOrders, setRecentOrders] = useState(loadStoredOrders);
  const [orderIdInput, setOrderIdInput] = useState('');
  const [orderDetail, setOrderDetail] = useState(null);
  const [paymentForm, setPaymentForm] = useState({ paymentId: 'mock-payment-001', amount: '' });
  const [cancelReason, setCancelReason] = useState('User initiated cancellation');
  const [verifyCode, setVerifyCode] = useState('');

  const [ticketCode, setTicketCode] = useState('');
  const [checkinResult, setCheckinResult] = useState(null);

  const isAuthed = Boolean(token);
  const displayUserId = claims?.sub || '未登录';

  const stats = useMemo(
    () => [
      { label: '服务数量', value: '6', note: 'Auth / User / Venue / Ticketing / Order / CheckIn' },
      { label: '网关入口', value: 'Kong', note: 'JWT 校验 + Header 注入' },
      { label: '查询压测', value: '4157.95 rps', note: 'Venue GraphQL at localhost:8080' },
      { label: '交易链路', value: 'Redis + MQ', note: '抢票同步入站，订单异步编排' },
    ],
    [],
  );

  useEffect(() => {
    safeSetStorage(TOKEN_KEY, token);
    setClaims(decodeJwtClaims(token));
  }, [token]);

  useEffect(() => {
    if (!token && activeTab !== 'auth') {
      setActiveTab('auth');
    }
  }, [token, activeTab]);

  useEffect(() => {
    safeSetStorage(ORDER_KEY, JSON.stringify(recentOrders));
  }, [recentOrders]);

  useEffect(() => {
    loadVenues().catch(() => {
      // Keep UI interactive even if initial data fetch fails.
    });
  }, []);

  async function withFeedback(task, successMessage) {
    try {
      setBusy(true);
      const result = await task();
      if (successMessage) {
        setMessage(successMessage);
      }
      return result;
    } catch (error) {
      setMessage(error.message || '请求失败');
      throw error;
    } finally {
      setBusy(false);
    }
  }

  async function loadVenues() {
    const filter = {};
    if (venueFilter.cityCode.trim()) {
      filter.cityCode = venueFilter.cityCode.trim();
    }
    if (venueFilter.minPrice) {
      filter.minPrice = Number(venueFilter.minPrice);
    }
    if (venueFilter.maxPrice) {
      filter.maxPrice = Number(venueFilter.maxPrice);
    }

    const response = await withFeedback(
      () => api.queryGraphQL(VENUE_LIST_QUERY, { first: 12, filter: Object.keys(filter).length ? filter : null }, token),
      '活动列表已刷新',
    );

    const edges = response?.data?.venuesConnection?.edges || [];
    const nextVenues = edges.map((edge) => edge.node);
    setVenues(nextVenues);
    if (nextVenues.length > 0 && !selectedVenue) {
      setSelectedVenue(nextVenues[0]);
      setSelectedTierId(nextVenues[0].ticketTiers?.[0]?.id || '');
    }
  }

  async function loadVenueDetail(id) {
    const response = await withFeedback(() => api.queryGraphQL(VENUE_DETAIL_QUERY, { id }, token), '活动详情已加载');
    const venue = response?.data?.venueById;
    setSelectedVenue(venue);
    setSelectedTierId(venue?.ticketTiers?.[0]?.id || '');
  }

  async function handleRegister(event) {
    event.preventDefault();
    await withFeedback(() => api.register(registerForm), '注册成功，可以直接登录');
    setRegisterForm({ username: '', email: '', password: '' });
  }

  async function handleLogin(event) {
    event.preventDefault();
    const response = await withFeedback(() => api.login(loginForm), '登录成功');
    setToken(response.token || '');
    setLoginForm({ username: '', password: '' });
    setActiveTab('discover');
  }

  async function handleLogout() {
    try {
      if (token) {
        await api.logout(token);
      }
    } catch {
      // Ignore logout failures and clear local state anyway.
    }
    setToken('');
    setClaims(null);
    setProfile(null);
    setOrderDetail(null);
    setActiveTab('auth');
    setMessage('已退出登录');
  }

  async function handleCreateVenue(event) {
    event.preventDefault();
    const payload = {
      name: venueForm.name,
      address: venueForm.address,
      cityCode: venueForm.cityCode,
      latitude: Number(venueForm.latitude),
      longitude: Number(venueForm.longitude),
      description: venueForm.description,
      capacity: Number(venueForm.capacity),
      startTime: venueForm.startTime,
      endTime: venueForm.endTime,
      posterUrl: venueForm.posterUrl,
      status: venueForm.status,
      ticketTiers: [
        {
          tierName: venueForm.tierName,
          price: Number(venueForm.price),
          totalCapacity: Number(venueForm.totalCapacity),
          purchaseLimit: Number(venueForm.purchaseLimit),
          saleStartTime: venueForm.saleStartTime,
          saleEndTime: venueForm.saleEndTime,
        },
      ],
    };

    const response = await withFeedback(() => api.createVenue(payload, token), '活动已创建');
    setCreatedVenue(response);
    await loadVenues();
  }

  async function handlePurchase() {
    if (!selectedTierId) {
      setMessage('请先选择票档');
      return;
    }

    const response = await withFeedback(
      () => api.purchaseTicket({ ticketTierId: selectedTierId, purchaseCount: Number(purchaseCount) }, token),
      '购票请求已发送',
    );

    if (response?.orderId) {
      const next = [response.orderId, ...recentOrders.filter((item) => item !== response.orderId)].slice(0, 8);
      setRecentOrders(next);
      setOrderIdInput(response.orderId);
    }
  }

  async function loadProfileData() {
    const [profileResponse, streakResponse, monthResponse] = await Promise.all([
      api.getProfile(token),
      api.getStreak(token),
      api.getMonthRecord(token),
    ]);

    setProfile(profileResponse.data);
    setProfileForm({
      username: profileResponse.data?.username || '',
      avatarUrl: profileResponse.data?.avatarUrl || '',
      bio: profileResponse.data?.bio || '',
    });
    setStreak(streakResponse.data);
    setMonthRecord(monthResponse.data || []);
    setMessage('用户资料已同步');
  }

  async function handleUpdateProfile(event) {
    event.preventDefault();
    const response = await withFeedback(() => api.updateProfile(profileForm, token), '资料已更新');
    setProfile(response.data);
  }

  async function handleDailySign() {
    await withFeedback(() => api.signDaily(token), '今日签到成功');
    await loadProfileData();
  }

  async function handleLoadOrder(id = orderIdInput) {
    if (!id) {
      setMessage('请输入订单号');
      return;
    }

    const response = await withFeedback(() => api.getOrder(id, token), '订单详情已加载');
    setOrderDetail(response.data);
    setPaymentForm((current) => ({
      ...current,
      amount: response.data?.totalAmount || current.amount,
    }));
    setVerifyCode(response.data?.verifyCode || '');
  }

  async function handlePayOrder() {
    if (!orderDetail) {
      setMessage('请先加载订单');
      return;
    }

    const response = await withFeedback(
      () =>
        api.payOrder(
          orderDetail.orderId,
          {
            orderId: orderDetail.orderId,
            paymentId: paymentForm.paymentId,
            amount: Number(paymentForm.amount || orderDetail.totalAmount),
          },
          token,
        ),
      '订单已支付',
    );
    setOrderDetail(response.data);
  }

  async function handleCancelOrder() {
    if (!orderDetail) {
      setMessage('请先加载订单');
      return;
    }

    const response = await withFeedback(
      () => api.cancelOrder(orderDetail.orderId, { orderId: orderDetail.orderId, reason: cancelReason }, token),
      '订单已取消',
    );
    setOrderDetail(response.data);
  }

  async function handleUseOrder() {
    if (!orderDetail) {
      setMessage('请先加载订单');
      return;
    }

    const response = await withFeedback(
      () => api.useOrder(orderDetail.orderId, { orderId: orderDetail.orderId, verifyCode }, token),
      '电子票已核销',
    );
    setOrderDetail(response.data);
  }

  async function handleCheckin(event) {
    event.preventDefault();
    const response = await withFeedback(() => api.scanTicket(ticketCode, token), '现场核销请求已完成');
    setCheckinResult(response);
  }

  return (
    <div className="shell">
      <div className="hero-glow hero-glow-left" />
      <div className="hero-glow hero-glow-right" />

      <header className="hero">
        <div>
          <p className="eyebrow">VenueGo Frontend</p>
          <h1>一个够用、能跑、能串起整条票务链路的前端控制台</h1>
          <p className="hero-copy">
            这不是完整商业站点，而是给当前微服务项目配套的轻量前端。它覆盖登录、活动查询、发活动、抢票、订单操作、用户资料、签到和核销。
          </p>
        </div>

        <div className="hero-panel">
          <div className="badge-row">
            <span className="badge">Kong / JWT</span>
            <span className="badge">GraphQL + REST</span>
            <span className="badge">Ticketing Flow</span>
          </div>
          <div className="identity-card">
            <span>当前用户</span>
            <strong>{claims?.username || claims?.email || displayUserId}</strong>
            <small>{isAuthed ? `sub: ${displayUserId}` : '请先登录以访问受保护接口'}</small>
          </div>
          <div className="message-bar">{message || '前端已准备好，可以直接对接本地 Kong 网关。'}</div>
        </div>
      </header>

      <section className="stats-grid">
        {stats.map((item) => (
          <article className="stat-card" key={item.label}>
            <span>{item.label}</span>
            <strong>{item.value}</strong>
            <small>{item.note}</small>
          </article>
        ))}
      </section>

      <nav className="tabs">
        {[
          ['discover', '活动广场'],
          ['auth', '登录注册'],
          ['publish', '发布活动'],
          ['profile', '用户中心'],
          ['orders', '订单台'],
          ['checkin', '核销'],
        ].map(([key, label]) => (
          <button
            key={key}
            type="button"
            className={activeTab === key ? 'tab active' : 'tab'}
            onClick={() => {
              setActiveTab(key);
              if (key === 'profile' && isAuthed) {
                void loadProfileData();
              }
            }}
          >
            {label}
          </button>
        ))}
      </nav>

      <main className="content-grid">
        {activeTab === 'discover' && (
          <>
            <section className="panel panel-wide">
              <div className="panel-head">
                <div>
                  <h2>活动查询</h2>
                  <p>走 VenueService GraphQL，适合读多写少的浏览场景。</p>
                </div>
                <button type="button" className="ghost-button" onClick={() => void loadVenues()} disabled={busy}>
                  刷新列表
                </button>
              </div>

              <div className="filter-row">
                <input
                  value={venueFilter.cityCode}
                  onChange={(event) => setVenueFilter((current) => ({ ...current, cityCode: event.target.value }))}
                  placeholder="城市代码，如 SH"
                />
                <input
                  type="number"
                  value={venueFilter.minPrice}
                  onChange={(event) => setVenueFilter((current) => ({ ...current, minPrice: event.target.value }))}
                  placeholder="最低价"
                />
                <input
                  type="number"
                  value={venueFilter.maxPrice}
                  onChange={(event) => setVenueFilter((current) => ({ ...current, maxPrice: event.target.value }))}
                  placeholder="最高价"
                />
                <button type="button" onClick={() => void loadVenues()} disabled={busy}>
                  应用筛选
                </button>
              </div>

              <div className="venue-grid">
                {venues.map((venue) => (
                  <button
                    type="button"
                    className={selectedVenue?.id === venue.id ? 'venue-card selected' : 'venue-card'}
                    key={venue.id}
                    onClick={() => void loadVenueDetail(venue.id)}
                  >
                    <div className="poster" style={{ backgroundImage: `linear-gradient(180deg, rgba(21,18,17,0.08), rgba(21,18,17,0.76)), url(${venue.posterUrl || ''})` }} />
                    <div className="venue-copy">
                      <strong>{venue.name}</strong>
                      <span>{venue.cityCode || 'CITY'} · {venue.status || 'UPCOMING'}</span>
                      <small>{venue.address}</small>
                    </div>
                  </button>
                ))}
              </div>
            </section>

            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>活动详情与购票</h2>
                  <p>选择一个活动后即可直接走 TicketingService 购票。</p>
                </div>
              </div>

              {selectedVenue ? (
                <>
                  <div className="detail-block">
                    <h3>{selectedVenue.name}</h3>
                    <p>{selectedVenue.description || '暂无描述'}</p>
                    <div className="detail-meta">
                      <span>{selectedVenue.cityCode}</span>
                      <span>{selectedVenue.address}</span>
                      <span>{formatDate(selectedVenue.startTime)}</span>
                    </div>
                  </div>

                  <div className="tier-list">
                    {selectedVenue.ticketTiers?.map((tier) => (
                      <label className="tier-card" key={tier.id}>
                        <input
                          type="radio"
                          name="tier"
                          checked={selectedTierId === tier.id}
                          onChange={() => setSelectedTierId(tier.id)}
                        />
                        <div>
                          <strong>{tier.tierName}</strong>
                          <span>¥{tier.price}</span>
                          <small>库存 {tier.totalCapacity} · 限购 {tier.purchaseLimit}</small>
                        </div>
                      </label>
                    ))}
                  </div>

                  <div className="purchase-box">
                    <input
                      type="number"
                      min="1"
                      value={purchaseCount}
                      onChange={(event) => setPurchaseCount(event.target.value)}
                    />
                    <button type="button" onClick={() => void handlePurchase()} disabled={!isAuthed || busy}>
                      {isAuthed ? '立即购票' : '登录后可购票'}
                    </button>
                  </div>
                </>
              ) : (
                <p className="muted">当前没有活动数据，或者请先刷新列表。</p>
              )}
            </section>
          </>
        )}

        {activeTab === 'auth' && (
          <>
            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>注册</h2>
                  <p>调用 AuthService 的公开接口。</p>
                </div>
              </div>
              <form className="stack-form" onSubmit={handleRegister}>
                <input value={registerForm.username} onChange={(event) => setRegisterForm((current) => ({ ...current, username: event.target.value }))} placeholder="用户名" required />
                <input value={registerForm.email} onChange={(event) => setRegisterForm((current) => ({ ...current, email: event.target.value }))} placeholder="邮箱" type="email" required />
                <input value={registerForm.password} onChange={(event) => setRegisterForm((current) => ({ ...current, password: event.target.value }))} placeholder="密码" type="password" required />
                <button type="submit" disabled={busy}>注册</button>
              </form>
            </section>

            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>登录</h2>
                  <p>登录后，前端会本地存储 JWT 并自动带到受保护接口。</p>
                </div>
              </div>
              <form className="stack-form" onSubmit={handleLogin}>
                <input value={loginForm.username} onChange={(event) => setLoginForm((current) => ({ ...current, username: event.target.value }))} placeholder="用户名" required />
                <input value={loginForm.password} onChange={(event) => setLoginForm((current) => ({ ...current, password: event.target.value }))} placeholder="密码" type="password" required />
                <button type="submit" disabled={busy}>登录</button>
              </form>

              <div className="token-box">
                <strong>Token 状态</strong>
                <small>{isAuthed ? '已登录，后续请求将通过 Kong 校验。' : '当前未持有 JWT。'}</small>
                <button type="button" className="ghost-button" onClick={() => void handleLogout()}>
                  清空登录状态
                </button>
              </div>
            </section>
          </>
        )}

        {activeTab === 'publish' && (
          <section className="panel panel-wide">
            <div className="panel-head">
              <div>
                <h2>主办方发活动</h2>
                <p>用 VenueService 的 REST 创建一个带单票档的活动。</p>
              </div>
            </div>

            <form className="publish-form" onSubmit={handleCreateVenue}>
              {[
                ['name', '活动名'],
                ['address', '地址'],
                ['cityCode', '城市代码'],
                ['latitude', '纬度'],
                ['longitude', '经度'],
                ['capacity', '总容量'],
                ['posterUrl', '海报 URL'],
                ['tierName', '票档名称'],
                ['price', '价格'],
                ['totalCapacity', '票档库存'],
                ['purchaseLimit', '限购数'],
              ].map(([key, label]) => (
                <label key={key}>
                  <span>{label}</span>
                  <input value={venueForm[key]} onChange={(event) => setVenueForm((current) => ({ ...current, [key]: event.target.value }))} required />
                </label>
              ))}

              <label className="span-2">
                <span>活动描述</span>
                <textarea value={venueForm.description} onChange={(event) => setVenueForm((current) => ({ ...current, description: event.target.value }))} rows="4" />
              </label>

              <label>
                <span>状态</span>
                <select value={venueForm.status} onChange={(event) => setVenueForm((current) => ({ ...current, status: event.target.value }))}>
                  <option value="UPCOMING">UPCOMING</option>
                  <option value="SALE_ON">SALE_ON</option>
                  <option value="SOLD_OUT">SOLD_OUT</option>
                  <option value="FINISHED">FINISHED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </label>

              <label>
                <span>开始时间</span>
                <input value={venueForm.startTime} onChange={(event) => setVenueForm((current) => ({ ...current, startTime: event.target.value }))} required />
              </label>

              <label>
                <span>结束时间</span>
                <input value={venueForm.endTime} onChange={(event) => setVenueForm((current) => ({ ...current, endTime: event.target.value }))} required />
              </label>

              <label>
                <span>开售时间</span>
                <input value={venueForm.saleStartTime} onChange={(event) => setVenueForm((current) => ({ ...current, saleStartTime: event.target.value }))} required />
              </label>

              <label>
                <span>停售时间</span>
                <input value={venueForm.saleEndTime} onChange={(event) => setVenueForm((current) => ({ ...current, saleEndTime: event.target.value }))} required />
              </label>

              <div className="span-2 action-row">
                <button type="submit" disabled={!isAuthed || busy}>创建活动</button>
              </div>
            </form>

            {createdVenue && (
              <div className="result-card">
                <strong>最新创建结果</strong>
                <pre>{JSON.stringify(createdVenue, null, 2)}</pre>
              </div>
            )}
          </section>
        )}

        {activeTab === 'profile' && (
          <>
            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>资料与签到</h2>
                  <p>UserService 的 REST 接口集合。</p>
                </div>
                <button type="button" className="ghost-button" onClick={() => void loadProfileData()} disabled={!isAuthed || busy}>
                  拉取资料
                </button>
              </div>

              <div className="mini-stats">
                <article>
                  <span>连续签到</span>
                  <strong>{streak ?? '-'}</strong>
                </article>
                <article>
                  <span>积分</span>
                  <strong>{profile?.points ?? '-'}</strong>
                </article>
                <article>
                  <span>本月记录</span>
                  <strong>{monthRecord.length}</strong>
                </article>
              </div>

              <form className="stack-form" onSubmit={handleUpdateProfile}>
                <input value={profileForm.username} onChange={(event) => setProfileForm((current) => ({ ...current, username: event.target.value }))} placeholder="用户名" />
                <input value={profileForm.avatarUrl} onChange={(event) => setProfileForm((current) => ({ ...current, avatarUrl: event.target.value }))} placeholder="头像 URL" />
                <textarea value={profileForm.bio} onChange={(event) => setProfileForm((current) => ({ ...current, bio: event.target.value }))} rows="4" placeholder="个人简介" />
                <button type="submit" disabled={!isAuthed || busy}>更新资料</button>
              </form>

              <button type="button" className="ghost-button" onClick={() => void handleDailySign()} disabled={!isAuthed || busy}>
                今日签到
              </button>
            </section>

            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>本月签到日历</h2>
                  <p>后端返回的是当前月已签到的日期数组。</p>
                </div>
              </div>
              <div className="day-grid">
                {Array.from({ length: 31 }, (_, index) => index + 1).map((day) => (
                  <span key={day} className={monthRecord.includes(day) ? 'day active' : 'day'}>
                    {day}
                  </span>
                ))}
              </div>
            </section>
          </>
        )}

        {activeTab === 'orders' && (
          <>
            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>订单查询</h2>
                  <p>OrderService 当前只暴露按 ID 查询，所以这里保留手动输入和最近订单。</p>
                </div>
              </div>

              <div className="filter-row">
                <input value={orderIdInput} onChange={(event) => setOrderIdInput(event.target.value)} placeholder="输入订单号" />
                <button type="button" onClick={() => void handleLoadOrder()} disabled={!isAuthed || busy}>查询订单</button>
              </div>

              <div className="chip-row">
                {recentOrders.map((id) => (
                  <button key={id} type="button" className="chip" onClick={() => { setOrderIdInput(id); void handleLoadOrder(id); }}>
                    {id}
                  </button>
                ))}
              </div>

              {orderDetail && (
                <div className="order-card">
                  <strong>{orderDetail.orderId}</strong>
                  <span>{orderDetail.status}</span>
                  <small>数量 {orderDetail.quantity} · 总额 ¥{orderDetail.totalAmount}</small>
                  <small>校验码 {orderDetail.verifyCode || '-'}</small>
                </div>
              )}
            </section>

            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>订单动作</h2>
                  <p>这里直接映射 pay / cancel / use 三个状态迁移接口。</p>
                </div>
              </div>

              <div className="stack-form">
                <input value={paymentForm.paymentId} onChange={(event) => setPaymentForm((current) => ({ ...current, paymentId: event.target.value }))} placeholder="paymentId" />
                <input value={paymentForm.amount} onChange={(event) => setPaymentForm((current) => ({ ...current, amount: event.target.value }))} placeholder="支付金额" />
                <button type="button" onClick={() => void handlePayOrder()} disabled={!isAuthed || busy}>模拟支付</button>

                <input value={cancelReason} onChange={(event) => setCancelReason(event.target.value)} placeholder="取消原因" />
                <button type="button" className="ghost-button" onClick={() => void handleCancelOrder()} disabled={!isAuthed || busy}>取消订单</button>

                <input value={verifyCode} onChange={(event) => setVerifyCode(event.target.value)} placeholder="核销码" />
                <button type="button" className="ghost-button" onClick={() => void handleUseOrder()} disabled={!isAuthed || busy}>使用电子票</button>
              </div>
            </section>
          </>
        )}

        {activeTab === 'checkin' && (
          <section className="panel panel-wide">
            <div className="panel-head">
              <div>
                <h2>现场核销</h2>
                <p>调用 CheckInService 的 `/api/checkin/scan`。</p>
              </div>
            </div>

            <form className="filter-row" onSubmit={handleCheckin}>
              <input value={ticketCode} onChange={(event) => setTicketCode(event.target.value)} placeholder="输入 ticketCode / 二维码解码内容" required />
              <button type="submit" disabled={!isAuthed || busy}>执行核销</button>
            </form>

            {checkinResult && (
              <div className={checkinResult.success ? 'result-card success' : 'result-card fail'}>
                <strong>{checkinResult.success ? '核销成功' : '核销失败'}</strong>
                <pre>{JSON.stringify(checkinResult, null, 2)}</pre>
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

export default App;