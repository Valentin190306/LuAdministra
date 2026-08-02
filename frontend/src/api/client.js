const BASE_URL = '/api';

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const raw = await res.text().catch(() => '');
    let mensaje = raw;
    try {
      const parsed = JSON.parse(raw);
      if (parsed?.mensaje) mensaje = parsed.mensaje;
    } catch { /* no es JSON, usamos texto crudo */ }
    throw new Error(mensaje);
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get: (path, opts) => request(path, opts),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: 'DELETE' }),
};
