// ── State ──────────────────────────────────────────────────────────────────
let userId = null;
let refreshTimer = null;

// ── Boot ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const onProfile = document.getElementById('has-profile') !== null;

  if (onProfile) {
    userId = parseInt(sessionStorage.getItem('userId'), 10);
    if (!userId) { location.href = '/'; return; }
    document.getElementById('user-chip').textContent = `User #${userId}`;
    loadProfile();
    startRefresh();
  } else {
    // on index.html — redirect to profile if already logged in
    if (sessionStorage.getItem('userId')) location.href = '/profile.html';
  }
});

// ── API helper ─────────────────────────────────────────────────────────────
async function api(method, url, body) {
  const opts = { method, headers: {} };
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json';
    opts.body = JSON.stringify(body);
  }
  const res = await fetch(url, opts);
  const text = await res.text();
  if (!res.ok) {
    let msg = text;
    try { msg = JSON.parse(text)?.message ?? text; } catch {}
    throw new Error(msg || `HTTP ${res.status}`);
  }
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

// ── Tabs (index.html) ──────────────────────────────────────────────────────
function switchTab(tab) {
  const toLogin = tab === 'login';
  document.getElementById('login-form').classList.toggle('hidden', !toLogin);
  document.getElementById('register-form').classList.toggle('hidden', toLogin);
  document.querySelectorAll('.tab').forEach((el, i) =>
    el.classList.toggle('active', toLogin ? i === 0 : i === 1)
  );
  document.getElementById('login-error').textContent = '';
  document.getElementById('reg-error').textContent   = '';
}

// ── Auth (index.html) ──────────────────────────────────────────────────────
async function login() {
  document.getElementById('login-error').textContent = '';
  const id       = parseInt(document.getElementById('login-userId').value, 10);
  const email    = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  try {
    await api('POST', '/auth/login', { email, password });
    sessionStorage.setItem('userId', id);
    location.href = '/profile.html';
  } catch (e) {
    document.getElementById('login-error').textContent = e.message;
  }
}

async function register() {
  document.getElementById('reg-error').textContent = '';
  const name     = document.getElementById('reg-name').value.trim();
  const email    = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value;
  try {
    const id = await api('POST', '/users', { name, email, password });
    await api('POST', '/auth/login', { email, password });
    sessionStorage.setItem('userId', id);
    location.href = '/profile.html';
  } catch (e) {
    document.getElementById('reg-error').textContent = e.message;
  }
}

// ── Logout (profile.html) ──────────────────────────────────────────────────
async function logout() {
  stopRefresh();
  try { await api('POST', `/auth/logout/${userId}`); } catch {}
  sessionStorage.removeItem('userId');
  location.href = '/';
}

// ── Session refresh every 15s (profile.html) ───────────────────────────────
function startRefresh() {
  refreshTimer = setInterval(async () => {
    try {
      await api('POST', `/auth/refresh/${userId}`);
      flashSession();
    } catch {}
  }, 15000);
}

function stopRefresh() {
  clearInterval(refreshTimer);
}

function flashSession() {
  const dot   = document.getElementById('session-dot');
  const label = document.getElementById('session-label');
  dot.classList.add('pulse');
  label.textContent = 'refreshed';
  setTimeout(() => {
    dot.classList.remove('pulse');
    label.textContent = 'session active';
  }, 1500);
}

// ── Payment profile (profile.html) ────────────────────────────────────────
async function loadProfile() {
  try {
    const profile = await api('GET', `/payment-profile/users/${userId}`);
    renderProfile(profile);
  } catch {
    document.getElementById('no-profile').classList.remove('hidden');
    document.getElementById('has-profile').classList.add('hidden');
  }
}

function renderProfile(p) {
  document.getElementById('no-profile').classList.add('hidden');
  document.getElementById('has-profile').classList.remove('hidden');

  document.getElementById('info-plan').textContent     = p.plan;
  document.getElementById('info-currency').textContent = p.currency;
  document.getElementById('info-balance').textContent  =
    `${(p.balanceInCents / 100).toFixed(2)} ${p.currency}`;
  document.getElementById('info-last').textContent     = fmtDate(p.lastPaymentAtEpochMillis);
  document.getElementById('info-next').textContent     = fmtDate(p.nextBillingAtEpochMillis);
  document.getElementById('info-failed').textContent   = p.failedPaymentsCount;

  const badge = document.getElementById('info-status');
  badge.textContent = p.paymentStatus;
  badge.className   = `badge ${p.paymentStatus === 'ACTIVE' ? 'badge--green' : 'badge--red'}`;
}

async function createProfile() {
  try {
    const profile = await api('POST', `/payment-profile/users/${userId}`);
    renderProfile(profile);
    toast('Profile created');
  } catch (e) {
    toast(e.message, 'err');
  }
}

async function updateProfile() {
  const body    = {};
  const plan    = document.getElementById('upd-plan').value;
  const cur     = document.getElementById('upd-currency').value.trim();
  const status  = document.getElementById('upd-status').value;
  const balance = document.getElementById('upd-balance').value;
  if (plan)    body.plan           = plan;
  if (cur)     body.currency       = cur;
  if (status)  body.paymentStatus  = status;
  if (balance) body.balanceInCents = parseInt(balance, 10);
  try {
    await api('PUT', `/payment-profile/users/${userId}`, body);
    await loadProfile();
    toast('Profile updated');
  } catch (e) {
    toast(e.message, 'err');
  }
}

async function deleteProfile() {
  if (!confirm('Delete payment profile?')) return;
  try {
    await api('DELETE', `/payment-profile/users/${userId}`);
    document.getElementById('has-profile').classList.add('hidden');
    document.getElementById('no-profile').classList.remove('hidden');
    toast('Profile deleted');
  } catch (e) {
    toast(e.message, 'err');
  }
}

async function simulatePayment(result) {
  const amount = parseInt(document.getElementById('pay-amount').value || '0', 10);
  try {
    await api('POST', `/payment-profile/users/${userId}/payment-result`, {
      amountInCents:        amount,
      paymentProcessStatus: result,
      status:               result,
    });
    await loadProfile();
    toast(
      result === 'SUCCESS' ? 'Payment success recorded' : 'Payment failure recorded',
      result === 'SUCCESS' ? 'ok' : 'warn'
    );
  } catch (e) {
    toast(e.message, 'err');
  }
}

// ── Utilities ──────────────────────────────────────────────────────────────
function fmtDate(epochMs) {
  return new Date(epochMs).toLocaleDateString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
}

let toastTimer = null;
function toast(msg, type = 'ok') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `toast toast--${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = 'toast hidden'; }, 3000);
}
