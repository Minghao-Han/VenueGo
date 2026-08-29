import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../api.js';
import { showToast } from '../components/Toast.jsx';

const ORDER_KEY = 'venuego.console.orders';

const STATUS_CONFIG = {
  PENDING: { label: '待支付', cls: 'status-pending' },
  PAID: { label: '已支付', cls: 'status-paid' },
  USED: { label: '已使用', cls: 'status-used' },
  CANCELLED: { label: '已取消', cls: 'status-cancelled' },
  REFUNDED: { label: '已退款', cls: 'status-refunded' },
};

function fmt(val) {
  if (!val) return '-';
  const d = new Date(val);
  return isNaN(d) ? val : d.toLocaleString('zh-CN');
}

function loadOrders() {
  try { return JSON.parse(localStorage.getItem(ORDER_KEY) || '[]'); } catch { return []; }
}

export default function OrdersPage() {
  const { token, isAuthed } = useAuth();
  const navigate = useNavigate();
  const [recentOrders, setRecentOrders] = useState(loadOrders);
  const [orderId, setOrderId] = useState('');
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [actionBusy, setActionBusy] = useState(false);
  const [payForm, setPayForm] = useState({ paymentId: 'mock-payment-001', amount: '' });
  const [cancelReason, setCancelReason] = useState('用户主动取消');
  const [verifyCode, setVerifyCode] = useState('');

  useEffect(() => {
    if (!isAuthed) navigate('/auth');
  }, [isAuthed, navigate]);

  async function loadOrder(id = orderId) {
    if (!id.trim()) { showToast('请输入订单号', 'error'); return; }
    setLoading(true);
    try {
      const res = await api.getOrder(id.trim(), token);
      const o = res.data;
      setOrder(o);
      setPayForm((f) => ({ ...f, amount: o?.totalAmount || f.amount }));
      setVerifyCode(o?.verifyCode || '');
    } catch (e) {
      showToast(e.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  async function doAction(fn, successMsg) {
    if (!order) { showToast('请先加载订单', 'error'); return; }
    setActionBusy(true);
    try {
      const res = await fn();
      setOrder(res.data);
      showToast(successMsg, 'success');
    } catch (e) {
      showToast(e.message, 'error');
    } finally {
      setActionBusy(false);
    }
  }

  function addRecent(id) {
    const next = [id, ...recentOrders.filter((x) => x !== id)].slice(0, 8);
    setRecentOrders(next);
    localStorage.setItem(ORDER_KEY, JSON.stringify(next));
  }

  const status = order ? (STATUS_CONFIG[order.status] || { label: order.status, cls: '' }) : null;

  return (
    <div className="page">
      <div className="page-header">
        <h1>我的订单</h1>
      </div>

      <div className="orders-layout">
        <section className="card order-search-card">
          <h3>查找订单</h3>
          <div className="search-row">
            <input
              value={orderId}
              onChange={(e) => setOrderId(e.target.value)}
              placeholder="输入订单号"
              onKeyDown={(e) => e.key === 'Enter' && loadOrder()}
            />
            <button
              type="button"
              className="btn-primary"
              onClick={() => loadOrder()}
              disabled={loading}
            >
              {loading ? '查询中…' : '查询'}
            </button>
          </div>

          {recentOrders.length > 0 && (
            <div className="recent-orders">
              <span className="muted-sm">最近订单</span>
              <div className="chip-row">
                {recentOrders.map((id) => (
                  <button
                    key={id}
                    type="button"
                    className="chip"
                    onClick={() => { setOrderId(id); loadOrder(id); }}
                  >
                    {id.slice(0, 8)}…
                  </button>
                ))}
              </div>
            </div>
          )}
        </section>

        {order && (
          <section className="card order-detail-card">
            <div className="order-header">
              <div>
                <code className="order-id">{order.orderId}</code>
                <span className={`status-badge-lg ${status.cls}`}>{status.label}</span>
              </div>
              <div className="order-stats">
                <div><span>数量</span><strong>{order.quantity}</strong></div>
                <div><span>金额</span><strong>¥{order.totalAmount}</strong></div>
                {order.verifyCode && <div><span>核销码</span><code>{order.verifyCode}</code></div>}
              </div>
            </div>

            <div className="order-actions">
              {order.status === 'PENDING' && (
                <div className="action-section">
                  <h4>模拟支付</h4>
                  <div className="action-row">
                    <input
                      value={payForm.paymentId}
                      onChange={(e) => setPayForm((f) => ({ ...f, paymentId: e.target.value }))}
                      placeholder="支付流水号"
                    />
                    <input
                      type="number"
                      value={payForm.amount}
                      onChange={(e) => setPayForm((f) => ({ ...f, amount: e.target.value }))}
                      placeholder="金额"
                    />
                    <button
                      type="button"
                      className="btn-primary"
                      disabled={actionBusy}
                      onClick={() => doAction(
                        () => api.payOrder(order.orderId, { orderId: order.orderId, paymentId: payForm.paymentId, amount: Number(payForm.amount || order.totalAmount) }, token),
                        '支付成功',
                      )}
                    >
                      确认支付
                    </button>
                  </div>
                </div>
              )}

              {order.status === 'PAID' && (
                <div className="action-section">
                  <h4>使用电子票</h4>
                  <div className="action-row">
                    <input
                      value={verifyCode}
                      onChange={(e) => setVerifyCode(e.target.value)}
                      placeholder="核销码"
                    />
                    <button
                      type="button"
                      className="btn-primary"
                      disabled={actionBusy}
                      onClick={() => doAction(
                        () => api.useOrder(order.orderId, { orderId: order.orderId, verifyCode }, token),
                        '电子票已核销',
                      )}
                    >
                      使用电子票
                    </button>
                  </div>
                </div>
              )}

              {['PENDING', 'PAID'].includes(order.status) && (
                <div className="action-section">
                  <h4>取消订单</h4>
                  <div className="action-row">
                    <input
                      value={cancelReason}
                      onChange={(e) => setCancelReason(e.target.value)}
                      placeholder="取消原因"
                    />
                    <button
                      type="button"
                      className="btn-ghost danger"
                      disabled={actionBusy}
                      onClick={() => doAction(
                        () => api.cancelOrder(order.orderId, { orderId: order.orderId, reason: cancelReason }, token),
                        '订单已取消',
                      )}
                    >
                      取消订单
                    </button>
                  </div>
                </div>
              )}
            </div>
          </section>
        )}

        {!order && !loading && (
          <div className="empty-state">
            <span className="empty-icon">📋</span>
            <p>输入订单号查看详情，或点击最近订单快速查询</p>
          </div>
        )}
      </div>
    </div>
  );
}
