const state = { token: null, cardNumber: null, fullName: null, apiBase: null };

function resolveApiBases() {
  const { protocol, hostname, port, origin } = window.location;
  const isHttp = protocol === 'http:' || protocol === 'https:';
  const configuredBase = new URLSearchParams(window.location.search).get('apiBase');

  const bases = [];
  const addBase = (base) => {
    if (!base || bases.includes(base)) return;
    bases.push(base);
  };

  // Allow forcing a backend URL for remote previews.
  addBase(configuredBase);

  const savedBase = localStorage.getItem('atm.apiBase');
  addBase(savedBase);

  // When opening index.html directly (file://), use the local backend.
  if (!isHttp) {
    addBase('http://localhost:8080/api');
    addBase('http://127.0.0.1:8080/api');
    return bases;
  }

  addBase(`${origin}/api`);

  if (port === '8080') {
    return bases;
  }

  // For frontend dev servers running on a different port, call the backend host on 8080.
  addBase(`${protocol}//${hostname}:8080/api`);
  addBase(`${protocol}//localhost:8080/api`);
  addBase(`${protocol}//127.0.0.1:8080/api`);
  return bases;
}

const apiBases = resolveApiBases();

function getApiBasesInPriorityOrder() {
  if (!state.apiBase) return apiBases;
  return [state.apiBase, ...apiBases.filter(base => base !== state.apiBase)];
}

const views = [...document.querySelectorAll('.view')];
const statusText = document.getElementById('status');
const pinInput = document.getElementById('pinInput');

function show(viewId) { views.forEach(v => v.classList.toggle('active', v.id === viewId)); }
function setStatus(msg) { statusText.textContent = msg; }

function showMessage(title, text) {
  document.getElementById('messageTitle').textContent = title;
  document.getElementById('messageText').textContent = text;
  show('message');
}

async function callApi(path, method = 'GET', body) {
  setStatus('Loading...');
  let res = null;
  let nonJsonError = null;
  for (const base of getApiBasesInPriorityOrder()) {
    try {
      const candidate = await fetch(`${base}${path}`, {
        method,
        headers: { 'Content-Type': 'application/json', 'X-Session-Token': state.token || '' },
        body: body ? JSON.stringify(body) : undefined
      });

      const contentType = candidate.headers.get('content-type') || '';
      if (!contentType.includes('application/json')) {
        const responseText = await candidate.text();
        const trimmed = responseText.trimStart().toLowerCase();
        const isHtml = trimmed.startsWith('<!doctype html') || trimmed.startsWith('<html');
        nonJsonError = new Error(
          isHtml
            ? 'API request returned HTML instead of JSON. Verify that the frontend points to the backend API.'
            : 'API request returned a non-JSON response.'
        );
        continue;
      }

      res = candidate;
      state.apiBase = base;
      localStorage.setItem('atm.apiBase', base);
      break;
    } catch (err) {
      // Try the next candidate base URL.
    }
  }

  if (!res) {
    if (nonJsonError) {
      throw nonJsonError;
    }
    throw new Error(`Unable to reach API. Tried: ${apiBases.join(', ')}. Make sure the backend is running and reachable.`);
  }

  const json = await res.json();
  setStatus(json.message || 'Ready');
  if (!json.success) throw new Error(json.message || 'Operation failed');
  return json.data;
}

async function login() {
  const pin = pinInput.value;
  if (!/^\d{16}$/.test(state.cardNumber || '')) return showMessage('Error', 'Card number must be 16 digits.');
  if (!/^\d{4}$/.test(pin)) return showMessage('Error', 'PIN must be 4 digits.');
  try {
    const data = await callApi('/auth/login', 'POST', { cardNumber: state.cardNumber, pin });
    state.token = data.sessionToken;
    state.fullName = data.fullName;
    pinInput.value = '';
    show('menu');
    setStatus(`Welcome ${state.fullName}`);
  } catch (e) { showMessage('Login Failed', e.message); }
}

function setupKeypad() {
  const keypad = document.getElementById('keypad');
  for (let i = 1; i <= 9; i++) keypad.innerHTML += `<button data-pin='${i}'>${i}</button>`;
  keypad.innerHTML += `<button data-pin='0'>0</button>`;
  keypad.addEventListener('click', (e) => {
    const key = e.target.dataset.pin;
    if (!key || pinInput.value.length >= 4) return;
    pinInput.value += key;
  });
}

async function refreshBalance() {
  try {
    const data = await callApi('/atm/balance');
    document.getElementById('balanceText').textContent = `${data.currency} ${Number(data.balance).toFixed(2)}`;
    show('balance');
  } catch (e) { showMessage('Error', e.message); }
}

async function doAmount(path, amount) {
  if (!(Number(amount) > 0)) return showMessage('Error', 'Enter a valid amount.');
  try {
    const data = await callApi(path, 'POST', { amount: Number(amount) });
    showMessage('Success', `New balance: ${data.currency} ${Number(data.balance).toFixed(2)}`);
  } catch (e) { showMessage('Failed', e.message); }
}

async function loadStatement() {
  try {
    const tx = await callApi('/atm/statement?limit=10');
    document.getElementById('statementBody').innerHTML = tx.map(t => `<tr><td>${t.type}</td><td>${t.amount}</td><td>${new Date(t.createdAt).toLocaleString()}</td></tr>`).join('');
    show('statement');
  } catch (e) { showMessage('Error', e.message); }
}

document.body.addEventListener('click', async (e) => {
  const action = e.target.dataset.action;
  if (!action) {
    const quick = e.target.dataset.quick;
    if (quick) return doAmount('/atm/withdraw', quick);
    return;
  }

  if (action === 'to-pin') { state.cardNumber = document.getElementById('cardNumber').value.trim(); show('pin'); }
  if (action === 'clear-pin') pinInput.value = '';
  if (action === 'back-pin') pinInput.value = pinInput.value.slice(0, -1);
  if (action === 'login') await login();
  if (action === 'menu') show('menu');
  if (action === 'balance') await refreshBalance();
  if (action === 'withdraw-view') show('withdraw');
  if (action === 'withdraw-custom') await doAmount('/atm/withdraw', document.getElementById('withdrawAmount').value);
  if (action === 'deposit-view') show('deposit');
  if (action === 'deposit') await doAmount('/atm/deposit', document.getElementById('depositAmount').value);
  if (action === 'transfer-view') show('transfer');
  if (action === 'transfer') {
    try {
      const toAccountNumber = document.getElementById('beneficiary').value.trim();
      const amount = Number(document.getElementById('transferAmount').value);
      await callApi('/atm/transfer', 'POST', { toAccountNumber, amount });
      showMessage('Success', 'Transfer completed.');
    } catch (err) { showMessage('Failed', err.message); }
  }
  if (action === 'statement') await loadStatement();
  if (action === 'change-pin-view') show('change-pin');
  if (action === 'change-pin') {
    try {
      await callApi('/atm/change-pin', 'POST', { oldPin: document.getElementById('oldPin').value, newPin: document.getElementById('newPin').value });
      showMessage('Success', 'PIN changed successfully.');
    } catch (err) { showMessage('Failed', err.message); }
  }
  if (action === 'exit') {
    if (state.token) await callApi('/auth/logout', 'POST', { sessionToken: state.token });
    state.token = null; state.cardNumber = null; state.fullName = null;
    document.getElementById('cardNumber').value = '';
    show('welcome');
    setStatus('Session ended.');
  }
});

setupKeypad();
