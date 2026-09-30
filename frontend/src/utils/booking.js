/**
 * Format a numeric amount as Indian-rupee currency.
 * Example: 12345 -> "₹12,345"
 * @param {number} amount
 * @returns {string}
 */
export function formatCurrency(amount) {
  return `₹${amount.toLocaleString('en-IN')}`;
}

/**
 * Return a visual symbol for a price/availability trend.
 * @param {'up'|'down'|string} trend
 * @returns {string}
 */
export function trendSymbol(trend) {
  if (trend === 'up') return '▲';
  if (trend === 'down') return '▼';
  return '●';
}

/**
 * Human-readable label for availability status used in the UI.
 * @param {string} status
 * @returns {string}
 */
export function availabilityLabel(status) {
  if (status === 'available') return 'Available';
  if (status === 'limited') return 'Limited rooms';
  return 'Fully booked';
}

/**
 * CSS class name corresponding to availability status.
 * This keeps style decisions centralized rather than duplicated in components.
 * @param {string} status
 * @returns {string}
 */
export function availabilityClass(status) {
  if (status === 'available') return 'avail-available';
  if (status === 'limited') return 'avail-limited';
  return 'avail-unavailable';
}

export const BOOKING_STEPS = [
  { id: 'grid', label: 'Choose trip' },
  { id: 'panel', label: 'Select stay' },
  { id: 'checkout', label: 'Checkout' },
  { id: 'confirmation', label: 'Confirmed' },
];

export const DEFAULT_CHECK_IN_TIME = '14:00';
export const DEFAULT_CHECK_OUT_TIME = '11:00';

export function todayDateString() {
  // Return date in YYYY-MM-DD using local timezone converted to ISO form.
  return new Date().toISOString().split('T')[0];
}

export function addDaysToDate(dateString, days) {
  // Using noon as a safe anchor to avoid DST issues when adding days.
  const date = new Date(`${dateString}T12:00:00`);
  date.setDate(date.getDate() + days);
  return date.toISOString().split('T')[0];
}

export function computeNightsBetween(checkInDate, checkOutDate) {
  // Compute full-night difference between two ISO date strings (YYYY-MM-DD).
  // If either date is missing, default to 1 night.
  if (!checkInDate || !checkOutDate) return 1;
  const start = new Date(`${checkInDate}T12:00:00`);
  const end = new Date(`${checkOutDate}T12:00:00`);
  const diff = Math.round((end - start) / (1000 * 60 * 60 * 24));
  return Math.max(1, diff);
}

export function clampNights(nights, min = 1, max = 30) {
  // Ensure nights is an integer and clamp within [min, max].
  const value = Number.parseInt(String(nights), 10);
  if (Number.isNaN(value)) return min;
  return Math.min(max, Math.max(min, value));
}

export function formatStayDate(dateString) {
  // Format a YYYY-MM-DD string into a friendly local date string.
  if (!dateString) return '';
  return new Date(`${dateString}T12:00:00`).toLocaleDateString('en-IN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

export function formatStayTime(timeString) {
  // Convert 'HH:MM' into a localized 12-hour time string.
  if (!timeString) return '';
  const [hours, minutes] = timeString.split(':').map(Number);
  const date = new Date();
  date.setHours(hours, minutes, 0, 0);
  return date.toLocaleTimeString('en-IN', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  });
}

export function createDefaultStaySchedule(minNights = 1, maxNights = 14) {
  // Create a minimal default stay schedule for new bookings.
  // Note: `nights` should be clamped between min and max; callers can override.
  const checkInDate = todayDateString();
  const nights = clampNights(1, minNights, maxNights);
  return {
    checkInDate,
    checkOutDate: addDaysToDate(checkInDate, nights),
    checkInTime: DEFAULT_CHECK_IN_TIME,
    checkOutTime: DEFAULT_CHECK_OUT_TIME,
    nights,
  };
}
