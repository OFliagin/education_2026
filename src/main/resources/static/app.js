// ── State ──────────────────────────────────────────────────────────────────
let userId = null;
let refreshTimer = null;

// ── Boot ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  const page = detectPage();

  if (page === 'auth') {
    if (sessionStorage.getItem('userId')) location.href = '/chat.html';
    return;
  }

  userId = parseInt(sessionStorage.getItem('userId'), 10);
  if (!userId) { location.href = '/'; return; }

  document.getElementById('sidebar-user').textContent = `User #${userId}`;
  startRefresh();

  if (page === 'chat')    initChat();
  if (page === 'billing') { /* forms are ready, no init needed */ }
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

function stopRefresh() {
  clearInterval(refreshTimer);
}

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
  return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── Billing (billing.html) ─────────────────────────────────────────────────
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
    toast(result === 'SUCCESS' ? 'Payment success recorded' : 'Payment failure recorded',
          result === 'SUCCESS' ? 'ok' : 'warn');
  } catch (e) {
    toast(e.message, 'err');
  }
}

// ── Toast ──────────────────────────────────────────────────────────────────
let toastTimer = null;
function toast(msg, type = 'ok') {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = msg;
  el.className = `toast toast--${type}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = 'toast hidden'; }, 3000);
}

function fmtDate(epochMs) {
  return new Date(epochMs).toLocaleDateString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric',
  });
}
