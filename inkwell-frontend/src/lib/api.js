const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });

  let payload = null;
  const text = await response.text();
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = { message: text };
    }
  }

  if (!response.ok) {
    const message =
      payload?.message || payload?.error || `Request failed: ${response.status}`;
    throw new Error(message);
  }

  return payload;
}

export { API_BASE };
