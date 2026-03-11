const JSON_HEADERS = {
  'Content-Type': 'application/json',
};

async function parseResponse(response) {
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = data?.message || data?.error || `HTTP ${response.status}`;
    throw new Error(message);
  }

  return data;
}

async function request(path, options = {}, token) {
  const headers = {
    ...JSON_HEADERS,
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, {
    ...options,
    headers,
  });

  return parseResponse(response);
}

export const api = {
  register(payload) {
    return request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  login(payload) {
    return request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  logout(token) {
    return request('/api/auth/logout', { method: 'POST' }, token);
  },
  queryGraphQL(query, variables, token) {
    return request(
      '/graphql',
      {
        method: 'POST',
        body: JSON.stringify({ query, variables }),
      },
      token,
    );
  },
  createVenue(payload, token) {
    return request(
      '/api/v1/venues',
      {
        method: 'POST',
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  getProfile(token) {
    return request('/api/user/profile', { method: 'GET' }, token);
  },
  updateProfile(payload, token) {
    return request(
      '/api/user/profile',
      {
        method: 'PUT',
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  signDaily(token) {
    return request('/api/user/daily-sign', { method: 'POST' }, token);
  },
  getStreak(token) {
    return request('/api/user/daily-sign/streak', { method: 'GET' }, token);
  },
  getMonthRecord(token) {
    return request('/api/user/daily-sign/month', { method: 'GET' }, token);
  },
  purchaseTicket(payload, token) {
    return request(
      '/api/v1/tickets/purchase',
      {
        method: 'POST',
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  getOrder(orderId, token) {
    return request(`/orders/${orderId}`, { method: 'GET' }, token);
  },
  payOrder(orderId, payload, token) {
    return request(
      `/orders/${orderId}/pay`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  cancelOrder(orderId, payload, token) {
    return request(
      `/orders/${orderId}/cancel`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  useOrder(orderId, payload, token) {
    return request(
      `/orders/${orderId}/use`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  scanTicket(ticketCode, token) {
    return request(`/api/checkin/scan?ticketCode=${encodeURIComponent(ticketCode)}`, { method: 'POST' }, token);
  },
};

export function decodeJwtClaims(token) {
  if (!token) {
    return null;
  }

  try {
    const payload = token.split('.')[1];
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
    const decoded = atob(padded);
    return JSON.parse(decoded);
  } catch {
    return null;
  }
}