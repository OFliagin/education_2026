const userId = () => document.getElementById('userId').value;
const val    = id => document.getElementById(id).value.trim();

async function call(method, url, body) {
  const opts = { method, headers: {} };
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json';
    opts.body = JSON.stringify(body);
  }

  setOutput('pending', `> ${method} ${url}` +
    (body !== undefined ? '\n' + JSON.stringify(body, null, 2) : '') + '\n\n');

  try {
    const res = await fetch(url, opts);
    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text || '(no body)'; }
    const pretty = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
    appendOutput(res.ok ? 'ok' : 'err', `HTTP ${res.status}\n${pretty}`);
  } catch (err) {
    appendOutput('err', `Network error: ${err.message}`);
  }
}

function setOutput(state, text) {
  const el = document.getElementById('output');
  el.textContent = text;
  el.className = state;
  document.getElementById('output-tag').textContent = state;
  document.getElementById('output-tag').className = state;
}

function appendOutput(state, text) {
  const el = document.getElementById('output');
  el.textContent += text;
  el.className = state;
  document.getElementById('output-tag').textContent = state;
  document.getElementById('output-tag').className = state;
}

function clearOutput() {
  setOutput('ready', '');
  document.getElementById('output-tag').textContent = 'ready';
  document.getElementById('output-tag').className = '';
}

// ── Users ──────────────────────────────────────────────────────────────────

function createUser() {
  call('POST', '/users', {
    name:     val('user-name'),
    email:    val('user-email'),
    password: val('user-password'),
  });
}

function getUser() {
  call('GET', `/users/${userId()}`);
}

function updateUser() {
  const body = {};
  const name  = val('user-name');
  const email = val('user-email');
  if (name)  body.name  = name;
  if (email) body.email = email;
  call('PATCH', `/users/${userId()}`, body);
}

function deleteUser() {
  call('DELETE', `/users/${userId()}`);
}

// ── Session ────────────────────────────────────────────────────────────────

function login() {
  call('POST', '/auth/login', {
    email:    val('login-email'),
    password: val('login-password'),
  });
}

function logout()          { call('POST', `/auth/logout/${userId()}`);  }
function checkOnline()     { call('GET',  `/auth/online/${userId()}`);  }
function refreshSession()  { call('POST', `/auth/refresh/${userId()}`); }

// ── Payment Profile ────────────────────────────────────────────────────────

function createProfile() { call('POST',   `/payment-profile/users/${userId()}`); }
function getProfile()    { call('GET',    `/payment-profile/users/${userId()}`); }
function deleteProfile() { call('DELETE', `/payment-profile/users/${userId()}`); }

function updateProfile() {
  const body = {};
  const plan    = val('profile-plan');
  const cur     = val('profile-currency');
  const status  = val('profile-status');
  const balance = val('profile-balance');
  if (plan)    body.plan           = plan;
  if (cur)     body.currency       = cur;
  if (status)  body.paymentStatus  = status;
  if (balance) body.balanceInCents = Number(balance);
  call('PUT', `/payment-profile/users/${userId()}`, body);
}

function savePaymentResult() {
  call('POST', `/payment-profile/users/${userId()}/payment-result`, {
    amountInCents:        Number(val('payment-amount')),
    paymentProcessStatus: val('payment-process-status'),
    status:               val('payment-status'),
  });
}
