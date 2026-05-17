// ── State ──────────────────────────────────────────────────────────────────
let userId = null;
let refreshTimer = null;
let lbOffset = 0;
const LB_LIMIT = 10;

// ── Boot ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const page = detectPage();

  if (page === 'auth') {
    if (sessionStorage.getItem('userId')) location.href = '/chat.html';
    loadLeaderboard();
    return;
  }

  userId = parseInt(sessionStorage.getItem('userId'), 10);
  if (!userId) { location.href = '/'; return; }

  document.getElementById('sidebar-user').textContent = `User #${userId}`;
  startRefresh();

  if (page === 'chat') {
    initChat();
    loadContextWindow();
  }
  if (page === 'billing') loadProfileInfo();
});

function detectPage() {
  if (document.getElementById('login-form'))  return 'auth';
  if (document.getElementById('messages'))    return 'chat';
  if (document.getElementById('upd-plan'))    return 'billing';
}

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

// ── Session refresh every 15s ──────────────────────────────────────────────
function startRefresh() {
  refreshTimer = setInterval(async () => {
    try {
      await api('POST', `/auth/refresh/${userId}`);
      flashSession();
    } catch {}
  }, 15000);
}

function stopRefresh() { clearInterval(refreshTimer); }

function flashSession() {
  const dot   = document.getElementById('session-dot');
  const label = document.getElementById('session-label');
  dot?.classList.add('pulse');
  if (label) {
    label.textContent = 'refreshed';
    setTimeout(() => { label.textContent = 'session active'; }, 1500);
  }
  setTimeout(() => dot?.classList.remove('pulse'), 1500);
}

// ── Auth (index.html) ──────────────────────────────────────────────────────
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

async function login() {
  document.getElementById('login-error').textContent = '';
  const id       = parseInt(document.getElementById('login-userId').value, 10);
  const email    = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;
  try {
    await api('POST', '/auth/login', { email, password });
    sessionStorage.setItem('userId', id);
    location.href = '/chat.html';
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
    location.href = '/chat.html';
  } catch (e) {
    document.getElementById('reg-error').textContent = e.message;
  }
}

// ── Logout ─────────────────────────────────────────────────────────────────
async function logout() {
  stopRefresh();
  try { await api('POST', `/auth/logout/${userId}`); } catch {}
  sessionStorage.removeItem('userId');
  location.href = '/';
}

// ── Chat (chat.html) ───────────────────────────────────────────────────────
function initChat() {
  document.getElementById('chat-input').addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
  });
}

async function sendMessage() {
  const input = document.getElementById('chat-input');
  const text  = input.value.trim();
  if (!text) return;

  appendMessage('user', text);
  input.value = '';

  // increment leaderboard score on every message sent
  api('GET', `/leaderboard/user/${userId}/increment`)
    .then(() => loadContextWindow())
    .catch(() => {});

  const typing = appendMessage('agent', '…');
  try {
    const res = await api('POST', '/api/agent/message', { userId, message: text });
    typing.querySelector('.bubble').textContent = res.response;
  } catch (e) {
    typing.querySelector('.bubble').textContent = `Error: ${e.message}`;
    typing.querySelector('.bubble').classList.add('bubble--error');
  }
}

function appendMessage(role, text) {
  const list = document.getElementById('messages');
  const el   = document.createElement('div');
  el.className = `message message--${role}`;
  el.innerHTML = `<div class="bubble">${escHtml(text)}</div>`;
  list.appendChild(el);
  list.scrollTop = list.scrollHeight;
  return el;
}

function escHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// ── Leaderboard (chat.html) ────────────────────────────────────────────────
async function loadLeaderboard() {
  try {
    const users = await api('GET', `/leaderboard?limit=${LB_LIMIT}&offset=${lbOffset}`);
    renderLeaderboard(users);
  } catch {
    document.getElementById('lb-list').innerHTML =
      '<div class="lb-placeholder">Failed to load.</div>';
  }
}

function renderLeaderboard(users) {
  const list = document.getElementById('lb-list');
  if (!users.length) {
    list.innerHTML = '<div class="lb-placeholder">No data yet.</div>';
    return;
  }
  list.innerHTML = users.map(u => `
    <div class="lb-row ${u.userId === userId ? 'lb-row--me' : ''}">
      <span class="lb-rank">#${u.rank}</span>
      <span class="lb-name">${escHtml(u.username)}</span>
      <span class="lb-score">${u.score}</span>
    </div>`).join('');

  const page = Math.floor(lbOffset / LB_LIMIT) + 1;
  document.getElementById('lb-page').textContent = `Page ${page}`;
  document.getElementById('lb-prev').disabled = lbOffset === 0;
  document.getElementById('lb-next').disabled = users.length < LB_LIMIT;
}

function lbPrev() {
  if (lbOffset === 0) return;
  lbOffset = Math.max(0, lbOffset - LB_LIMIT);
  loadLeaderboard();
}

function lbNext() {
  lbOffset += LB_LIMIT;
  loadLeaderboard();
}

// ── Context Window (chat.html) ─────────────────────────────────────────────
async function loadContextWindow() {
  try {
    const [score, rank] = await Promise.all([
      api('GET', `/leaderboard/user/${userId}/score`),
      api('GET', `/leaderboard/user/${userId}/rank`),
    ]);

    const s = score ?? 0;
    const r = rank != null ? rank : '—'; // rank is 1-based (converted on backend)

    document.getElementById('ctx-score').textContent = s;
    document.getElementById('ctx-rank').textContent  = `#${r}`;
    document.getElementById('context-text').textContent =
      `You've sent ${s} message${s !== 1 ? 's' : ''}. ` +
      `You're ranked #${r} among all chat users.`;
  } catch {
    document.getElementById('context-text').textContent = 'Stats unavailable.';
  }
}

// ── Billing (billing.html) ─────────────────────────────────────────────────
async function loadProfileInfo() {
  try {
    const p = await api('GET', `/payment-profile/users/${userId}`);
    document.getElementById('profile-loading').classList.add('hidden');
    document.getElementById('profile-empty').classList.add('hidden');
    document.getElementById('profile-data').classList.remove('hidden');

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
  } catch {
    document.getElementById('profile-loading').classList.add('hidden');
    document.getElementById('profile-data').classList.add('hidden');
    document.getElementById('profile-empty').classList.remove('hidden');
  }
}

async function updateProfile() {
  const body   = {};
  const plan   = document.getElementById('upd-plan').value;
  const cur    = document.getElementById('upd-currency').value.trim();
  const status = document.getElementById('upd-status').value;
  const bal    = document.getElementById('upd-balance').value;
  if (plan)   body.plan           = plan;
  if (cur)    body.currency       = cur;
  if (status) body.paymentStatus  = status;
  if (bal)    body.balanceInCents = parseInt(bal, 10);
  try {
    await api('PUT', `/payment-profile/users/${userId}`, body);
    await loadProfileInfo();
    toast('Profile updated');
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
    await loadProfileInfo();
    toast(result === 'SUCCESS' ? 'Payment success recorded' : 'Payment failure recorded',
          result === 'SUCCESS' ? 'ok' : 'warn');
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
  if (!el) return;
  el.textContent = msg;
  el.className = `toast toast--${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = 'toast hidden'; }, 3000);
}
