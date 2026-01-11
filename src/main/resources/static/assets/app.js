const $ = (id) => document.getElementById(id);

let pie;

function setToken(token) {
    if (token) localStorage.setItem("accessToken", token);
    else localStorage.removeItem("accessToken");
    updateTokenState();
}

function getToken() {
    return localStorage.getItem("accessToken");
}

function updateTokenState() {
    $("tokenState").textContent = getToken() ? "есть" : "нет";
}

async function api(path, { method="GET", body=null } = {}) {
    const headers = { "Accept": "application/json" };
    const token = getToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
    if (body !== null) headers["Content-Type"] = "application/json";

    const res = await fetch(path, {
        method,
        headers,
        body: body !== null ? JSON.stringify(body) : null
    });

    const contentType = res.headers.get("content-type") || "";
    const text = await res.text();

    if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${text}`);
    }

    // иногда у вас auth/register отдаёт string, а login — json
    if (contentType.includes("application/json")) return JSON.parse(text);
    return text;
}

// datetime-local -> ISO with offset Z (OffsetDateTime нормально хавает)
function dtLocalToIso(dtLocalValue) {
    if (!dtLocalValue) return null;
    const d = new Date(dtLocalValue);
    return d.toISOString();
}

function setThisMonth() {
    const now = new Date();
    const from = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0);
    const to = new Date(now.getFullYear(), now.getMonth() + 1, 1, 0, 0, 0);
    $("fromDt").value = from.toISOString().slice(0,16);
    $("toDt").value = to.toISOString().slice(0,16);

    // occurredAt по умолчанию = сейчас
    $("txOccurredAt").value = new Date().toISOString().slice(0,16);
}

async function register() {
    const username = $("username").value.trim();
    const password = $("password").value.trim();
    const out = await api("/api/auth/register", { method: "POST", body: { username, password } });
    $("authOut").textContent = String(out);
}

async function login() {
    const username = $("username").value.trim();
    const password = $("password").value.trim();
    const out = await api("/api/auth/login", { method: "POST", body: { username, password } });
    $("authOut").textContent = JSON.stringify(out, null, 2);
    if (out?.accessToken) setToken(out.accessToken);
}

function logout() {
    setToken(null);
    $("authOut").textContent = "logout";
}

async function loadCategories() {
    const out = await api("/api/categories");
    $("catsOut").textContent = JSON.stringify(out, null, 2);
    return out;
}

async function addCategory() {
    const name = $("catName").value.trim();
    const type = $("catType").value;
    const out = await api("/api/categories", { method:"POST", body: { name, type }});
    $("catsOut").textContent = JSON.stringify(out, null, 2);
    await loadCategories();
}

async function loadTransactions() {
    const from = dtLocalToIso($("fromDt").value);
    const to = dtLocalToIso($("toDt").value);
    const qs = new URLSearchParams({ from, to }).toString();
    const out = await api(`/api/transactions?${qs}`);
    $("txOut").textContent = JSON.stringify(out, null, 2);
    return out;
}

async function addTransaction() {
    const categoryIdRaw = $("txCategoryId").value.trim();
    const body = {
        categoryId: categoryIdRaw ? categoryIdRaw : null,
        amount: Number($("txAmount").value),
        type: $("txType").value,
        occurredAt: dtLocalToIso($("txOccurredAt").value),
        note: $("txNote").value.trim() || null
    };
    const out = await api("/api/transactions", { method:"POST", body });
    $("txOut").textContent = JSON.stringify(out, null, 2);
    await loadTransactions();
}

async function loadAnalytics() {
    const from = dtLocalToIso($("fromDt").value);
    const to = dtLocalToIso($("toDt").value);
    const qs = new URLSearchParams({ from, to }).toString();
    const out = await api(`/api/analytics/expenses-by-category?${qs}`);
    $("anOut").textContent = JSON.stringify(out, null, 2);

    const labels = out.map(x => x.categoryName);
    const data = out.map(x => Number(x.totalAmount));

    const ctx = $("pieChart").getContext("2d");
    if (pie) pie.destroy();
    pie = new Chart(ctx, {
        type: "pie",
        data: { labels, datasets: [{ data }] },
    });
}

async function refreshAll() {
    await loadCategories();
    await loadTransactions();
    await loadAnalytics();
}

document.addEventListener("DOMContentLoaded", () => {
    updateTokenState();
    setThisMonth();

    $("btnRegister").onclick = () => register().catch(e => $("authOut").textContent = e.message);
    $("btnLogin").onclick = () => login().catch(e => $("authOut").textContent = e.message);
    $("btnLogout").onclick = logout;

    $("btnSetMonth").onclick = setThisMonth;
    $("btnRefresh").onclick = () => refreshAll().catch(e => alert(e.message));

    $("btnLoadCats").onclick = () => loadCategories().catch(e => $("catsOut").textContent = e.message);
    $("btnAddCat").onclick = () => addCategory().catch(e => $("catsOut").textContent = e.message);

    $("btnLoadTx").onclick = () => loadTransactions().catch(e => $("txOut").textContent = e.message);
    $("btnAddTx").onclick = () => addTransaction().catch(e => $("txOut").textContent = e.message);

    $("btnLoadAnalytics").onclick = () => loadAnalytics().catch(e => $("anOut").textContent = e.message);
});