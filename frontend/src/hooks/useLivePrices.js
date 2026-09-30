import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { api } from '../services/api';

const POLL_INTERVAL_MS = 15_000;

/**
 * Hook for fetching live hotel pricing data and keeping it up to date.
 *
 * @param {string[]} hotelIds - Hotel IDs to request.
 * @param {number} days - Number of days for the live price window.
 * @param {boolean} [enabled=true] - Whether polling and fetching should run.
 */
export function useLivePrices(hotelIds, days, enabled = true) {
  const [prices, setPrices] = useState([]);
  const [updatedAt, setUpdatedAt] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [tick, setTick] = useState(0);
  const initialLoad = useRef(true);

  // Callers may create the IDs array inline (for example, `[hotel.id]`).
  // Use a value-based key so a new array reference does not restart effects
  // and cause a render/update loop.
  const hotelIdsKey = Array.isArray(hotelIds)
    ? [...new Set(hotelIds)].sort().join(',')
    : '';
  const requestedHotelIds = useMemo(
    () => (hotelIdsKey ? hotelIdsKey.split(',') : []),
    [hotelIdsKey]
  );

  const fetchPrices = useCallback(async () => {
    if (!enabled || !requestedHotelIds.length) {
      // If polling is disabled or there are no hotels, clear state.
      setPrices([]);
      setLoading(false);
      setRefreshing(false);
      return;
    }

    setError(null);
    if (initialLoad.current) {
      // Show full loading state only the first time.
      setLoading(true);
    } else {
      // Subsequent refreshes should show a smaller refreshing indicator.
      setRefreshing(true);
    }

    try {
      const result = await api.getLiveHotelPrices(requestedHotelIds, days);
      setPrices(result.prices || []);
      const latest = result.prices?.[0]?.updatedAt;
      setUpdatedAt(latest || Date.now());
    } catch (err) {
      setError(err.message || 'Failed to load live prices');
    } finally {
      setLoading(false);
      setRefreshing(false);
      initialLoad.current = false;
    }
  }, [requestedHotelIds, days, enabled]);

  useEffect(() => {
    // Always fetch immediately when hotelIds/days/enabled change.
    initialLoad.current = true;
    fetchPrices();
  }, [fetchPrices]);

  useEffect(() => {
    if (!enabled || !requestedHotelIds.length) {
      return undefined;
    }

    // Poll live prices on a fixed interval while enabled.
    const timer = setInterval(fetchPrices, POLL_INTERVAL_MS);
    return () => clearInterval(timer);
  }, [fetchPrices, enabled, requestedHotelIds]);

  useEffect(() => {
    if (!enabled || !updatedAt) {
      return undefined;
    }

    // Track elapsed seconds since the last update so UI can show a freshness timer.
    const timer = setInterval(() => setTick((value) => value + 1), 1000);
    return () => clearInterval(timer);
  }, [enabled, updatedAt]);

  const secondsAgo = updatedAt
    ? Math.max(0, Math.floor((Date.now() - updatedAt) / 1000))
    : null;

  const secondsUntilRefresh = updatedAt && enabled
    ? Math.max(0, POLL_INTERVAL_MS / 1000 - (secondsAgo ?? 0))
    : null;

  return {
    prices,
    updatedAt,
    secondsAgo,
    secondsUntilRefresh,
    loading,
    refreshing,
    error,
    refetch: fetchPrices,
    tick,
  };
}
