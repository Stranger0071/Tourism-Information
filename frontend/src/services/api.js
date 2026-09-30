/**
 * Base URL for backend API requests.
 * Uses `VITE_API_URL` when provided, otherwise falls back to local `/api` proxy.
 * Trailing slashes are stripped to make path joining predictable.
 */
const API_BASE = (import.meta.env.VITE_API_URL || '/api').replace(/\/+$/, '');

/**
 * Attempt to fix mojibake (garbled UTF-8 shown as Latin-1) that may come
 * from poorly-encoded server responses. If the value does not look like
 * mojibake or decoding fails, the original value is returned unchanged.
 *
 * @param {any} value
 * @returns {any}
 */
function decodeMojibake(value) {
  if (typeof value !== 'string' || !/[\u00c2\u00c3\u00e2\u00f0]/.test(value)) {
    return value;
  }

  try {
    // Map characters to raw bytes and decode as UTF-8
    const bytes = Uint8Array.from([...value].map((char) => char.charCodeAt(0) & 0xff));
    const decoded = new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    // If decoder produced replacement characters, keep original value.
    return decoded.includes('\uFFFD') ? value : decoded;
  } catch {
    return value;
  }
}

/**
 * Walk response data structure and normalize string values.
 * This handles arrays and nested objects recursively so callers always
 * receive cleaned, UTF-8-correct strings where possible.
 *
 * @param {any} value
 * @returns {any}
 */
function normalizeData(value) {
  if (Array.isArray(value)) {
    return value.map(normalizeData);
  }

  if (value && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value).map(([key, entry]) => [key, normalizeData(entry)])
    );
  }

  return decodeMojibake(value);
}

/**
 * Lightweight fetch wrapper that:
 * - prefixes requests with `API_BASE`
 * - sets `Accept: application/json` by default
 * - throws an Error for non-2xx responses (attempts to surface text body)
 * - parses JSON and normalizes string encoding issues
 *
 * @param {string} path - path portion starting with '/'
 * @param {RequestInit} [options]
 * @returns {Promise<any>}
 */
async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { Accept: 'application/json', ...options.headers },
    ...options,
  });

  // Non-2xx responses: try to read a textual error message from the body,
  // otherwise fallback to the status text so the caller gets something useful.
  if (!response.ok) {
    const message = await response.text().catch(() => response.statusText);
    throw new Error(message || `Request failed (${response.status})`);
  }

  // Parse JSON then normalize any mojibake in strings before returning.
  return normalizeData(await response.json());
}

export const api = {
  // Simple health check endpoint used by the frontend to detect backend availability
  getHealth: () => request('/health'),

  // Fetch attractions, optionally filtered by category (skip 'All')
  getAttractions: (category) => {
    const query = category && category !== 'All' ? `?category=${encodeURIComponent(category)}` : '';
    return request(`/attractions${query}`);
  },

  // Get a single attraction by id
  getAttraction: (id) => request(`/attractions/${id}`),

  // List available attraction categories
  getAttractionCategories: () => request('/attractions/categories'),

  // Fetch hotels, optionally filtered by type
  getHotels: (type) => {
    const query = type && type !== 'All' ? `?type=${encodeURIComponent(type)}` : '';
    return request(`/hotels${query}`);
  },

  // Supported hotel types for filtering
  getHotelTypes: () => request('/hotels/types'),

  // Guides list
  getGuides: () => request('/guides'),

  // Single guide by id
  getGuide: (id) => request(`/guides/${id}`),

  // Map-related endpoints used by mapping UI
  getMapLocations: () => request('/maps/locations'),

  getDistances: () => request('/maps/distances'),

  // Booking endpoints
  getBooking: () => request('/booking'),

  getBookingOption: (id) => request(`/booking/options/${encodeURIComponent(id)}`),

  // Request live pricing for hotels: `ids` is an array of hotel ids, `days` controls window
  getLiveHotelPrices: (ids, days) => {
    const query = new URLSearchParams({
      ids: ids.join(','),
      days: String(days),
    });
    return request(`/hotels/live-prices?${query}`);
  },

  // Create a booking order (POST JSON)
  createBookingOrder: (payload) =>
    request('/booking/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }),

  // Verify a payment for a booking (POST JSON)
  verifyBookingPayment: (payload) =>
    request('/booking/verify', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }),

  // Fetch booking confirmation information by booking id
  getBookingConfirmation: (bookingId) =>
    request(`/booking/confirmations/${encodeURIComponent(bookingId)}`),
};
